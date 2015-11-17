package org.xbib.io.redis;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.TimeUnit;

/**
 * Redis Future, extends a Listenable Future (Notification on Complete). The execution of the notification happens either on
 * finish of the future execution or, if the future is completed already, immediately.
 *
 * @param <V> Value type.
 */
public interface RedisFuture<V> extends ListenableFuture<V> {

    /**
     * @return error text, if any error occured.
     */
    String getError();

    /**
     * Wait up to the specified time for the command output to become available.
     *
     * @param timeout Maximum time to wait for a result.
     * @param unit    Unit of time for the timeout.
     * @return true if the output became available.
     */
    boolean await(long timeout, TimeUnit unit);
}
