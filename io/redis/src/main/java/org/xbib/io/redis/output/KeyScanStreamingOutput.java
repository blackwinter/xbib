package org.xbib.io.redis.output;

import org.xbib.io.redis.StreamScanCursor;
import org.xbib.io.redis.codec.RedisCodec;

import java.nio.ByteBuffer;

/**
 * Streaming API for multiple Keys. You can implement this interface in order to receive a call to {@code onKey} on every key.
 * Key uniqueness is not guaranteed.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class KeyScanStreamingOutput<K, V> extends ScanOutput<K, V, StreamScanCursor> {

    private final KeyStreamingChannel<K> channel;

    public KeyScanStreamingOutput(RedisCodec<K, V> codec, KeyStreamingChannel<K> channel) {
        super(codec, new StreamScanCursor());
        this.channel = channel;
    }

    @Override
    protected void setOutput(ByteBuffer bytes) {
        channel.onKey(bytes == null ? null : codec.decodeKey(bytes));
        output.setCount(output.getCount() + 1);
    }

}
