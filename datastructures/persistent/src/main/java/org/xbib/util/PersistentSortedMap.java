package org.xbib.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public interface PersistentSortedMap<K, V> extends PersistentMap<K, V> {

    @Override
    PersistentSortedMap<K, V> assoc(K key, V value);

    @Override
    PersistentSortedMap<K, V> assocAll(Map<? extends K, ? extends V> map);

    @Override
    PersistentSortedMap<K, V> assocAll(Iterable<Map.Entry<K, V>> entries);

    @Override
    PersistentSortedMap<K, V> merge(K key, V value, Merger<Map.Entry<K, V>> merger);

    @Override
    PersistentSortedMap<K, V> mergeAll(Map<? extends K, ? extends V> map, Merger<Entry<K, V>> merger);

    @Override
    PersistentSortedMap<K, V> mergeAll(Iterable<Entry<K, V>> entries, Merger<Entry<K, V>> merger);

    @Override
    PersistentSortedMap<K, V> dissoc(Object key);

    @Override
    PersistentSortedMap<K, V> dissoc(Object key, Merger<Entry<K, V>> merger);

    @Override
    MutableSortedMap<K, V> toMutableMap();

    @Override
    Map<K, V> asMap();

    Iterator<Map.Entry<K, V>> iterator(boolean asc);

    Iterable<Map.Entry<K, V>> range(K from, K to);

    Iterable<Map.Entry<K, V>> range(K from, K to, boolean asc);

    Iterable<Map.Entry<K, V>> range(final K from, final boolean fromInclusive, final K to, final boolean toInclusive);

    Iterable<Map.Entry<K, V>> range(final K from, final boolean fromInclusive, final K to, final boolean toInclusive, final boolean asc);

    Map.Entry<K, V> getFirstEntry();

    Map.Entry<K, V> getLastEntry();

}