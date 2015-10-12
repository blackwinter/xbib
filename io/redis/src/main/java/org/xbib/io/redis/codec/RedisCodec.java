package org.xbib.io.redis.codec;

import java.nio.ByteBuffer;

/**
 * A {@link RedisCodec} encodes keys and values sent to Redis, and decodes keys and values in the command output.
 * <p>
 * The methods are called by multiple threads and must be thread-safe.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public abstract class RedisCodec<K, V> {
    /**
     * Decode the key output by redis.
     *
     * @param bytes Raw bytes of the key.
     * @return The decoded key.
     */
    public abstract K decodeKey(ByteBuffer bytes);

    /**
     * Decode the value output by redis.
     *
     * @param bytes Raw bytes of the value.
     * @return The decoded value.
     */
    public abstract V decodeValue(ByteBuffer bytes);

    /**
     * Encode the key for output to redis.
     *
     * @param key Key.
     * @return The encoded key.
     */
    public abstract byte[] encodeKey(K key);

    /**
     * Encode the value for output to redis.
     *
     * @param value Value.
     * @return The encoded value.
     */
    public abstract byte[] encodeValue(V value);
}
