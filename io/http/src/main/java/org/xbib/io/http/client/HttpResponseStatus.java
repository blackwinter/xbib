package org.xbib.io.http.client;

import org.xbib.io.http.client.uri.Uri;

import java.net.SocketAddress;

/**
 * A class that represent the HTTP response' status line (code + text)
 */
public abstract class HttpResponseStatus {

    protected final AsyncHttpClientConfig config;
    private final Uri uri;

    public HttpResponseStatus(Uri uri, AsyncHttpClientConfig config) {
        this.uri = uri;
        this.config = config;
    }

    /**
     * Return the request {@link Uri}
     *
     * @return the request {@link Uri}
     */
    public final Uri getUri() {
        return uri;
    }

    /**
     * Return the response status code
     *
     * @return the response status code
     */
    public abstract int getStatusCode();

    /**
     * Return the response status text
     *
     * @return the response status text
     */
    public abstract String getStatusText();

    /**
     * Protocol name from status line.
     *
     * @return Protocol name.
     */
    public abstract String getProtocolName();

    /**
     * Protocol major version.
     *
     * @return Major version.
     */
    public abstract int getProtocolMajorVersion();

    /**
     * Protocol minor version.
     *
     * @return Minor version.
     */
    public abstract int getProtocolMinorVersion();

    /**
     * Full protocol name + version
     *
     * @return protocol name + version
     */
    public abstract String getProtocolText();

    /**
     * Get remote address client initiated request to.
     *
     * @return remote address client initiated request to, may be {@code null}
     * if asynchronous provider is unable to provide the remote address
     */
    public abstract SocketAddress getRemoteAddress();

    /**
     * Get local address client initiated request from.
     *
     * @return local address client initiated request from, may be {@code null}
     * if asynchronous provider is unable to provide the local address
     */
    public abstract SocketAddress getLocalAddress();
}
