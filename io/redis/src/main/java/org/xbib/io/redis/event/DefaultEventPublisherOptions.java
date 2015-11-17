package org.xbib.io.redis.event;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

public class DefaultEventPublisherOptions implements EventPublisherOptions {

    public static final long DEFAULT_EMIT_INTERVAL = 10;
    public static final TimeUnit DEFAULT_EMIT_INTERVAL_UNIT = TimeUnit.MINUTES;

    private static final DefaultEventPublisherOptions DISABLED = new Builder().eventEmitInterval(0, TimeUnit.SECONDS).build();

    private final long eventEmitInterval;
    private final TimeUnit eventEmitIntervalUnit;

    protected DefaultEventPublisherOptions(Builder builder) {
        this.eventEmitInterval = builder.eventEmitInterval;
        this.eventEmitIntervalUnit = builder.eventEmitIntervalUnit;
    }

    /**
     * Create a new {@link DefaultEventPublisherOptions} using default settings.
     *
     * @return a new instance of a default {@link DefaultEventPublisherOptions} instance
     */
    public static DefaultEventPublisherOptions create() {
        return new Builder().build();
    }

    /**
     * Create a disabled {@link DefaultEventPublisherOptions} using default settings.
     *
     * @return a new instance of a default {@link DefaultEventPublisherOptions} instance with disabled event emission
     */
    public static DefaultEventPublisherOptions disabled() {
        return DISABLED;
    }

    @Override
    public long eventEmitInterval() {
        return eventEmitInterval;
    }

    @Override
    public TimeUnit eventEmitIntervalUnit() {
        return eventEmitIntervalUnit;
    }

    /**
     * Builder for {@link DefaultEventPublisherOptions}.
     */
    public static class Builder {

        private long eventEmitInterval = DEFAULT_EMIT_INTERVAL;
        private TimeUnit eventEmitIntervalUnit = DEFAULT_EMIT_INTERVAL_UNIT;

        public Builder() {
        }

        /**
         * Sets the emit interval and the interval unit. Event emission will be disabled if the {@code eventEmitInterval} is set
         * to 0}. Defaults to 10} {@link TimeUnit#MINUTES}. See {@link DefaultEventPublisherOptions#DEFAULT_EMIT_INTERVAL}
         * {@link DefaultEventPublisherOptions#DEFAULT_EMIT_INTERVAL_UNIT}.
         *
         * @param eventEmitInterval     the event interval, must be greater or equal to 0}
         * @param eventEmitIntervalUnit the {@link TimeUnit} for the interval, must not be null
         * @return this
         */
        public Builder eventEmitInterval(long eventEmitInterval, TimeUnit eventEmitIntervalUnit) {
            checkArgument(eventEmitInterval >= 0, "eventEmitInterval must be greater or equal to 0");
            checkArgument(eventEmitIntervalUnit != null, "eventEmitIntervalUnit must not be null");

            this.eventEmitInterval = eventEmitInterval;
            this.eventEmitIntervalUnit = eventEmitIntervalUnit;
            return this;
        }

        /**
         * @return a new instance of {@link DefaultEventPublisherOptions}.
         */
        public DefaultEventPublisherOptions build() {
            return new DefaultEventPublisherOptions(this);
        }
    }
}
