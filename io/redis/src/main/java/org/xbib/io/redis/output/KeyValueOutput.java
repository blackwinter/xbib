package org.xbib.io.redis.output;

import org.xbib.io.redis.KeyValue;
import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;

/**
 * Key-value pair output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class KeyValueOutput<K, V> extends CommandOutput<K, V, KeyValue<K, V>> {
    private K key;

    public KeyValueOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(ByteBuffer bytes) {
        if (bytes != null) {
            if (key == null) {
                key = codec.decodeKey(bytes);
            } else {
                V value = codec.decodeValue(bytes);
                output = new KeyValue<K, V>(key, value);
            }
        }
    }
}
