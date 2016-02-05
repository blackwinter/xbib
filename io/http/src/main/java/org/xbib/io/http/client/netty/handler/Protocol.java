package org.xbib.io.http.client.netty.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.HttpResponseHeaders;
import org.xbib.io.http.client.HttpResponseStatus;
import org.xbib.io.http.client.Realm;
import org.xbib.io.http.client.Request;
import org.xbib.io.http.client.RequestBuilder;
import org.xbib.io.http.client.cookie.Cookie;
import org.xbib.io.http.client.cookie.CookieDecoder;
import org.xbib.io.http.client.filter.FilterContext;
import org.xbib.io.http.client.filter.FilterException;
import org.xbib.io.http.client.filter.ResponseFilter;
import org.xbib.io.http.client.handler.MaxRedirectException;
import org.xbib.io.http.client.netty.NettyResponseFuture;
import org.xbib.io.http.client.netty.channel.ChannelManager;
import org.xbib.io.http.client.netty.request.NettyRequestSender;
import org.xbib.io.http.client.uri.Uri;
import org.xbib.io.http.client.util.MiscUtils;

import java.util.HashSet;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaders.Names.AUTHORIZATION;
import static io.netty.handler.codec.http.HttpHeaders.Names.PROXY_AUTHORIZATION;
import static org.xbib.io.http.client.util.HttpConstants.Methods.GET;
import static org.xbib.io.http.client.util.HttpConstants.ResponseStatusCodes.FOUND_302;
import static org.xbib.io.http.client.util.HttpConstants.ResponseStatusCodes.MOVED_PERMANENTLY_301;
import static org.xbib.io.http.client.util.HttpConstants.ResponseStatusCodes.SEE_OTHER_303;
import static org.xbib.io.http.client.util.HttpConstants.ResponseStatusCodes.TEMPORARY_REDIRECT_307;
import static org.xbib.io.http.client.util.HttpUtils.followRedirect;
import static org.xbib.io.http.client.util.HttpUtils.isSameBase;

public abstract class Protocol {

    public static final Set<Integer> REDIRECT_STATUSES = new HashSet<>();

    static {
        REDIRECT_STATUSES.add(MOVED_PERMANENTLY_301);
        REDIRECT_STATUSES.add(FOUND_302);
        REDIRECT_STATUSES.add(SEE_OTHER_303);
        REDIRECT_STATUSES.add(TEMPORARY_REDIRECT_307);
    }

    protected final ChannelManager channelManager;
    protected final AsyncHttpClientConfig config;
    protected final NettyRequestSender requestSender;
    protected final boolean hasIOExceptionFilters;
    private final boolean hasResponseFilters;
    private final MaxRedirectException maxRedirectException;

    public Protocol(ChannelManager channelManager, AsyncHttpClientConfig config, NettyRequestSender requestSender) {
        this.channelManager = channelManager;
        this.config = config;
        this.requestSender = requestSender;

        hasResponseFilters = !config.getResponseFilters().isEmpty();
        hasIOExceptionFilters = !config.getIoExceptionFilters().isEmpty();
        maxRedirectException = new MaxRedirectException("Maximum redirect reached: " + config.getMaxRedirects());
    }

    public abstract void handle(Channel channel, NettyResponseFuture<?> future, Object message) throws Exception;

    public abstract void onError(NettyResponseFuture<?> future, Throwable error);

    public abstract void onClose(NettyResponseFuture<?> future);

    private HttpHeaders propagatedHeaders(Request request, Realm realm, boolean keepBody) {

        HttpHeaders headers = request.getHeaders()//
                .remove(HttpHeaders.Names.HOST)//
                .remove(HttpHeaders.Names.CONTENT_LENGTH);

        if (!keepBody) {
            headers.remove(HttpHeaders.Names.CONTENT_TYPE);
        }

        if (realm != null && realm.getScheme() == Realm.AuthScheme.NTLM) {
            headers.remove(AUTHORIZATION)//
                    .remove(PROXY_AUTHORIZATION);
        }
        return headers;
    }

    protected boolean exitAfterHandlingRedirect(//
                                                Channel channel,//
                                                NettyResponseFuture<?> future,//
                                                HttpResponse response,//
                                                Request request,//
                                                int statusCode,//
                                                Realm realm) throws Exception {

        if (followRedirect(config, request)) {
            if (future.incrementAndGetCurrentRedirectCount() >= config.getMaxRedirects()) {
                throw maxRedirectException;

            } else {
                // We must allow auth handling again.
                future.getInAuth().set(false);
                future.getInProxyAuth().set(false);

                String originalMethod = request.getMethod();
                boolean switchToGet = !originalMethod.equals(GET) && (statusCode == MOVED_PERMANENTLY_301 || statusCode == SEE_OTHER_303 || (statusCode == FOUND_302 && !config.isStrict302Handling()));
                boolean keepBody = statusCode == TEMPORARY_REDIRECT_307 || (statusCode == FOUND_302 && config.isStrict302Handling());

                final RequestBuilder requestBuilder = new RequestBuilder(switchToGet ? GET : originalMethod)//
                        .setCookies(request.getCookies())//
                        .setChannelPoolPartitioning(request.getChannelPoolPartitioning())//
                        .setFollowRedirect(true)//
                        .setLocalAddress(request.getLocalAddress())//
                        .setNameResolver(request.getNameResolver())//
                        .setProxyServer(request.getProxyServer())//
                        .setRealm(request.getRealm())//
                        .setRequestTimeout(request.getRequestTimeout());

                if (keepBody) {
                    requestBuilder.setCharset(request.getCharset());
                    if (MiscUtils.isNonEmpty(request.getFormParams())) {
                        requestBuilder.setFormParams(request.getFormParams());
                    } else if (request.getStringData() != null) {
                        requestBuilder.setBody(request.getStringData());
                    } else if (request.getByteData() != null) {
                        requestBuilder.setBody(request.getByteData());
                    } else if (request.getByteBufferData() != null) {
                        requestBuilder.setBody(request.getByteBufferData());
                    } else if (request.getBodyGenerator() != null) {
                        requestBuilder.setBody(request.getBodyGenerator());
                    }
                }

                requestBuilder.setHeaders(propagatedHeaders(request, realm, keepBody));

                // in case of a redirect from HTTP to HTTPS, future
                // attributes might change
                final boolean initialConnectionKeepAlive = future.isKeepAlive();
                final Object initialPartitionKey = future.getPartitionKey();

                HttpHeaders responseHeaders = response.headers();
                String location = responseHeaders.get(HttpHeaders.Names.LOCATION);
                Uri newUri = Uri.create(future.getUri(), location);

                for (String cookieStr : responseHeaders.getAll(HttpHeaders.Names.SET_COOKIE)) {
                    Cookie c = CookieDecoder.decode(cookieStr);
                    if (c != null) {
                        requestBuilder.addOrReplaceCookie(c);
                    }
                }

                boolean sameBase = isSameBase(request.getUri(), newUri);

                if (sameBase) {
                    // we can only assume the virtual host is still valid if the baseUrl is the same
                    requestBuilder.setVirtualHost(request.getVirtualHost());
                }

                final Request nextRequest = requestBuilder.setUri(newUri).build();
                future.setTargetRequest(nextRequest);

                if (future.isKeepAlive() && !HttpHeaders.isTransferEncodingChunked(response)) {

                    if (sameBase) {
                        future.setReuseChannel(true);
                        // we can't directly send the next request because we still have to received LastContent
                        requestSender.drainChannelAndExecuteNextRequest(channel, future, nextRequest);
                    } else {
                        channelManager.drainChannelAndOffer(channel, future, initialConnectionKeepAlive, initialPartitionKey);
                        requestSender.sendNextRequest(nextRequest, future);
                    }

                } else {
                    // redirect + chunking = WAT
                    channelManager.closeChannel(channel);
                    requestSender.sendNextRequest(nextRequest, future);
                }

                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected boolean exitAfterProcessingFilters(//
                                                 Channel channel,//
                                                 NettyResponseFuture<?> future,//
                                                 AsyncHandler<?> handler, //
                                                 HttpResponseStatus status,//
                                                 HttpResponseHeaders responseHeaders) {

        if (hasResponseFilters) {
            FilterContext fc = new FilterContext.FilterContextBuilder().asyncHandler(handler).request(future.getCurrentRequest()).responseStatus(status).responseHeaders(responseHeaders)
                    .build();

            for (ResponseFilter asyncFilter : config.getResponseFilters()) {
                try {
                    fc = asyncFilter.filter(fc);
                } catch (FilterException efe) {
                    requestSender.abort(channel, future, efe);
                }
            }

            // The handler may have been wrapped.
            future.setAsyncHandler(fc.getAsyncHandler());

            // The request has changed
            if (fc.replayRequest()) {
                requestSender.replayRequest(future, fc, channel);
                return true;
            }
        }
        return false;
    }
}
