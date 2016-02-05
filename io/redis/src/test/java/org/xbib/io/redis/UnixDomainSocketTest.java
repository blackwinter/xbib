package org.xbib.io.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.xbib.io.redis.TestSettings.*;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;

import io.netty.util.internal.SystemPropertyUtil;

public class UnixDomainSocketTest {

    public static final String MASTER_ID = "mymaster";

    private static RedisClient sentinelClient;

    @Rule
    public SentinelRule sentinelRule = new SentinelRule(sentinelClient, false, 26379, 26380);

    protected Logger log = LogManager.getLogger(getClass());

    protected String key = "key";
    protected String value = "value";

    @BeforeClass
    public static void setupClient() {
        sentinelClient = getRedisSentinelClient();
    }

    @AfterClass
    public static void shutdownClient() {
        FastShutdown.shutdown(sentinelClient);
    }

    @Before
    public void openConnection() throws Exception {
        sentinelRule.monitor(MASTER_ID, hostAddr(), TestSettings.port(), 1, true);
    }

    @Test
    public void standalone_Linux_x86_64_socket() throws Exception {

        linuxOnly();

        RedisURI redisURI = getSocketRedisUri();

        RedisClient redisClient = new RedisClient(redisURI);

        RedisConnection<String, String> connection = redisClient.connect();

        someRedisAction(connection);
        connection.close();

        FastShutdown.shutdown(redisClient);
    }

    private void linuxOnly() {
        String osName = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
        assumeTrue("Only supported on Linux, your os is " + osName, osName.startsWith("linux"));
    }

    private RedisURI getSocketRedisUri() throws IOException {
        File file = new File(TestSettings.socket()).getCanonicalFile();
        return RedisURI.create(RedisURI.URI_SCHEME_REDIS_SOCKET + "://" + file.getCanonicalPath());
    }

    private RedisURI getSentinelSocketRedisUri() throws IOException {
        File file = new File(TestSettings.sentinelSocket()).getCanonicalFile();
        return RedisURI.create(RedisURI.URI_SCHEME_REDIS_SOCKET + "://" + file.getCanonicalPath());
    }

    @Test
    public void sentinel_Linux_x86_64_socket() throws Exception {

        linuxOnly();

        RedisURI uri = new RedisURI();
        uri.getSentinels().add(getSentinelSocketRedisUri());
        uri.setSentinelMasterId("mymaster");

        RedisClient redisClient = new RedisClient(uri);

        RedisConnection<String, String> connection = redisClient.connect();

        someRedisAction(connection);

        connection.close();

        RedisSentinelAsyncConnection<String, String> sentinelConnection = redisClient.connectSentinelAsync();

        assertThat(sentinelConnection.ping().get()).isEqualTo("PONG");
        sentinelConnection.close();

        FastShutdown.shutdown(redisClient);
    }

    @Test
    public void sentinel_Linux_x86_64_socket_and_inet() throws Exception {

        linuxOnly();

        RedisURI uri = new RedisURI();
        uri.getSentinels().add(getSentinelSocketRedisUri());
        uri.getSentinels().add(RedisURI.create(RedisURI.URI_SCHEME_REDIS + "://" + TestSettings.host() + ":26379"));
        uri.setSentinelMasterId("mymaster");

        RedisClient redisClient = new RedisClient(uri);

        RedisSentinelAsyncConnection<String, String> sentinelConnection = redisClient
                .connectSentinelAsync(getSentinelSocketRedisUri());
        log.info("Masters: " + sentinelConnection.masters().get());

        try {
            redisClient.connect();
            fail("Missing validation exception");
        } catch (RedisConnectionException e) {
            assertThat(e).hasMessageContaining("You cannot mix unix domain socket and IP socket URI's");
        } finally {
            FastShutdown.shutdown(redisClient);
        }

    }

    private void someRedisAction(RedisConnection<String, String> connection) {
        connection.set(key, value);
        String result = connection.get(key);

        assertThat(result).isEqualTo(value);
    }

    protected static RedisClient getRedisSentinelClient() {
        return new RedisClient(RedisURI.Builder.sentinel(TestSettings.host(), MASTER_ID).build());
    }
}
