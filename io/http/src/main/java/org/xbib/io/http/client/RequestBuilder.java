package org.xbib.io.http.client;

import static org.xbib.io.http.client.util.HttpConstants.Methods.GET;

/**
 * Builder for a {@link org.asynchttpclient.Request}. Warning: mutable and not thread-safe! Beware that it holds a
 * reference on the Request instance it builds, so modifying the builder will modify the
 * request even after it has been built.
 */
public class RequestBuilder extends RequestBuilderBase<RequestBuilder> {

    public RequestBuilder() {
        this(GET);
    }

    public RequestBuilder(String method) {
        this(method, false);
    }

    public RequestBuilder(String method, boolean disableUrlEncoding) {
        super(method, disableUrlEncoding);
    }

    public RequestBuilder(String method, boolean disableUrlEncoding, boolean validateHeaders) {
        super(method, disableUrlEncoding, validateHeaders);
    }

    public RequestBuilder(Request prototype) {
        super(prototype);
    }

    public RequestBuilder(Request prototype, boolean disableUrlEncoding, boolean validateHeaders) {
        super(prototype, disableUrlEncoding, validateHeaders);
    }
}
