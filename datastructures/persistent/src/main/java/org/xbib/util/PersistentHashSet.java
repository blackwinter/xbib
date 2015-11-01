package org.xbib.util;

import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Collectors;

public class PersistentHashSet<E> extends AbstractTrieSet<E, PersistentHashSet<E>> implements PersistentSet<E> {

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
}