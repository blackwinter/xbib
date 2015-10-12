package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;

import static java.lang.Double.parseDouble;

/**
 * Double output, may be null.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class DoubleOutput<K, V> extends CommandOutput<K, V, Double> {
    public DoubleOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(ByteBuffer bytes) {
        output = (bytes == null) ? null : parseDouble(decodeAscii(bytes));
    }
}
