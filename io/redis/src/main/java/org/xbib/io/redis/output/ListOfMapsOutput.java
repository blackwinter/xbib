package org.xbib.io.redis.output;

import org.xbib.io.redis.codec.RedisCodec;
import org.xbib.io.redis.protocol.CommandOutput;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link List} of maps output.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class ListOfMapsOutput<K, V> extends CommandOutput<K, V, List<Map<K, V>>> {
    private MapOutput<K, V> nested;
    private int mapCount = -1;
    private List<Integer> counts = new ArrayList<Integer>();

    public ListOfMapsOutput(RedisCodec<K, V> codec) {
        super(codec, new ArrayList<Map<K, V>>());
        nested = new MapOutput<K, V>(codec);
    }

    @Override
    public void set(ByteBuffer bytes) {
        nested.set(bytes);
    }

    @Override
    public void complete(int depth) {

        if (!counts.isEmpty()) {
            int expectedSize = counts.get(0);

            if (nested.get().size() == expectedSize) {
                counts.remove(0);
                output.add(new HashMap<K, V>(nested.get()));
                nested.get().clear();
            }
        }
    }

    @Override
    public void multi(int count) {
        if (mapCount == -1) {
            mapCount = count;
        } else {
            // div 2 because of key value pair counts twice
            counts.add(count / 2);
        }
    }
}
