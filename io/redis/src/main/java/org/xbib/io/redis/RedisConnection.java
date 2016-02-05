package org.xbib.io.redis;

import java.util.concurrent.TimeUnit;

/**
 * Complete synchronous Redis API with 400+ Methods.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public interface RedisConnection<K, V> extends RedisHashesConnection<K, V>, RedisKeysConnection<K, V>,
        RedisStringsConnection<K, V>, RedisListsConnection<K, V>, RedisSetsConnection<K, V>, RedisSortedSetsConnection<K, V>,
        RedisScriptingConnection<K, V>, RedisServerConnection<K, V>, RedisHLLConnection<K, V>, RedisGeoConnection<K, V>,
        BaseRedisConnection<K, V>, RedisClusterConnection<K, V> {

    /**
     * Set the default timeout for operations.
     *
     * @param timeout the timeout value
     * @param unit    the unit of the timeout value
     */
    void setTimeout(long timeout, TimeUnit unit);

    /**
     * Authenticate to the server.
     *
     * @param password the password
     * @return String simple-string-reply
     */
    String auth(String password);

    /**
     * Change the selected database for the current connection.
     *
     * @param db the database number
     * @return String simple-string-reply
     */
    String select(int db);
}
