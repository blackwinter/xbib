package org.xbib.io.redis;

/**
 * Connection provider for redis connections.
 *
 * @param <T> Connection type.
 */
interface RedisConnectionProvider<T> {
    T createConnection();

    Class<? extends T> getComponentType();
}
