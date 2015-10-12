package org.xbib.io.redis.cluster;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.xbib.io.redis.RedisAsyncConnectionImpl;
import org.xbib.io.redis.RedisCommandInterruptedException;
import org.xbib.io.redis.RedisFuture;
import org.xbib.io.redis.RedisURI;
import org.xbib.io.redis.cluster.models.partitions.ClusterPartitionParser;
import org.xbib.io.redis.cluster.models.partitions.Partitions;
import org.xbib.io.redis.cluster.models.partitions.RedisClusterNode;
import org.xbib.io.redis.codec.Utf8StringCodec;
import org.xbib.io.redis.output.StatusOutput;
import org.xbib.io.redis.protocol.Command;
import org.xbib.io.redis.protocol.CommandArgs;
import org.xbib.io.redis.protocol.CommandKeyword;
import org.xbib.io.redis.protocol.CommandOutput;
import org.xbib.io.redis.protocol.CommandType;
import org.xbib.io.redis.protocol.ProtocolKeyword;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Utility to refresh the cluster topology view based on {@link Partitions}.
 */
class ClusterTopologyRefresh {

    private static final Utf8StringCodec CODEC = new Utf8StringCodec();
    private static final Logger logger = LogManager.getLogger(ClusterTopologyRefresh.class);

    private RedisClusterClient client;

    public ClusterTopologyRefresh(RedisClusterClient client) {
        this.client = client;
    }

    /**
     * Check if properties changed which are essential for cluster operations.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return {@literal true} if {@code MASTER} or {@code SLAVE} flags changed or the responsible slots changed.
     */
    public static boolean isChanged(Partitions o1, Partitions o2) {

        if (o1.size() != o2.size()) {
            return true;
        }

        for (RedisClusterNode base : o2) {
            if (!essentiallyEqualsTo(base, o1.getPartitionByNodeId(base.getNodeId()))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check for {@code MASTER} or {@code SLAVE} flags and whether the responsible slots changed.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return {@literal true} if {@code MASTER} or {@code SLAVE} flags changed or the responsible slots changed.
     */
    protected static boolean essentiallyEqualsTo(RedisClusterNode o1, RedisClusterNode o2) {

        if (o2 == null) {
            return false;
        }

        if (!sameFlags(o1, o2, RedisClusterNode.NodeFlag.MASTER)) {
            return false;
        }

        if (!sameFlags(o1, o2, RedisClusterNode.NodeFlag.SLAVE)) {
            return false;
        }

        if (!Sets.newHashSet(o1.getSlots()).equals(Sets.newHashSet(o2.getSlots()))) {
            return false;
        }

        return true;
    }

    private static boolean sameFlags(RedisClusterNode base, RedisClusterNode other, RedisClusterNode.NodeFlag flag) {
        if (base.getFlags().contains(flag)) {
            if (!other.getFlags().contains(flag)) {
                return false;
            }
        } else {
            if (other.getFlags().contains(flag)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Load partition views from a collection of {@link RedisURI}s and return the view per {@link RedisURI}. Partitions contain
     * an ordered list of {@link RedisClusterNode}s. The sort key is the latency. Nodes with lower latency come first.
     *
     * @param seed collection of {@link RedisURI}s
     * @return mapping between {@link RedisURI} and {@link Partitions}
     */
    public Map<RedisURI, Partitions> loadViews(Iterable<RedisURI> seed) {

        Map<RedisURI, RedisAsyncConnectionImpl<String, String>> connections = getConnections(seed);
        Map<RedisURI, TimedAsyncCommand<String, String, String>> rawViews = requestViews(connections);
        Map<RedisURI, Partitions> nodeSpecificViews = getNodeSpecificViews(rawViews);
        close(connections);

        return nodeSpecificViews;
    }

    protected Map<RedisURI, Partitions> getNodeSpecificViews(Map<RedisURI, TimedAsyncCommand<String, String, String>> rawViews) {
        Map<RedisURI, Partitions> nodeSpecificViews = Maps.newTreeMap(RedisUriComparator.INSTANCE);
        long timeout = client.getFirstUri().getUnit().toNanos(client.getFirstUri().getTimeout());
        long waitTime = 0;
        Map<String, Long> latencies = Maps.newHashMap();

        for (Map.Entry<RedisURI, TimedAsyncCommand<String, String, String>> entry : rawViews.entrySet()) {
            long timeoutLeft = timeout - waitTime;

            if (timeoutLeft <= 0) {
                break;
            }

            long startWait = System.nanoTime();
            RedisFuture<String> future = entry.getValue();

            try {

                if (!future.await(timeoutLeft, TimeUnit.NANOSECONDS)) {
                    break;
                }
                waitTime += System.nanoTime() - startWait;

                String raw = future.get();
                Partitions partitions = ClusterPartitionParser.parse(raw);

                for (RedisClusterNode partition : partitions) {
                    if (partition.getFlags().contains(RedisClusterNode.NodeFlag.MYSELF)) {
                        partition.setUri(entry.getKey());

                        // record latency for later partition ordering
                        latencies.put(partition.getNodeId(), entry.getValue().duration());
                    }
                }

                nodeSpecificViews.put(entry.getKey(), partitions);
            } catch (InterruptedException e) {
                Thread.interrupted();
                throw new RedisCommandInterruptedException(e);
            } catch (ExecutionException e) {
                logger.warn("Cannot retrieve partition view from " + entry.getKey(), e);
            }
        }

        LatencyComparator comparator = new LatencyComparator(latencies);

        for (Partitions redisClusterNodes : nodeSpecificViews.values()) {
            Collections.sort(redisClusterNodes.getPartitions(), comparator);
        }

        return nodeSpecificViews;
    }

    /*
     * Async request of views.
     */
    @SuppressWarnings("unchecked")
    private Map<RedisURI, TimedAsyncCommand<String, String, String>> requestViews(
            Map<RedisURI, RedisAsyncConnectionImpl<String, String>> connections) {
        Map<RedisURI, TimedAsyncCommand<String, String, String>> rawViews = Maps.newTreeMap(RedisUriComparator.INSTANCE);
        for (Map.Entry<RedisURI, RedisAsyncConnectionImpl<String, String>> entry : connections.entrySet()) {

            TimedAsyncCommand<String, String, String> timed = createClusterNodesCommand();

            entry.getValue().dispatch(timed);
            rawViews.put(entry.getKey(), timed);
        }
        return rawViews;
    }

    protected TimedAsyncCommand<String, String, String> createClusterNodesCommand() {
        CommandArgs<String, String> args = new CommandArgs<String, String>(CODEC).add(CommandKeyword.NODES);
        return new TimedAsyncCommand<String, String, String>(CommandType.CLUSTER, new StatusOutput<String, String>(CODEC), args);
    }

    protected void close(Map<RedisURI, RedisAsyncConnectionImpl<String, String>> connections) {
        for (RedisAsyncConnectionImpl<String, String> connection : connections.values()) {
            connection.close();
        }
    }

    /*
     * Open connections where an address can be resolved.
     */
    protected Map<RedisURI, RedisAsyncConnectionImpl<String, String>> getConnections(Iterable<RedisURI> seed) {
        Map<RedisURI, RedisAsyncConnectionImpl<String, String>> connections = Maps.newTreeMap(RedisUriComparator.INSTANCE);

        for (RedisURI redisURI : seed) {
            if (redisURI.getResolvedAddress() == null) {
                continue;
            }

            try {
                RedisAsyncConnectionImpl<String, String> connection = client.connectAsyncImpl(redisURI.getResolvedAddress());
                connections.put(redisURI, connection);
            } catch (RuntimeException e) {
                logger.warn("Cannot connect to " + redisURI, e);
            }
        }
        return connections;
    }

    /**
     * Resolve a {@link RedisURI} from a map of cluster views by {@link Partitions} as key
     *
     * @param map        the map
     * @param partitions the key
     * @return a {@link RedisURI} or null
     */
    protected RedisURI getViewedBy(Map<RedisURI, Partitions> map, Partitions partitions) {

        for (Map.Entry<RedisURI, Partitions> entry : map.entrySet()) {
            if (entry.getValue() == partitions) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * Compare {@link RedisURI} based on their host and port representation.
     */
    static class RedisUriComparator implements Comparator<RedisURI> {

        public final static RedisUriComparator INSTANCE = new RedisUriComparator();

        @Override
        public int compare(RedisURI o1, RedisURI o2) {
            String h1 = "";
            String h2 = "";

            if (o1 != null) {
                h1 = o1.getHost() + ":" + o1.getPort();
            }

            if (o2 != null) {
                h2 = o2.getHost() + ":" + o2.getPort();
            }

            return h1.compareToIgnoreCase(h2);
        }
    }

    /**
     * Timed command that records the time at which the command was encoded and completed.
     *
     * @param <K> Key type
     * @param <V> Value type
     * @param <T> Result type
     */
    static class TimedAsyncCommand<K, V, T> extends Command<K, V, T> {

        long encodedAtNs = -1;
        long completedAtNs = -1;

        public TimedAsyncCommand(ProtocolKeyword type, CommandOutput<K, V, T> output, CommandArgs<K, V> args) {
            super(type, output, args);
        }

        @Override
        public void encode(ByteBuf buf) {
            completedAtNs = -1;
            encodedAtNs = -1;

            super.encode(buf);
            encodedAtNs = System.nanoTime();
        }

        @Override
        public void complete() {
            completedAtNs = System.nanoTime();
            super.complete();
        }

        public long duration() {
            if (completedAtNs == -1 || encodedAtNs == -1) {
                return -1;
            }
            return completedAtNs - encodedAtNs;
        }
    }

    static class LatencyComparator implements Comparator<RedisClusterNode> {

        private final Map<String, Long> latencies;

        public LatencyComparator(Map<String, Long> latencies) {
            this.latencies = latencies;
        }

        @Override
        public int compare(RedisClusterNode o1, RedisClusterNode o2) {

            Long latency1 = latencies.get(o1.getNodeId());
            Long latency2 = latencies.get(o2.getNodeId());

            if (latency1 != null && latency2 != null) {
                return latency1.compareTo(latency2);
            }

            if (latency1 != null && latency2 == null) {
                return -1;
            }

            if (latency1 == null && latency2 != null) {
                return 1;
            }

            return 0;
        }

    }

}
