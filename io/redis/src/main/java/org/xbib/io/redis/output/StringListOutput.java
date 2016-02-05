package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link List} of string output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class StringListOutput<K, V> extends CommandOutput<K, V, List<String>> {
    public StringListOutput(RedisCodec<K, V> codec) {
        super(codec, new ArrayList<String>());
    }

    @Override
    public void set(ByteBuffer bytes) {
        output.add(bytes == null ? null : decodeAscii(bytes));
    }
}
