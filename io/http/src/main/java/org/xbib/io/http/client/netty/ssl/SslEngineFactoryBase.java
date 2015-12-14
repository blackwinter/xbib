package org.xbib.io.http.client.netty.ssl;

import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.SslEngineFactory;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import static org.xbib.io.http.client.util.MiscUtils.isNonEmpty;

public abstract class SslEngineFactoryBase implements SslEngineFactory {

    protected void configureSslEngine(SSLEngine sslEngine, AsyncHttpClientConfig config) {
        sslEngine.setUseClientMode(true);
        if (!config.isAcceptAnyCertificate()) {
            SSLParameters params = sslEngine.getSSLParameters();
            params.setEndpointIdentificationAlgorithm("HTTPS");
            sslEngine.setSSLParameters(params);
        }

        if (isNonEmpty(config.getEnabledProtocols())) {
            sslEngine.setEnabledProtocols(config.getEnabledProtocols());
        }

        if (isNonEmpty(config.getEnabledCipherSuites())) {
            sslEngine.setEnabledCipherSuites(config.getEnabledCipherSuites());
        }
    }
}
