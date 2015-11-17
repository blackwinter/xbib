package org.xbib.io.redis.resource;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.redis.EpollProvider;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.xbib.io.redis.resource.Futures.toBooleanPromise;

/**
 * Default implementation which manages one event loop group instance per type.
 */
public class DefaultEventLoopGroupProvider implements EventLoopGroupProvider {

    protected static final Logger logger = LogManager.getLogger(DefaultEventLoopGroupProvider.class);

    private final Map<Class<? extends EventExecutorGroup>, EventExecutorGroup> eventLoopGroups = new ConcurrentHashMap<Class<? extends EventExecutorGroup>, EventExecutorGroup>();
    private final int numberOfThreads;

    private volatile boolean shutdownCalled = false;

    /**
     * Creates a new instance of {@link DefaultEventLoopGroupProvider}.
     *
     * @param numberOfThreads number of threads (pool size)
     */
    public DefaultEventLoopGroupProvider(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    /**
     * Create an instance of a {@link EventExecutorGroup}. Supported types are:
     * <ul>
     * <li>DefaultEventExecutorGroup</li>
     * <li>NioEventLoopGroup</li>
     * <li>EpollEventLoopGroup</li>
     * </ul>
     *
     * @param type            the type
     * @param numberOfThreads the number of threads to use for the {@link EventExecutorGroup}
     * @param <T>             type parameter
     * @return a new instance of a {@link EventExecutorGroup}
     * @throws IllegalArgumentException if the {@code type} is not supported.
     */
    public static <T extends EventExecutorGroup> EventExecutorGroup createEventLoopGroup(Class<T> type, int numberOfThreads) {
        if (DefaultEventExecutorGroup.class.equals(type)) {
            return new DefaultEventExecutorGroup(numberOfThreads, new DefaultThreadFactory("lettuce-eventExecutorLoop", true));
        }

        if (NioEventLoopGroup.class.equals(type)) {
            return new NioEventLoopGroup(numberOfThreads, new DefaultThreadFactory("lettuce-nioEventLoop", true));
        }

        if (EpollProvider.epollEventLoopGroupClass != null && EpollProvider.epollEventLoopGroupClass.equals(type)) {
            return EpollProvider.newEventLoopGroup(numberOfThreads, new DefaultThreadFactory("lettuce-epollEventLoop", true));
        }
        throw new IllegalArgumentException("Type " + type.getName() + " not supported");
    }

    @Override
    public <T extends EventLoopGroup> T allocate(Class<T> type) {
        synchronized (this) {
            return getOrCreate(type);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends EventLoopGroup> T getOrCreate(Class<T> type) {

        if (shutdownCalled) {
            throw new IllegalStateException("Provider is shut down and can not longer provide resources");
        }

        if (!eventLoopGroups.containsKey(type)) {
            eventLoopGroups.put(type, createEventLoopGroup(type, numberOfThreads));
        }

        return (T) eventLoopGroups.get(type);
    }

    @Override
    public Promise<Boolean> release(EventExecutorGroup eventLoopGroup, long quietPeriod, long timeout, TimeUnit unit) {

        Class<?> key = getKey(eventLoopGroup);

        if (key == null && eventLoopGroup.isShuttingDown()) {
            DefaultPromise<Boolean> promise = new DefaultPromise<Boolean>(GlobalEventExecutor.INSTANCE);
            promise.setSuccess(true);
            return promise;
        }

        if (key != null) {
            eventLoopGroups.remove(key);
        }

        Future<?> shutdownFuture = eventLoopGroup.shutdownGracefully(quietPeriod, timeout, unit);
        return toBooleanPromise(shutdownFuture);
    }

    private Class<?> getKey(EventExecutorGroup eventLoopGroup) {
        Class<?> key = null;

        Map<Class<? extends EventExecutorGroup>, EventExecutorGroup> copy = Maps.newHashMap(eventLoopGroups);
        for (Map.Entry<Class<? extends EventExecutorGroup>, EventExecutorGroup> entry : copy.entrySet()) {
            if (entry.getValue() == eventLoopGroup) {
                key = entry.getKey();
                break;
            }
        }
        return key;
    }

    @Override
    public int threadPoolSize() {
        return numberOfThreads;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Future<Boolean> shutdown(long quietPeriod, long timeout, TimeUnit timeUnit) {
        shutdownCalled = true;

        Map<Class<? extends EventExecutorGroup>, EventExecutorGroup> copy = Maps.newHashMap(eventLoopGroups);

        DefaultPromise<Boolean> overall = new DefaultPromise<Boolean>(GlobalEventExecutor.INSTANCE);
        DefaultPromise<Boolean> lastRelease = new DefaultPromise<Boolean>(GlobalEventExecutor.INSTANCE);
        Futures.PromiseAggregator<Boolean, Promise<Boolean>> aggregator = new Futures.PromiseAggregator<Boolean, Promise<Boolean>>(
                overall);

        aggregator.expectMore(1 + copy.size());

        aggregator.arm();

        for (EventExecutorGroup executorGroup : copy.values()) {
            Promise<Boolean> shutdown = toBooleanPromise(executorGroup.shutdownGracefully(quietPeriod, timeout, timeUnit));
            aggregator.add(shutdown);
        }

        aggregator.add(lastRelease);
        lastRelease.setSuccess(null);

        return toBooleanPromise(overall);
    }
}
