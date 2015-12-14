package org.xbib.io.http.client;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.http.client.channel.ChannelPool;
import org.xbib.io.http.client.filter.FilterContext;
import org.xbib.io.http.client.filter.FilterException;
import org.xbib.io.http.client.filter.RequestFilter;
import org.xbib.io.http.client.handler.resumable.ResumableAsyncHandler;
import org.xbib.io.http.client.netty.channel.ChannelManager;
import org.xbib.io.http.client.netty.request.NettyRequestSender;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultAsyncHttpClient implements AsyncHttpClient {

    private final static Logger logger = LogManager.getLogger(DefaultAsyncHttpClient.class);
    private final AsyncHttpClientConfig config;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ChannelManager channelManager;
    private final NettyRequestSender requestSender;
    private final boolean allowStopNettyTimer;
    private final Timer nettyTimer;

    /**
     * Default signature calculator to use for all requests constructed by this
     * client instance.
     *
     * @since 1.1
     */
    protected SignatureCalculator signatureCalculator;

    /**
     * Create a new HTTP Asynchronous Client using the default
     * {@link DefaultAsyncHttpClientConfig} configuration. The default
     * {@link AsyncHttpClient} that will be used will be based on the classpath
     * configuration.
     *
     * If none of those providers are found, then the engine will throw an
     * IllegalStateException.
     */
    public DefaultAsyncHttpClient() {
        this(new DefaultAsyncHttpClientConfig.Builder().build());
    }

    /**
     * Create a new HTTP Asynchronous Client using the specified
     * {@link DefaultAsyncHttpClientConfig} configuration. This configuration
     * will be passed to the default {@link AsyncHttpClient} that will be
     * selected based on the classpath configuration.
     *
     * @param config a {@link DefaultAsyncHttpClientConfig}
     */
    public DefaultAsyncHttpClient(AsyncHttpClientConfig config) {

        this.config = config;

        allowStopNettyTimer = config.getNettyTimer() == null;
        nettyTimer = allowStopNettyTimer ? newNettyTimer() : config.getNettyTimer();

        channelManager = new ChannelManager(config, nettyTimer);
        requestSender = new NettyRequestSender(config, channelManager, nettyTimer, closed);
        channelManager.configureBootstraps(requestSender);
    }

    private Timer newNettyTimer() {
        HashedWheelTimer timer = new HashedWheelTimer();
        timer.start();
        return timer;
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            try {
                channelManager.close();

                if (allowStopNettyTimer) {
                    nettyTimer.stop();
                }

            } catch (Throwable t) {
                throw new IOException(t);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (!closed.get()) {
                logger.error("AsyncHttpClient.close() hasn't been invoked, which may produce file descriptor leaks");
            }
        } finally {
            super.finalize();
        }
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public DefaultAsyncHttpClient setSignatureCalculator(SignatureCalculator signatureCalculator) {
        this.signatureCalculator = signatureCalculator;
        return this;
    }

    @Override
    public BoundRequestBuilder prepareGet(String url) {
        return requestBuilder("GET", url);
    }

    @Override
    public BoundRequestBuilder prepareConnect(String url) {
        return requestBuilder("CONNECT", url);
    }

    @Override
    public BoundRequestBuilder prepareOptions(String url) {
        return requestBuilder("OPTIONS", url);
    }

    @Override
    public BoundRequestBuilder prepareHead(String url) {
        return requestBuilder("HEAD", url);
    }

    @Override
    public BoundRequestBuilder preparePost(String url) {
        return requestBuilder("POST", url);
    }

    @Override
    public BoundRequestBuilder preparePut(String url) {
        return requestBuilder("PUT", url);
    }

    @Override
    public BoundRequestBuilder prepareDelete(String url) {
        return requestBuilder("DELETE", url);
    }

    @Override
    public BoundRequestBuilder preparePatch(String url) {
        return requestBuilder("PATCH", url);
    }

    @Override
    public BoundRequestBuilder prepareTrace(String url) {
        return requestBuilder("TRACE", url);
    }

    @Override
    public BoundRequestBuilder prepareRequest(Request request) {
        return requestBuilder(request);
    }

    @Override
    public BoundRequestBuilder prepareRequest(RequestBuilder requestBuilder) {
        return prepareRequest(requestBuilder.build());
    }

    @Override
    public <T> ListenableFuture<T> executeRequest(Request request, AsyncHandler<T> handler) {

        if (config.getRequestFilters().isEmpty()) {
            return execute(request, handler);

        } else {
            FilterContext<T> fc = new FilterContext.FilterContextBuilder<T>().asyncHandler(handler).request(request).build();
            try {
                fc = preProcessRequest(fc);
            } catch (Exception e) {
                handler.onThrowable(e);
                return new ListenableFuture.CompletedFailure<>("preProcessRequest failed", e);
            }

            return execute(fc.getRequest(), fc.getAsyncHandler());
        }
    }

    @Override
    public <T> ListenableFuture<T> executeRequest(RequestBuilder requestBuilder, AsyncHandler<T> handler) {
        return executeRequest(requestBuilder.build(), handler);
    }

    @Override
    public ListenableFuture<Response> executeRequest(Request request) {
        return executeRequest(request, new AsyncCompletionHandlerBase());
    }

    @Override
    public ListenableFuture<Response> executeRequest(RequestBuilder requestBuilder) {
        return executeRequest(requestBuilder.build());
    }

    private <T> ListenableFuture<T> execute(Request request, final AsyncHandler<T> asyncHandler) {
        try {
            return requestSender.sendRequest(request, asyncHandler, null, false);
        } catch (Exception e) {
            asyncHandler.onThrowable(e);
            return new ListenableFuture.CompletedFailure<>(e);
        }
    }

    /**
     * Configure and execute the associated {@link RequestFilter}. This class
     * may decorate the {@link Request} and {@link AsyncHandler}
     *
     * @param fc {@link FilterContext}
     * @return {@link FilterContext}
     */
    private <T> FilterContext<T> preProcessRequest(FilterContext<T> fc) throws FilterException {
        for (RequestFilter asyncFilter : config.getRequestFilters()) {
            fc = asyncFilter.filter(fc);
        }

        Request request = fc.getRequest();
        if (fc.getAsyncHandler() instanceof ResumableAsyncHandler) {
            request = ResumableAsyncHandler.class.cast(fc.getAsyncHandler()).adjustRequestRange(request);
        }

        if (request.getRangeOffset() != 0) {
            RequestBuilder builder = new RequestBuilder(request);
            builder.setHeader("Range", "bytes=" + request.getRangeOffset() + "-");
            request = builder.build();
        }
        fc = new FilterContext.FilterContextBuilder<>(fc).request(request).build();
        return fc;
    }

    public ChannelPool getChannelPool() {
        return channelManager.getChannelPool();
    }

    protected BoundRequestBuilder requestBuilder(String method, String url) {
        return new BoundRequestBuilder(this, method, config.isDisableUrlEncodingForBoundRequests()).setUrl(url).setSignatureCalculator(signatureCalculator);
    }

    protected BoundRequestBuilder requestBuilder(Request prototype) {
        return new BoundRequestBuilder(this, prototype).setSignatureCalculator(signatureCalculator);
    }
}
