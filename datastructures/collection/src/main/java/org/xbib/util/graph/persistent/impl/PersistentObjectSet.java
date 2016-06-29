package org.xbib.util.graph.persistent.impl;

import org.xbib.util.graph.persistent.ObjectSet;
import org.xbib.util.persistent.PersistentHashSet;

import java.util.Collection;
import java.util.Iterator;

public class PersistentObjectSet<T> implements ObjectSet<T> {

    private final PersistentHashSet<T> set;

    public PersistentObjectSet() {
        this(PersistentHashSet.<T>empty());
    }

    public PersistentObjectSet(T value) {
        this(PersistentHashSet.<T>of(value));
    }

    public PersistentObjectSet(Collection<? extends T> values) {
        this(PersistentHashSet.<T>copyOf(values));
    }

    public PersistentObjectSet(PersistentHashSet<T> set) {
        this.set = set;
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(T value) {
        return set.contains(value);
    }

    @Override
    public ObjectSet<T> add(T value) {
        return new PersistentObjectSet<T>(set.conj(value));
    }

    @Override
    public ObjectSet<T> addAll(Collection<? extends T> values) {
        return new PersistentObjectSet<T>(set.conjAll(values));
    }

    @Override
    public ObjectSet<T> remove(T value) {
        return new PersistentObjectSet<T>(set.disj(value));
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        PersistentObjectSet<T> other = (PersistentObjectSet<T>) obj;
        return set.equals(other.set);
    }

    @Override
    public String toString() {
        return set.toString();
    }
}
