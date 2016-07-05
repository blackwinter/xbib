package org.xbib.util.persistent;

import java.util.Collection;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface PersistentSet<E> extends Iterable<E> {

    Set<E> asSet();

    PersistentSet<E> conj(E element);

    PersistentSet<E> conjAll(Collection<? extends E> elements);

    boolean contains(Object o);

    PersistentSet<E> disj(Object element);

    MutableSet<E> toMutableSet();

    int size();

    Spliterator<E> spliterator();

    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    default Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

}