package org.xbib.io.redis.output;

import org.xbib.io.redis.ScoredValue;
import org.xbib.io.redis.StreamScanCursor;
import org.xbib.io.redis.codec.RedisCodec;

import java.nio.ByteBuffer;

/**
 * Streaming-Output of of values and their associated scores. Returns the count of all values (including null).
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class ScoredValueScanStreamingOutput<K, V> extends ScanOutput<K, V, StreamScanCursor> {

    private final ScoredValueStreamingChannel<V> channel;
    private V value;

    public ScoredValueScanStreamingOutput(RedisCodec<K, V> codec, ScoredValueStreamingChannel<V> channel) {
        super(codec, new StreamScanCursor());
        this.channel = channel;
    }

    @Override
    protected void setOutput(ByteBuffer bytes) {
        if (value == null) {
            value = codec.decodeValue(bytes);
            return;
        }

        double score = Double.parseDouble(decodeAscii(bytes));
        channel.onValue(new ScoredValue<V>(score, value));
        value = null;
        output.setCount(output.getCount() + 1);
    }

}
