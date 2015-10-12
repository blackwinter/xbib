package org.xbib.io.redis.models.role;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Redis sentinel instance.
 */
public class RedisSentinelInstance implements RedisInstance {
    private List<String> monitoredMasters = Collections.emptyList();

    public RedisSentinelInstance() {
    }

    /**
     * Constructs a {@link RedisSentinelInstance}
     *
     * @param monitoredMasters list of monitored masters, must not be {@literal null} but may be empty
     */
    public RedisSentinelInstance(List<String> monitoredMasters) {
        checkArgument(monitoredMasters != null, "list of monitoredMasters must not be null");
        this.monitoredMasters = monitoredMasters;
    }

    /**
     * @return always {@link Role#SENTINEL}
     */
    @Override
    public Role getRole() {
        return Role.SENTINEL;
    }

    /**
     * @return List of monitored master names.
     */
    public List<String> getMonitoredMasters() {
        return monitoredMasters;
    }

    public void setMonitoredMasters(List<String> monitoredMasters) {
        checkArgument(monitoredMasters != null, "list of monitoredMasters must not be null");
        this.monitoredMasters = monitoredMasters;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [monitoredMasters=").append(monitoredMasters);
        sb.append(']');
        return sb.toString();
    }
}
