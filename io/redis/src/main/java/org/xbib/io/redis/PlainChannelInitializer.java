package org.xbib.io.redis;

import com.google.common.util.concurrent.SettableFuture;
import org.xbib.io.redis.codec.Utf8StringCodec;
import org.xbib.io.redis.event.EventBus;
import org.xbib.io.redis.event.connection.ConnectedEvent;
import org.xbib.io.redis.event.connection.ConnectionActivatedEvent;
import org.xbib.io.redis.event.connection.DisconnectedEvent;
import org.xbib.io.redis.protocol.Command;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

import java.util.List;
import java.util.concurrent.Future;

import static org.xbib.io.redis.ConnectionEventTrigger.local;
import static org.xbib.io.redis.ConnectionEventTrigger.remote;

/**
 */
class PlainChannelInitializer extends io.netty.channel.ChannelInitializer<Channel> implements RedisChannelInitializer {

    final static RedisCommandBuilder<String, String> INITIALIZING_CMD_BUILDER = new RedisCommandBuilder<String, String>(
            new Utf8StringCodec());
    private final List<ChannelHandler> handlers;
    private final EventBus eventBus;
    protected boolean pingBeforeActivate;
    protected SettableFuture<Boolean> initializedFuture = SettableFuture.create();

    public PlainChannelInitializer(boolean pingBeforeActivateConnection, List<ChannelHandler> handlers, EventBus eventBus) {
        this.pingBeforeActivate = pingBeforeActivateConnection;
        this.handlers = handlers;
        this.eventBus = eventBus;
    }

    static void pingBeforeActivate(final Command<?, ?, ?> cmd, final SettableFuture<Boolean> initializedFuture,
                                   final ChannelHandlerContext ctx, final List<ChannelHandler> handlers) throws Exception {
        cmd.addListener(new PingResponseListener(initializedFuture, cmd, ctx), ctx.executor());

        ctx.channel().writeAndFlush(cmd);
    }

    static void removeIfExists(ChannelPipeline pipeline, Class<? extends ChannelHandler> handlerClass) {
        ChannelHandler channelHandler = pipeline.get(handlerClass);
        if (channelHandler != null) {
            pipeline.remove(channelHandler);
        }
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {

        if (channel.pipeline().get("channelActivator") == null) {

            channel.pipeline().addLast("channelActivator", new RedisChannelInitializerImpl() {

                private Command<?, ?, ?> pingCommand;

                @Override
                public Future<Boolean> channelInitialized() {
                    return initializedFuture;
                }

                @Override
                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                    eventBus.publish(new DisconnectedEvent(local(ctx), remote(ctx)));
                    initializedFuture = SettableFuture.create();
                    pingCommand = null;
                    super.channelInactive(ctx);
                }

                @Override
                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                    if (evt instanceof ConnectionEvents.Close) {
                        if (ctx.channel().isOpen()) {
                            ctx.channel().close();
                        }
                    }

                    if (evt instanceof ConnectionEvents.Activated) {
                        if (!initializedFuture.isDone()) {
                            initializedFuture.set(true);
                            eventBus.publish(new ConnectionActivatedEvent(local(ctx), remote(ctx)));
                        }
                    }
                    super.userEventTriggered(ctx, evt);
                }

                @Override
                public void channelActive(final ChannelHandlerContext ctx) throws Exception {
                    eventBus.publish(new ConnectedEvent(local(ctx), remote(ctx)));
                    if (pingBeforeActivate) {
                        pingCommand = INITIALIZING_CMD_BUILDER.ping();
                        pingBeforeActivate(pingCommand, initializedFuture, ctx, handlers);
                    } else {
                        super.channelActive(ctx);
                    }
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    if (!initializedFuture.isDone()) {
                        initializedFuture.setException(cause);
                    }
                    super.exceptionCaught(ctx, cause);
                }
            });
        }

        for (ChannelHandler handler : handlers) {
            removeIfExists(channel.pipeline(), handler.getClass());
            channel.pipeline().addLast(handler);
        }
    }

    @Override
    public Future<Boolean> channelInitialized() {
        return initializedFuture;
    }

    private static class PingResponseListener implements Runnable {

        private final SettableFuture<Boolean> initializedFuture;
        private final Command<?, ?, ?> cmd;
        private final ChannelHandlerContext ctx;

        public PingResponseListener(SettableFuture<Boolean> initializedFuture, Command<?, ?, ?> cmd, ChannelHandlerContext ctx) {
            this.initializedFuture = initializedFuture;
            this.cmd = cmd;
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!initializedFuture.isDone()) {
                if (cmd.getException() != null) {
                    initializedFuture.setException(cmd.getException());
                    return;
                }

                initializedFuture.set(true);
                ctx.fireChannelActive();
            }
        }
    }
}
