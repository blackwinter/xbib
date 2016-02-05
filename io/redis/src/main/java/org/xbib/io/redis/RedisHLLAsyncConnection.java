package org.xbib.io.redis;

/**
 * Asynchronous executed commands for HyperLogLog (PF* commands).
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public interface RedisHLLAsyncConnection<K, V> {
    /**
     * Adds the specified elements to the specified HyperLogLog.
     *
     * @param key        the key
     * @param value      the value
     * @param moreValues more values
     * @return RedisFuture&lt;Long&gt; integer-reply specifically:
     * <p>
     * 1 if at least 1 HyperLogLog internal register was altered. 0 otherwise.
     */
    RedisFuture<Long> pfadd(K key, V value, V... moreValues);

    /**
     * Merge N different HyperLogLogs into a single one.
     *
     * @param destkey        the destination key
     * @param sourcekey      the source key
     * @param moreSourceKeys more source keys
     * @return RedisFuture&lt;Long&gt; simple-string-reply The command just returns {@code OK}.
     */
    RedisFuture<Long> pfmerge(K destkey, K sourcekey, K... moreSourceKeys);

    /**
     * Return the approximated cardinality of the set(s) observed by the HyperLogLog at key(s).
     *
     * @param key      the key
     * @param moreKeys more keys
     * @return RedisFuture&lt;Long&gt; integer-reply specifically:
     * <p>
     * The approximated number of unique elements observed via {@code PFADD}.
     */
    RedisFuture<Long> pfcount(K key, K... moreKeys);

}
