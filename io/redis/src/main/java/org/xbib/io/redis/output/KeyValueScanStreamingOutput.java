package org.xbib.io.redis.output;

import org.xbib.io.redis.StreamScanCursor;
import org.xbib.io.redis.codec.RedisCodec;

import java.nio.ByteBuffer;

/**
 * Streaming-Output of Key Value Pairs. Returns the count of all Key-Value pairs (including null).
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class KeyValueScanStreamingOutput<K, V> extends ScanOutput<K, V, StreamScanCursor> {

    private K key;
    private KeyValueStreamingChannel<K, V> channel;

    public KeyValueScanStreamingOutput(RedisCodec<K, V> codec, KeyValueStreamingChannel<K, V> channel) {
        super(codec, new StreamScanCursor());
        this.channel = channel;
    }

    @Override
    protected void setOutput(ByteBuffer bytes) {

        if (key == null) {
            key = codec.decodeKey(bytes);
            return;
        }

        V value = (bytes == null) ? null : codec.decodeValue(bytes);

        channel.onKeyValue(key, value);
        output.setCount(output.getCount() + 1);
        key = null;
    }

}
