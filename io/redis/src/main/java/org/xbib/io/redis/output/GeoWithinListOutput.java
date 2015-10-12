package org.xbib.io.redis.output;

import com.google.common.collect.Lists;
import org.xbib.io.redis.GeoCoordinates;
import org.xbib.io.redis.GeoWithin;
import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.List;

import static java.lang.Double.parseDouble;

/**
 * A list output that creates a list with either double/long or {@link GeoCoordinates}'s.
 */
public class GeoWithinListOutput<K, V> extends CommandOutput<K, V, List<GeoWithin<V>>> {

    private V member;
    private Double distance;
    private Long geohash;
    private GeoCoordinates coordinates;

    private Double x;

    private boolean withDistance;
    private boolean withHash;
    private boolean withCoordinates;

    public GeoWithinListOutput(RedisCodec<K, V> codec, boolean withDistance, boolean withHash, boolean withCoordinates) {
        super(codec, null);
        this.withDistance = withDistance;
        this.withHash = withHash;
        this.withCoordinates = withCoordinates;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(long integer) {
        if (member == null) {
            member = (V) (Long) integer;
            return;
        }

        if (withHash) {
            geohash = integer;
        }
    }

    @Override
    public void set(ByteBuffer bytes) {

        if (member == null) {
            member = codec.decodeValue(bytes);
            return;
        }

        Double value = (bytes == null) ? 0 : parseDouble(decodeAscii(bytes));
        if (withDistance) {
            if (distance == null) {
                distance = value;
                return;
            }
        }
        if (withCoordinates) {
            if (x == null) {
                x = value;
                return;
            }
            coordinates = new GeoCoordinates(x, value);
        }
    }

    @Override
    public void multi(int count) {
        if (output == null) {
            output = Lists.newArrayListWithCapacity(count);
        }
    }

    @Override
    public void complete(int depth) {
        if (depth == 1) {
            output.add(new GeoWithin<V>(member, distance, geohash, coordinates));

            member = null;
            distance = null;
            geohash = null;
            coordinates = null;
        }
    }
}
