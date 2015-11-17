package org.xbib.io.redis.output;

import org.xbib.io.redis.ScoredValue;
import org.xbib.io.redis.ScoredValueScanCursor;
import org.xbib.io.redis.codec.RedisCodec;

import java.nio.ByteBuffer;

/**
 * {@link org.xbib.io.redis.ScoredValueScanCursor} for scan cursor output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class ScoredValueScanOutput<K, V> extends ScanOutput<K, V, ScoredValueScanCursor<V>> {

    private V value;

    public ScoredValueScanOutput(RedisCodec<K, V> codec) {
        super(codec, new ScoredValueScanCursor<V>());
    }

    @Override
    protected void setOutput(ByteBuffer bytes) {

        if (value == null) {
            value = codec.decodeValue(bytes);
            return;
        }

        double score = Double.parseDouble(decodeAscii(bytes));
        output.getValues().add(new ScoredValue<V>(score, value));
        value = null;
    }

}
