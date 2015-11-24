package org.xbib.time.impl;

import org.xbib.time.Duration;
import org.xbib.time.TimeUnit;

public class DurationImpl implements Duration {
    private long quantity;
    private long delta;
    private TimeUnit unit;

    @Override
    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(final long quantity) {
        this.quantity = quantity;
    }

    @Override
    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(final TimeUnit unit) {
        this.unit = unit;
    }

    @Override
    public long getDelta() {
        return delta;
    }

    public void setDelta(final long delta) {
        this.delta = delta;
    }

    @Override
    public boolean isInPast() {
        return getQuantity() < 0;
    }

    @Override
    public boolean isInFuture() {
        return !isInPast();
    }

    @Override
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
