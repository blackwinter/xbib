package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;

/**
 * 64-bit integer output, may be null.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class IntegerOutput<K, V> extends CommandOutput<K, V, Long> {
    public IntegerOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(long integer) {
        output = integer;
    }

    @Override
    public void set(ByteBuffer bytes) {
        output = null;
    }
}
