package org.xbib.io.redis.models.role;

import com.google.common.net.HostAndPort;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Replication partner providing the host and the replication offset.
 */
public class ReplicationPartner {
    private HostAndPort host;
    private long replicationOffset;

    public ReplicationPartner() {

    }

    /**
     * Constructs a replication partner.
     *
     * @param host              host information, must not be {@literal null}
     * @param replicationOffset the replication offset
     */
    public ReplicationPartner(HostAndPort host, long replicationOffset) {
        checkArgument(host != null, "host must not be null");
        this.host = host;
        this.replicationOffset = replicationOffset;
    }

    /**
     * @return host with port of the replication partner.
     */
    public HostAndPort getHost() {
        return host;
    }

    public void setHost(HostAndPort host) {
        checkArgument(host != null, "host must not be null");
        this.host = host;
    }

    /**
     * @return the replication offset.
     */
    public long getReplicationOffset() {
        return replicationOffset;
    }

    public void setReplicationOffset(long replicationOffset) {
        this.replicationOffset = replicationOffset;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [host=").append(host);
        sb.append(", replicationOffset=").append(replicationOffset);
        sb.append(']');
        return sb.toString();
    }
}
