package org.xbib.io.redis.metrics;

/**
 * Generic metrics collector interface. A metrics collector collects metrics and emits metric events.
 *
 * @param <T> data type of the metrics
 */
public interface MetricCollector<T> {

    /**
     * Shut down the metrics collector.
     */
    void shutdown();

    /**
     * Returns the collected/aggregated metrics.
     *
     * @return the the collected/aggregated metrics
     */
    T retrieveMetrics();

    /**
     * Returns {@literal true} if the metric collector is enabled.
     *
     * @return {@literal true} if the metric collector is enabled
     */
    boolean isEnabled();
}
