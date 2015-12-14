package org.xbib.io.http.client.netty.ssl;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.xbib.io.http.client.AsyncHttpClientConfig;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

public class DefaultSslEngineFactory extends SslEngineFactoryBase {

    private final SslContext sslContext;

    public DefaultSslEngineFactory(AsyncHttpClientConfig config) throws SSLException {
        this.sslContext = getSslContext(config);
    }

    private SslContext getSslContext(AsyncHttpClientConfig config) throws SSLException {
        if (config.getSslContext() != null) {
            return config.getSslContext();
        }

        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()//
                .sslProvider(config.isUseOpenSsl() ? SslProvider.OPENSSL : SslProvider.JDK)//
                .sessionCacheSize(config.getSslSessionCacheSize())//
                .sessionTimeout(config.getSslSessionTimeout());

        if (config.isAcceptAnyCertificate()) {
            sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        }

        return sslContextBuilder.build();
    }

    @Override
    public SSLEngine newSslEngine(AsyncHttpClientConfig config, String peerHost, int peerPort) {
        // FIXME should be using ctx allocator
        SSLEngine sslEngine = sslContext.newEngine(ByteBufAllocator.DEFAULT, peerHost, peerPort);
        configureSslEngine(sslEngine, config);
        return sslEngine;
    }
}
