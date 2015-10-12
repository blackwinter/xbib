package org.xbib.io.redis.event;

import rx.Observable;
import rx.Scheduler;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Default implementation for an {@link EventBus}. Events are published using a {@link Scheduler}.
 */
public class DefaultEventBus implements EventBus {

    private final Subject<Event, Event> bus;
    private final Scheduler scheduler;

    public DefaultEventBus(Scheduler scheduler) {
        this.bus = PublishSubject.<Event>create().toSerialized();
        this.scheduler = scheduler;
    }

    @Override
    public Observable<Event> get() {
        return bus.onBackpressureDrop().observeOn(scheduler);
    }

    @Override
    public void publish(Event event) {
        if (bus.hasObservers()) {
            bus.onNext(event);
        }
    }
}
