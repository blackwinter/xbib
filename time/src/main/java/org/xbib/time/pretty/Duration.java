package org.xbib.time.pretty;

/**
 * Represents a quantity of any given {@link TimeUnit}
 */
public class Duration {
    private long quantity;
    private long delta;
    private TimeUnit unit;

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(final long quantity) {
        this.quantity = quantity;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(final TimeUnit unit) {
        this.unit = unit;
    }

    public long getDelta() {
        return delta;
    }

    public void setDelta(final long delta) {
        this.delta = delta;
    }

    public boolean isInPast() {
        return getQuantity() < 0;
    }

    public boolean isInFuture() {
        return !isInPast();
    }

    public long getQuantityRounded(int tolerance) {
        long quantity = Math.abs(getQuantity());

        if (getDelta() != 0) {
            double threshold = Math
                    .abs(((double) getDelta() / (double) getUnit().getMillisPerUnit()) * 100);
            if (threshold > tolerance) {
                quantity = quantity + 1;
            }
        }
        return quantity;
    }
}
