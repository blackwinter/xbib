package org.xbib.io.redis.event.metrics;

import org.xbib.io.redis.event.Event;
import org.xbib.io.redis.metrics.CommandLatencyId;
import org.xbib.io.redis.metrics.CommandMetrics;

import java.util.Map;

/**
 * Event that transports command latency metrics. This event carries latencies for multiple commands and connections.
 */
public class CommandLatencyEvent implements Event {

    private Map<CommandLatencyId, CommandMetrics> latencies;

    public CommandLatencyEvent(Map<CommandLatencyId, CommandMetrics> latencies) {
        this.latencies = latencies;
    }

    /**
     * Returns the latencies mapped between {@link CommandLatencyId connection/command} and the {@link CommandMetrics metrics}.
     *
     * @return the latency map.
     */
    public Map<CommandLatencyId, CommandMetrics> getLatencies() {
        return latencies;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(latencies);
        return sb.toString();
    }
}
