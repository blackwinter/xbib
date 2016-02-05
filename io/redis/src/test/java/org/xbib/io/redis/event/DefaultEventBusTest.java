package org.xbib.io.redis.event;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEventBusTest {

    @Mock
    private Event event;

    @Test
    public void publishToSubscriber() throws Exception {
        TestScheduler testScheduler = Schedulers.test();
        EventBus sut = new DefaultEventBus(testScheduler);

        TestSubscriber<Event> subscriber = new TestSubscriber<Event>();
        sut.get().subscribe(subscriber);

        sut.publish(event);

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        assertThat(subscriber.getOnNextEvents()).hasSize(1).contains(event);
    }
}
