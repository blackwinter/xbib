package org.xbib.io.http.client.ws;

import org.xbib.io.http.client.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class WebSocketUtils {
    public static final String MAGIC_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public static String getKey() {
        byte[] nonce = createRandomBytes(16);
        return base64Encode(nonce);
    }

    public static String getAcceptKey(String key) throws UnsupportedEncodingException {
        String acceptSeed = key + MAGIC_GUID;
        byte[] sha1 = sha1(acceptSeed.getBytes("US-ASCII"));
        return base64Encode(sha1);
    }

    public static byte[] md5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("MD5 not supported on this platform");
        }
    }

    public static byte[] sha1(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return md.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("SHA-1 not supported on this platform");
        }
    }

    public static String base64Encode(byte[] bytes) {
        return Base64.encode(bytes);
    }

    public static byte[] createRandomBytes(int size) {
        byte[] bytes = new byte[size];

        for (int i = 0; i < size; i++) {
            bytes[i] = (byte) createRandomNumber(0, 255);
        }

        return bytes;
    }

    public static int createRandomNumber(int min, int max) {
        return (int) (Math.random() * max + min);
    }

}

