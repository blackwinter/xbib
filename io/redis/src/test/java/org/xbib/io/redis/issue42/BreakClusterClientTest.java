package org.xbib.io.redis.issue42;

import static com.google.code.tempusfugit.temporal.Duration.*;
import static com.google.code.tempusfugit.temporal.Timeout.*;

import java.util.concurrent.TimeUnit;

import org.xbib.io.redis.FastShutdown;
import org.junit.*;

import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.Duration;
import com.google.code.tempusfugit.temporal.ThreadSleep;
import com.google.code.tempusfugit.temporal.WaitFor;
import org.xbib.io.redis.RedisClusterConnection;
import org.xbib.io.redis.RedisURI;
import org.xbib.io.redis.TestSettings;
import org.xbib.io.redis.cluster.ClusterRule;
import org.xbib.io.redis.cluster.RedisClusterClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BreakClusterClientTest extends BreakClientBase {
    public static final String host = TestSettings.hostAddr();
    public static final int port1 = 7379;
    public static final int port2 = 7380;
    public static final int port3 = 7381;
    public static final int port4 = 7382;

    private static RedisClusterClient clusterClient;
    private RedisClusterConnection<String, String> clusterConnection;

    @Rule
    public ClusterRule clusterRule = new ClusterRule(clusterClient, port1, port2, port3, port4);

    @BeforeClass
    public static void setupClient() {
        clusterClient = new RedisClusterClient(RedisURI.Builder.redis(host, port1).withTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build());
    }

    @AfterClass
    public static void shutdownClient() {
        FastShutdown.shutdown(clusterClient);
    }

    @Before
    public void setUp() throws Exception {
        WaitFor.waitOrTimeout(new Condition() {
            @Override
            public boolean isSatisfied() {
                return clusterRule.isStable();
            }
        }, timeout(seconds(5)), new ThreadSleep(Duration.millis(500)));

        clusterConnection = clusterClient.connectCluster(this.slowCodec);

    }

    @After
    public void tearDown() throws Exception {
        clusterConnection.close();
    }

    @Test
    @Ignore
    public void testStandAlone() throws Exception {
        testSingle(clusterConnection);
    }

    @Test
    @Ignore
    public void testLooping() throws Exception {
        testLoop(clusterConnection);
    }

}
