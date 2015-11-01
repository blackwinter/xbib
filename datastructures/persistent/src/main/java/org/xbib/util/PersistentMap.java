package org.xbib.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface PersistentMap<K, V> extends Iterable<Entry<K, V>> {

    PersistentMap<K, V> assoc(K key, V value);

    PersistentMap<K, V> assocAll(Map<? extends K, ? extends V> map);

    PersistentMap<K, V> assocAll(Iterable<Entry<K, V>> entries);

    PersistentMap<K, V> merge(K key, V value, Merger<Entry<K, V>> merger);

    PersistentMap<K, V> mergeAll(Map<? extends K, ? extends V> map, Merger<Entry<K, V>> merger);

    PersistentMap<K, V> mergeAll(Iterable<Entry<K, V>> entries, Merger<Entry<K, V>> merger);

    PersistentMap<K, V> dissoc(Object key);

    PersistentMap<K, V> dissoc(Object key, Merger<Entry<K, V>> merger);

    V get(Object key);

    boolean containsKey(Object key);

    Spliterator<Entry<K, V>> spliterator();

    Spliterator<K> keySpliterator();

    Spliterator<V> valueSpliterator();

    int size();

    boolean isEmpty();

    MutableMap<K, V> toMutableMap();

    Map<K, V> asMap();

    default Stream<Entry<K, V>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    default Stream<Entry<K, V>> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    default Stream<K> keyStream() {
        return StreamSupport.stream(keySpliterator(), false);
    }

    default Stream<K> parallelKeyStream() {
        return StreamSupport.stream(keySpliterator(), true);
    }

    default Stream<V> valueStream() {
        return StreamSupport.stream(valueSpliterator(), false);
    }

    default Stream<V> parallelValueStream() {
        return StreamSupport.stream(valueSpliterator(), true);
    }

}