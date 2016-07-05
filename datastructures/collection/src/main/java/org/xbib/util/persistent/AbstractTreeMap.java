package org.xbib.util.persistent;

import org.xbib.util.persistent.AbstractTreeMap.Node;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import static org.xbib.util.persistent.AbstractRedBlackTree.Color.RED;

public abstract class AbstractTreeMap<K, V, This extends AbstractTreeMap<K, V, This>>
        extends AbstractRedBlackTree<K, Node<K, V>, This> implements Iterable<Map.Entry<K, V>> {

    protected AbstractTreeMap() {
        super();
    }

    protected AbstractTreeMap(Comparator<? super K> comparator) {
        super(comparator);
    }

    public abstract int size();

    protected abstract Node<K, V> root();

    protected UpdateContext<Entry<K, V>> updateContext() {
        return updateContext(null);
    }

    protected UpdateContext<Entry<K, V>> updateContext(Merger<Entry<K, V>> merger) {
        return new UpdateContext<Entry<K, V>>(1, merger);
    }

    public V get(Object key) {
        Node<K, V> node = find(root(), key);
        return node != null ? node.value : null;
    }

    public V max() {
        Node<K, V> max = findMax(root());
        return max != null ? max.value : null;
    }

    public V min() {
        Node<K, V> min = findMin(root());
        return min != null ? min.value : null;
    }

    public This assoc(K key, V value) {
        UpdateContext<Entry<K, V>> context = updateContext();
        return doAdd(context, root(), new Node<K, V>(context, key, value, RED));
    }

    @SuppressWarnings("unchecked")
    public This assocAll(Map<? extends K, ? extends V> map) {
        final UpdateContext<Entry<K, V>> context = updateContext();
        return (This) doAddAll(context, root(), FluentIterable.transform(map.entrySet(), entry -> entryToNode(entry, context)));
    }

    @SuppressWarnings("unchecked")
    private Node entryToNode(Entry<? extends K, ? extends V> entry, UpdateContext<Entry<K, V>> context) {
        if (entry instanceof Node) {
            return (Node) entry;
        } else {
            return new Node(context, entry.getKey(), entry.getValue(), RED);
        }
    }

    public This dissoc(Object keyObj) {
        return doRemove(updateContext(), root(), keyObj);
    }

    @SuppressWarnings("unchecked")
    public This assocAll(Iterable<Entry<K, V>> entries) {
        final UpdateContext<Entry<K, V>> context = updateContext();
        return (This) doAddAll(context, root(), FluentIterable.transform(entries, (entry) -> entryToNode(entry, context)));
    }

    public This merge(K key, V value, Merger<Entry<K, V>> merger) {
        final UpdateContext<Entry<K, V>> context = updateContext(merger);
        return doAdd(context, root(), new Node<K, V>(context, key, value, RED));
    }

    @SuppressWarnings("unchecked")
    public This mergeAll(Map<? extends K, ? extends V> map, Merger<Entry<K, V>> merger) {
        final UpdateContext<Entry<K, V>> context = updateContext(merger);
        return (This) doAddAll(context, root(), FluentIterable.transform(map.entrySet(), entry -> entryToNode(entry, context)));
    }

    @SuppressWarnings("unchecked")
    public This mergeAll(Iterable<Entry<K, V>> entries, Merger<Entry<K, V>> merger) {
        final UpdateContext<Entry<K, V>> context = updateContext(merger);
        return (This) doAddAll(context, root(), FluentIterable.transform(entries, (entry) -> entryToNode(entry, context)));
    }

    public This dissoc(Object key, Merger<Entry<K, V>> merger) {
        final UpdateContext<Entry<K, V>> context = updateContext(merger);
        return doRemove(context, root(), key);
    }

    public boolean containsKey(Object key) {
        return find(root(), key) != null;
    }


    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return iterator(true);
    }

    public Iterator<Map.Entry<K, V>> iterator(boolean asc) {
        return TransformedIterator.transform(doIterator(root(), true), MapUtils.<K, V>mapEntryFunction());
    }

    public Iterable<Map.Entry<K, V>> range(K from, K to) {
        return range(from, true, to, false, true);
    }

    public Iterable<Map.Entry<K, V>> range(K from, K to, boolean asc) {
        return range(from, true, to, false, asc);
    }

    public Iterable<Map.Entry<K, V>> range(final K from, final boolean fromInclusive, final K to, final boolean toInclusive) {
        return range(from, fromInclusive, to, toInclusive, true);
    }

    public Iterable<Map.Entry<K, V>> range(final K from, final boolean fromInclusive, final K to, final boolean toInclusive, final boolean asc) {
        return () -> TransformedIterator.transform(doRangeIterator(root(), asc, from, fromInclusive, to, toInclusive), MapUtils.<K, V>mapEntryFunction());
    }

    public Iterable<K> keys() {
        return FluentIterable.transform(this, MapUtils.<K>mapKeyFunction());
    }

    public Iterable<V> values() {
        return FluentIterable.transform(this, MapUtils.<V>mapValueFunction());
    }

    static class Node<K, V> extends AbstractRedBlackTree.Node<K, Node<K, V>> implements Map.Entry<K, V> {
        V value;

        public Node(UpdateContext<? super Node<K, V>> context, K key, V value, Color color) {
            this(context, key, value, color, null, null);
        }

        public Node(UpdateContext<? super Node<K, V>> context, K key, V value, Color color, Node<K, V> left, Node<K, V> right) {
            super(context, key, color, left, right);
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public Node<K, V> self() {
            return this;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Node<K, V> cloneWith(UpdateContext<? super Node<K, V>> currentContext) {
            return new Node<K, V>(currentContext, key, value, color, left, right);
        }

        @Override
        protected Node<K, V> replaceWith(UpdateContext<? super Node<K, V>> currentContext, Node<K, V> node) {
            if (node == this || Objects.equals(this.value, node.value)) {
                return null;
            } else if (context.isSameAs(currentContext)) {
                this.value = node.value;
                return this;
            } else {
                node.color = this.color;
                node.left = this.left;
                node.right = this.right;
                return node;
            }
        }

        public String toString() {
            return getKey() + ": " + getValue();
        }
    }

    static class EntrySpliterator<K, V> extends RBSpliterator<Map.Entry<K, V>, Node<K, V>> {

        private final Comparator<? super K> comparator;

        public EntrySpliterator(Node<K, V> root, int size, Comparator<? super K> comparator, boolean immutable) {
            super(root, size, SORTED | DISTINCT | (immutable ? IMMUTABLE : 0));
            this.comparator = comparator;
        }

        protected EntrySpliterator(int sizeEstimate, Comparator<? super K> comparator, boolean immutable) {
            super(sizeEstimate, SORTED | DISTINCT | (immutable ? IMMUTABLE : 0));
            this.comparator = comparator;
        }

        @Override
        protected RBSpliterator<Entry<K, V>, Node<K, V>> newSpliterator(int sizeEstimate) {
            return new EntrySpliterator<>(sizeEstimate, comparator, hasCharacteristics(IMMUTABLE));
        }

        @Override
        protected Entry<K, V> apply(Node<K, V> node) {
            return node;
        }

        @Override
        public Comparator<? super Map.Entry<K, V>> getComparator() {
            return Map.Entry.comparingByKey(comparator);
        }
    }

    static class KeySpliterator<K, V> extends RBSpliterator<K, Node<K, V>> {

        private final Comparator<? super K> comparator;

        public KeySpliterator(Node<K, V> root, int size, Comparator<? super K> comparator, boolean immutable) {
            super(root, size, SORTED | DISTINCT | (immutable ? IMMUTABLE : 0));
            this.comparator = comparator;
        }

        protected KeySpliterator(int sizeEstimate, Comparator<? super K> comparator, boolean immutable) {
            super(sizeEstimate, SORTED | DISTINCT | (immutable ? IMMUTABLE : 0));
            this.comparator = comparator;
        }

        @Override
        protected RBSpliterator<K, Node<K, V>> newSpliterator(int sizeEstimate) {
            return new KeySpliterator<>(sizeEstimate, comparator, hasCharacteristics(IMMUTABLE));
        }

        @Override
        protected K apply(Node<K, V> node) {
            return node.key;
        }

        @Override
        public Comparator<? super K> getComparator() {
            return comparator;
        }
    }

    static class ValueSpliterator<K, V> extends RBSpliterator<V, Node<K, V>> {

        private final Comparator<? super K> comparator;

        public ValueSpliterator(Node<K, V> root, int size, Comparator<? super K> comparator, boolean immutable) {
            super(root, size, (immutable ? IMMUTABLE : 0));
            this.comparator = comparator;
        }

        protected ValueSpliterator(int sizeEstimate, Comparator<? super K> comparator, boolean immutable) {
            super(sizeEstimate, (immutable ? IMMUTABLE : 0));
            this.comparator = comparator;
        }

        @Override
        protected RBSpliterator<V, Node<K, V>> newSpliterator(int sizeEstimate) {
            return new ValueSpliterator<>(sizeEstimate, comparator, hasCharacteristics(IMMUTABLE));
        }

        @Override
        protected V apply(Node<K, V> node) {
            return node.value;
        }

    }

}