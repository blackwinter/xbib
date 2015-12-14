package org.xbib.io.http.client.netty.ssl;

import org.xbib.io.http.client.AsyncHttpClientConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class JsseSslEngineFactory extends SslEngineFactoryBase {

    private final SSLContext sslContext;

    public JsseSslEngineFactory(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    public SSLEngine newSslEngine(AsyncHttpClientConfig config, String peerHost, int peerPort) {
        SSLEngine sslEngine = sslContext.createSSLEngine(peerHost, peerPort);
        configureSslEngine(sslEngine, config);
        return sslEngine;
    }
}
