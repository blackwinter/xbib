package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.util.Date;

/**
 * Date output with no milliseconds.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class DateOutput<K, V> extends CommandOutput<K, V, Date> {
    public DateOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(long time) {
        output = new Date(time * 1000);
    }
}
