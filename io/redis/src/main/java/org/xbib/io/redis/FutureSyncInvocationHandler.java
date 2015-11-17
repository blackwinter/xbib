package org.xbib.io.redis;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.AbstractInvocationHandler;
import org.xbib.io.redis.protocol.Command;
import org.xbib.io.redis.protocol.RedisCommand;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Invocation-handler to synchronize API calls which use Futures as backend. This class leverages the need to implement a full
 * sync class which just delegates every request.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
class FutureSyncInvocationHandler<K, V> extends AbstractInvocationHandler {

    private final RedisChannelHandler<K, V> connection;
    protected long timeout;
    protected TimeUnit unit;
    private LoadingCache<Method, Method> methodCache;

    public FutureSyncInvocationHandler(final RedisChannelHandler<K, V> connection) {
        this.connection = connection;
        this.timeout = connection.timeout;
        this.unit = connection.unit;

        methodCache = CacheBuilder.newBuilder().build(new CacheLoader<Method, Method>() {
            @Override
            public Method load(Method key) throws Exception {
                return connection.getClass().getMethod(key.getName(), key.getParameterTypes());
            }
        });

    }

    /**
     * @see com.google.common.reflect.AbstractInvocationHandler#handleInvocation(Object, Method,
     * Object[])
     */
    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {

        try {

            // void setTimeout(long timeout, TimeUnit unit)
            if (method.getName().equals("setTimeout")) {
                setTimeout((Long) args[0], (TimeUnit) args[1]);
                return null;
            }

            Method targetMethod = methodCache.get(method);
            Object result = targetMethod.invoke(connection, args);

            if (result instanceof RedisCommand) {
                RedisCommand<?, ?, ?> redisCommand = (RedisCommand<?, ?, ?>) result;
                if (!method.getName().equals("exec") && !method.getName().equals("multi")) {
                    if (connection instanceof RedisAsyncConnectionImpl && ((RedisAsyncConnectionImpl) connection).isMulti()) {
                        return null;
                    }
                }

                Object awaitedResult = LettuceFutures.awaitOrCancel(redisCommand, timeout, unit);

                if (redisCommand instanceof Command) {
                    Command<?, ?, ?> command = (Command<?, ?, ?>) redisCommand;
                    if (command.getException() != null) {
                        throw new RedisException(command.getException());
                    }
                }

                if (redisCommand instanceof Future<?>) {
                    if (redisCommand.isDone()) {
                        try {
                            redisCommand.get();
                        } catch (InterruptedException e) {
                            throw e;
                        } catch (ExecutionException e) {
                            throw new RedisException(e.getCause());
                        }
                    }
                }

                return awaitedResult;
            }

            if (result instanceof RedisClusterAsyncConnection) {
                return AbstractRedisClient.syncHandler((RedisChannelHandler<?, ?>) result, RedisConnection.class,
                        RedisClusterConnection.class);
            }

            return result;

        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

    }

    private void setTimeout(long timeout, TimeUnit unit) {
        this.timeout = timeout;
        this.unit = unit;
    }
}
