package org.xbib.io.http.client.oauth;

import org.xbib.io.http.client.util.StringUtils;
import org.xbib.io.http.client.util.Utf8UrlEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Since cloning (of MAC instances)  is not necessarily supported on all platforms
 * (and specifically seems to fail on MacOS), let's wrap synchronization/reuse details here.
 * Assumption is that this is bit more efficient (even considering synchronization)
 * than locating and reconstructing instance each time.
 * In future we may want to use soft references and thread local instance.
 */
public class ThreadSafeHMAC {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private final Mac mac;

    public ThreadSafeHMAC(ConsumerKey consumerAuth, RequestToken userAuth) {
        StringBuilder sb = new StringBuilder(); //StringUtils.stringBuilder();
        Utf8UrlEncoder.encodeAndAppendQueryElement(sb, consumerAuth.getSecret());
        sb.append('&');
        if (userAuth != null && userAuth.getSecret() != null) {
            Utf8UrlEncoder.encodeAndAppendQueryElement(sb, userAuth.getSecret());
        }
        byte[] keyBytes = StringUtils.charSequence2Bytes(sb, UTF_8);
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM);

        // Get an hmac_sha1 instance and initialize with the signing key
        try {
            mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

    }

    public synchronized byte[] digest(ByteBuffer message) {
        mac.reset();
        mac.update(message);
        return mac.doFinal();
    }
}
