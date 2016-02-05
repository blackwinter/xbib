package org.xbib.io.redis;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.SettableFuture;

import java.util.Set;

/**
 * Close Events Facility. Can register/unregister CloseListener and fire a closed event to all registered listeners.
 */
public class ConnectionEvents {
    private final Set<RedisConnectionStateListener> listeners = Sets.newConcurrentHashSet();

    protected void fireEventRedisConnected(RedisChannelHandler<?, ?> connection) {
        for (RedisConnectionStateListener listener : listeners) {
            listener.onRedisConnected(connection);
        }
    }

    protected void fireEventRedisDisconnected(RedisChannelHandler<?, ?> connection) {
        for (RedisConnectionStateListener listener : listeners) {
            listener.onRedisDisconnected(connection);
        }
    }

    protected void fireEventRedisExceptionCaught(RedisChannelHandler<?, ?> connection, Throwable cause) {
        for (RedisConnectionStateListener listener : listeners) {
            listener.onRedisExceptionCaught(connection, cause);
        }
    }

    public void addListener(RedisConnectionStateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(RedisConnectionStateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Event before a channel is closed.
     */
    public static class PrepareClose {
        private SettableFuture<Boolean> prepareCloseFuture = SettableFuture.create();

        public SettableFuture<Boolean> getPrepareCloseFuture() {
            return prepareCloseFuture;
        }
    }

    /**
     * Event when a channel is closed.
     */
    public static class Close {
    }

    /**
     * Event when a channel is activated.
     */
    public static class Activated {
    }

}
