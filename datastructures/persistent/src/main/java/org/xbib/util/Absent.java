package org.xbib.util;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

final class Absent<T> extends Optional<T> {
    static final Absent<Object> INSTANCE = new Absent<>();

    private Absent() {
    }

    @SuppressWarnings("unchecked")
    static <T> Optional<T> withType() {
        return (Optional<T>) INSTANCE;
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public T get() {
        throw new IllegalStateException("Optional.get() cannot be called on an absent value");
    }

    @Override
    public T or(T defaultValue) {
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<T> or(Optional<? extends T> secondChoice) {
        return (Optional<T>) secondChoice;
    }

    @Override
    public T or(Supplier<? extends T> supplier) {
        return supplier.get();
    }

    @Override
    public T orNull() {
        return null;
    }

    @Override
    public Set<T> asSet() {
        return Collections.emptySet();
    }

    @Override
    public <V> Optional<V> transform(Function<? super T, V> function) {
        return Optional.absent();
    }

    @Override
    public boolean equals(Object object) {
        return object == this;
    }

    @Override
    public int hashCode() {
        return 0x598df91c;
    }

    @Override
    public String toString() {
        return "Optional.absent()";
    }

}
