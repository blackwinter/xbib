package org.xbib.io.redis.output;

/**
 * Streaming API for multiple Keys. You can implement this interface in order to receive a call to {@code onValue} on every
 * value.
 *
 * @param <V> Value type.
 */
@FunctionalInterface
public interface ValueStreamingChannel<V> {
    /**
     * Called on every incoming value.
     *
     * @param value the value
     */
    void onValue(V value);
}
