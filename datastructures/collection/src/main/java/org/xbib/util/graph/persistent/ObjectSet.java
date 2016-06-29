package org.xbib.util.graph.persistent;

import java.util.Collection;

/**
 * Persistent object set.
 *
 * @param <T> Type of values in the set. Must implement {@link Object#hashCode} and {@link Object#equals}.
 */
public interface ObjectSet<T> extends Iterable<T> {
    /**
     * Checks if the set is empty.
     *
     * @return True if empty, false if not.
     */
    boolean isEmpty();

    /**
     * Checks if set contains given value.
     *
     * @param value Value to check.
     * @return True if contained, false if not.
     */
    boolean contains(T value);

    /**
     * Adds given value to the set. Adding a value that already exist does not change the set.
     *
     * @param value Value to add.
     * @return Set with value added.
     */
    ObjectSet<T> add(T value);

    /**
     * Adds given values to the set. Adding values that already exist does not change the set.
     *
     * @param values Values to add.
     * @return Set with values added.
     */
    ObjectSet<T> addAll(Collection<? extends T> values);

    /**
     * Removes given value from the set. Removing values that do not exist in the set do not change the set.
     *
     * @param value Value to remove.
     * @return Set with value removed.
     */
    ObjectSet<T> remove(T value);
}
