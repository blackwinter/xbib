package org.xbib.io.http.netty.ssl;

import org.xbib.io.http.client.SslEngineFactory;
import org.xbib.io.http.client.netty.ssl.JsseSslEngineFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpsUtils {

    public static SslEngineFactory createSslEngineFactory(boolean trusted) throws SSLException {
        final AtomicBoolean trust = new AtomicBoolean(trusted);
        try {
            KeyManager[] keyManagers = createKeyManagers();
            TrustManager[] trustManagers = new TrustManager[]{dummyTrustManager(trust, (X509TrustManager) createTrustManagers()[0])};
            SecureRandom secureRandom = new SecureRandom();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, secureRandom);
            return new JsseSslEngineFactory(sslContext);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static KeyManager[] createKeyManagers() throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance("JKS");
        try (InputStream keyStoreStream = HttpsUtils.class.getClassLoader().getResourceAsStream("/ssltest-cacerts.jks")) {
            char[] keyStorePassword = "changeit".toCharArray();
            ks.load(keyStoreStream, keyStorePassword);
        }
        assert (ks.size() > 0);
        char[] certificatePassword = "changeit".toCharArray();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, certificatePassword);
        return kmf.getKeyManagers();
    }

    private static TrustManager[] createTrustManagers() throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance("JKS");
        try (InputStream keyStoreStream = HttpsUtils.class.getClassLoader().getResourceAsStream("/ssltest-keystore.jks")) {
            char[] keyStorePassword = "changeit".toCharArray();
            ks.load(keyStoreStream, keyStorePassword);
        }
        assert (ks.size() > 0);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        return tmf.getTrustManagers();
    }

    private static TrustManager dummyTrustManager(final AtomicBoolean trust, final X509TrustManager tm) {
        return new DummyTrustManager(trust, tm);

    }
}
