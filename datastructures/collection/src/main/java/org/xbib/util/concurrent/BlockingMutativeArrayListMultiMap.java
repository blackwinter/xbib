package org.xbib.util.concurrent;

import org.xbib.util.MultiMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Advantages

 Strongly consistent.
 Doesn’t allocate any more than it needs to (unlike the copy on write pattern).

 Disadvantages

 Very poor performance.
 Uses a hashmap which isn’t thread safe so offers no visibility guarantees.
 All calls – reads/writes are blocking.
 All paths through the blocking calls are blocking.
 *
 * @param <K>
 * @param <V>
 */
public class BlockingMutativeArrayListMultiMap <K, V> implements MultiMap<K, V> {

    private final Map<K, Collection<V>> map = new HashMap<>();

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
    public synchronized Collection<V> get(K k) {
        return map.get(k);
    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public synchronized Collection<V> remove(K k) {
        return map.remove(k);
    }

    public synchronized boolean put(K k, V v) {
        Collection<V> list = map.get(k);
        if (list == null) {
            list = new ArrayList<V>();
            list.add(v);
            map.put(k, list);
            return true;
        } else {
            list.add(v);
            return false;
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
            list.addAll(values);
            map.put(key, list);
        }
    }

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
        if (removed && list.isEmpty()) {
            map.remove(k);
        }
        return removed;
    }

}