package org.xbib.util.persistent;

import org.xbib.util.persistent.PersistentTreeSet.Node;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Spliterators.emptySpliterator;
import static org.xbib.util.persistent.AbstractRedBlackTree.Color.RED;

public class PersistentTreeSet<E>
        extends AbstractRedBlackTree<E, Node<E>, PersistentTreeSet<E>>
        implements PersistentSet<E> {

    @SuppressWarnings("rawtypes")
    private static final PersistentTreeSet EMPTY = new PersistentTreeSet();

    @SuppressWarnings("rawtypes")
    private static final Function GET_ELEMENT = input -> ((Node) input).getKey();
    private final Node<E> root;
    private final int size;

    private PersistentTreeSet() {
        super();
        root = null;
        size = 0;
    }

    private PersistentTreeSet(Comparator<? super E> comparator) {
        super(comparator);
        root = null;
        size = 0;
    }

    private PersistentTreeSet(Comparator<? super E> comparator, Node<E> root, int size) {
        super(comparator);
        this.root = root;
        this.size = size;
    }

    @SuppressWarnings("unchecked")
    public static <E> PersistentTreeSet<E> empty() {
        return EMPTY;
    }

    public static <E> PersistentTreeSet<E> empty(Comparator<? super E> comparator) {
        return new PersistentTreeSet<>(comparator);
    }

    public static <E extends Comparable<? super E>> PersistentTreeSet<E> of(E... elements) {
        return new PersistentTreeSet<E>().conjAll(asList(elements));
    }

    public static <E> PersistentTreeSet<E> of(Comparator<E> comparator, E... elements) {
        return new PersistentTreeSet<>(comparator).conjAll(asList(elements));
    }

    public int size() {
        return size;
    }

    @Override
    public boolean contains(Object key) {
        return find(root, key) != null;
    }

    Node<E> root() {
        return root;
    }

    @Override
    public Set<E> asSet() {
        return new ImmutableSet<>(this);
    }

    @Override
    public PersistentTreeSet<E> conj(E value) {
        UpdateContext<Node<E>> context = new UpdateContext<>(1);
        return doAdd(context, root, new Node<>(context, value, RED));
    }

    @Override
    public PersistentTreeSet<E> conjAll(Collection<? extends E> coll) {
        final UpdateContext<Node<E>> context = new UpdateContext<>(32);
        return doAddAll(context, root, FluentIterable.transform(coll, new EntryToNode<>(context)));
    }

    @Override
    public PersistentTreeSet<E> disj(Object keyObj) {
        return doRemove(new UpdateContext<>(1), root, keyObj);
    }

    @Override
    public MutableSet<E> toMutableSet() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {
        return TransformedIterator.transform(doIterator(root, true), GET_ELEMENT);
    }

    @Override
    public Spliterator<E> spliterator() {
        if (root != null) {
            return new ElementSpliterator<E>(root, size, comparator);
        } else {
            return emptySpliterator();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected PersistentTreeSet<E> doReturn(Comparator<? super E> comparator, Node<E> newRoot, int newSize) {
        if (newRoot == root) {
            return this;
        } else if (newRoot == null) {
            return EMPTY;
        }
        return new PersistentTreeSet<>(comparator, newRoot, newSize);
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
        return object != null && object instanceof PersistentTreeSet && toString().equals(object.toString());
    }

    private static final class EntryToNode<E> implements Function<E, Node<E>> {
        private final UpdateContext<Node<E>> context;

        private EntryToNode(UpdateContext<Node<E>> context) {
            this.context = context;
        }

        @Override
        public Node<E> apply(E input) {
            return new Node<E>(context, input, RED);
        }
    }

    static class Node<E> extends AbstractRedBlackTree.Node<E, Node<E>> {

        public Node(UpdateContext<? super Node<E>> context, E key, Color color) {
            this(context, key, color, null, null);
        }

        public Node(UpdateContext<? super Node<E>> context, E key, Color color, Node<E> left, Node<E> right) {
            super(context, key, color, left, right);
        }

        public E getKey() {
            return key;
        }

        @Override
        public Node<E> self() {
            return this;
        }

        @Override
        protected Node<E> cloneWith(UpdateContext<? super Node<E>> currentContext) {
            return new Node<E>(currentContext, key, color, left, right);
        }

        @Override
        protected Node<E> replaceWith(UpdateContext<? super Node<E>> currentContext, Node<E> node) {
            return this;
        }
    }

    private static class ElementSpliterator<E> extends RBSpliterator<E, Node<E>> {

        private final Comparator<? super E> comparator;

        ElementSpliterator(Node<E> root, int size, Comparator<? super E> comparator) {
            super(root, size, SORTED | DISTINCT | IMMUTABLE);
            this.comparator = comparator;
        }

        ElementSpliterator(int sizeEstimate, Comparator<? super E> comparator) {
            super(sizeEstimate, SORTED | DISTINCT | IMMUTABLE);
            this.comparator = comparator;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected RBSpliterator<E, Node<E>> newSpliterator(int sizeEstimate) {
            return new ElementSpliterator(sizeEstimate, comparator);
        }

        @Override
        protected E apply(Node<E> node) {
            return node.key;
        }

        @Override
        public Comparator<? super E> getComparator() {
            return comparator;
        }

    }
}