package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link List} of keys output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class KeyListOutput<K, V> extends CommandOutput<K, V, List<K>> {
    public KeyListOutput(RedisCodec<K, V> codec) {
        super(codec, new ArrayList<K>());
    }

    @Override
    public void set(ByteBuffer bytes) {
        output.add(codec.decodeKey(bytes));
    }
}
