package org.xbib.util.persistent;

import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Collectors;

public class PersistentHashMap<K, V>
        extends AbstractHashMap<K, V, PersistentHashMap<K, V>>
        implements PersistentMap<K, V> {

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final PersistentHashMap EMPTY_MAP = new PersistentHashMap(EMPTY_NODE, 0);

    private final Node<K, EntryNode<K, V>> root;

    private final int size;

    private PersistentHashMap(Node<K, EntryNode<K, V>> newRoot, int newSize) {
        this.root = newRoot;
        this.size = newSize;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentHashMap<K, V> empty() {
        return (PersistentHashMap<K, V>) EMPTY_MAP;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentHashMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        return ((PersistentHashMap<K, V>) EMPTY_MAP).assocAll(map);
    }

    public static <K, V> PersistentHashMap<K, V> of() {
        return empty();
    }

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentHashMap<K, V> of(K k1, V v1) {
        return (PersistentHashMap<K, V>) EMPTY_MAP.assoc(k1, v1);
    }

    public static <K, V> PersistentHashMap<K, V> of(K k1, V v1, K k2, V v2) {
        MutableHashMap<K, V> map = new MutableHashMap<K, V>(2);
        map.put(k1, v1);
        map.put(k2, v2);
        return map.toPersistentMap();
    }

    public static <K, V> PersistentHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        MutableHashMap<K, V> map = new MutableHashMap<K, V>(3);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map.toPersistentMap();
    }

    @SuppressWarnings("unchecked")
    static <K, V> PersistentHashMap<K, V> create(Node<K, EntryNode<K, V>> newRoot, int newSize) {
        return newRoot == null ? (PersistentHashMap<K, V>) EMPTY_MAP : new PersistentHashMap<K, V>(newRoot, newSize);
    }

    public MutableHashMap<K, V> toMutableMap() {
        return new MutableHashMap<K, V>(root, size);
    }

    @Override
    public Map<K, V> asMap() {
        return new ImmutableMap<>(this);
    }

    @Override
    protected Node<K, EntryNode<K, V>> root() {
        return root;
    }

    @Override
    public Spliterator<Map.Entry<K, V>> spliterator() {
        return new EntrySpliterator<>(root, size, true);
    }

    @Override
    public Spliterator<K> keySpliterator() {
        return new KeySpliterator<>(root, size, true);
    }

    @Override
    public Spliterator<V> valueSpliterator() {
        return new ValueSpliterator<>(root, size, true);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    protected PersistentHashMap<K, V> doReturn(Node<K, EntryNode<K, V>> newRoot, int newSize) {
        if (newRoot == root) {
            return this;
        } else {
            return create(newRoot, newSize);
        }
    }

    public String toString() {
        return stream().map(Objects::toString).collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof PersistentHashMap && toString().equals(object.toString());
    }
}