package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;

/**
 * Streaming-Output of Values. Returns the count of all values (including null).
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class ValueStreamingOutput<K, V> extends CommandOutput<K, V, Long> {
    private final ValueStreamingChannel<V> channel;

    public ValueStreamingOutput(RedisCodec<K, V> codec, ValueStreamingChannel<V> channel) {
        super(codec, 0L);
        this.channel = channel;
    }

    @Override
    public void set(ByteBuffer bytes) {

        channel.onValue(bytes == null ? null : codec.decodeValue(bytes));
        output = output + 1;
    }

}
