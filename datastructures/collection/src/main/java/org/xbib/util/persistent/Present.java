package org.xbib.util.persistent;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

final class Present<T> extends Optional<T> {
    private final T reference;

    Present(T reference) {
        this.reference = reference;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public T get() {
        return reference;
    }

    @Override
    public T or(T defaultValue) {
        return reference;
    }

    @Override
    public Optional<T> or(Optional<? extends T> secondChoice) {
        return this;
    }

    @Override
    public T or(Supplier<? extends T> supplier) {
        return reference;
    }

    @Override
    public T orNull() {
        return reference;
    }

    @Override
    public Set<T> asSet() {
        return Collections.singleton(reference);
    }

    @Override
    public <V> Optional<V> transform(Function<? super T, V> function) {
        return new Present<V>(function.apply(reference));
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Present) {
            Present<?> other = (Present<?>) object;
            return reference.equals(other.reference);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 0x598df91c + reference.hashCode();
    }

    @Override
    public String toString() {
        return "Optional.of(" + reference + ")";
    }
}
