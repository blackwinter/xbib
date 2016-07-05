package org.xbib.util.persistent;

import java.util.Collection;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Collectors;

public class PersistentHashSet<E> extends AbstractTrieSet<E, PersistentHashSet<E>> implements PersistentSet<E> {

    private final static PersistentHashSet EMPTY_SET = new PersistentHashSet();

    private final Node<E, EntryNode<E>> root;

    private final int size;

    public PersistentHashSet() {
        this(null, 0);
    }

    @SuppressWarnings("unchecked")
    PersistentHashSet(Node<E, EntryNode<E>> root, int size) {
        this.root = root != null ? root : EMPTY_NODE;
        this.size = size;
    }

    @SuppressWarnings("unchecked")
    public static <E> PersistentHashSet<E> empty() {
        return (PersistentHashSet<E>) EMPTY_SET;
    }

    @SuppressWarnings("unchecked")
    public static <E> PersistentHashSet<E> copyOf(Collection<E> collection) {
        return ((PersistentHashSet<E>) EMPTY_SET).conjAll(collection);
    }

    public static <E> PersistentHashSet<E> of() {
        return empty();
    }

    @SuppressWarnings("unchecked")
    public static <E> PersistentHashSet<E> of(E e1) {
        return (PersistentHashSet<E>) EMPTY_SET.conj(e1);
    }

    @Override
    public MutableHashSet<E> toMutableSet() {
        return new MutableHashSet<E>(root, size);
    }

    @Override
    public ImmutableSet<E> asSet() {
        return new ImmutableSet<E>(this);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Spliterator<E> spliterator() {
        return new ElementSpliterator<E>(root, size, true);
    }

    @Override
    protected PersistentHashSet<E> doReturn(Node<E, EntryNode<E>> newRoot, int newSize) {
        if (newRoot == root) {
            return this;
        }
        return new PersistentHashSet<>(newRoot, newSize);
    }

    @Override
    protected Node<E, EntryNode<E>> root() {
        return root;
    }

    @Override
    public String toString() {
        return stream().map(Objects::toString).collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof PersistentHashSet && toString().equals(object.toString());
    }
}