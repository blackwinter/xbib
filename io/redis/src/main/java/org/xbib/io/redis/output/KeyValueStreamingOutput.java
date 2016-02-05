package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;

/**
 * Streaming-Output of Key Value Pairs. Returns the count of all Key-Value pairs (including null).
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class KeyValueStreamingOutput<K, V> extends CommandOutput<K, V, Long> {
    private K key;
    private KeyValueStreamingChannel<K, V> channel;

    public KeyValueStreamingOutput(RedisCodec<K, V> codec, KeyValueStreamingChannel<K, V> channel) {
        super(codec, Long.valueOf(0));
        this.channel = channel;
    }

    @Override
    public void set(ByteBuffer bytes) {
        if (key == null) {
            key = codec.decodeKey(bytes);
            return;
        }

        V value = (bytes == null) ? null : codec.decodeValue(bytes);
        channel.onKeyValue(key, value);
        output = output.longValue() + 1;
        key = null;
    }
}
