package org.xbib.io.redis;

import java.util.ArrayList;
import java.util.List;

/**
 * Cursor providing a list of {@link ScoredValue}
 *
 * @param <V> Value type.
 */
public class ScoredValueScanCursor<V> extends ScanCursor {

    private final List<ScoredValue<V>> values = new ArrayList<ScoredValue<V>>();

    public ScoredValueScanCursor() {
    }

    public List<ScoredValue<V>> getValues() {
        return values;
    }
}
