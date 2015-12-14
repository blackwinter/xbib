package org.xbib.io.http.client.netty.request;

import io.netty.handler.codec.http.HttpRequest;
import org.xbib.io.http.client.netty.request.body.NettyBody;

public final class NettyRequest {

    private final HttpRequest httpRequest;
    private final NettyBody body;

    public NettyRequest(HttpRequest httpRequest, NettyBody body) {
        this.httpRequest = httpRequest;
        this.body = body;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public NettyBody getBody() {
        return body;
    }
}
