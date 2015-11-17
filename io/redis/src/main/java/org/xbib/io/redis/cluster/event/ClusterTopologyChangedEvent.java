package org.xbib.io.redis.cluster.event;

import org.xbib.io.redis.cluster.models.partitions.RedisClusterNode;
import org.xbib.io.redis.event.Event;

import java.util.List;

/**
 * Signals a discovered cluster topology change. The event carries the view {@link #before()} and {@link #after} the change.
 *
 */
public class ClusterTopologyChangedEvent implements Event {
    private final List<RedisClusterNode> before;
    private final List<RedisClusterNode> after;

    /**
     * Creates a new {@link ClusterTopologyChangedEvent}.
     *
     * @param before the cluster topology view before the topology changed, must not be {@literal null}
     * @param after  the cluster topology view after the topology changed, must not be {@literal null}
     */
    public ClusterTopologyChangedEvent(List<RedisClusterNode> before, List<RedisClusterNode> after) {
        this.before = before;
        this.after = after;
    }

    /**
     * Returns the cluster topology view before the topology changed.
     *
     * @return the cluster topology view before the topology changed.
     */
    public List<RedisClusterNode> before() {
        return before;
    }

    /**
     * Returns the cluster topology view after the topology changed.
     *
     * @return the cluster topology view after the topology changed.
     */
    public List<RedisClusterNode> after() {
        return after;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [before=" + before.size() + ", after=" + after.size() + ']';
    }
}
