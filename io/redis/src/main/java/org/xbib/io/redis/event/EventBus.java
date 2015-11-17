package org.xbib.io.redis.event;

import rx.Observable;

/**
 * Interface for an EventBus. Events can be published over the bus that are delivered to the subscribers.
 */
public interface EventBus {

    /**
     * Subscribe to the event bus and {@link Event}s. The {@link Observable} drops events on backpressure to avoid contention.
     *
     * @return the observable to obtain events.
     */
    Observable<Event> get();

    /**
     * Publish a {@link Event} to the bus.
     *
     * @param event the event to publish
     */
    void publish(Event event);
}
