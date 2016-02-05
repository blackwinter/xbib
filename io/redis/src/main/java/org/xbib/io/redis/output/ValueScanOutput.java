package org.xbib.io.redis.output;

import org.xbib.io.redis.ValueScanCursor;
import org.xbib.io.redis.codec.RedisCodec;

import java.nio.ByteBuffer;

/**
 * {@link org.xbib.io.redis.ValueScanCursor} for scan cursor output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class ValueScanOutput<K, V> extends ScanOutput<K, V, ValueScanCursor<V>> {

    public ValueScanOutput(RedisCodec<K, V> codec) {
        super(codec, new ValueScanCursor<V>());
    }

    @Override
    protected void setOutput(ByteBuffer bytes) {
        output.getValues().add(bytes == null ? null : codec.decodeValue(bytes));
    }

}
