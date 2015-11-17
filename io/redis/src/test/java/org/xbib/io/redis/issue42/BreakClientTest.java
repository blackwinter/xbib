package org.xbib.io.redis.issue42;

import java.util.concurrent.TimeUnit;

import org.junit.*;

import org.xbib.io.redis.RedisClient;
import org.xbib.io.redis.RedisConnection;
import org.xbib.io.redis.TestSettings;

public class BreakClientTest extends BreakClientBase {
    public static final String host = TestSettings.host();
    public static final int port = TestSettings.port();

    protected static RedisClient client;

    protected RedisConnection<String, String> redis;

    @BeforeClass
    public static void setupClient() {
        client = new RedisClient(host, port);
    }

    @Before
    public void setUp() throws Exception {
        client.setDefaultTimeout(TIMEOUT, TimeUnit.SECONDS);
        redis = client.connect(this.slowCodec);
        redis.flushall();
        redis.flushdb();
    }

    @After
    public void tearDown() throws Exception {
        redis.close();
    }

    @Test
    @Ignore
    public void testStandAlone() throws Exception {
        testSingle(redis);
    }

    @Test
    @Ignore
    public void testLooping() throws Exception {
        testLoop(redis);
    }

}
