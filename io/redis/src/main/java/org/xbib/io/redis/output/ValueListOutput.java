package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link List} of values output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class ValueListOutput<K, V> extends CommandOutput<K, V, List<V>> {
    public ValueListOutput(RedisCodec<K, V> codec) {
        super(codec, new ArrayList<V>());
    }

    @Override
    public void set(ByteBuffer bytes) {
        output.add(bytes == null ? null : codec.decodeValue(bytes));
    }
}
