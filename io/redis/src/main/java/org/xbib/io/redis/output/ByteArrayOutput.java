package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;

/**
 * Byte array output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class ByteArrayOutput<K, V> extends CommandOutput<K, V, byte[]> {
    public ByteArrayOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(ByteBuffer bytes) {
        if (bytes != null) {
            output = new byte[bytes.remaining()];
            bytes.get(output);
        }
    }
}
