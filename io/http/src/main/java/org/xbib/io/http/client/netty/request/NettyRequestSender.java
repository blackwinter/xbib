package org.xbib.io.http.client.netty.request;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.ListenableFuture;
import org.xbib.io.http.client.Realm;
import org.xbib.io.http.client.Request;
import org.xbib.io.http.client.exception.RemotelyClosedException;
import org.xbib.io.http.client.filter.FilterContext;
import org.xbib.io.http.client.filter.FilterException;
import org.xbib.io.http.client.filter.IOExceptionFilter;
import org.xbib.io.http.client.handler.AsyncHandlerExtensions;
import org.xbib.io.http.client.handler.TransferCompletionHandler;
import org.xbib.io.http.client.netty.Callback;
import org.xbib.io.http.client.netty.NettyResponseFuture;
import org.xbib.io.http.client.netty.SimpleGenericFutureListener;
import org.xbib.io.http.client.netty.channel.ChannelManager;
import org.xbib.io.http.client.netty.channel.ChannelState;
import org.xbib.io.http.client.netty.channel.Channels;
import org.xbib.io.http.client.netty.channel.NettyConnectListener;
import org.xbib.io.http.client.netty.timeout.ReadTimeoutTimerTask;
import org.xbib.io.http.client.netty.timeout.RequestTimeoutTimerTask;
import org.xbib.io.http.client.netty.timeout.TimeoutsHolder;
import org.xbib.io.http.client.proxy.ProxyServer;
import org.xbib.io.http.client.resolver.RequestNameResolver;
import org.xbib.io.http.client.uri.Uri;
import org.xbib.io.http.client.ws.WebSocketUpgradeHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.xbib.io.http.client.util.AuthenticatorUtils.perConnectionAuthorizationHeader;
import static org.xbib.io.http.client.util.AuthenticatorUtils.perConnectionProxyAuthorizationHeader;
import static org.xbib.io.http.client.util.HttpConstants.Methods.CONNECT;
import static org.xbib.io.http.client.util.HttpConstants.Methods.GET;
import static org.xbib.io.http.client.util.HttpUtils.requestTimeout;
import static org.xbib.io.http.client.util.MiscUtils.getCause;
import static org.xbib.io.http.client.util.ProxyUtils.getProxyServer;

public final class NettyRequestSender {

    private final AsyncHttpClientConfig config;
    private final ChannelManager channelManager;
    private final Timer nettyTimer;
    private final AtomicBoolean closed;
    private final NettyRequestFactory requestFactory;

    public NettyRequestSender(AsyncHttpClientConfig config,//
                              ChannelManager channelManager,//
                              Timer nettyTimer,//
                              AtomicBoolean closed) {
        this.config = config;
        this.channelManager = channelManager;
        this.nettyTimer = nettyTimer;
        this.closed = closed;
        requestFactory = new NettyRequestFactory(config);
    }

    public <T> ListenableFuture<T> sendRequest(final Request request,//
                                               final AsyncHandler<T> asyncHandler,//
                                               NettyResponseFuture<T> future,//
                                               boolean reclaimCache) {

        if (isClosed()) {
            throw new IllegalStateException("Closed");
        }

        validateWebSocketRequest(request, asyncHandler);

        ProxyServer proxyServer = getProxyServer(config, request);

        // websockets use connect tunnelling to work with proxies
        if (proxyServer != null && (request.getUri().isSecured() || request.getUri().isWebSocket()) && !isConnectDone(request, future)) {
            if (future != null && future.isConnectAllowed())
            // SSL proxy or websocket: CONNECT for sure
            {
                return sendRequestWithCertainForceConnect(request, asyncHandler, future, reclaimCache, proxyServer, true);
            } else
            // CONNECT will depend if we can pool or connection or if we have to open a new one
            {
                return sendRequestThroughSslProxy(request, asyncHandler, future, reclaimCache, proxyServer);
            }
        } else
        // no CONNECT for sure
        {
            return sendRequestWithCertainForceConnect(request, asyncHandler, future, reclaimCache, proxyServer, false);
        }
    }

    private boolean isConnectDone(Request request, NettyResponseFuture<?> future) {
        return future != null //
                && future.getNettyRequest() != null //
                && future.getNettyRequest().getHttpRequest().getMethod() == HttpMethod.CONNECT //
                && !request.getMethod().equals(CONNECT);
    }

    /**
     * We know for sure if we have to force to connect or not, so we can build the HttpRequest right away This reduces
     * the probability of having a pooled channel closed by the
     * server by the time we build the request
     */
    private <T> ListenableFuture<T> sendRequestWithCertainForceConnect(//
                                                                       Request request,//
                                                                       AsyncHandler<T> asyncHandler,//
                                                                       NettyResponseFuture<T> future,//
                                                                       boolean reclaimCache,//
                                                                       ProxyServer proxyServer,//
                                                                       boolean forceConnect) {

        NettyResponseFuture<T> newFuture = newNettyRequestAndResponseFuture(request, asyncHandler, future, proxyServer, forceConnect);

        Channel channel = getOpenChannel(future, request, proxyServer, asyncHandler);

        if (Channels.isChannelValid(channel)) {
            return sendRequestWithOpenChannel(request, proxyServer, newFuture, asyncHandler, channel);
        } else {
            return sendRequestWithNewChannel(request, proxyServer, newFuture, asyncHandler, reclaimCache);
        }
    }

    /**
     * Using CONNECT depends on wither we can fetch a valid channel or not Loop until we get a valid channel from the
     * pool and it's still valid once the request is built @
     */
    @SuppressWarnings("unused")
    private <T> ListenableFuture<T> sendRequestThroughSslProxy(//
                                                               Request request,//
                                                               AsyncHandler<T> asyncHandler,//
                                                               NettyResponseFuture<T> future,//
                                                               boolean reclaimCache,//
                                                               ProxyServer proxyServer) {

        NettyResponseFuture<T> newFuture = null;
        for (int i = 0; i < 3; i++) {
            Channel channel = getOpenChannel(future, request, proxyServer, asyncHandler);
            if (Channels.isChannelValid(channel)) {
                if (newFuture == null) {
                    newFuture = newNettyRequestAndResponseFuture(request, asyncHandler, future, proxyServer, false);
                }
            }

            if (Channels.isChannelValid(channel))
            // if the channel is still active, we can use it, otherwise try
            // gain
            {
                return sendRequestWithOpenChannel(request, proxyServer, newFuture, asyncHandler, channel);
            } else
            // pool is empty
            {
                break;
            }
        }

        newFuture = newNettyRequestAndResponseFuture(request, asyncHandler, future, proxyServer, true);
        return sendRequestWithNewChannel(request, proxyServer, newFuture, asyncHandler, reclaimCache);
    }

    private <T> NettyResponseFuture<T> newNettyRequestAndResponseFuture(final Request request, final AsyncHandler<T> asyncHandler, NettyResponseFuture<T> originalFuture,
                                                                        ProxyServer proxy, boolean forceConnect) {

        Realm realm = null;
        if (originalFuture != null) {
            realm = originalFuture.getRealm();
        } else if (config.getRealm() != null) {
            realm = config.getRealm();
        } else {
            realm = request.getRealm();
        }

        Realm proxyRealm = null;
        if (originalFuture != null) {
            proxyRealm = originalFuture.getProxyRealm();
        } else if (proxy != null) {
            proxyRealm = proxy.getRealm();
        }

        NettyRequest nettyRequest = requestFactory.newNettyRequest(request, forceConnect, proxy, realm, proxyRealm);

        if (originalFuture == null) {
            NettyResponseFuture<T> future = newNettyResponseFuture(request, asyncHandler, nettyRequest, proxy);
            future.setRealm(realm);
            future.setProxyRealm(proxyRealm);
            return future;
        } else {
            originalFuture.setNettyRequest(nettyRequest);
            originalFuture.setCurrentRequest(request);
            return originalFuture;
        }
    }

    private Channel getOpenChannel(NettyResponseFuture<?> future, Request request, ProxyServer proxyServer, AsyncHandler<?> asyncHandler) {

        if (future != null && future.reuseChannel() && Channels.isChannelValid(future.channel())) {
            return future.channel();
        } else {
            return pollPooledChannel(request, proxyServer, asyncHandler);
        }
    }

    private <T> ListenableFuture<T> sendRequestWithOpenChannel(Request request, ProxyServer proxy, NettyResponseFuture<T> future, AsyncHandler<T> asyncHandler, Channel channel) {

        if (asyncHandler instanceof AsyncHandlerExtensions) {
            AsyncHandlerExtensions.class.cast(asyncHandler).onConnectionPooled(channel);
        }

        future.setChannelState(ChannelState.POOLED);
        future.attachChannel(channel, false);

        if (Channels.isChannelValid(channel)) {
            Channels.setAttribute(channel, future);
            writeRequest(future, channel);
        } else {
            // bad luck, the channel was closed in-between
            // there's a very good chance onClose was already notified but the
            // future wasn't already registered
            handleUnexpectedClosedChannel(channel, future);
        }

        return future;
    }

    private <T> ListenableFuture<T> sendRequestWithNewChannel(//
                                                              Request request,//
                                                              ProxyServer proxy,//
                                                              NettyResponseFuture<T> future,//
                                                              AsyncHandler<T> asyncHandler,//
                                                              boolean reclaimCache) {

        // some headers are only set when performing the first request
        HttpHeaders headers = future.getNettyRequest().getHttpRequest().headers();
        Realm realm = future.getRealm();
        Realm proxyRealm = future.getProxyRealm();
        requestFactory.addAuthorizationHeader(headers, perConnectionAuthorizationHeader(request, proxy, realm));
        requestFactory.setProxyAuthorizationHeader(headers, perConnectionProxyAuthorizationHeader(request, proxyRealm));

        future.getInAuth().set(realm != null && realm.isUsePreemptiveAuth() && realm.getScheme() != Realm.AuthScheme.NTLM);
        future.getInProxyAuth().set(proxyRealm != null && proxyRealm.isUsePreemptiveAuth() && proxyRealm.getScheme() != Realm.AuthScheme.NTLM);

        // Do not throw an exception when we need an extra connection for a redirect
        // FIXME why? This violate the max connection per host handling, right?
        Bootstrap bootstrap = channelManager.getBootstrap(request.getUri(), proxy);

        Object partitionKey = future.getPartitionKey();

        final boolean channelPreempted = !reclaimCache;

        try {
            // Do not throw an exception when we need an extra connection for a
            // redirect.
            if (channelPreempted) {
                // if there's an exception here, channel wasn't preempted and resolve won't happen
                channelManager.preemptChannel(partitionKey);
            }
        } catch (Throwable t) {
            abort(null, future, getCause(t));
            // exit and don't try to resolve address
            return future;
        }

        RequestNameResolver.INSTANCE.resolve(request, proxy, asyncHandler)//
                .addListener(new SimpleGenericFutureListener<List<InetSocketAddress>>() {

                    @Override
                    protected void onSuccess(List<InetSocketAddress> addresses) {
                        NettyConnectListener<T> connectListener = new NettyConnectListener<>(future, NettyRequestSender.this, channelManager, channelPreempted, partitionKey);
                        new NettyChannelConnector(request.getLocalAddress(), addresses, asyncHandler).connect(bootstrap, connectListener);
                    }

                    @Override
                    protected void onFailure(Throwable cause) {
                        if (channelPreempted) {
                            channelManager.abortChannelPreemption(partitionKey);
                        }
                        abort(null, future, getCause(cause));
                    }
                });

        return future;
    }

    private <T> NettyResponseFuture<T> newNettyResponseFuture(Request request, AsyncHandler<T> asyncHandler, NettyRequest nettyRequest, ProxyServer proxyServer) {

        NettyResponseFuture<T> future = new NettyResponseFuture<>(//
                request,//
                asyncHandler,//
                nettyRequest,//
                config.getMaxRequestRetry(),//
                request.getChannelPoolPartitioning(),//
                proxyServer);

        String expectHeader = request.getHeaders().get(HttpHeaders.Names.EXPECT);
        if (expectHeader != null && expectHeader.equalsIgnoreCase(HttpHeaders.Values.CONTINUE)) {
            future.setDontWriteBodyBecauseExpectContinue(true);
        }
        return future;
    }

    public <T> void writeRequest(NettyResponseFuture<T> future, Channel channel) {

        NettyRequest nettyRequest = future.getNettyRequest();
        HttpRequest httpRequest = nettyRequest.getHttpRequest();
        AsyncHandler<T> handler = future.getAsyncHandler();

        // if the channel is dead because it was pooled and the remote
        // server decided to close it,
        // we just let it go and the channelInactive do its work
        if (!Channels.isChannelValid(channel)) {
            return;
        }

        try {
            if (handler instanceof TransferCompletionHandler) {
                configureTransferAdapter(handler, httpRequest);
            }

            boolean writeBody = !future.isDontWriteBodyBecauseExpectContinue() && httpRequest.getMethod() != HttpMethod.CONNECT && nettyRequest.getBody() != null;

            if (!future.isHeadersAlreadyWrittenOnContinue()) {
                if (future.getAsyncHandler() instanceof AsyncHandlerExtensions) {
                    AsyncHandlerExtensions.class.cast(future.getAsyncHandler()).onRequestSend(nettyRequest);
                }

                ChannelProgressivePromise promise = channel.newProgressivePromise();
                ChannelFuture f = writeBody ? channel.write(httpRequest, promise) : channel.writeAndFlush(httpRequest, promise);
                f.addListener(new ProgressListener(future.getAsyncHandler(), future, true, 0L));
            }

            if (writeBody) {
                nettyRequest.getBody().write(channel, future);
            }

            // don't bother scheduling timeouts if channel became invalid
            if (Channels.isChannelValid(channel)) {
                scheduleTimeouts(future);
            }

        } catch (Exception e) {
            abort(channel, future, e);
        }
    }

    private void configureTransferAdapter(AsyncHandler<?> handler, HttpRequest httpRequest) {
        HttpHeaders h = new DefaultHttpHeaders(false).set(httpRequest.headers());
        TransferCompletionHandler.class.cast(handler).headers(h);
    }

    private void scheduleTimeouts(NettyResponseFuture<?> nettyResponseFuture) {

        nettyResponseFuture.touch();
        int requestTimeoutInMs = requestTimeout(config, nettyResponseFuture.getTargetRequest());
        TimeoutsHolder timeoutsHolder = new TimeoutsHolder();
        if (requestTimeoutInMs != -1) {
            Timeout requestTimeout = newTimeout(new RequestTimeoutTimerTask(nettyResponseFuture, this, timeoutsHolder, requestTimeoutInMs), requestTimeoutInMs);
            timeoutsHolder.requestTimeout = requestTimeout;
        }

        int readTimeoutValue = config.getReadTimeout();
        if (readTimeoutValue != -1 && readTimeoutValue < requestTimeoutInMs) {
            // no need to schedule a readTimeout if the requestTimeout happens first
            Timeout readTimeout = newTimeout(new ReadTimeoutTimerTask(nettyResponseFuture, this, timeoutsHolder, requestTimeoutInMs, readTimeoutValue), readTimeoutValue);
            timeoutsHolder.readTimeout = readTimeout;
        }
        nettyResponseFuture.setTimeoutsHolder(timeoutsHolder);
    }

    public Timeout newTimeout(TimerTask task, long delay) {
        return nettyTimer.newTimeout(task, delay, TimeUnit.MILLISECONDS);
    }

    public void abort(Channel channel, NettyResponseFuture<?> future, Throwable t) {

        if (channel != null) {
            channelManager.closeChannel(channel);
        }

        if (!future.isDone()) {
            future.setChannelState(ChannelState.CLOSED);
            future.abort(t);
        }
    }

    public void handleUnexpectedClosedChannel(Channel channel, NettyResponseFuture<?> future) {
        if (future.isDone()) {
            channelManager.closeChannel(channel);
        } else if (!retry(future)) {
            abort(channel, future, RemotelyClosedException.INSTANCE);
        }
    }

    public boolean retry(NettyResponseFuture<?> future) {

        if (isClosed()) {
            return false;
        }

        if (future.canBeReplayed()) {
            future.setChannelState(ChannelState.RECONNECTED);
            future.getAndSetStatusReceived(false);

            if (future.getAsyncHandler() instanceof AsyncHandlerExtensions) {
                AsyncHandlerExtensions.class.cast(future.getAsyncHandler()).onRetry();
            }

            try {
                sendNextRequest(future.getCurrentRequest(), future);
                return true;

            } catch (Exception e) {
                abort(future.channel(), future, e);
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean applyIoExceptionFiltersAndReplayRequest(NettyResponseFuture<?> future, IOException e, Channel channel) {

        boolean replayed = false;

        @SuppressWarnings({"unchecked", "rawtypes"})
        FilterContext<?> fc = new FilterContext.FilterContextBuilder().asyncHandler(future.getAsyncHandler()).request(future.getCurrentRequest()).ioException(e).build();
        for (IOExceptionFilter asyncFilter : config.getIoExceptionFilters()) {
            try {
                fc = asyncFilter.filter(fc);
            } catch (FilterException efe) {
                abort(channel, future, efe);
            }
        }

        if (fc.replayRequest() && future.canBeReplayed()) {
            replayRequest(future, fc, channel);
            replayed = true;
        }
        return replayed;
    }

    public <T> void sendNextRequest(final Request request, final NettyResponseFuture<T> future) {
        sendRequest(request, future.getAsyncHandler(), future, true);
    }

    private void validateWebSocketRequest(Request request, AsyncHandler<?> asyncHandler) {
        Uri uri = request.getUri();
        boolean isWs = uri.isWebSocket();
        if (asyncHandler instanceof WebSocketUpgradeHandler) {
            if (!isWs) {
                throw new IllegalArgumentException("WebSocketUpgradeHandler but scheme isn't ws or wss: " + uri.getScheme());
            } else if (!request.getMethod().equals(GET)) {
                throw new IllegalArgumentException("WebSocketUpgradeHandler but method isn't GET: " + request.getMethod());
            }
        } else if (isWs) {
            throw new IllegalArgumentException("No WebSocketUpgradeHandler but scheme is " + uri.getScheme());
        }
    }

    private Channel pollPooledChannel(Request request, ProxyServer proxy, AsyncHandler<?> asyncHandler) {

        if (asyncHandler instanceof AsyncHandlerExtensions) {
            AsyncHandlerExtensions.class.cast(asyncHandler).onConnectionPoolAttempt();
        }

        Uri uri = request.getUri();
        String virtualHost = request.getVirtualHost();
        final Channel channel = channelManager.poll(uri, virtualHost, proxy, request.getChannelPoolPartitioning());

        return channel;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void replayRequest(final NettyResponseFuture<?> future, FilterContext fc, Channel channel) {

        Request newRequest = fc.getRequest();
        future.setAsyncHandler(fc.getAsyncHandler());
        future.setChannelState(ChannelState.NEW);
        future.touch();

        if (future.getAsyncHandler() instanceof AsyncHandlerExtensions) {
            AsyncHandlerExtensions.class.cast(future.getAsyncHandler()).onRetry();
        }

        channelManager.drainChannelAndOffer(channel, future);
        sendNextRequest(newRequest, future);
    }

    public boolean isClosed() {
        return closed.get();
    }

    public final Callback newExecuteNextRequestCallback(final NettyResponseFuture<?> future, final Request nextRequest) {

        return new Callback(future) {
            @Override
            public void call() {
                sendNextRequest(nextRequest, future);
            }
        };
    }

    public void drainChannelAndExecuteNextRequest(final Channel channel, final NettyResponseFuture<?> future, Request nextRequest) {
        Channels.setAttribute(channel, newExecuteNextRequestCallback(future, nextRequest));
    }
}
