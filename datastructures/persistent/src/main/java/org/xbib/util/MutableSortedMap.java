package org.xbib.util;

public interface MutableSortedMap<K, V> extends MutableMap<K, V> {

    @Override
    PersistentSortedMap<K, V> toPersistentMap();

}