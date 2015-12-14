package org.xbib.io.http.client.config;

public final class AsyncHttpClientConfigDefaults {

    public static final String ASYNC_CLIENT_CONFIG_ROOT = "org.asynchttpclient.";

    private AsyncHttpClientConfigDefaults() {
    }

    public static String defaultThreadPoolName() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getString(ASYNC_CLIENT_CONFIG_ROOT + "threadPoolName");
    }

    public static int defaultMaxConnections() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "maxConnections");
    }

    public static int defaultMaxConnectionsPerHost() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "maxConnectionsPerHost");
    }

    public static int defaultConnectTimeout() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "connectTimeout");
    }

    public static int defaultPooledConnectionIdleTimeout() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "pooledConnectionIdleTimeout");
    }

    public static int defaultReadTimeout() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "readTimeout");
    }

    public static int defaultRequestTimeout() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "requestTimeout");
    }

    public static int defaultConnectionTtl() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "connectionTtl");
    }

    public static boolean defaultFollowRedirect() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "followRedirect");
    }

    public static int defaultMaxRedirects() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "maxRedirects");
    }

    public static boolean defaultCompressionEnforced() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "compressionEnforced");
    }

    public static String defaultUserAgent() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getString(ASYNC_CLIENT_CONFIG_ROOT + "userAgent");
    }

    public static String[] defaultEnabledProtocols() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getStringArray(ASYNC_CLIENT_CONFIG_ROOT + "enabledProtocols");
    }

    public static boolean defaultUseProxySelector() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "useProxySelector");
    }

    public static boolean defaultUseProxyProperties() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "useProxyProperties");
    }

    public static boolean defaultValidateResponseHeaders() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "validateResponseHeaders");
    }

    public static boolean defaultStrict302Handling() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "strict302Handling");
    }

    public static boolean defaultKeepAlive() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "keepAlive");
    }

    public static int defaultMaxRequestRetry() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "maxRequestRetry");
    }

    public static boolean defaultDisableUrlEncodingForBoundRequests() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "disableUrlEncodingForBoundRequests");
    }

    public static boolean defaultUseOpenSsl() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "useOpenSsl");
    }

    public static boolean defaultAcceptAnyCertificate() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "acceptAnyCertificate");
    }

    public static int defaultSslSessionCacheSize() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "sslSessionCacheSize");
    }

    public static int defaultSslSessionTimeout() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "sslSessionTimeout");
    }

    public static int defaultHttpClientCodecMaxInitialLineLength() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "httpClientCodecMaxInitialLineLength");
    }

    public static int defaultHttpClientCodecMaxHeaderSize() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "httpClientCodecMaxHeaderSize");
    }

    public static int defaultHttpClientCodecMaxChunkSize() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "httpClientCodecMaxChunkSize");
    }

    public static boolean defaultDisableZeroCopy() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "disableZeroCopy");
    }

    public static int defaultHandshakeTimeout() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "handshakeTimeout");
    }

    public static int defaultChunkedFileChunkSize() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "chunkedFileChunkSize");
    }

    public static int defaultWebSocketMaxBufferSize() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "webSocketMaxBufferSize");
    }

    public static int defaultWebSocketMaxFrameSize() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "webSocketMaxFrameSize");
    }

    public static boolean defaultKeepEncodingHeader() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "keepEncodingHeader");
    }

    public static int defaultShutdownQuietPeriod() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "shutdownQuietPeriod");
    }

    public static int defaultShutdownTimeout() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getInt(ASYNC_CLIENT_CONFIG_ROOT + "shutdownTimeout");
    }

    public static boolean defaultUseNativeTransport() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "useNativeTransport");
    }

    public static boolean defaultUsePooledMemory() {
        return AsyncHttpClientConfigHelper.getAsyncHttpClientConfig().getBoolean(ASYNC_CLIENT_CONFIG_ROOT + "usePooledMemory");
    }
}
