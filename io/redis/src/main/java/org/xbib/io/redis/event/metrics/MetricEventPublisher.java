package org.xbib.io.redis.event.metrics;

import org.xbib.io.redis.event.Event;

/**
 * Event publisher which publishes metrics by the use of {@link Event events}.
 */
public interface MetricEventPublisher {

    /**
     * Emit immediately a metrics event.
     */
    void emitMetricsEvent();

    /**
     * Returns {@literal true} if the metric collector is enabled.
     *
     * @return {@literal true} if the metric collector is enabled
     */
    boolean isEnabled();

    /**
     * Shut down the event publisher.
     */
    void shutdown();
}
