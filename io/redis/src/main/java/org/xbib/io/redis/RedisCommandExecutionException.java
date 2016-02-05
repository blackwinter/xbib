package org.xbib.io.redis;

/**
 * Exception for errors states reported by Redis.
 */
@SuppressWarnings("serial")
public class RedisCommandExecutionException extends RedisException {

    public RedisCommandExecutionException(String msg) {
        super(msg);
    }
}
