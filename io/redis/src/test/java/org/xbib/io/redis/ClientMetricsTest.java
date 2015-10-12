package org.xbib.io.redis;

import org.junit.Test;

import rx.Subscription;
import rx.observers.TestSubscriber;

import com.google.code.tempusfugit.temporal.WaitFor;
import org.xbib.io.redis.event.metrics.CommandLatencyEvent;
import org.xbib.io.redis.event.EventBus;
import org.xbib.io.redis.event.metrics.MetricEventPublisher;

import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ClientMetricsTest extends AbstractCommandTest {

    @Test
    public void testMetricsEvent() throws Exception {
        EventBus eventBus = client.getResources().eventBus();
        MetricEventPublisher publisher = client.getResources().metricEventPublisher();
        publisher.emitMetricsEvent();
        final TestSubscriber<CommandLatencyEvent> subscriber = new TestSubscriber<>();
        Subscription subscription = eventBus.get()
                .filter(redisEvent -> redisEvent instanceof CommandLatencyEvent)
                .cast(CommandLatencyEvent.class).subscribe(subscriber);
        generateTestData();
        publisher.emitMetricsEvent();
        WaitFor.waitOrTimeout(() -> !subscriber.getOnNextEvents().isEmpty(), timeout(seconds(5)));
        subscription.unsubscribe();
        subscriber.assertValueCount(1);
        CommandLatencyEvent event = subscriber.getOnNextEvents().get(0);
        assertThat(event.getLatencies().keySet(), hasSize(2));
        assertThat(event.toString().contains("local:any ->"), equalTo(true));
        assertThat(event.toString().contains("commandType=GET"), equalTo(true));
    }

    private void generateTestData() {
        redis.set(key, value);
        redis.set(key, value);
        redis.set(key, value);
        redis.set(key, value);
        redis.set(key, value);
        redis.set(key, value);

        redis.get(key);
        redis.get(key);
        redis.get(key);
        redis.get(key);
        redis.get(key);
    }
}
