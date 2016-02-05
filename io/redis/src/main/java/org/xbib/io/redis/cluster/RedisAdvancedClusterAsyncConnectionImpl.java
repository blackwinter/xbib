package org.xbib.io.redis.cluster;

import org.xbib.io.redis.ReadFrom;
import org.xbib.io.redis.RedisAsyncConnectionImpl;
import org.xbib.io.redis.RedisChannelWriter;
import org.xbib.io.redis.RedisClusterAsyncConnection;
import org.xbib.io.redis.RedisFuture;
import org.xbib.io.redis.cluster.models.partitions.Partitions;
import org.xbib.io.redis.cluster.models.partitions.RedisClusterNode;
import org.xbib.io.redis.codec.RedisCodec;
import io.netty.channel.ChannelHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Advanced asynchronous Cluster connection.
 */
@ChannelHandler.Sharable
public class RedisAdvancedClusterAsyncConnectionImpl<K, V> extends RedisAsyncConnectionImpl<K, V> implements
        RedisAdvancedClusterAsyncConnection<K, V> {

    private Partitions partitions;

    /**
     * Initialize a new connection.
     *
     * @param writer  the channel writer
     * @param codec   Codec used to encode/decode keys and values.
     * @param timeout Maximum time to wait for a response.
     * @param unit    Unit of time for the timeout.
     */
    public RedisAdvancedClusterAsyncConnectionImpl(RedisChannelWriter<K, V> writer, RedisCodec<K, V> codec, long timeout,
                                                   TimeUnit unit) {
        super(writer, codec, timeout, unit);
    }

    @SuppressWarnings("unchecked")
    ClusterDistributionChannelWriter<K, V> getWriter() {
        return (ClusterDistributionChannelWriter<K, V>) super.getChannelWriter();
    }

    @Override
    public RedisClusterAsyncConnection<K, V> getConnection(String nodeId) {

        RedisAsyncConnectionImpl<K, V> connection = getWriter().getClusterConnectionProvider().getConnection(
                ClusterConnectionProvider.Intent.WRITE, nodeId);

        return connection;
    }

    @Override
    public RedisFuture<List<K>> clusterGetKeysInSlot(int slot, int count) {
        RedisClusterAsyncConnection<K, V> connectionBySlot = findConnectionBySlot(slot);

        if (connectionBySlot != null) {
            return connectionBySlot.clusterGetKeysInSlot(slot, count);
        }

        return super.clusterGetKeysInSlot(slot, count);
    }

    @Override
    public RedisFuture<Long> clusterCountKeysInSlot(int slot) {
        RedisClusterAsyncConnection<K, V> connectionBySlot = findConnectionBySlot(slot);

        if (connectionBySlot != null) {
            return connectionBySlot.clusterCountKeysInSlot(slot);
        }

        return super.clusterCountKeysInSlot(slot);
    }

    private RedisClusterAsyncConnection<K, V> findConnectionBySlot(int slot) {
        RedisClusterNode node = partitions.getPartitionBySlot(slot);
        if (node != null) {
            return getConnection(node.getUri().getHost(), node.getUri().getPort());
        }

        return null;
    }

    @Override
    public RedisClusterAsyncConnection<K, V> getConnection(String host, int port) {
        RedisAsyncConnectionImpl<K, V> connection = getWriter().getClusterConnectionProvider().getConnection(
                ClusterConnectionProvider.Intent.WRITE, host, port);

        return connection;
    }

    public void setPartitions(Partitions partitions) {
        getWriter().getClusterConnectionProvider().setPartitions(partitions);
        this.partitions = partitions;
    }

    @Override
    public ReadFrom getReadFrom() {
        return getWriter().getReadFrom();
    }

    @Override
    public void setReadFrom(ReadFrom readFrom) {
        checkArgument(readFrom != null, "readFrom must not be null");
        getWriter().setReadFrom(readFrom);
    }

}
