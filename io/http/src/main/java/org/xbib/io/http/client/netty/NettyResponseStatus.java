package org.xbib.io.http.client.netty;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponse;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.HttpResponseStatus;
import org.xbib.io.http.client.uri.Uri;

import java.net.SocketAddress;

/**
 * A class that represent the HTTP response' status line (code + text)
 */
public class NettyResponseStatus extends HttpResponseStatus {

    private final HttpResponse response;
    private final SocketAddress remoteAddress;
    private final SocketAddress localAddress;

    public NettyResponseStatus(Uri uri, AsyncHttpClientConfig config, HttpResponse response, Channel channel) {
        super(uri, config);
        this.response = response;
        if (channel != null) {
            remoteAddress = channel.remoteAddress();
            localAddress = channel.localAddress();
        } else {
            remoteAddress = null;
            localAddress = null;
        }
    }

    /**
     * Return the response status code
     *
     * @return the response status code
     */
    public int getStatusCode() {
        return response.getStatus().code();
    }

    /**
     * Return the response status text
     *
     * @return the response status text
     */
    public String getStatusText() {
        return response.getStatus().reasonPhrase();
    }

    @Override
    public String getProtocolName() {
        return response.getProtocolVersion().protocolName();
    }

    @Override
    public int getProtocolMajorVersion() {
        return response.getProtocolVersion().majorVersion();
    }

    @Override
    public int getProtocolMinorVersion() {
        return response.getProtocolVersion().minorVersion();
    }

    @Override
    public String getProtocolText() {
        return response.getProtocolVersion().text();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public SocketAddress getLocalAddress() {
        return localAddress;
    }
}
