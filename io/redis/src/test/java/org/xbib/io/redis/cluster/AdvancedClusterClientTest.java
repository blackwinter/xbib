package org.xbib.io.redis.cluster;

import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.Duration;
import com.google.code.tempusfugit.temporal.ThreadSleep;
import com.google.code.tempusfugit.temporal.WaitFor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.xbib.io.redis.LettuceFutures;
import org.xbib.io.redis.RedisClusterAsyncConnection;
import org.xbib.io.redis.RedisClusterConnection;
import org.xbib.io.redis.RedisException;
import org.xbib.io.redis.RedisFuture;
import org.xbib.io.redis.RedisURI;
import org.xbib.io.redis.TestSettings;
import org.xbib.io.redis.cluster.models.partitions.Partitions;
import org.xbib.io.redis.cluster.models.partitions.RedisClusterNode;

import static org.assertj.core.api.Assertions.assertThat;

public class AdvancedClusterClientTest extends AbstractClusterTest {

    private RedisAdvancedClusterAsyncConnection<String, String> connection;

    @Before
    public void before() throws Exception {

        WaitFor.waitOrTimeout(new Condition() {
            @Override
            public boolean isSatisfied() {
                return clusterRule.isStable();
            }
        }, timeout(seconds(5)), new ThreadSleep(Duration.millis(500)));

        clusterClient.reloadPartitions();
        connection = clusterClient.connectClusterAsync();
    }

    @After
    public void after() throws Exception {
        connection.close();
    }

    @Test
    public void nodeConnections() throws Exception {

        assertThat(clusterClient.getPartitions()).hasSize(4);

        for (RedisClusterNode redisClusterNode : clusterClient.getPartitions()) {
            RedisClusterAsyncConnection<String, String> nodeConnection = connection.getConnection(redisClusterNode.getNodeId());

            String myid = nodeConnection.clusterMyId().get();
            assertThat(myid).isEqualTo(redisClusterNode.getNodeId());
        }
    }

    @Test
    public void differentConnections() throws Exception {

        for (RedisClusterNode redisClusterNode : clusterClient.getPartitions()) {
            RedisClusterAsyncConnection<String, String> nodeId = connection.getConnection(redisClusterNode.getNodeId());
            RedisClusterAsyncConnection<String, String> hostAndPort = connection.getConnection(redisClusterNode.getUri()
                    .getHost(), redisClusterNode.getUri().getPort());

            assertThat(nodeId).isNotSameAs(hostAndPort);
        }
    }

    @Test(expected = RedisException.class)
    public void unknownNodeId() throws Exception {

        connection.getConnection("unknown");
    }

    @Test(expected = RedisException.class)
    public void invalidHost() throws Exception {
        connection.getConnection("invalid-host", -1);
    }

    @Test
    public void doWeirdThingsWithClusterconnections() throws Exception {

        assertThat(clusterClient.getPartitions()).hasSize(4);

        for (RedisClusterNode redisClusterNode : clusterClient.getPartitions()) {
            RedisClusterAsyncConnection<String, String> nodeConnection = connection.getConnection(redisClusterNode.getNodeId());

            nodeConnection.close();

            RedisClusterAsyncConnection<String, String> nextConnection = connection.getConnection(redisClusterNode.getNodeId());
            assertThat(connection).isNotSameAs(nextConnection);
        }
    }

    @Test
    public void syncConnections() throws Exception {

        assertThat(clusterClient.getPartitions()).hasSize(4);

        RedisAdvancedClusterConnection<String, String> sync = clusterClient.connectCluster();
        for (RedisClusterNode redisClusterNode : clusterClient.getPartitions()) {
            RedisClusterConnection<String, String> nodeConnection = sync.getConnection(redisClusterNode.getNodeId());

            String myid = nodeConnection.clusterMyId();
            assertThat(myid).isEqualTo(redisClusterNode.getNodeId());
        }
    }

    @Test
    public void noAddr() throws Exception {

        RedisAdvancedClusterConnection<String, String> sync = clusterClient.connectCluster();
        try {

            Partitions partitions = clusterClient.getPartitions();
            for (RedisClusterNode partition : partitions) {
                partition.setUri(RedisURI.create("redis://non.existent.host:1234"));
            }

            sync.set("A", "value");// 6373
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RedisException.class).hasMessageContaining("Unable to connect to");
        }
        sync.close();
    }

    @Test
    public void forbiddenHostOnRedirect() throws Exception {

        RedisAdvancedClusterConnection<String, String> sync = clusterClient.connectCluster();
        try {

            Partitions partitions = clusterClient.getPartitions();
            for (RedisClusterNode partition : partitions) {
                partition.setSlots(ImmutableList.of(0));
                if (partition.getUri().getPort() == 7380) {
                    partition.setSlots(ImmutableList.of(6373));
                } else {
                    partition.setUri(RedisURI.create("redis://non.existent.host:1234"));
                }
            }

            partitions.updateCache();

            sync.set("A", "value");// 6373
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RedisException.class).hasMessageContaining("not allowed");
        }
        sync.close();
    }

    @Test
    public void getConnectionToNotAClusterMemberForbidden() throws Exception {

        RedisAdvancedClusterConnection<String, String> sync = clusterClient.connectCluster();
        try {
            sync.getConnection(TestSettings.host(), TestSettings.port());
        } catch (RedisException e) {
            assertThat(e).hasRootCauseExactlyInstanceOf(IllegalArgumentException.class);
        }
        sync.close();
    }


    @Test
    public void getConnectionToNotAClusterMemberAllowed() throws Exception {

        clusterClient.setOptions(new ClusterClientOptions.Builder().validateClusterNodeMembership(false).build());
        RedisAdvancedClusterConnection<String, String> sync = clusterClient.connectCluster();
        sync.getConnection(TestSettings.host(), TestSettings.port());
        sync.close();
    }

    @Test
    public void pipelining() throws Exception {

        RedisAdvancedClusterConnection<String, String> verificationConnection = clusterClient.connectCluster();

        // preheat the first connection
        connection.get(key(0)).get();

        int iterations = 1000;
        connection.setAutoFlushCommands(false);
        List<RedisFuture<?>> futures = Lists.newArrayList();
        for (int i = 0; i < iterations; i++) {
            futures.add(connection.set(key(i), value(i)));
        }

        for (int i = 0; i < iterations; i++) {
            assertThat(verificationConnection.get(key(i))).as("Key " + key(i) + " must be null").isNull();
        }

        connection.flushCommands();
        boolean result = LettuceFutures.awaitAll(5, TimeUnit.SECONDS, futures.toArray(new RedisFuture[futures.size()]));
        assertThat(result).isTrue();

        for (int i = 0; i < iterations; i++) {
            assertThat(verificationConnection.get(key(i))).as("Key " + key(i) + " must be " + value(i)).isEqualTo(value(i));
        }

        verificationConnection.close();
    }

    protected String value(int i) {
        return value + "-" + i;
    }

    protected String key(int i) {
        return key + "-" + i;
    }
}
