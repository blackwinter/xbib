package org.xbib.metrics;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.Mockito.*;

public class SamplerTest {
    private final Reservoir reservoir = mock(Reservoir.class);
    private final Clock clock = new Clock() {
        // a mock clock that increments its ticker by 50msec per call
        private long val = 0;

        @Override
        public long getTick() {
            return val += 50000000;
        }
    };
    private final Sampler sampler = new Sampler(reservoir, clock);

    @Test
    public void hasRates() throws Exception {
        assertThat(sampler.getCount())
                .isZero();

        assertThat(sampler.getMeanRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(sampler.getOneMinuteRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(sampler.getFiveMinuteRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(sampler.getFifteenMinuteRate())
                .isEqualTo(0.0, offset(0.001));
    }

    @Test
    public void updatesTheCountOnUpdates() throws Exception {
        assertThat(sampler.getCount())
                .isZero();

        sampler.update(1, TimeUnit.SECONDS);

        assertThat(sampler.getCount())
                .isEqualTo(1);
    }

    @Test
    public void timesCallableInstances() throws Exception {
        final String value = sampler.time(() -> "one");

        assertThat(sampler.getCount())
                .isEqualTo(1);

        assertThat(value)
                .isEqualTo("one");

        verify(reservoir).update(50000000);
    }

    @Test
    public void timesRunnableInstances() throws Exception {
        final boolean[] called = {false};
        sampler.time((Runnable) () -> called[0] = true);

        assertThat(sampler.getCount())
                .isEqualTo(1);

        assertThat(called[0])
                .isTrue();

        verify(reservoir).update(50000000);
    }

    @Test
    public void timesContexts() throws Exception {
        sampler.time().stop();

        assertThat(sampler.getCount())
                .isEqualTo(1);

        verify(reservoir).update(50000000);
    }

    @Test
    public void returnsTheSnapshotFromTheReservoir() throws Exception {
        final Snapshot snapshot = mock(Snapshot.class);
        when(reservoir.getSnapshot()).thenReturn(snapshot);

        assertThat(sampler.getSnapshot())
                .isEqualTo(snapshot);
    }

    @Test
    public void ignoresNegativeValues() throws Exception {
        sampler.update(-1, TimeUnit.SECONDS);

        assertThat(sampler.getCount())
                .isZero();

        verifyZeroInteractions(reservoir);
    }

    @Test
    public void tryWithResourcesWork() {
        assertThat(sampler.getCount()).isZero();

        int dummy = 0;

        try (Sampler.Context context = sampler.time()) {
            dummy += 1;
        }

        assertThat(sampler.getCount())
                .isEqualTo(1);

        verify(reservoir).update(50000000);
    }

}
