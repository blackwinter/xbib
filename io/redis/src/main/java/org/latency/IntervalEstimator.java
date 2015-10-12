
package org.latency;

/**
 * IntervalEstimator is used to estimate intervals, potentially based on observed intervals recorded in it.
 */
public abstract class IntervalEstimator {

    /**
     * Record an interval
     *
     * @param when the end time (in nanoTime units) at which the interval was observed.
     */
    abstract public void recordInterval(long when);

    /**
     * Provides the estimated interval
     *
     * @param when the time (preferably now) at which the estimated interval is requested.
     * @return estimated interval
     */
    abstract public long getEstimatedInterval(long when);
}
