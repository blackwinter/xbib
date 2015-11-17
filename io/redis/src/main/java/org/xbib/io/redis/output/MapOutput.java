package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Map} of keys and values output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class MapOutput<K, V> extends CommandOutput<K, V, Map<K, V>> {
    private K key;

    public MapOutput(RedisCodec<K, V> codec) {
        super(codec, new HashMap<K, V>());
    }

    @Override
    public void set(ByteBuffer bytes) {
        if (key == null) {
            key = codec.decodeKey(bytes);
            return;
        }

        V value = (bytes == null) ? null : codec.decodeValue(bytes);
        output.put(key, value);
        key = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(long integer) {
        if (key == null) {
            key = (K) Long.valueOf(integer);
            return;
        }

        V value = (V) Long.valueOf(integer);
        output.put(key, value);
        key = null;
    }
}
