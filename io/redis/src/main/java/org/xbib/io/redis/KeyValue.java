package org.xbib.io.redis;

/**
 * A key-value pair.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class KeyValue<K, V> {

    public final K key;
    public final V value;

    /**
     * @param key   the key
     * @param value the value
     */
    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KeyValue<?, ?> that = (KeyValue<?, ?>) o;
        return key.equals(that.key) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return 31 * key.hashCode() + value.hashCode();
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", key, value);
    }
}
