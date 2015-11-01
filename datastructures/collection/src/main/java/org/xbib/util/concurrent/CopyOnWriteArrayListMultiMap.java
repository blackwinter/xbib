package org.xbib.util.concurrent;

import org.xbib.util.MultiMap;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * Advantages

 Uses {@link ConcurrentHashMap} for thread safety and visibility.
 Uses {@link CopyOnWriteArrayList} for list thread safety and visibility.
 No blocking in class itself. Instead the backing jdk classes handle blocking for us.
 Blocking has been reduced to key level granularity instead of being at the cache level.

 Disadvantages

 Prone to interleaving. It is weakly consistent and does not guarantee mutually exclusive and atomic calls.
 The {@link #remove(K,V)} call can interleave through the lines of the put method and potentially
 key value pairs can be added back in if a{@link #remove(K,V)} is called part way through the {@link #put(K,V)} call.
 To be strongly consistent the {@link #remove(K,V)} and {@link #put(K,V)} need to be mutually exclusive.
 *
 * @param <K>
 * @param <V>
 */
public class CopyOnWriteArrayListMultiMap <K, V> implements MultiMap<K,V> {

    private final ConcurrentMap<K, Collection<V>> map = new ConcurrentHashMap<>();

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    @Override
    public Collection<V> get(K k) {
        return map.get(k);
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> remove(K k) {
        return map.remove(k);
    }

    @Override
    public boolean put(K k, V v) {
        Collection<V> list = map.get(k);
        if (list == null) {
            list = new CopyOnWriteArrayList<>();
            Collection<V> oldList = map.putIfAbsent(k, list);
            if (oldList != null) {
                list = oldList;
            }
            list.add(v);
            return true;
        } else {
            list.add(v);
            return false;
        }
    }

    @Override
    public void putAll(K key, Collection<V> values) {
        Collection<V> list = map.get(key);
        if (list == null) {
            list = new CopyOnWriteArrayList<>();
            Collection<V> oldList = map.putIfAbsent(key, list);
            if (oldList != null) {
                list = oldList;
            }
            list.addAll(values);
        } else {
            list.addAll(values);
        }
    }

    @Override
    public boolean remove(K k, V v) {
        Collection<V> list = map.get(k);
        if (list == null) {
            return false;
        }
        if (list.isEmpty()) {
            map.remove(k);
            return false;
        }
        boolean removed = list.remove(v);
        if (removed && list.isEmpty()) {
            map.remove(k);
        }
        return removed;
    }

}