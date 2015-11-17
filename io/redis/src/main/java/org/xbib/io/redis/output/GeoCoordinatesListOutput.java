package org.xbib.io.redis.output;

import com.google.common.collect.Lists;
import org.xbib.io.redis.GeoCoordinates;
import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.List;

import static java.lang.Double.parseDouble;

/**
 * A list output that creates a list with {@link GeoCoordinates}'s.
 */
public class GeoCoordinatesListOutput<K, V> extends CommandOutput<K, V, List<GeoCoordinates>> {

    private Double x;

    public GeoCoordinatesListOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(ByteBuffer bytes) {
        Double value = (bytes == null) ? 0 : parseDouble(decodeAscii(bytes));

        if (x == null) {
            x = value;
            return;
        }

        output.add(new GeoCoordinates(x, value));
        x = null;
    }

    @Override
    public void multi(int count) {
        if (output == null) {
            output = Lists.newArrayListWithCapacity(count);
        } else if (count == -1) {
            output.add(null);
        }
    }
}
