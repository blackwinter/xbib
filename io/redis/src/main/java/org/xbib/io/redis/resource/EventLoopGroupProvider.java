package org.xbib.io.redis.resource;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;

import java.util.concurrent.TimeUnit;

/**
 * Provider for {@link EventExecutorGroup EventLoopGroups and EventExecutorGroups}. A event loop group is a heavy-weight
 * instance holding and providing {@link Thread} instances. Multiple instances can be created but are expensive. Keeping too
 * many instances open can exhaust the number of open files.
 * <p>
 * Usually, the default settings are sufficient. However, customizing might be useful for some special cases where multiple
 * RedisClient or RedisClusterClient instances are needed that share one or more event loop groups.
 * </p>
 * <p>
 * The {@link EventLoopGroupProvider} allows to allocate and release instances implementing {@link EventExecutorGroup}. The
 * {@link EventExecutorGroup} instances must not be terminated or shutdown by the user. Resources are managed by the particular
 * {@link EventLoopGroupProvider}.
 * </p>
 */
public interface EventLoopGroupProvider {

    /**
     * Retrieve a {@link EventLoopGroup} for the type {@code type}. Do not terminate or shutdown the instance. Call the
     * {@link #shutdown(long, long, TimeUnit)} method to free the resources.
     *
     * @param type type of the event loop group, must not be {@literal null}
     * @param <T>  type parameter
     * @return the {@link EventLoopGroup}.
     */
    <T extends EventLoopGroup> T allocate(Class<T> type);

    /**
     * Returns the pool size (number of threads) for IO threads. The indicated size does not reflect the number for all IO
     * threads, it is the number of threads that are used to create a particular thread pool.
     *
     * @return the pool size (number of threads) for all IO tasks.
     */
    int threadPoolSize();

    /**
     * Release the {@code eventLoopGroup} instance. The method will shutdown/terminate the event loop group if it is no longer
     * needed.
     *
     * @param eventLoopGroup the eventLoopGroup instance, must not be {@literal null}
     * @param quietPeriod    the quiet period
     * @param timeout        the timeout
     * @param unit           time unit for the quiet period/the timeout
     * @return a close future to synchronize the called for shutting down.
     */
    Future<Boolean> release(EventExecutorGroup eventLoopGroup, long quietPeriod, long timeout, TimeUnit unit);

    /**
     * Shutdown the provider and release all instances.
     *
     * @param quietPeriod the quiet period
     * @param timeout     the timeout
     * @param timeUnit    the unit of {@code quietPeriod} and {@code timeout}
     * @return a close future to synchronize the called for shutting down.
     */
    Future<Boolean> shutdown(long quietPeriod, long timeout, TimeUnit timeUnit);
}
