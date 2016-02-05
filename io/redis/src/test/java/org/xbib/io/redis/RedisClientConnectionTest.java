package org.xbib.io.redis;

import static org.xbib.io.redis.RedisURI.Builder.redis;

import org.junit.Test;

import org.xbib.io.redis.codec.Utf8StringCodec;

public class RedisClientConnectionTest extends AbstractCommandTest {

    @Test
    public void connectClienturi() throws Exception {
        client.connect().close();
    }

    @Test
    public void connectCodecClientUri() throws Exception {
        client.connect(new Utf8StringCodec()).close();
    }

    @Test
    public void connectOwnUri() throws Exception {
        client.connect(redis(host, port).build()).close();
    }

    @Test
    public void connectCodecOwnUri() throws Exception {
        client.connect(new Utf8StringCodec(), redis(host, port).build()).close();
    }

    @Test
    public void connectAsyncClientUri() throws Exception {
        client.connectAsync().close();
    }

    @Test
    public void connectAsyncCodecClientUri() throws Exception {
        client.connectAsync(new Utf8StringCodec()).close();
    }

    @Test
    public void connectAsyncOwnUri() throws Exception {
        client.connectAsync(redis(host, port).build()).close();
    }

    @Test
    public void connectAsyncCodecOwnUri() throws Exception {
        client.connectAsync(new Utf8StringCodec(), redis(host, port).build()).close();
    }

    /*
     * Standalone/PubSub Stateful
     */
    @Test
    public void connectPubSubClientUri() throws Exception {
        client.connectPubSub().close();
    }

    @Test
    public void connectPubSubCodecClientUri() throws Exception {
        client.connectPubSub(new Utf8StringCodec()).close();
    }

    @Test
    public void connectPubSubOwnUri() throws Exception {
        client.connectPubSub(redis(host, port).build()).close();
    }

    @Test
    public void connectPubSubCodecOwnUri() throws Exception {
        client.connectPubSub(new Utf8StringCodec(), redis(host, port).build()).close();
    }

    @Test
    public void connectSentinelAsyncClientUri() throws Exception {
        client.connectSentinelAsync().close();
    }

    @Test
    public void connectSentinelAsyncCodecClientUri() throws Exception {
        client.connectSentinelAsync(new Utf8StringCodec()).close();
    }

    @Test
    public void connectSentineAsynclOwnUri() throws Exception {
        client.connectSentinelAsync(redis(host, port).build()).close();
    }

    @Test
    public void connectSentinelAsyncCodecOwnUri() throws Exception {
        client.connectSentinelAsync(new Utf8StringCodec(), redis(host, port).build()).close();
    }
}
