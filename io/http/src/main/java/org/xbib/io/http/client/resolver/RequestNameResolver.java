package org.xbib.io.http.client.resolver;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.Request;
import org.xbib.io.http.client.handler.AsyncHandlerExtensions;
import org.xbib.io.http.client.netty.SimpleGenericFutureListener;
import org.xbib.io.http.client.proxy.ProxyServer;
import org.xbib.io.http.client.uri.Uri;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

import static org.xbib.io.http.client.handler.AsyncHandlerExtensionsUtils.toAsyncHandlerExtensions;

public enum RequestNameResolver {

    INSTANCE;

    public Future<List<InetSocketAddress>> resolve(Request request, ProxyServer proxy, AsyncHandler<?> asyncHandler) {

        Uri uri = request.getUri();

        if (request.getAddress() != null) {
            List<InetSocketAddress> resolved = Collections.singletonList(new InetSocketAddress(request.getAddress(), uri.getExplicitPort()));
            Promise<List<InetSocketAddress>> promise = ImmediateEventExecutor.INSTANCE.newPromise();
            return promise.setSuccess(resolved);

        }

        // don't notify on explicit address
        final AsyncHandlerExtensions asyncHandlerExtensions = request.getAddress() == null ? toAsyncHandlerExtensions(asyncHandler) : null;
        final String name;
        final int port;

        if (proxy != null && !proxy.isIgnoredForHost(uri.getHost())) {
            name = proxy.getHost();
            port = uri.isSecured() ? proxy.getSecuredPort() : proxy.getPort();
        } else {
            name = uri.getHost();
            port = uri.getExplicitPort();
        }

        if (asyncHandlerExtensions != null) {
            asyncHandlerExtensions.onHostnameResolutionAttempt(name);
        }

        final Future<List<InetSocketAddress>> whenResolved = request.getNameResolver().resolve(name, port);

        if (asyncHandlerExtensions == null) {
            return whenResolved;
        } else {
            Promise<List<InetSocketAddress>> promise = ImmediateEventExecutor.INSTANCE.newPromise();

            whenResolved.addListener(new SimpleGenericFutureListener<List<InetSocketAddress>>() {

                @Override
                protected void onSuccess(List<InetSocketAddress> addresses) throws Exception {
                    asyncHandlerExtensions.onHostnameResolutionSuccess(name, addresses);
                    promise.setSuccess(addresses);
                }

                @Override
                protected void onFailure(Throwable t) throws Exception {
                    asyncHandlerExtensions.onHostnameResolutionFailure(name, t);
                    promise.setFailure(t);
                }
            });

            return promise;
        }
    }
}
