package org.xbib.util.persistent;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Optional<T> {
    Optional() {
    }

    public static <T> Optional<T> absent() {
        return Absent.withType();
    }

    public static <T> Optional<T> of(T reference) {
        return new Present<T>(reference);
    }

    public static <T> Optional<T> fromNullable(T nullableReference) {
        return (nullableReference == null)
                ? Optional.<T>absent()
                : new Present<T>(nullableReference);
    }

    public abstract boolean isPresent();

    public abstract T get();


    public abstract T or(T defaultValue);

    public abstract Optional<T> or(Optional<? extends T> secondChoice);

    public abstract T or(Supplier<? extends T> supplier);

    public abstract T orNull();

    public abstract Set<T> asSet();

    public abstract <V> Optional<V> transform(Function<? super T, V> function);

    @Override
    public abstract boolean equals(Object object);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();

}
