package org.xbib.io.http.client;

import org.xbib.io.http.client.proxy.ProxyServer;

import static org.xbib.io.http.client.util.HttpConstants.Methods.DELETE;
import static org.xbib.io.http.client.util.HttpConstants.Methods.GET;
import static org.xbib.io.http.client.util.HttpConstants.Methods.HEAD;
import static org.xbib.io.http.client.util.HttpConstants.Methods.OPTIONS;
import static org.xbib.io.http.client.util.HttpConstants.Methods.PATCH;
import static org.xbib.io.http.client.util.HttpConstants.Methods.POST;
import static org.xbib.io.http.client.util.HttpConstants.Methods.PUT;
import static org.xbib.io.http.client.util.HttpConstants.Methods.TRACE;

public final class Dsl {

    private Dsl() {
    }

    public static AsyncHttpClient asyncHttpClient() {
        return new DefaultAsyncHttpClient();
    }

    public static AsyncHttpClient asyncHttpClient(DefaultAsyncHttpClientConfig.Builder configBuilder) {
        return new DefaultAsyncHttpClient(configBuilder.build());
    }

    public static AsyncHttpClient asyncHttpClient(AsyncHttpClientConfig config) {
        return new DefaultAsyncHttpClient(config);
    }

    public static RequestBuilder get(String url) {
        return request(GET, url);
    }

    public static RequestBuilder put(String url) {
        return request(PUT, url);
    }

    public static RequestBuilder post(String url) {
        return request(POST, url);
    }

    public static RequestBuilder delete(String url) {
        return request(DELETE, url);
    }

    public static RequestBuilder head(String url) {
        return request(HEAD, url);
    }

    public static RequestBuilder options(String url) {
        return request(OPTIONS, url);
    }

    public static RequestBuilder path(String url) {
        return request(PATCH, url);
    }

    public static RequestBuilder trace(String url) {
        return request(TRACE, url);
    }

    public static RequestBuilder request(String method, String url) {
        return new RequestBuilder(method).setUrl(url);
    }

    public static ProxyServer.Builder proxyServer(String host, int port) {
        return new ProxyServer.Builder(host, port);
    }

    public static DefaultAsyncHttpClientConfig.Builder config() {
        return new DefaultAsyncHttpClientConfig.Builder();
    }

    public static Realm.Builder realm(Realm prototype) {
        return new Realm.Builder(prototype.getPrincipal(), prototype.getPassword())//
                .setRealmName(prototype.getRealmName())//
                .setAlgorithm(prototype.getAlgorithm())//
                .setMethodName(prototype.getMethodName())//
                .setNc(prototype.getNc())//
                .setNonce(prototype.getNonce())//
                .setCharset(prototype.getCharset())//
                .setOpaque(prototype.getOpaque())//
                .setQop(prototype.getQop())//
                .setScheme(prototype.getScheme())//
                .setUri(prototype.getUri())//
                .setUsePreemptiveAuth(prototype.isUsePreemptiveAuth())//
                .setNtlmDomain(prototype.getNtlmDomain())//
                .setNtlmHost(prototype.getNtlmHost())//
                .setUseAbsoluteURI(prototype.isUseAbsoluteURI())//
                .setOmitQuery(prototype.isOmitQuery());
    }

    public static Realm.Builder realm(Realm.AuthScheme scheme, String principal, String password) {
        return new Realm.Builder(principal, password)//
                .setScheme(scheme);
    }

    public static Realm.Builder basicAuthRealm(String principal, String password) {
        return realm(Realm.AuthScheme.BASIC, principal, password);
    }

    public static Realm.Builder digestAuthRealm(String principal, String password) {
        return realm(Realm.AuthScheme.DIGEST, principal, password);
    }

    public static Realm.Builder ntlmAuthRealm(String principal, String password) {
        return realm(Realm.AuthScheme.NTLM, principal, password);
    }
}
