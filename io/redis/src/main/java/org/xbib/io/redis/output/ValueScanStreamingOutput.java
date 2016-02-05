package org.xbib.io.redis.output;

import org.xbib.io.redis.StreamScanCursor;
import org.xbib.io.redis.codec.RedisCodec;

import java.nio.ByteBuffer;

/**
 * Streaming API for multiple Values. You can implement this interface in order to receive a call to {@code onValue} on every
 * key.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class ValueScanStreamingOutput<K, V> extends ScanOutput<K, V, StreamScanCursor> {

    private final ValueStreamingChannel<V> channel;

    public ValueScanStreamingOutput(RedisCodec<K, V> codec, ValueStreamingChannel<V> channel) {
        super(codec, new StreamScanCursor());
        this.channel = channel;
    }

    @Override
    protected void setOutput(ByteBuffer bytes) {
        channel.onValue(bytes == null ? null : codec.decodeValue(bytes));
        output.setCount(output.getCount() + 1);
    }

}
