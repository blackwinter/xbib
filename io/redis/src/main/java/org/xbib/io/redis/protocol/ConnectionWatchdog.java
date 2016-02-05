package org.xbib.io.redis.protocol;

import com.google.common.base.Supplier;
import org.xbib.io.redis.ClientOptions;
import org.xbib.io.redis.ConnectionEvents;
import org.xbib.io.redis.RedisChannelHandler;
import org.xbib.io.redis.RedisChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A netty {@link ChannelHandler} responsible for monitoring the channel and reconnecting when the connection is lost.
 */
@ChannelHandler.Sharable
public class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask {

    public static final long LOGGING_QUIET_TIME_MS = TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS);
    public static final int RETRY_TIMEOUT_MAX = 14;

    private static final Logger logger = LogManager.getLogger(ConnectionWatchdog.class);

    private final EventExecutorGroup reconnectWorkers;
    private final ClientOptions clientOptions;
    private final Bootstrap bootstrap;
    private final Timer timer;
    private final Supplier<SocketAddress> socketAddressSupplier;
    private boolean listenOnChannelInactive;
    private boolean reconnectSuspended;
    private Channel channel;
    private SocketAddress remoteAddress;
    private int attempts;
    private long lastReconnectionLogging = -1;
    private String logPrefix;

    private TimeUnit timeoutUnit = TimeUnit.SECONDS;
    private long timeout = 60;

    private volatile ChannelFuture currentFuture;

    /**
     * Create a new watchdog that adds to new connections to the supplied {@link ChannelGroup} and establishes a new
     * {@link Channel} when disconnected, while reconnect is true.
     *
     * @param clientOptions    client options for the current connection
     * @param bootstrap        Configuration for new channels.
     * @param reconnectWorkers executor group for reconnect tasks.
     * @param timer            Timer used for delayed reconnect.
     */
    public ConnectionWatchdog(ClientOptions clientOptions, Bootstrap bootstrap, EventExecutorGroup reconnectWorkers, Timer timer) {
        this(clientOptions, bootstrap, timer, reconnectWorkers, null);
    }

    /**
     * Create a new watchdog that adds to new connections to the supplied {@link ChannelGroup} and establishes a new
     * {@link Channel} when disconnected, while reconnect is true. The socketAddressSupplier can supply the reconnect address.
     *
     * @param clientOptions         client options for the current connection
     * @param bootstrap             Configuration for new channels.
     * @param timer                 Timer used for delayed reconnect.
     * @param reconnectWorkers      executor group for reconnect tasks.
     * @param socketAddressSupplier the socket address suplier for gaining an address to reconnect to
     */
    public ConnectionWatchdog(ClientOptions clientOptions, Bootstrap bootstrap, Timer timer,
                              EventExecutorGroup reconnectWorkers, Supplier<SocketAddress> socketAddressSupplier) {
        this.clientOptions = clientOptions;
        this.bootstrap = bootstrap;
        this.timer = timer;
        this.reconnectWorkers = reconnectWorkers;
        this.socketAddressSupplier = socketAddressSupplier;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.debug("{} userEventTriggered({}, {})", logPrefix, ctx, evt);
        if (evt instanceof ConnectionEvents.PrepareClose) {

            ConnectionEvents.PrepareClose prepareClose = (ConnectionEvents.PrepareClose) evt;
            setListenOnChannelInactive(false);
            setReconnectSuspended(true);
            prepareClose.getPrepareCloseFuture().set(true);

            if (currentFuture != null && !currentFuture.isDone()) {
                currentFuture.cancel(true);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        logger.debug("{} channelActive({})", logPrefix, ctx);
        channel = ctx.channel();
        attempts = 0;
        remoteAddress = channel.remoteAddress();

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        logger.debug("{} channelInactive({})", logPrefix, ctx);
        channel = null;
        if (listenOnChannelInactive && !reconnectSuspended) {
            RedisChannelHandler<?, ?> channelHandler = ctx.pipeline().get(RedisChannelHandler.class);
            if (channelHandler != null) {
                timeout = channelHandler.getTimeout();
                timeoutUnit = channelHandler.getTimeoutUnit();
            }

            scheduleReconnect();
        } else {
            logger.debug("{} Reconnect scheduling disabled", logPrefix(), ctx);
            logger.debug("");
        }
        super.channelInactive(ctx);
    }

    /**
     * Schedule reconnect if channel is not available/not active.
     */
    public void scheduleReconnect() {
        logger.debug("{} scheduleReconnect()", logPrefix);

        if (!isEventLoopGroupActive()) {
            logger.debug("isEventLoopGroupActive() == false");
            return;
        }

        if (channel == null || !channel.isActive()) {
            if (attempts < RETRY_TIMEOUT_MAX) {
                attempts++;
            }
            int timeout = 2 << attempts;
            timer.newTimeout(new TimerTask() {
                @Override
                public void run(final Timeout timeout) throws Exception {

                    if (!isEventLoopGroupActive()) {
                        logger.debug("isEventLoopGroupActive() == false");
                        return;
                    }

                    if (reconnectWorkers != null) {
                        ConnectionWatchdog.this.run(timeout);
                        return;
                    }

                    reconnectWorkers.submit(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            ConnectionWatchdog.this.run(timeout);
                            return null;
                        }
                    });
                }
            }, timeout, TimeUnit.MILLISECONDS);
        } else {
            logger.debug("{} Skipping scheduleReconnect() because I have an active channel", logPrefix);
        }
    }

    /**
     * Reconnect to the remote address that the closed channel was connected to. This creates a new {@link ChannelPipeline} with
     * the same handler instances contained in the old channel's pipeline.
     *
     * @param timeout Timer task handle.
     * @throws Exception when reconnection fails.
     */
    @Override
    public void run(Timeout timeout) throws Exception {

        if (!isEventLoopGroupActive()) {
            logger.debug("isEventLoopGroupActive() == false");
            return;
        }

        boolean shouldLog = shouldLog();

        if (shouldLog) {
            lastReconnectionLogging = System.currentTimeMillis();
        }

        try {
            reconnect();
        } catch (InterruptedException e) {
            return;
        } catch (Exception e) {
            logger.warn("Cannot connect: {}", e.toString());
            if (!isReconnectSuspended()) {
                scheduleReconnect();
            }
        }
    }

    private void reconnect() throws Exception {

        logger.info("Reconnecting, last destination was " + remoteAddress);

        if (socketAddressSupplier != null) {
            try {
                remoteAddress = socketAddressSupplier.get();
            } catch (RuntimeException e) {
                logger.warn("Cannot retrieve the current address from socketAddressSupplier: " + e.toString()
                        + ", reusing old address " + remoteAddress);
            }
        }

        try {
            long timeLeft = timeoutUnit.toNanos(timeout);
            long start = System.nanoTime();
            currentFuture = bootstrap.connect(remoteAddress);
            if (!currentFuture.await(timeLeft, TimeUnit.NANOSECONDS)) {
                if (currentFuture.isCancellable()) {
                    currentFuture.cancel(true);
                }

                throw new TimeoutException("Reconnection attempt exceeded timeout of " + timeout + " " + timeoutUnit);
            }
            currentFuture.sync();

            RedisChannelInitializer channelInitializer = currentFuture.channel().pipeline().get(RedisChannelInitializer.class);
            CommandHandler<?, ?> commandHandler = currentFuture.channel().pipeline().get(CommandHandler.class);

            if (channelInitializer == null) {
                logger.warn("Reconnection attempt without a RedisChannelInitializer in the channel pipeline");
                closeChannel();
                return;
            }

            if (commandHandler == null) {
                logger.warn("Reconnection attempt without a CommandHandler in the channel pipeline");
                closeChannel();
                return;
            }

            try {
                timeLeft -= System.nanoTime() - start;
                channelInitializer.channelInitialized().get(Math.max(0, timeLeft), TimeUnit.NANOSECONDS);
                logger.info("Reconnected to " + remoteAddress);
            } catch (TimeoutException e) {
                channelInitializer.channelInitialized().cancel(true);
            } catch (Exception e) {
                if (clientOptions.isCancelCommandsOnReconnectFailure()) {
                    commandHandler.reset();
                }

                if (clientOptions.isSuspendReconnectOnProtocolFailure()) {
                    logger.error("Cannot initialize channel. Disabling autoReconnect", e);
                    setReconnectSuspended(true);
                } else {
                    logger.error("Cannot initialize channel.", e);
                    throw e;
                }
            }
        } finally {
            currentFuture = null;
        }
    }

    private void closeChannel() {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }

    private boolean isEventLoopGroupActive() {
        if (bootstrap.group().isShutdown() || bootstrap.group().isTerminated() || bootstrap.group().isShuttingDown()) {
            return false;
        }

        if (reconnectWorkers != null
                && (reconnectWorkers.isShutdown() || reconnectWorkers.isTerminated() || reconnectWorkers.isShuttingDown())) {
            return false;
        }

        return true;
    }

    private boolean shouldLog() {

        long quietUntil = lastReconnectionLogging + LOGGING_QUIET_TIME_MS;

        if (quietUntil > System.currentTimeMillis()) {
            return false;
        }

        return true;
    }

    /**
     * @param reconnect {@literal true} if reconnect is active
     * @deprecated use {@link #setListenOnChannelInactive(boolean)}
     */
    @Deprecated
    public void setReconnect(boolean reconnect) {
        setListenOnChannelInactive(reconnect);
    }

    public boolean isListenOnChannelInactive() {
        return listenOnChannelInactive;
    }

    public void setListenOnChannelInactive(boolean listenOnChannelInactive) {
        this.listenOnChannelInactive = listenOnChannelInactive;
    }

    public boolean isReconnectSuspended() {
        return reconnectSuspended;
    }

    public void setReconnectSuspended(boolean reconnectSuspended) {

        this.reconnectSuspended = reconnectSuspended;
    }

    private String logPrefix() {
        if (logPrefix != null) {
            return logPrefix;
        }
        StringBuffer buffer = new StringBuffer(64);
        buffer.append('[').append(ChannelLogDescriptor.logDescriptor(channel)).append(']');
        return logPrefix = buffer.toString();
    }

}
