package org.xbib.io.redis;

import java.util.HashMap;
import java.util.Map;

/**
 * Scan cursor for maps.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class MapScanCursor<K, V> extends ScanCursor {

    private final Map<K, V> map = new HashMap<K, V>();

    /**
     * @return the map result.
     */
    public Map<K, V> getMap() {
        return map;
    }
}
