package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;

/**
 * Key output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class KeyOutput<K, V> extends CommandOutput<K, V, K> {
    public KeyOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(ByteBuffer bytes) {
        output = (bytes == null) ? null : codec.decodeKey(bytes);
    }
}
