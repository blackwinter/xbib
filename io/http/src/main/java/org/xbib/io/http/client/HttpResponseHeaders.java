package org.xbib.io.http.client;

import io.netty.handler.codec.http.HttpHeaders;

/**
 * A class that represent the HTTP headers.
 */
public class HttpResponseHeaders {

    private final HttpHeaders headers;
    private final boolean trailling;

    public HttpResponseHeaders(HttpHeaders headers) {
        this(headers, false);
    }

    public HttpResponseHeaders(HttpHeaders headers, boolean trailling) {
        this.headers = headers;
        this.trailling = trailling;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public boolean isTrailling() {
        return trailling;
    }
}
