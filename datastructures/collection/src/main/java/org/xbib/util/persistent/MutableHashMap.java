package org.xbib.util.persistent;

import org.xbib.util.persistent.AbstractHashTrie.Node;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MutableHashMap<K, V> extends AbstractMap<K, V> implements MutableMap<K, V> {

    private MMap<K, V> map;

    private V previousValue;

    private final Merger<Entry<K, V>> defaultMerger = new Merger<Map.Entry<K, V>>() {

        @Override
        public void insert(Map.Entry<K, V> newEntry) {
            previousValue = null;
        }

        @Override
        public boolean merge(Map.Entry<K, V> oldEntry, Map.Entry<K, V> newEntry) {
            previousValue = oldEntry.getValue();
            return true;
        }

        @Override
        public void delete(java.util.Map.Entry<K, V> oldEntry) {
            previousValue = oldEntry.getValue();
        }
    };

    public MutableHashMap() {
        this.map = new MMap<K, V>();
    }

    public MutableHashMap(int expectedSize) {
        this.map = new MMap<K, V>(expectedSize);
    }

    MutableHashMap(Node<K, AbstractHashMap.EntryNode<K, V>> root, int size) {
        this.map = new MMap<K, V>(root, size);
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

            // TODO: clear, contains etc
        };
    }

    // TODO: values(), keySet() with spliterator()

    @Override
    public V put(final K key, final V value) {
        map.merge(key, value, defaultMerger);
        return previousValue;
    }

    @Override
    public V remove(final Object key) {
        map.dissoc(key, defaultMerger);
        return previousValue;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        map.assocAll(m);
    }

    @Override
    public void clear() {
        if (map.size() > 0) {
            map = new MMap<K, V>();
        }
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return map.iterator();
    }


    @Override
    public void merge(K key, V value, Merger<Map.Entry<K, V>> merger) {
        map.merge(key, value, merger);
    }

    @Override
    public void mergeAll(Map<? extends K, ? extends V> m, Merger<Map.Entry<K, V>> merger) {
        map.mergeAll(m, merger);
    }

    @Override
    public void mergeAll(Iterable<Map.Entry<K, V>> entries, Merger<Map.Entry<K, V>> merger) {
        map.mergeAll(entries, merger);
    }

    @Override
    public PersistentHashMap<K, V> toPersistentMap() {
        return map.toPersistentMap();
    }

    private static class MMap<K, V> extends AbstractHashMap<K, V, MMap<K, V>> {

        private UpdateContext<Map.Entry<K, V>> updateContext;

        private Node<K, EntryNode<K, V>> root;

        private int size;

        @SuppressWarnings("unchecked")
        private MMap(int expectedSize) {
            this(expectedSize, EMPTY_NODE, 0);
        }

        @SuppressWarnings("unchecked")
        private MMap() {
            this(EMPTY_NODE, 0);
        }

        private MMap(Node<K, EntryNode<K, V>> root, int size) {
            this(32, root, size);
        }

        private MMap(int expectedSize, Node<K, EntryNode<K, V>> root, int size) {
            this.updateContext = new UpdateContext<Map.Entry<K, V>>(expectedSize);
            this.root = root;
            this.size = size;
        }

        @Override
        protected Node<K, EntryNode<K, V>> root() {
            return root;
        }

        @Override
        protected MMap<K, V> self() {
            return this;
        }

        public PersistentHashMap<K, V> toPersistentMap() {
            updateContext.commit();
            return PersistentHashMap.create(root, size);
        }

        @Override
        public int size() {
            return size;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected MMap<K, V> doReturn(Node<K, EntryNode<K, V>> newRoot, int newSize) {
            this.root = (Node<K, EntryNode<K, V>>) (newRoot == null ? EMPTY_NODE : newRoot);
            this.size = newSize;
            return this;
        }

        @Override
        protected UpdateContext<Map.Entry<K, V>> updateContext(int expectedUpdates, Merger<Map.Entry<K, V>> merger) {
            if (updateContext.isCommitted()) {
                updateContext = new UpdateContext<Map.Entry<K, V>>(expectedUpdates, merger);
            } else {
                updateContext.validate();
                updateContext.merger(merger);
            }
            return updateContext;
        }

        @Override
        protected void commit(UpdateContext<?> updateContext) {
            // Nothing to do here
        }
    }
}