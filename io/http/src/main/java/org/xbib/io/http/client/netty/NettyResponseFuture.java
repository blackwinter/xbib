package org.xbib.io.http.client.netty;

import io.netty.channel.Channel;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.Realm;
import org.xbib.io.http.client.Request;
import org.xbib.io.http.client.channel.ChannelPoolPartitioning;
import org.xbib.io.http.client.future.AbstractListenableFuture;
import org.xbib.io.http.client.netty.channel.ChannelState;
import org.xbib.io.http.client.netty.channel.Channels;
import org.xbib.io.http.client.netty.request.NettyRequest;
import org.xbib.io.http.client.netty.timeout.TimeoutsHolder;
import org.xbib.io.http.client.proxy.ProxyServer;
import org.xbib.io.http.client.uri.Uri;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.xbib.io.http.client.util.DateUtils.millisTime;
import static org.xbib.io.http.client.util.MiscUtils.getCause;

/**
 * A {@link Future} that can be used to track when an asynchronous HTTP request has been fully processed.
 *
 * @param <V> the result type
 */
public final class NettyResponseFuture<V> extends AbstractListenableFuture<V> {

    private final long start = millisTime();
    private final ChannelPoolPartitioning connectionPoolPartitioning;
    private final ProxyServer proxyServer;
    private final int maxRetry;
    private final CountDownLatch latch = new CountDownLatch(1);

    // state mutated from outside the event loop
    // TODO check if they are indeed mutated outside the event loop
    private final AtomicBoolean isDone = new AtomicBoolean(false);
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private final AtomicInteger redirectCount = new AtomicInteger();
    private final AtomicBoolean inAuth = new AtomicBoolean(false);
    private final AtomicBoolean inProxyAuth = new AtomicBoolean(false);
    private final AtomicBoolean statusReceived = new AtomicBoolean(false);
    private final AtomicLong touch = new AtomicLong(millisTime());
    private final AtomicReference<ChannelState> channelState = new AtomicReference<>(ChannelState.NEW);
    private final AtomicBoolean contentProcessed = new AtomicBoolean(false);
    private final AtomicInteger currentRetry = new AtomicInteger(0);
    private final AtomicBoolean onThrowableCalled = new AtomicBoolean(false);
    private final AtomicReference<V> content = new AtomicReference<>();
    private final AtomicReference<ExecutionException> exEx = new AtomicReference<>();
    private volatile TimeoutsHolder timeoutsHolder;

    // state mutated only inside the event loop
    private Channel channel;
    private boolean keepAlive = true;
    private Request targetRequest;
    private Request currentRequest;
    private NettyRequest nettyRequest;
    private AsyncHandler<V> asyncHandler;
    private boolean streamWasAlreadyConsumed;
    private boolean reuseChannel;
    private boolean headersAlreadyWrittenOnContinue;
    private boolean dontWriteBodyBecauseExpectContinue;
    private boolean allowConnect;
    private Realm realm;
    private Realm proxyRealm;

    public NettyResponseFuture(Request originalRequest,//
                               AsyncHandler<V> asyncHandler,//
                               NettyRequest nettyRequest,//
                               int maxRetry,//
                               ChannelPoolPartitioning connectionPoolPartitioning,//
                               ProxyServer proxyServer) {

        this.asyncHandler = asyncHandler;
        this.targetRequest = currentRequest = originalRequest;
        this.nettyRequest = nettyRequest;
        this.connectionPoolPartitioning = connectionPoolPartitioning;
        this.proxyServer = proxyServer;
        this.maxRetry = maxRetry;
    }

    // java.util.concurrent.Future

    @Override
    public boolean isDone() {
        return isDone.get() || isCancelled();
    }

    @Override
    public boolean isCancelled() {
        return isCancelled.get();
    }

    @Override
    public boolean cancel(boolean force) {
        cancelTimeouts();

        if (isCancelled.getAndSet(true)) {
            return false;
        }

        // cancel could happen before channel was attached
        if (channel != null) {
            Channels.setDiscard(channel);
            Channels.silentlyCloseChannel(channel);
        }

        if (!onThrowableCalled.getAndSet(true)) {
            try {
                asyncHandler.onThrowable(new CancellationException());
            } catch (Throwable t) {
                //
            }
        }
        latch.countDown();
        runListeners();
        return true;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        latch.await();
        return getContent();
    }

    @Override
    public V get(long l, TimeUnit tu) throws InterruptedException, TimeoutException, ExecutionException {
        if (!latch.await(l, tu)) {
            throw new TimeoutException();
        }
        return getContent();
    }

    private V getContent() throws ExecutionException {

        if (isCancelled()) {
            throw new CancellationException();
        }

        ExecutionException e = exEx.get();
        if (e != null) {
            throw e;
        }

        V update = content.get();
        // No more retry
        currentRetry.set(maxRetry);
        if (!contentProcessed.getAndSet(true)) {
            try {
                update = asyncHandler.onCompleted();
            } catch (Throwable ex) {
                if (!onThrowableCalled.getAndSet(true)) {
                    try {
                        try {
                            asyncHandler.onThrowable(ex);
                        } catch (Throwable t) {
                            //
                        }
                        throw new RuntimeException(ex);
                    } finally {
                        cancelTimeouts();
                    }
                }
            }
            content.compareAndSet(null, update);
        }
        return update;
    }

    // org.asynchttpclient.ListenableFuture

    private boolean terminateAndExit() {
        cancelTimeouts();
        this.channel = null;
        this.reuseChannel = false;
        return isDone.getAndSet(true) || isCancelled.get();
    }

    public final void done() {

        if (terminateAndExit()) {
            return;
        }

        try {
            getContent();

        } catch (ExecutionException t) {
            return;
        } catch (RuntimeException t) {
            exEx.compareAndSet(null, new ExecutionException(getCause(t)));

        } finally {
            latch.countDown();
        }

        runListeners();
    }

    public final void abort(final Throwable t) {

        exEx.compareAndSet(null, new ExecutionException(t));

        if (terminateAndExit()) {
            return;
        }

        if (onThrowableCalled.compareAndSet(false, true)) {
            try {
                asyncHandler.onThrowable(t);
            } catch (Throwable te) {
                //
            }
        }
        latch.countDown();
        runListeners();
    }

    @Override
    public void touch() {
        touch.set(millisTime());
    }

    @Override
    public CompletableFuture<V> toCompletableFuture() {
        CompletableFuture<V> completable = new CompletableFuture<>();
        addListener(new Runnable() {
            @Override
            public void run() {
                ExecutionException e = exEx.get();
                if (e != null) {
                    completable.completeExceptionally(e);
                } else {
                    completable.complete(content.get());
                }
            }

        }, new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        });

        return completable;
    }

    public Uri getUri() {
        return targetRequest.getUri();
    }

    public ChannelPoolPartitioning getConnectionPoolPartitioning() {
        return connectionPoolPartitioning;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public void cancelTimeouts() {
        if (timeoutsHolder != null) {
            timeoutsHolder.cancel();
            timeoutsHolder = null;
        }
    }

    public final Request getTargetRequest() {
        return targetRequest;
    }

    public void setTargetRequest(Request targetRequest) {
        this.targetRequest = targetRequest;
    }

    public final Request getCurrentRequest() {
        return currentRequest;
    }

    public void setCurrentRequest(Request currentRequest) {
        this.currentRequest = currentRequest;
    }

    public final NettyRequest getNettyRequest() {
        return nettyRequest;
    }

    public final void setNettyRequest(NettyRequest nettyRequest) {
        this.nettyRequest = nettyRequest;
    }

    public final AsyncHandler<V> getAsyncHandler() {
        return asyncHandler;
    }

    public void setAsyncHandler(AsyncHandler<V> asyncHandler) {
        this.asyncHandler = asyncHandler;
    }

    public final boolean isKeepAlive() {
        return keepAlive;
    }

    public final void setKeepAlive(final boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int incrementAndGetCurrentRedirectCount() {
        return redirectCount.incrementAndGet();
    }

    public void setTimeoutsHolder(TimeoutsHolder timeoutsHolder) {
        this.timeoutsHolder = timeoutsHolder;
    }

    public AtomicBoolean getInAuth() {
        return inAuth;
    }

    public AtomicBoolean getInProxyAuth() {
        return inProxyAuth;
    }

    public ChannelState getChannelState() {
        return channelState.get();
    }

    public void setChannelState(ChannelState channelState) {
        this.channelState.set(channelState);
    }

    public boolean getAndSetStatusReceived(boolean sr) {
        return statusReceived.getAndSet(sr);
    }

    public boolean isStreamWasAlreadyConsumed() {
        return streamWasAlreadyConsumed;
    }

    public void setStreamWasAlreadyConsumed(boolean streamWasAlreadyConsumed) {
        this.streamWasAlreadyConsumed = streamWasAlreadyConsumed;
    }

    public long getLastTouch() {
        return touch.get();
    }

    public boolean isHeadersAlreadyWrittenOnContinue() {
        return headersAlreadyWrittenOnContinue;
    }

    public void setHeadersAlreadyWrittenOnContinue(boolean headersAlreadyWrittenOnContinue) {
        this.headersAlreadyWrittenOnContinue = headersAlreadyWrittenOnContinue;
    }

    public boolean isDontWriteBodyBecauseExpectContinue() {
        return dontWriteBodyBecauseExpectContinue;
    }

    public void setDontWriteBodyBecauseExpectContinue(boolean dontWriteBodyBecauseExpectContinue) {
        this.dontWriteBodyBecauseExpectContinue = dontWriteBodyBecauseExpectContinue;
    }

    public void setReuseChannel(boolean reuseChannel) {
        this.reuseChannel = reuseChannel;
    }

    public boolean isConnectAllowed() {
        return allowConnect;
    }

    public void setConnectAllowed(boolean allowConnect) {
        this.allowConnect = allowConnect;
    }

    public void attachChannel(Channel channel, boolean reuseChannel) {

        // future could have been cancelled first
        if (isDone()) {
            Channels.silentlyCloseChannel(channel);
        }

        this.channel = channel;
        this.reuseChannel = reuseChannel;
    }

    public Channel channel() {
        return channel;
    }

    public boolean reuseChannel() {
        return reuseChannel;
    }

    public boolean canRetry() {
        return maxRetry > 0 && currentRetry.incrementAndGet() <= maxRetry;
    }

    /**
     * Return true if the {@link Future} can be recovered. There is some scenario where a connection can be closed by an
     * unexpected IOException, and in some situation we can
     * recover from that exception.
     *
     * @return true if that {@link Future} cannot be recovered.
     */
    public boolean canBeReplayed() {
        return !isDone() && canRetry() && !(Channels.isChannelValid(channel) && !getUri().getScheme().equalsIgnoreCase("https")) && !inAuth.get() && !inProxyAuth.get();
    }

    public long getStart() {
        return start;
    }

    public Object getPartitionKey() {
        return connectionPoolPartitioning.getPartitionKey(targetRequest.getUri(), targetRequest.getVirtualHost(), proxyServer);
    }

    public Realm getRealm() {
        return realm;
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    public Realm getProxyRealm() {
        return proxyRealm;
    }

    public void setProxyRealm(Realm proxyRealm) {
        this.proxyRealm = proxyRealm;
    }

    @Override
    public String toString() {
        return "NettyResponseFuture{" + //
                "currentRetry=" + currentRetry + //
                ",\n\tisDone=" + isDone + //
                ",\n\tisCancelled=" + isCancelled + //
                ",\n\tasyncHandler=" + asyncHandler + //
                ",\n\tnettyRequest=" + nettyRequest + //
                ",\n\tcontent=" + content + //
                ",\n\turi=" + getUri() + //
                ",\n\tkeepAlive=" + keepAlive + //
                ",\n\texEx=" + exEx + //
                ",\n\tredirectCount=" + redirectCount + //
                ",\n\ttimeoutsHolder=" + timeoutsHolder + //
                ",\n\tinAuth=" + inAuth + //
                ",\n\tstatusReceived=" + statusReceived + //
                ",\n\ttouch=" + touch + //
                '}';
    }
}
