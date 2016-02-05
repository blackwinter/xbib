package org.xbib.io.redis.output;

import org.xbib.io.redis.MapScanCursor;
import org.xbib.io.redis.codec.RedisCodec;

import java.nio.ByteBuffer;

/**
 * {@link org.xbib.io.redis.MapScanCursor} for scan cursor output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class MapScanOutput<K, V> extends ScanOutput<K, V, MapScanCursor<K, V>> {

    private K key;

    public MapScanOutput(RedisCodec<K, V> codec) {
        super(codec, new MapScanCursor<K, V>());
    }

    @Override
    protected void setOutput(ByteBuffer bytes) {

        if (key == null) {
            key = codec.decodeKey(bytes);
            return;
        }

        V value = (bytes == null) ? null : codec.decodeValue(bytes);
        output.getMap().put(key, value);
        key = null;
    }

}
