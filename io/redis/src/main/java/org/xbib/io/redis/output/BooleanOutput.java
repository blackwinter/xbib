package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;

/**
 * Boolean output. The actual value is returned as an integer where 0 indicates false and 1 indicates true, or as a null bulk
 * reply for script output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class BooleanOutput<K, V> extends CommandOutput<K, V, Boolean> {
    public BooleanOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(long integer) {
        output = (integer == 1) ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public void set(ByteBuffer bytes) {
        output = (bytes != null) ? Boolean.TRUE : Boolean.FALSE;
    }
}
