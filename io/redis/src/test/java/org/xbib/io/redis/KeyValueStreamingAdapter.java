package org.xbib.io.redis;

import org.xbib.io.redis.output.KeyValueStreamingChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for a {@link KeyValueStreamingChannel}. Stores the output in a map.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class KeyValueStreamingAdapter<K, V> implements KeyValueStreamingChannel<K, V> {

    private final Map<K, V> map = new HashMap<K, V>();

    @Override
    public void onKeyValue(K key, V value) {
        map.put(key, value);
    }

    public Map<K, V> getMap() {
        return map;
    }
}
