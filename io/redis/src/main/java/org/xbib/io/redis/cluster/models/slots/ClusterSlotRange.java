package org.xbib.io.redis.cluster.models.slots;

import com.google.common.net.HostAndPort;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a range of slots together with its master and slaves.
 *
 */
public class ClusterSlotRange {
    private int from;
    private int to;
    private HostAndPort master;
    private List<HostAndPort> slaves = Collections.emptyList();

    public ClusterSlotRange() {

    }

    /**
     * Constructs a {@link ClusterSlotRange}
     *
     * @param from   from slot
     * @param to     to slot
     * @param master master for the slots, may be {@literal null}
     * @param slaves list of slaves must not be {@literal null} but may be empty
     */
    public ClusterSlotRange(int from, int to, HostAndPort master, List<HostAndPort> slaves) {

        checkArgument(master != null, "master must not be null");
        checkArgument(slaves != null, "slaves must not be null");

        this.from = from;
        this.to = to;
        this.master = master;
        this.slaves = slaves;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public HostAndPort getMaster() {
        return master;
    }

    public void setMaster(HostAndPort master) {
        checkArgument(master != null, "master must not be null");
        this.master = master;
    }

    public List<HostAndPort> getSlaves() {
        return slaves;
    }

    public void setSlaves(List<HostAndPort> slaves) {

        checkArgument(slaves != null, "slaves must not be null");
        this.slaves = slaves;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [from=" + from + ", to=" + to + ", master=" + master + ", slaves=" + slaves + ']';
    }
}
