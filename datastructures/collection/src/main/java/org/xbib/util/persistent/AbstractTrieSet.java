package org.xbib.util.persistent;

import org.xbib.util.persistent.AbstractTrieSet.EntryNode;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractTrieSet<E, This extends AbstractTrieSet<E, This>> extends AbstractHashTrie<E, EntryNode<E>, This> implements Iterable<E> {

    private static final Function ELEMENT_TO_ENTRY = EntryNode::new;

    private static final Function ENTRY_TO_ELEMENT = input -> ((EntryNode) input).element();

    public This conj(E element) {
        final UpdateContext<EntryNode<E>> updateContext = updateContext(1, null);
        try {
            return doAdd(updateContext, new EntryNode<E>(element));
        } finally {
            commit(updateContext);
        }
    }

    public This conjAll(final Collection<? extends E> elements) {
        return conjAll(elements, elements.size());
    }

    public This conjAll(AbstractTrieSet<? extends E, ?> elements) {
        return conjAll(elements, elements.size());
    }

    public This conjAll(final Iterable<? extends E> elements) {
        return conjAll(elements, 32);
    }

    @SuppressWarnings("unchecked")
    private This conjAll(final Iterable<? extends E> elements, int size) {
        final UpdateContext<EntryNode<E>> updateContext = updateContext(size, null);
        try {
            return (This) doAddAll(updateContext, TransformedIterator.transform(elements.iterator(), ELEMENT_TO_ENTRY));
        } finally {
            commit(updateContext);
        }
    }

    public This disj(Object element) {
        final UpdateContext<EntryNode<E>> updateContext = updateContext(1, null);
        try {
            return doRemove(updateContext, element);
        } finally {
            commit(updateContext);
        }
    }

    public This disjAll(final Iterable<? extends E> elements) {
        final UpdateContext<EntryNode<E>> updateContext = updateContext(1, null);
        try {
            return doRemoveAll(updateContext, elements.iterator());
        } finally {
            commit(updateContext);
        }
    }

    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {
        return TransformedIterator.transform(doIterator(), ENTRY_TO_ELEMENT);
    }

    protected UpdateContext<EntryNode<E>> updateContext(int expectedSize, Merger<EntryNode<E>> merger) {
        return new UpdateContext<>(expectedSize, merger);
    }

    public boolean contains(Object o) {
        return containsKey(o);
    }

    public static final class EntryNode<E> extends AbstractHashTrie.EntryNode<E, EntryNode<E>> {

        public EntryNode(E element) {
            super(element);
        }

        public E element() {
            return key;
        }

        @Override
        public Node<E, EntryNode<E>> assocInternal(final UpdateContext<? super EntryNode<E>> currentContext, final int shift, final int hash, final EntryNode<E> newEntry) {
            if (Objects.equals(this.key, newEntry.key)) {
                currentContext.merge(this, newEntry);
                return this;
            } else {
                return split(currentContext, shift, hash, newEntry);
            }
        }
    }

    static class ElementSpliterator<E> extends NodeSpliterator<E, E, EntryNode<E>> {

        public ElementSpliterator(Node<E, EntryNode<E>> node, int sizeEstimate, boolean immutable) {
            super(node, sizeEstimate, DISTINCT | (immutable ? IMMUTABLE : 0));
        }

        public ElementSpliterator(Node<E, EntryNode<E>>[] array, int pos, int limit, int sizeEstimate, boolean immutable) {
            super(array, pos, limit, sizeEstimate, DISTINCT | (immutable ? IMMUTABLE : 0));
        }

        @Override
        protected NodeSpliterator<E, E, EntryNode<E>> newSubSpliterator(Node<E, EntryNode<E>>[] array, int pos, int limit, int sizeEstimate) {
            return new ElementSpliterator<>(array, pos, limit, sizeEstimate, hasCharacteristics(IMMUTABLE));
        }

        @Override
        protected E apply(EntryNode<E> entry) {
            return entry.key;
        }
    }

}