package org.xbib.util.persistent;

import org.xbib.util.persistent.AbstractHashTrie.EntryNode;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.System.arraycopy;

public abstract class AbstractHashTrie<K, E extends EntryNode<K, E>, This extends AbstractHashTrie<K, E, This>> {

    @SuppressWarnings("rawtypes")
    private static final Iterator<EntryNode> EMTPY_ITER = Collections.emptyIterator();

    @SuppressWarnings("rawtypes")
    static final Node EMPTY_NODE = new Node() {

        @Override
        public Iterator<EntryNode> iterator() {
            return EMTPY_ITER;
        }

        @Override
        EntryNode findInternal(int shift, int hash, Object key) {
            return null;
        }

        @Override
        Node dissocInternal(UpdateContext currentContext, int shift, int hash, Object key) {
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        Node assocInternal(UpdateContext currentContext, int shift, int hash, EntryNode newEntryNode) {
            currentContext.insert(newEntryNode);
            if (currentContext.expectedUpdates() == 1) {
                return newEntryNode;
            } else {
                Node node = new HashNode(currentContext);
                return node.assocInternal(currentContext, shift, hash, newEntryNode);
            }
        }

        @Override
        protected Node[] getChildren() {
            return null;
        }
    };

    public abstract int size();

    public boolean isEmpty() {
        return size() == 0;
    }

    @SuppressWarnings("unchecked")
    protected This self() {
        return (This) this;
    }

    protected abstract This doReturn(Node<K, E> newRoot, int newSize);

    protected abstract Node<K, E> root();

    public boolean containsKey(Object key) {
        return root().find(key) != null;
    }

    protected final Iterator<E> doIterator() {
        return root().iterator();
    }

    private This commitAndReturn(UpdateContext<? super E> updateContext, Node<K, E> newRoot, int newSize) {
        commit(updateContext);
        return doReturn(newRoot, newSize);
    }

    protected final This doAdd(UpdateContext<? super E> updateContext, E newEntry) {
        Node<K, E> newRoot = root().assoc(updateContext, newEntry);
        return commitAndReturn(updateContext, newRoot, size() + updateContext.getChangeAndReset());
    }

    @SuppressWarnings("rawtypes")
    protected final This doAddAll(UpdateContext<? super E> updateContext, Iterator entries) {
        Node<K, E> newRoot = root();
        int size = size();
        while (entries.hasNext()) {
            @SuppressWarnings("unchecked")
            E entry = (E) entries.next();
            newRoot = newRoot.assoc(updateContext, entry);
            size += updateContext.getChangeAndReset();
        }

        return commitAndReturn(updateContext, newRoot, size);
    }

    protected final This doRemove(UpdateContext<? super E> updateContext, Object key) {
        Node<K, E> newRoot = root().dissoc(updateContext, key);
        return commitAndReturn(updateContext, newRoot, size() + updateContext.getChangeAndReset());
    }

    @SuppressWarnings("rawtypes")
    protected final This doRemoveAll(UpdateContext<? super E> updateContext, Iterator keys) {
        Node<K, E> newRoot = root();
        int size = size();
        while (keys.hasNext()) {
            newRoot = newRoot.dissoc(updateContext, keys.next());
            size += updateContext.getChangeAndReset();
        }
        return commitAndReturn(updateContext, newRoot, size);
    }

    protected void commit(UpdateContext<?> updateContext) {
        updateContext.commit();
    }

    static abstract class Node<K, E extends EntryNode<K, E>> implements Iterable<E> {

        static final int SHIFT_INCREMENT = 5;

        static int hash(Object key) {
            return key == null ? 0 : key.hashCode();
        }

        static int index(int bitmap, int bit) {
            return Integer.bitCount(bitmap & (bit - 1));
        }

        static int bit(int hash, int shift) {
            return 1 << bitIndex(hash, shift);
        }

        static int bitIndex(int hash, int shift) {
            // xx xxxxx xxxxx xxxxx xxxxx NNNNN xxxxx   >>> 5
            // 00 00000 00000 00000 00000 00000 NNNNN   & 0x01f
            // return number (NNNNN) between 0..31
            return (hash >>> shift) & 0x01f;
        }

        E find(Object key) {
            return findInternal(0, hash(key), key);
        }

        Node<K, E> assoc(UpdateContext<? super E> currentContext, E newEntry) {
            return assocInternal(currentContext, 0, newEntry.getHash(), newEntry);
        }

        Node<K, E> dissoc(UpdateContext<? super E> currentContext, Object key) {
            return dissocInternal(currentContext, 0, hash(key), key);
        }

        abstract E findInternal(int shift, int hash, Object key);

        abstract Node<K, E> assocInternal(UpdateContext<? super E> currentContext, int shift, int hash, E newEntry);

        abstract Node<K, E> dissocInternal(UpdateContext<? super E> currentContext, int shift, int hash, Object key);

        protected abstract Node<K, E>[] getChildren();

    }

    static abstract class EntryNode<K, E extends EntryNode<K, E>> extends Node<K, E> {

        final K key;

        public EntryNode(K key) {
            this.key = key;
        }

        public int getHash() {
            return hash(key);
        }

        @SuppressWarnings("unchecked")
        protected E self() {
            return (E) this;
        }

        @SuppressWarnings("unchecked")
        protected Node<K, E> split(final UpdateContext<? super E> currentContext, final int shift, final int hash, final E newEntry) {
            int thisHash = getHash();
            if (hash == thisHash) {
                currentContext.insert(newEntry);
                return new CollisionNode<>((E) this, newEntry);
            } else {
                @SuppressWarnings("rawtypes")
                Node[] newChildren = new Node[HashNode.newSizeForInsert(currentContext, 1)];
                newChildren[0] = this;
                return new HashNode<>(currentContext, bit(thisHash, shift), newChildren)
                        .assocInternal(currentContext, shift, hash, newEntry);
            }
        }

        @Override
        Node<K, E> dissocInternal(UpdateContext<? super E> currentContext, int shift, int hash, Object key) {
            if (Objects.equals(key, this.key)) {
                currentContext.delete(self());
                return null;
            }
            return this;
        }

        @Override
        E findInternal(int shift, int hash, Object key) {
            if (Objects.equals(this.key, key)) {
                return self();
            }
            return null;
        }

        @Override
        public Iterator<E> iterator() {
            return UnmodifiableIterator.singletonIterator(self());
        }

        @Override
        protected Node<K, E>[] getChildren() {
            return null;
        }
    }

    static final class HashNode<K, E extends EntryNode<K, E>> extends Node<K, E> {

        final UpdateContext<? super E> updateContext;

        private int bitmap;

        private Node<K, E>[] children;

        HashNode(UpdateContext<? super E> contextReference) {
            this(contextReference, contextReference.expectedUpdates());
        }

        @SuppressWarnings("unchecked")
        HashNode(UpdateContext<? super E> contextReference, int expectedSize) {
            this(contextReference, 0, new Node[expectedSize < 32 ? expectedSize : 32]);
        }

        HashNode(UpdateContext<? super E> contextReference, int bitmap, Node<K, E>[] children) {
            this.updateContext = contextReference;
            this.bitmap = bitmap;
            this.children = children;
        }

        static int newSizeForInsert(UpdateContext<?> currentContext, int currentChildCount) {
            if (currentContext.expectedUpdates() == 1) {
                return currentChildCount + 1;
            } else {
                return currentChildCount < 16 ? 2 * (currentChildCount + 1) : 32;
            }
        }

        @Override
        Node<K, E> assocInternal(final UpdateContext<? super E> currentContext, final int shift, int hash, final E newEntry) {
            int bit = bit(hash, shift);
            int index = index(bitmap, bit);
            if ((bitmap & bit) != 0) {
                Node<K, E> oldNode = children[index];
                Node<K, E> newNode = oldNode.assocInternal(currentContext, shift + SHIFT_INCREMENT, hash, newEntry);
                if (newNode == oldNode) {
                    return this;
                } else {
                    HashNode<K, E> editable = cloneForReplace(currentContext);
                    editable.children[index] = newNode;

                    return editable;
                }
            } else {
                currentContext.insert(newEntry);
                return insert(currentContext, index, newEntry, bit);
            }
        }

        @Override
        Node<K, E> dissocInternal(UpdateContext<? super E> currentContext, int shift, int hash, Object key) {
            int bit = bit(hash, shift);
            if ((bitmap & bit) == 0) {
                return this;
            }
            int index = index(bitmap, bit);
            Node<K, E> oldNode = children[index];
            Node<K, E> newNode = oldNode.dissocInternal(currentContext, shift + SHIFT_INCREMENT, hash, key);

            if (newNode == oldNode) {
                return this;
            } else if (newNode == null) {
                if (bitmap == bit) {
                    return null;
                } else {
                    return cloneForDelete(currentContext, index, bit);
                }
            } else {
                HashNode<K, E> editable = cloneForReplace(currentContext);
                editable.children[index] = newNode;

                return editable;
            }
        }

        @Override
        public E findInternal(int shift, int hash, Object key) {
            int bit = bit(hash, shift);
            if ((bitmap & bit) == 0) {
                return null;
            }
            int index = index(bitmap, bit);
            Node<K, E> nodeOrEntry = children[index];
            return nodeOrEntry.findInternal(shift + SHIFT_INCREMENT, hash, key);
        }

        @SuppressWarnings("unchecked")
        private Node<K, E> insert(UpdateContext<? super E> currentContext, int index, E newEntry, int bit) {
            int childCount = childCount();
            boolean editInPlace = updateContext.isSameAs(currentContext);

            Node<K, E>[] newChildren;
            if (editInPlace && childCount < children.length) {
                newChildren = this.children;
            } else {
                newChildren = new Node[newSizeForInsert(currentContext, childCount)];
                if (index > 0) {
                    arraycopy(children, 0, newChildren, 0, index);
                }
            }

            // make room for insertion
            if (index < childCount) {
                arraycopy(children, index, newChildren, index + 1, childCount - index);
            }

            newChildren[index] = newEntry;

            if (childCount == 31) {
                // Convert to ArrayNode as it can be done here practically with no extra cost
                return new ArrayNode<>(currentContext, newChildren, childCount + 1);
            } else if (editInPlace) {
                this.bitmap |= bit;
                this.children = newChildren;
                return this;
            } else {
                return new HashNode<>(currentContext, bitmap | bit, newChildren);
            }
        }

        private int childCount() {
            return Integer.bitCount(bitmap);
        }

        @SuppressWarnings("unchecked")
        private Node<K, E> cloneForDelete(UpdateContext<? super E> currentContext, int index, int bit) {
            int childCount = childCount();
            boolean editInPlace = updateContext.isSameAs(currentContext);

            Node<K, E>[] newChildren;
            if (editInPlace) {
                newChildren = this.children;
            } else {
                newChildren = new Node[childCount - 1];
                if (index > 0) {
                    arraycopy(children, 0, newChildren, 0, index);
                }
            }

            // Delete given node
            if (index + 1 < childCount) {
                arraycopy(children, index + 1, newChildren, index, childCount - index - 1);
                if (newChildren.length >= childCount) {
                    newChildren[childCount - 1] = null;
                }
            }

            if (editInPlace) {
                this.bitmap = bitmap ^ bit;
                return this;
            } else {
                return new HashNode<>(currentContext, bitmap ^ bit, newChildren);
            }
        }

        private HashNode<K, E> cloneForReplace(UpdateContext<? super E> currentContext) {
            if (this.updateContext.isSameAs(currentContext)) {
                return this;
            } else {
                return new HashNode<>(currentContext, bitmap, children.clone());
            }
        }

        @Override
        public Iterator<E> iterator() {
            return new ArrayIterator<>(children, childCount());
        }

        @Override
        public Node<K, E>[] getChildren() {
            return children;
        }

    }

    static final class ArrayNode<K, E extends EntryNode<K, E>> extends Node<K, E> {

        final UpdateContext<? super E> updateContext;

        private Node<K, E>[] children;

        private int childCount;

        ArrayNode(UpdateContext<? super E> contextReference, Node<K, E>[] children, int childCount) {
            this.updateContext = contextReference;
            this.children = children;
            this.childCount = childCount;
        }

        @Override
        Node<K, E> assocInternal(final UpdateContext<? super E> currentContext, final int shift, int hash, final E newEntry) {
            int index = bitIndex(hash, shift);
            Node<K, E> node = children[index];
            int newChildCount = childCount;
            Node<K, E> newChild;
            if (node != null) {
                newChild = node.assocInternal(currentContext, shift + SHIFT_INCREMENT, hash, newEntry);
                if (newChild == node) {
                    return this;
                }
            } else {
                currentContext.insert(newEntry);
                newChildCount++;
                newChild = newEntry;
            }
            if (isEditInPlace(currentContext)) {
                this.children[index] = newChild;
                this.childCount = newChildCount;
                return this;
            } else {
                Node<K, E>[] newChildren = this.children.clone();
                newChildren[index] = newChild;
                return new ArrayNode<>(currentContext, newChildren, newChildCount);
            }
        }

        @Override
        Node<K, E> dissocInternal(UpdateContext<? super E> currentContext, int shift, int hash, Object key) {
            int index = bitIndex(hash, shift);
            Node<K, E> node = children[index];
            if (node == null) {
                return this;
            }
            int newChildCount = childCount;
            Node<K, E> newChild = node.dissocInternal(currentContext, shift + SHIFT_INCREMENT, hash, key);
            if (newChild == node) {
                return this;
            } else if (newChild == null) {
                newChildCount--;
                if (newChildCount < 16) {
                    return toBitmapNode(currentContext, newChildCount, index);
                }
            }
            if (isEditInPlace(currentContext)) {
                this.children[index] = newChild;
                this.childCount = newChildCount;
                return this;
            } else {
                Node<K, E>[] newChildren = this.children.clone();
                newChildren[index] = newChild;
                return new ArrayNode<>(currentContext, newChildren, newChildCount);
            }
        }

        private Node<K, E> toBitmapNode(UpdateContext<? super E> currentContext, int newChildCount, int removedIndex) {
            @SuppressWarnings("unchecked")
            Node<K, E>[] newChildren = new Node[newChildCount];
            int bitmap = 0;
            for (int i = 0, j = 0; i < children.length; i++) {
                if (children[i] != null && i != removedIndex) {
                    newChildren[j] = children[i];
                    bitmap |= 1 << i;
                    j++;
                }
            }
            return new HashNode<>(currentContext, bitmap, newChildren);
        }

        @Override
        E findInternal(int shift, int hash, Object key) {
            int index = bitIndex(hash, shift);
            Node<K, E> node = children[index];
            if (node != null) {
                return node.findInternal(shift + SHIFT_INCREMENT, hash, key);
            } else {
                return null;
            }
        }

        private boolean isEditInPlace(UpdateContext<? super E> currentContext) {
            return this.updateContext.isSameAs(currentContext);
        }

        @Override
        public Iterator<E> iterator() {
            return new ArrayIterator<>(children);
        }

        @Override
        public Node<K, E>[] getChildren() {
            return children;
        }

    }

    private static final class CollisionNode<K, E extends EntryNode<K, E>> extends Node<K, E> {

        final int hash;

        private E[] entries;

        @SuppressWarnings("unchecked")
        public CollisionNode(E first, E second) {
            this.hash = first.getHash();
            this.entries = (E[]) new EntryNode[]{first, second};
        }

        @SuppressWarnings("unchecked")
        private CollisionNode(EntryNode<? extends K, ? extends E>[] entries) {
            this.hash = entries[0].getHash();
            this.entries = (E[]) entries;
        }

        @Override
        public E findInternal(int shift, int hash, Object key) {
            for (E entry : entries) {
                if (Objects.equals(entry.key, key)) {
                    return entry;
                }
            }
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Node<K, E> assocInternal(final UpdateContext<? super E> currentContext, final int shift, int hash, final E newEntry) {
            if (hash == this.hash) {
                for (int i = 0; i < entries.length; i++) {
                    if (Objects.equals(entries[i], newEntry)) {
                        return this;
                    } else if (Objects.equals(entries[i].key, newEntry.key)) {
                        if (currentContext.merge(entries[i], newEntry)) {
                            E[] newEntries = entries.clone();
                            newEntries[i] = newEntry;
                            return new CollisionNode<>(newEntries);
                        } else {
                            return this;
                        }
                    }
                }

                currentContext.insert(newEntry);

                E[] newEntries = (E[]) new EntryNode[entries.length + 1];
                arraycopy(entries, 0, newEntries, 0, entries.length);
                newEntries[entries.length] = newEntry;
                return new CollisionNode<>(newEntries);
            }


            Node<K, E>[] newChildren = (currentContext.expectedUpdates() == 1
                    ? new Node[]{this, null} : new Node[]{this, null, null, null});

            Node<K, E> newNode = new HashNode<>(currentContext, bit(this.hash, shift), newChildren);
            return newNode.assocInternal(currentContext, shift, hash, newEntry);
        }

        @Override
        Node<K, E> dissocInternal(UpdateContext<? super E> currentContext, int shift, int hash, Object key) {
            if (hash == this.hash) {
                for (int i = 0; i < entries.length; i++) {
                    if (Objects.equals(entries[i].key, key)) {
                        currentContext.delete(entries[i]);

                        if (entries.length == 2) {
                            if (i == 1) {
                                return entries[0];
                            } else {
                                return entries[1];
                            }
                        }
                        @SuppressWarnings("unchecked")
                        E[] newEntries = (E[]) new EntryNode[entries.length - 1];
                        arraycopy(entries, 0, newEntries, 0, i);
                        if (i + 1 < entries.length) {
                            arraycopy(entries, i + 1, newEntries, i, entries.length - i - 1);
                        }
                        return new CollisionNode<>(newEntries);
                    }
                }
            }
            return this;
        }

        @Override
        public Iterator<E> iterator() {
            return new ArrayIterator<>(entries);
        }

        @Override
        protected Node<K, E>[] getChildren() {
            return entries;
        }

    }

    private static class ArrayIterator<K, E extends EntryNode<K, E>> extends UnmodifiableIterator<E> {

        private final Node<K, E>[] array;

        private final int limit;

        private Iterator<E> subIterator;

        private int pos = 0;

        public ArrayIterator(Node<K, E>[] array) {
            this(array, array.length);
        }

        public ArrayIterator(Node<K, E>[] array, int limit) {
            this.array = array;
            this.limit = limit;
        }

        @Override
        public boolean hasNext() {
            if (subIterator != null) {
                if (subIterator.hasNext()) {
                    return true;
                } else {
                    pos++;
                }
            }
            while (pos < limit && array[pos] == null) {
                pos++;
            }
            if (pos < limit) {
                if (array[pos] instanceof EntryNode) {
                    subIterator = null;
                } else {
                    subIterator = array[pos].iterator();
                }
                return true;
            }
            subIterator = null;
            return false;
        }

        @SuppressWarnings("unchecked")
        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (subIterator != null) {
                return subIterator.next();
            } else {
                return (E) array[pos++];
            }
        }
    }

    static abstract class NodeSpliterator<T, K, E extends EntryNode<K, E>> implements Spliterator<T> {

        private final int characteristics;
        private Node<K, E>[] array;
        private int pos;
        private int limit;
        private int sizeEstimate;
        private NodeSpliterator<T, K, E> subSpliterator;

        @SuppressWarnings("unchecked")
        protected NodeSpliterator(Node<K, E> node, int sizeEstimate, int additionalCharacteristics) {
            if (node instanceof EntryNode) {
                this.array = (Node<K, E>[]) new Node[]{node};
                pos = 0;
                limit = 1;
                this.sizeEstimate = 1;
            } else {
                array = node.getChildren();
                if (array == null) {
                    pos = limit = this.sizeEstimate = 0;
                } else {
                    pos = 0;
                    limit = array.length;
                    this.sizeEstimate = sizeEstimate;
                }
            }
            this.characteristics = ORDERED | SIZED | additionalCharacteristics;
        }

        NodeSpliterator(Node<K, E>[] array, int pos, int limit, int sizeEstimate, int additionalCharacteristics) {
            this.array = array;
            this.pos = pos;
            this.limit = limit;
            this.sizeEstimate = sizeEstimate;
            this.characteristics = ORDERED | additionalCharacteristics;
        }

        @Override
        public final boolean tryAdvance(Consumer<? super T> action) {
            if (subSpliterator != null) {
                if (subSpliterator.tryAdvance(action)) {
                    return true;
                }
                subSpliterator = null;
            }
            if (pos >= limit) {
                return false;
            }
            Node<K, E> node = array[pos++];
            if (node instanceof EntryNode) {
                @SuppressWarnings("unchecked")
                T value = apply((E) node);
                action.accept(value);
                return true;
            } else {
                Node<K, E>[] children = node.getChildren();
                subSpliterator = newSubSpliterator(children, 0, children.length, sizeEstimate / (limit - pos));
                return subSpliterator.tryAdvance(action);
            }
        }

        @Override
        public final void forEachRemaining(Consumer<? super T> action) {
            if (subSpliterator != null) {
                subSpliterator.forEachRemaining(action);
                subSpliterator = null;
            }
            forEach(array, pos, limit, action);
        }

        private void forEach(Node<K, E>[] nodes, Consumer<? super T> action) {
            forEach(nodes, 0, nodes.length, action);
        }

        private void forEach(Node<K, E>[] nodes, int pos, int limit, Consumer<? super T> action) {
            for (; pos < limit; pos++) {
                if (nodes[pos] != null) {
                    forEach(nodes[pos], action);
                }
            }
        }

        private void forEach(Node<K, E> node, Consumer<? super T> action) {
            if (node instanceof EntryNode) {
                @SuppressWarnings("unchecked")
                T value = apply((E) node);
                action.accept(value);
            } else {
                Node<K, E>[] children = node.getChildren();
                if (children != null) {
                    forEach(children, action);
                }
            }
        }

        private void trim() {
            while (pos < limit && array[pos] == null) {
                pos++;
            }
            while (limit > pos && array[limit - 1] == null) {
                limit--;
            }
        }

        @Override
        public NodeSpliterator<T, K, E> trySplit() {
            trim();
            NodeSpliterator<T, K, E> prefix;
            if (subSpliterator != null) {
                return trySplitSubSpliterator();
            } else if (pos >= limit) {
                return null;
            } else if (pos + 1 == limit) {
                return trySplitLastNode();
            }

            int mid = (pos + limit) >>> 1;
            prefix = newSubSpliterator(array, pos, mid, sizeEstimate >>> 1);
            this.pos = mid;
            return prefix;
        }

        private NodeSpliterator<T, K, E> trySplitLastNode() {
            Node<K, E>[] children = array[pos].getChildren();
            if (children == null) {
                return null;
            }
            array = children;
            pos = 0;
            limit = array.length;
            return trySplit();
        }

        private NodeSpliterator<T, K, E> trySplitSubSpliterator() {
            NodeSpliterator<T, K, E> prefix;
            if (pos >= limit) {
                // Array is already consumed, recurse split into subIterator
                prefix = subSpliterator.trySplit();
                this.sizeEstimate = subSpliterator.sizeEstimate;
            } else {
                // Split subIterator off
                prefix = subSpliterator;
                this.sizeEstimate -= subSpliterator.sizeEstimate;
                subSpliterator = null;
            }
            return prefix;
        }

        @Override
        public final long estimateSize() {
            return sizeEstimate;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        protected abstract NodeSpliterator<T, K, E> newSubSpliterator(Node<K, E>[] array, int pos, int limit, int sizeEstimate);

        protected abstract T apply(E entry);

    }
}