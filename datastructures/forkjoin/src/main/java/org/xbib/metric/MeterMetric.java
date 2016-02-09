
package org.xbib.metric;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * A meter metric which measures mean throughput and one-, five-, and
 * fifteen-minute exponentially-weighted moving average throughputs.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">EMA</a>
 */
public class MeterMetric implements Metric {

    private final ExpWeightedMovingAverage m1Rate = ExpWeightedMovingAverage.oneMinuteEWMA();

    private final ExpWeightedMovingAverage m5Rate = ExpWeightedMovingAverage.fiveMinuteEWMA();

    private final ExpWeightedMovingAverage m15Rate = ExpWeightedMovingAverage.fifteenMinuteEWMA();

    private final LongAdder count;

    private final Instant startTime;

    private final TimeUnit rateUnit;

    private final static ScheduledExecutorService service = Executors.newScheduledThreadPool(3);

    private final ScheduledFuture<?> future;

    private Instant stopTime;

    public MeterMetric(long intervalSeconds, TimeUnit rateUnit) {
        this.rateUnit = rateUnit;
        this.count = new LongAdder();
        this.startTime = Instant.now();
        this.future = service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public TimeUnit rateUnit() {
        return rateUnit;
    }

    /**
     * Updates the moving averages.
     */
    void tick() {
        m1Rate.tick();
        m5Rate.tick();
        m15Rate.tick();
    }

    /**
     * Mark the occurrence of an event.
     */
    public void mark() {
        mark(1);
    }

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    public void mark(long n) {
        count.add(n);
        m1Rate.update(n);
        m5Rate.update(n);
        m15Rate.update(n);
    }

    public long count() {
        return count.sum();
    }

    public Instant started() {
        return startTime;
    }

    public Instant stopped() {
        return stopTime;
    }

    public long elapsed() {
        return ChronoUnit.SECONDS.between(startTime, Instant.now());
    }

    public double fifteenMinuteRate() {
        return m15Rate.rate(rateUnit);
    }

    public double fiveMinuteRate() {
        return m5Rate.rate(rateUnit);
    }

    public double meanRate() {
        long count = count();
        if (count == 0) {
            return 0.0;
        } else {
            return convertNsRate(count / (double) elapsed());
        }
    }

    public double oneMinuteRate() {
        return m1Rate.rate(rateUnit);
    }

    public void stop() {
        this.stopTime = Instant.now();
        future.cancel(false);
    }

    private double convertNsRate(double ratePerNs) {
        return ratePerNs * (double) rateUnit.toNanos(1);
    }

}
