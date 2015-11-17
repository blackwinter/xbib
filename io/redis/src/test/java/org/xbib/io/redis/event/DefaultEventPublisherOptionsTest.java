package org.xbib.io.redis.event;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultEventPublisherOptionsTest {

    @Test
    public void testDefault() throws Exception {

        DefaultEventPublisherOptions sut = DefaultEventPublisherOptions.create();

        assertThat(sut.eventEmitInterval()).isEqualTo(10);
        assertThat(sut.eventEmitIntervalUnit()).isEqualTo(TimeUnit.MINUTES);
    }

    @Test
    public void testDisabled() throws Exception {

        DefaultEventPublisherOptions sut = DefaultEventPublisherOptions.disabled();

        assertThat(sut.eventEmitInterval()).isEqualTo(0);
        assertThat(sut.eventEmitIntervalUnit()).isEqualTo(TimeUnit.SECONDS);
    }

    @Test
    public void testBuilder() throws Exception {

        DefaultEventPublisherOptions sut = new DefaultEventPublisherOptions.Builder().eventEmitInterval(1, TimeUnit.SECONDS)
                .build();

        assertThat(sut.eventEmitInterval()).isEqualTo(1);
        assertThat(sut.eventEmitIntervalUnit()).isEqualTo(TimeUnit.SECONDS);
    }
}
