package org.xbib.util.concurrent;

import org.xbib.util.MultiMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PartiallyBlockingCopyOnWriteArrayListMultiMap <K, V> implements MultiMap<K,V> {

    private final Map<K, List<V>> map = new ConcurrentHashMap<>();

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
        synchronized (map) {
            return map.remove(k);
        }
    }

    @Override
    public boolean put(K k, V v) {
        List<V> list = Collections.singletonList(v);
        List<V> oldList = map.putIfAbsent(k, list);
        if (oldList != null) {
            synchronized (map) {
                list = map.get(k);
                if (list == null || list.isEmpty()) {
                    list = new ArrayList<>();
                    list.add(v);
                    map.put(k, list);
                    return true;
                } else {
                    list = new ArrayList<>(list);
                }
                list.add(v);
                map.put(k, list);
            }
        }
        return false;
    }

    @Override
    public void putAll(K key, Collection<V> values) {
        List<V> list = new ArrayList<>(values);
        List<V> oldList = map.putIfAbsent(key, list);
        if (oldList != null) {
            synchronized (map) {
                list = map.get(key);
                if (list == null || list.isEmpty()) {
                    list = new ArrayList<>();
                } else {
                    list = new ArrayList<>(list);
                }
                list.addAll(values);
                map.put(key, list);
            }
        }
    }

    @Override
    public boolean remove(K k, V v) {
        List<V> list = map.get(k);
        if (list == null) {
            return false;
        }
        synchronized (map) {
            list = map.get(k);
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
                    list = new ArrayList<>(list);
                    map.put(k, list);
                }
            }
            return removed;
        }
    }

}