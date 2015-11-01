package org.xbib.util;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Collectors;

import static java.util.Spliterators.emptySpliterator;

public class PersistentTreeMap<K, V> extends AbstractTreeMap<K, V, PersistentTreeMap<K, V>> implements PersistentSortedMap<K, V> {

    @SuppressWarnings("rawtypes")
    public static final PersistentTreeMap EMPTY = new PersistentTreeMap();
    private final Node<K, V> root;
    private final int size;

    private PersistentTreeMap() {
        root = null;
        size = 0;
    }

    private PersistentTreeMap(Comparator<? super K> comparator) {
        super(comparator);
        root = null;
        size = 0;
    }

    PersistentTreeMap(Comparator<? super K> comparator, Node<K, V> root, int size) {
        super(comparator);
        this.root = root;
        this.size = size;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentTreeMap<K, V> empty() {
        return EMPTY;
    }

    public static <K, V> PersistentTreeMap<K, V> empty(Comparator<? super K> comparator) {
        return new PersistentTreeMap<K, V>(comparator);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentTreeMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        PersistentTreeMap<K, V> m = EMPTY;
        map.entrySet().stream().forEach(entry -> m.assoc(entry.getKey(), entry.getValue()));
        return m;
    }

    public static <K, V> PersistentTreeMap<K, V> of() {
        return empty();
    }

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentTreeMap<K, V> of(K k1, V v1) {
        return (PersistentTreeMap<K, V>) EMPTY.assoc(k1, v1);
    }

    public static <K, V> PersistentTreeMap<K, V> of(K k1, V v1, K k2, V v2) {
        MutableTreeMap<K, V> map = new MutableTreeMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map.toPersistentMap();
    }

    public static <K, V> PersistentTreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        MutableTreeMap<K, V> map = new MutableTreeMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map.toPersistentMap();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    protected Node<K, V> root() {
        return root;
    }

    @Override
    public MutableTreeMap<K, V> toMutableMap() {
        return new MutableTreeMap<>(comparator, root, size);
    }

    @Override
    public Map<K, V> asMap() {
        return new ImmutableMap<>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected PersistentTreeMap<K, V> doReturn(Comparator<? super K> comparator, Node<K, V> newRoot, int newSize) {
        if (newRoot == root) {
            return this;
        } else if (newRoot == null) {
            return EMPTY;
        }
        return new PersistentTreeMap<K, V>(comparator, newRoot, newSize);
    }

    @Override
    public Entry<K, V> getFirstEntry() {
        return findMin(root);
    }

    @Override
    public Entry<K, V> getLastEntry() {
        return findMax(root);
    }

    public Spliterator<Entry<K, V>> spliterator() {
        if (root != null) {
            return new EntrySpliterator<K, V>(root, size, comparator, true);
        } else {
            return emptySpliterator();
        }
    }

    public Spliterator<K> keySpliterator() {
        if (root != null) {
            return new KeySpliterator<K, V>(root, size, comparator, true);
        } else {
            return emptySpliterator();
        }
    }

    public Spliterator<V> valueSpliterator() {
        if (root != null) {
            return new ValueSpliterator<K, V>(root, size, comparator, true);
        } else {
            return emptySpliterator();
        }
    }

    public String toString() {
        return stream().map(Objects::toString).collect(Collectors.joining(", ", "{", "}"));
    }

}