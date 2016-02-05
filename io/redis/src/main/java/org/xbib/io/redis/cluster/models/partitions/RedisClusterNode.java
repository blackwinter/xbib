package org.xbib.io.redis.cluster.models.partitions;

import org.xbib.io.redis.RedisURI;
import org.xbib.io.redis.models.role.RedisNodeDescription;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Representation of a Redis Cluster node. A {@link RedisClusterNode} is identified by its {@code nodeId}. A
 * {@link RedisClusterNode} can be a {@link #getRole() responsible master} for zero to
 * {@link org.xbib.io.redis.cluster.SlotHash#SLOT_COUNT 16384} slots, a slave of one {@link #getSlaveOf() master} of carry
 * different {@link NodeFlag flags}.
 */
public class RedisClusterNode implements RedisNodeDescription {
    private RedisURI uri;
    private String nodeId;

    private boolean connected;
    private String slaveOf;
    private long pingSentTimestamp;
    private long pongReceivedTimestamp;
    private long configEpoch;

    private List<Integer> slots;
    private Set<NodeFlag> flags;

    public RedisClusterNode() {

    }

    public RedisClusterNode(RedisURI uri, String nodeId, boolean connected, String slaveOf, long pingSentTimestamp,
                            long pongReceivedTimestamp, long configEpoch, List<Integer> slots, Set<NodeFlag> flags) {
        this.uri = uri;
        this.nodeId = nodeId;
        this.connected = connected;
        this.slaveOf = slaveOf;
        this.pingSentTimestamp = pingSentTimestamp;
        this.pongReceivedTimestamp = pongReceivedTimestamp;
        this.configEpoch = configEpoch;
        this.slots = slots;
        this.flags = flags;
    }

    public RedisURI getUri() {
        return uri;
    }

    /**
     * Sets thhe connection point details. Usually the host/ip/port where a particular Redis Cluster node server is running.
     *
     * @param uri the {@link RedisURI}, must not be {@literal null}
     */
    public void setUri(RedisURI uri) {
        checkArgument(uri != null, "uri must not be null");
        this.uri = uri;
    }

    public String getNodeId() {
        return nodeId;
    }

    /**
     * Sets {@code nodeId}.
     *
     * @param nodeId the {@code nodeId}
     */
    public void setNodeId(String nodeId) {
        checkArgument(nodeId != null, "nodeId must not be null");
        this.nodeId = nodeId;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Sets the {@code connected} flag. The {@code connected} flag describes whether the node which provided details about the
     * node is connected to the particular {@link RedisClusterNode}.
     *
     * @param connected the {@code connected} flag
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getSlaveOf() {
        return slaveOf;
    }

    /**
     * Sets the replication source.
     *
     * @param slaveOf the replication source, can be {@literal null}
     */
    public void setSlaveOf(String slaveOf) {
        this.slaveOf = slaveOf;
    }

    public long getPingSentTimestamp() {
        return pingSentTimestamp;
    }

    /**
     * Sets the last {@code pingSentTimestamp}.
     *
     * @param pingSentTimestamp the last {@code pingSentTimestamp}
     */
    public void setPingSentTimestamp(long pingSentTimestamp) {
        this.pingSentTimestamp = pingSentTimestamp;
    }

    public long getPongReceivedTimestamp() {
        return pongReceivedTimestamp;
    }

    /**
     * Sets the last {@code pongReceivedTimestamp}.
     *
     * @param pongReceivedTimestamp the last {@code pongReceivedTimestamp}
     */
    public void setPongReceivedTimestamp(long pongReceivedTimestamp) {
        this.pongReceivedTimestamp = pongReceivedTimestamp;
    }

    public long getConfigEpoch() {
        return configEpoch;
    }

    /**
     * Sets the {@code configEpoch}.
     *
     * @param configEpoch the {@code configEpoch}
     */
    public void setConfigEpoch(long configEpoch) {
        this.configEpoch = configEpoch;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    /**
     * Sets the list of slots for which this {@link RedisClusterNode} is the
     * {@link NodeFlag#MASTER}. The list is empty if this node
     * is not a master or the node is not responsible for any slots at all.
     *
     * @param slots list of slots, must not be {@literal null} but may be empty
     */
    public void setSlots(List<Integer> slots) {
        checkArgument(slots != null, "slots must not be null");

        this.slots = slots;
    }

    public Set<NodeFlag> getFlags() {
        return flags;
    }

    /**
     * Set of {@link NodeFlag node flags}.
     *
     * @param flags the set of node flags.
     */
    public void setFlags(Set<NodeFlag> flags) {
        this.flags = flags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RedisClusterNode)) {
            return false;
        }

        RedisClusterNode that = (RedisClusterNode) o;

        if (nodeId != null ? !nodeId.equals(that.nodeId) : that.nodeId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 31 * (nodeId != null ? nodeId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [uri=").append(uri);
        sb.append(", nodeId='").append(nodeId).append('\'');
        sb.append(", connected=").append(connected);
        sb.append(", slaveOf='").append(slaveOf).append('\'');
        sb.append(", pingSentTimestamp=").append(pingSentTimestamp);
        sb.append(", pongReceivedTimestamp=").append(pongReceivedTimestamp);
        sb.append(", configEpoch=").append(configEpoch);
        sb.append(", flags=").append(flags);
        if (slots != null) {
            sb.append(", slot count=").append(slots.size());
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Returns the {@link Role} of the Redis Cluster node based on the
     * {@link #getFlags() flags}.
     *
     * @return the Redis Cluster node role
     */
    @Override
    public Role getRole() {
        return getFlags().contains(NodeFlag.MASTER) ? Role.MASTER : Role.SLAVE;
    }

    /**
     * Redis Cluster node flags.
     */
    public enum NodeFlag {
        NOFLAGS, MYSELF, SLAVE, MASTER, EVENTUAL_FAIL, FAIL, HANDSHAKE, NOADDR;
    }

}
