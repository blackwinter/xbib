package org.xbib.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class UnmodifiableIterator<E> implements Iterator<E> {
    protected UnmodifiableIterator() {
    }

    public static <T> UnmodifiableIterator<T> singletonIterator(
            final T value) {
        return new UnmodifiableIterator<T>() {
            boolean done;

            @Override
            public boolean hasNext() {
                return !done;
            }

            @Override
            public T next() {
                if (done) {
                    throw new NoSuchElementException();
                }
                done = true;
                return value;
            }
        };
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }
}

