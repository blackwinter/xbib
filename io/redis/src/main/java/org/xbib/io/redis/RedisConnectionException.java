package org.xbib.io.redis;

/**
 * Exception for connection failures.
 */
@SuppressWarnings("serial")
public class RedisConnectionException extends RedisException {

    public RedisConnectionException(String msg) {
        super(msg);
    }

    public RedisConnectionException(String msg, Throwable e) {
        super(msg, e);
    }
}
