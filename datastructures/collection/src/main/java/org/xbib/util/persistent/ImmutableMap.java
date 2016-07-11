package org.xbib.util.persistent;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class ImmutableMap<K, V> extends AbstractMap<K, V> {

    private final PersistentMap<K, V> map;

    ImmutableMap(PersistentMap<K, V> map) {
        this.map = map;
    }

    public PersistentMap<K, V> getPersistentMap() {
        return map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return map.iterator();
            }

            @Override
            public int size() {
                return map.size();
            }
        };
    }

    @Override
    public V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

}