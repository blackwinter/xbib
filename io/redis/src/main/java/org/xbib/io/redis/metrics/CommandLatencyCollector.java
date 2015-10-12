package org.xbib.io.redis.metrics;

import org.xbib.io.redis.protocol.ProtocolKeyword;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * {@link MetricCollector} for command latencies. Command latencies are collected per connection (identified by local/remote
 * tuples of {@link SocketAddress}es) and {@link ProtocolKeyword command type}. Two command latencies are available:
 * <ul>
 * <li>Latency between command send and first response (first response received)</li>
 * <li>Latency between command send and command completion (complete response received)</li>
 * </ul>
 */
public interface CommandLatencyCollector extends MetricCollector<Map<CommandLatencyId, CommandMetrics>> {

    /**
     * Record the command latency per {@code connectionPoint} and {@code commandType}.
     *
     * @param local                the local address
     * @param remote               the remote address
     * @param commandType          the command type
     * @param firstResponseLatency latency value in {@link TimeUnit#NANOSECONDS} from send to the first response
     * @param completionLatency    latency value in {@link TimeUnit#NANOSECONDS} from send to the command completion
     */
    void recordCommandLatency(SocketAddress local, SocketAddress remote, ProtocolKeyword commandType,
                              long firstResponseLatency, long completionLatency);

}
