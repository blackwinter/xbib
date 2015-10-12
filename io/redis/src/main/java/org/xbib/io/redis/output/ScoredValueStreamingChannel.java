package org.xbib.io.redis.output;

import org.xbib.io.redis.ScoredValue;

/**
 * Streaming API for multiple Keys. You can implement this interface in order to receive a call to {@code onValue} on every
 * value.
 *
 * @param <V> Value type.
 */
@FunctionalInterface
public interface ScoredValueStreamingChannel<V> {
    /**
     * Called on every incoming ScoredValue.
     *
     * @param value the scored value
     */
    void onValue(ScoredValue<V> value);
}
