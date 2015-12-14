package org.xbib.io.http.client;

import javax.net.ssl.SSLEngine;

public interface SslEngineFactory {

    /**
     * Creates new {@link SSLEngine}.
     *
     * @param config   the client config
     * @param peerHost the peer hostname
     * @param peerPort the peer port
     * @return new engine
     */
    SSLEngine newSslEngine(AsyncHttpClientConfig config, String peerHost, int peerPort);
}
