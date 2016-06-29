package org.xbib.util.persistent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class FluentIterable<E> implements Iterable<E> {

    private final Iterable<E> iterable;

    protected FluentIterable() {
        this.iterable = this;
    }

    FluentIterable(Iterable<E> iterable) {
        this.iterable = iterable;
    }

    public static <E> FluentIterable<E> from(final Iterable<E> iterable) {
        return (iterable instanceof FluentIterable) ? (FluentIterable<E>) iterable
                : new FluentIterable<E>(iterable) {
            @Override
            public Iterator<E> iterator() {
                return iterable.iterator();
            }
        };
    }

    public static <E> FluentIterable<E> of(E[] elements) {
        List<E> list = new ArrayList<>();
        Collections.addAll(list, elements);
        return from(list);
    }

    public static int size(Iterable<?> iterable) {
        return (iterable instanceof Collection)
                ? ((Collection<?>) iterable).size()
                : size(iterable.iterator());
    }

    public static int size(Iterator<?> iterator) {
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    public static <T> T get(Iterable<T> iterable, int position) {
        return (iterable instanceof List)
                ? ((List<T>) iterable).get(position)
                : get(iterable.iterator(), position);
    }

    public static <T> T get(Iterator<T> iterator, int position) {
        int skipped = advance(iterator, position);
        if (!iterator.hasNext()) {
            throw new IndexOutOfBoundsException("position (" + position
                    + ") must be less than the number of elements that remained ("
                    + skipped + ")");
        }
        return iterator.next();
    }

    public static int advance(Iterator<?> iterator, int numberToAdvance) {
        int i;
        for (i = 0; i < numberToAdvance && iterator.hasNext(); i++) {
            iterator.next();
        }
        return i;
    }

    public static <T> Iterable<T> limit(final Iterable<T> iterable, final int limitSize) {
        return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return limit(iterable.iterator(), limitSize);
            }
        };
    }

    public static <T> Iterator<T> limit(final Iterator<T> iterator, final int limitSize) {
        return new Iterator<T>() {
            private int count;

            @Override
            public boolean hasNext() {
                return count < limitSize && iterator.hasNext();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                count++;
                return iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    public static <T> Iterable<T> skip(final Iterable<T> iterable, final int numberToSkip) {
        if (iterable instanceof List) {
            final List<T> list = (List<T>) iterable;
            return new FluentIterable<T>() {
                @Override
                public Iterator<T> iterator() {
                    int toSkip = Math.min(list.size(), numberToSkip);
                    return list.subList(toSkip, list.size()).iterator();
                }
            };
        }
        return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                final Iterator<T> iterator = iterable.iterator();

                advance(iterator, numberToSkip);

                return new Iterator<T>() {
                    boolean atStart = true;

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public T next() {
                        T result = iterator.next();
                        atStart = false; // not called if next() fails
                        return result;
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }
                };
            }
        };
    }

    public static <T> T[] toArray(Iterable<? extends T> iterable, Class<T> type) {
        Collection<? extends T> collection = toCollection(iterable);
        T[] array = newArray(type, collection.size());
        return collection.toArray(array);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(Class<T> type, int length) {
        return (T[]) Array.newInstance(type, length);
    }

    private static <E> Collection<E> toCollection(Iterable<E> iterable) {
        List<E> list = new ArrayList<>();
        for (E e : iterable) {
            list.add(e);
        }
        return (iterable instanceof Collection) ? (Collection<E>) iterable : list;
    }

    @Override
    public String toString() {
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(Objects::toString)
                .collect(Collectors.joining(", ", "{", "}"));
    }

    public final int size() {
        return size(iterable);
    }

    public final Optional<E> first() {
        Iterator<E> iterator = iterable.iterator();
        return iterator.hasNext()
                ? Optional.of(iterator.next())
                : Optional.<E>absent();
    }

    public final Optional<E> last() {

        if (iterable instanceof List) {
            List<E> list = (List<E>) iterable;
            if (list.isEmpty()) {
                return Optional.absent();
            }
            return Optional.of(list.get(list.size() - 1));
        }
        Iterator<E> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return Optional.absent();
        }

        if (iterable instanceof SortedSet) {
            SortedSet<E> sortedSet = (SortedSet<E>) iterable;
            return Optional.of(sortedSet.last());
        }

        while (true) {
            E current = iterator.next();
            if (!iterator.hasNext()) {
                return Optional.of(current);
            }
        }
    }

    public final FluentIterable<E> skip(int numberToSkip) {
        return from(skip(iterable, numberToSkip));
    }

    public final FluentIterable<E> limit(int size) {
        return from(limit(iterable, size));
    }

    public final boolean isEmpty() {
        return !iterable.iterator().hasNext();
    }

    public final E[] toArray(Class<E> type) {
        return toArray(iterable, type);
    }

    public final E get(int position) {
        return get(iterable, position);
    }

    public static <F, T> Iterable<T> transform(final Iterable<F> fromIterable,
                                               final Function<? super F, ? extends T> function) {
        return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return TransformedIterator.transform(fromIterable.iterator(), function);
            }
        };
    }

}
