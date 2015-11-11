package org.xbib.util.concurrent;

import org.xbib.util.MultiMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Advantages

 Strongly consistent.
 Uses concurrent hash map so we can have non-blocking read.

 Disadvantages

 The synchronisation lock blocks on the entire cache.
 The blocking calls are entirely blocking so all paths through them will block.
 Concurrent hash map is blocking itself although at a fine grained level using stripes.

 * @param <K>
 * @param <V>
 */
public class ConcurrentHashMapArrayListMultiMap<K, V> implements MultiMap<K, V> {

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
    public synchronized Collection<V> remove(K k) {
        return map.remove(k);
    }

    @Override
    public synchronized boolean put(K k, V v) {
        Collection<V> list = map.get(k);
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            list.add(v);
            map.put(k, list);
            return true;
        } else {
            list = new ArrayList<>(list);
            list.add(v);
            map.put(k, list);
            return true;
        }
    }

    @Override
    public synchronized void putAll(K key, Collection<V> values) {
        Collection<V> list = map.get(key);
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            list.addAll(values);
            map.put(key, list);
        } else {
            list = new ArrayList<>(list);
            list.addAll(values);
            map.put(key, list);
        }
    }

    @Override
    public synchronized boolean remove(K k, V v) {
        Collection<V> list = map.get(k);
        if (list == null) {
            return false;
        }
        if (list.isEmpty()) {
            map.remove(k);
            return false;
        }
        boolean removed = list.remove(v);
        if (removed) {
            if (list.isEmpty()) {
                map.remove(k);
            } else {
                list = new ArrayList<V>(list);
                map.put(k, list);
            }
        }
        return removed;
    }

}