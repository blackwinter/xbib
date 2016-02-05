package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;

/**
 * Streaming-Output of Keys. Returns the count of all keys (including null).
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class KeyStreamingOutput<K, V> extends CommandOutput<K, V, Long> {
    private final KeyStreamingChannel<K> channel;

    public KeyStreamingOutput(RedisCodec<K, V> codec, KeyStreamingChannel<K> channel) {
        super(codec, Long.valueOf(0));
        this.channel = channel;
    }

    @Override
    public void set(ByteBuffer bytes) {

        channel.onKey(bytes == null ? null : codec.decodeKey(bytes));
        output = output.longValue() + 1;
    }

}
