package org.xbib.io.redis.output;

import org.xbib.io.redis.ScoredValue;
import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;

/**
 * Streaming-Output of of values and their associated scores. Returns the count of all values (including null).
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class ScoredValueStreamingOutput<K, V> extends CommandOutput<K, V, Long> {
    private final ScoredValueStreamingChannel<V> channel;
    private V value;

    public ScoredValueStreamingOutput(RedisCodec<K, V> codec, ScoredValueStreamingChannel<V> channel) {
        super(codec, (long) 0);
        this.channel = channel;
    }

    @Override
    public void set(ByteBuffer bytes) {

        if (value == null) {
            value = codec.decodeValue(bytes);
            return;
        }

        double score = Double.parseDouble(decodeAscii(bytes));
        channel.onValue(new ScoredValue<V>(score, value));
        value = null;
        output = output + 1;
    }

}
