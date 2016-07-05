package org.xbib.util.graph.persistent;

/**
 * Persistent integer to object map.
 *
 * @param <T> Type of values in the map. Must implement {@link Object#hashCode} and {@link Object#equals}.
 */
public interface IntMap<T> {
    /**
     * Checks if the map is empty.
     *
     * @return True if empty, false if not.
     */
    boolean isEmpty();

    /**
     * Checks if the map contains given key.
     *
     * @param key Key to check.
     * @return True if contained, false if not.
     */
    boolean contains(int key);

    /**
     * Gets value with given key.
     *
     * @param key Key to get value for.
     * @return Value for given key, or null if key does not exist.
     */
    T get(int key);

    /**
     * Gets the keys.
     *
     * @return Keys.
     */
    Iterable<Integer> keys();

    /**
     * Gets the values.
     *
     * @return Values.
     */
    Iterable<T> values();

    /**
     * Adds or replaces a mapping from given key to given value.
     *
     * @param key   Key to add a mapping for.
     * @param value Value to add a mapping to.
     * @return Map with mapping added.
     */
    IntMap<T> put(int key, T value);

    /**
     * Removes mapping with given key.
     *
     * @param key Key to remove mapping for.
     * @return Map with mapping removed.
     */
    IntMap<T> remove(int key);
}
