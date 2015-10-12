package org.xbib.io.redis.output;

import org.xbib.io.redis.KeyScanCursor;
import org.xbib.io.redis.codec.RedisCodec;

import java.nio.ByteBuffer;

/**
 * {@link org.xbib.io.redis.KeyScanCursor} for scan cursor output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class KeyScanOutput<K, V> extends ScanOutput<K, V, KeyScanCursor<K>> {

    public KeyScanOutput(RedisCodec<K, V> codec) {
        super(codec, new KeyScanCursor<K>());
    }

    @Override
    protected void setOutput(ByteBuffer bytes) {
        output.getKeys().add(bytes == null ? null : codec.decodeKey(bytes));
    }

}
