package org.xbib.util.persistent;

public interface MutableSortedMap<K, V> extends MutableMap<K, V> {

    @Override
    PersistentSortedMap<K, V> toPersistentMap();

}