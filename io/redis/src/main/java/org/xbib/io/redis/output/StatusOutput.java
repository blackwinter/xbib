package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;

import static org.xbib.io.redis.protocol.Charsets.buffer;

/**
 * Status message output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class StatusOutput<K, V> extends CommandOutput<K, V, String> {
    private static final ByteBuffer OK = buffer("OK");

    public StatusOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(ByteBuffer bytes) {
        output = OK.equals(bytes) ? "OK" : decodeAscii(bytes);
    }
}
