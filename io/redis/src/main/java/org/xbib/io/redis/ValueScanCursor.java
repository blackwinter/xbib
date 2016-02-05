package org.xbib.io.redis;

import java.util.ArrayList;
import java.util.List;

/**
 * Cursor providing a list of values.
 *
 * @param <V> Value type.
 */
public class ValueScanCursor<V> extends ScanCursor {

    private final List<V> values = new ArrayList<V>();

    public ValueScanCursor() {
    }

    public List<V> getValues() {
        return values;
    }
}
