package org.xbib.time;

/**
 * Represents a quantity of any given {@link TimeUnit}
 */
public interface Duration {
    /**
     * Return the calculated quantity of {@link TimeUnit} instances.
     */
    long getQuantity();

    /**
     * Return the calculated quantity of {@link TimeUnit} instances, rounded up if {@link #getDelta()} is greater than
     * or
     * equal to the given tolerance.
     */
    long getQuantityRounded(int tolerance);

    /**
     * Return the {@link TimeUnit} represented by this {@link Duration}
     */
    TimeUnit getUnit();

    /**
     * Return the number of milliseconds left over when calculating the number of {@link TimeUnit}'s that fit into the
     * given time range.
     */
    long getDelta();

    /**
     * Return true if this {@link Duration} represents a number of {@link TimeUnit} instances in the past.
     */
    boolean isInPast();

    /**
     * Return true if this {@link Duration} represents a number of {@link TimeUnit} instances in the future.
     */
    boolean isInFuture();
}
