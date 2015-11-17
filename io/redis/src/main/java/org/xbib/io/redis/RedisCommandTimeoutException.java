package org.xbib.io.redis;

/**
 * Exception thrown when the command waiting timeout is exceeded.
 */
@SuppressWarnings("serial")
public class RedisCommandTimeoutException extends RedisException {

    public RedisCommandTimeoutException() {
        super("Command timed out");
    }
}
