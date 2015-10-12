package org.xbib.io.redis.output;

/**
 * Streaming API for multiple Keys. You can implement this interface in order to receive a call to {@code onKey} on every key.
 * Key uniqueness is not guaranteed.
 *
 * @param <K> Key type.
 */
@FunctionalInterface
public interface KeyStreamingChannel<K> {
    /**
     * Called on every incoming key.
     *
     * @param key the key
     */
    void onKey(K key);
}
