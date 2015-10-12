package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link List} of boolean output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class BooleanListOutput<K, V> extends CommandOutput<K, V, List<Boolean>> {

    public BooleanListOutput(RedisCodec<K, V> codec) {
        super(codec, new ArrayList<Boolean>());
    }

    @Override
    public void set(long integer) {
        output.add((integer == 1) ? Boolean.TRUE : Boolean.FALSE);
    }
}
