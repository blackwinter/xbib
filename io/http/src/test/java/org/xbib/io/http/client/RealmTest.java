package org.xbib.io.http.client;

import org.testng.annotations.Test;
import org.xbib.io.http.client.uri.Uri;

import java.math.BigInteger;
import java.security.MessageDigest;

import static java.nio.charset.StandardCharsets.UTF_16;
import static org.testng.Assert.assertEquals;
import static org.xbib.io.http.client.Dsl.basicAuthRealm;
import static org.xbib.io.http.client.Dsl.digestAuthRealm;
import static org.xbib.io.http.client.Dsl.realm;

public class RealmTest {
    @Test(groups = "standalone")
    public void testClone() {
        Realm orig = basicAuthRealm("user", "pass").setCharset(UTF_16)//
                .setUsePreemptiveAuth(true)//
                .setRealmName("realm")//
                .setAlgorithm("algo").build();

        Realm clone = realm(orig).build();
        assertEquals(clone.getPrincipal(), orig.getPrincipal());
        assertEquals(clone.getPassword(), orig.getPassword());
        assertEquals(clone.getCharset(), orig.getCharset());
        assertEquals(clone.isUsePreemptiveAuth(), orig.isUsePreemptiveAuth());
        assertEquals(clone.getRealmName(), orig.getRealmName());
        assertEquals(clone.getAlgorithm(), orig.getAlgorithm());
        assertEquals(clone.getScheme(), orig.getScheme());
    }

    @Test(groups = "standalone")
    public void testOldDigestEmptyString() {
        String qop = "";
        testOldDigest(qop);
    }

    @Test(groups = "standalone")
    public void testOldDigestNull() {
        String qop = null;
        testOldDigest(qop);
    }

    private void testOldDigest(String qop) {
        String user = "user";
        String pass = "pass";
        String realm = "realm";
        String nonce = "nonce";
        String method = "GET";
        Uri uri = Uri.create("http://ahc.io/foo");
        Realm orig = digestAuthRealm(user, pass)//
                .setNonce(nonce)//
                .setUri(uri)//
                .setMethodName(method)//
                .setRealmName(realm)//
                .setQop(qop).build();

        String ha1 = getMd5(user + ":" + realm + ":" + pass);
        String ha2 = getMd5(method + ":" + uri.getPath());
        String expectedResponse = getMd5(ha1 + ":" + nonce + ":" + ha2);

        assertEquals(expectedResponse, orig.getResponse());
    }

    @Test(groups = "standalone")
    public void testStrongDigest() {
        String user = "user";
        String pass = "pass";
        String realm = "realm";
        String nonce = "nonce";
        String method = "GET";
        Uri uri = Uri.create("http://ahc.io/foo");
        String qop = "auth";
        Realm orig = digestAuthRealm(user, pass)//
                .setNonce(nonce)//
                .setUri(uri)//
                .setMethodName(method)//
                .setRealmName(realm)//
                .setQop(qop).build();

        String nc = orig.getNc();
        String cnonce = orig.getCnonce();
        String ha1 = getMd5(user + ":" + realm + ":" + pass);
        String ha2 = getMd5(method + ":" + uri.getPath());
        String expectedResponse = getMd5(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2);

        assertEquals(expectedResponse, orig.getResponse());
    }

    private String getMd5(String what) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(what.getBytes("ISO-8859-1"));
            byte[] hash = md.digest();
            BigInteger bi = new BigInteger(1, hash);
            String result = bi.toString(16);
            if (result.length() % 2 != 0) {
                return "0" + result;
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
