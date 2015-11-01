package org.xbib.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

class ImmutableSet<E> extends AbstractSet<E> {

    private PersistentSet<E> set;

    ImmutableSet(PersistentSet<E> set) {
        this.set = set;
    }

    public PersistentSet<E> toPersistentSet() {
        return set;
    }

    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public boolean add(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

}