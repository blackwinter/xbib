package org.xbib.io.redis.models.role;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a master instance.
 */
public class RedisMasterInstance implements RedisInstance {

    private long replicationOffset;
    private List<ReplicationPartner> slaves = Collections.emptyList();

    public RedisMasterInstance() {
    }

    /**
     * Constructs a {@link RedisMasterInstance}
     *
     * @param replicationOffset the replication offset
     * @param slaves            list of slaves, must not be {@literal null} but may be empty
     */
    public RedisMasterInstance(long replicationOffset, List<ReplicationPartner> slaves) {
        checkArgument(slaves != null, "slaves must not be null");
        this.replicationOffset = replicationOffset;
        this.slaves = slaves;
    }

    /**
     * @return always {@link Role#MASTER}
     */
    @Override
    public Role getRole() {
        return Role.MASTER;
    }

    public long getReplicationOffset() {
        return replicationOffset;
    }

    public void setReplicationOffset(long replicationOffset) {
        this.replicationOffset = replicationOffset;
    }

    public List<ReplicationPartner> getSlaves() {
        return slaves;
    }

    public void setSlaves(List<ReplicationPartner> slaves) {

        checkArgument(slaves != null, "slaves must not be null");
        this.slaves = slaves;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [replicationOffset=").append(replicationOffset);
        sb.append(", slaves=").append(slaves);
        sb.append(']');
        return sb.toString();
    }
}
