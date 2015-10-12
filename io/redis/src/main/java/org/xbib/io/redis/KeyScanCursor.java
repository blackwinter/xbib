package org.xbib.io.redis;

import java.util.ArrayList;
import java.util.List;

/**
 * Cursor providing a list of keys.
 *
 * @param <K> Key type.
 */
public class KeyScanCursor<K> extends ScanCursor {

    private final List<K> keys = new ArrayList<K>();

    public List<K> getKeys() {
        return keys;
    }
}
