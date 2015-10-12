package org.xbib.io.redis;

/**
 * Synchronous executed commands for HyperLogLog (PF* commands).
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public interface RedisHLLConnection<K, V> {
    /**
     * Adds the specified elements to the specified HyperLogLog.
     *
     * @param key        the key
     * @param value      the value
     * @param moreValues more values
     * @return Long integer-reply specifically:
     * <p>
     * 1 if at least 1 HyperLogLog internal register was altered. 0 otherwise.
     */
    Long pfadd(K key, V value, V... moreValues);

    /**
     * Merge N different HyperLogLogs into a single one.
     *
     * @param destkey        the destination key
     * @param sourcekey      the source key
     * @param moreSourceKeys more source keys
     * @return Long simple-string-reply The command just returns {@code OK}.
     */
    Long pfmerge(K destkey, K sourcekey, K... moreSourceKeys);

    /**
     * Return the approximated cardinality of the set(s) observed by the HyperLogLog at key(s).
     *
     * @param key      the key
     * @param moreKeys more keys
     * @return Long integer-reply specifically:
     * <p>
     * The approximated number of unique elements observed via {@code PFADD}.
     */
    Long pfcount(K key, K... moreKeys);

}
