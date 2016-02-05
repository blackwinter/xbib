package org.xbib.io.redis.resource;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Utility class to support netty's future handling.
 */
class Futures {

    /**
     * Create a promise that emits a {@code Boolean} value on completion of the {@code future}
     *
     * @param future the future.
     * @return Promise emitting a {@code Boolean} value. {@literal true} if the {@code future} completed successfully, otherwise
     * the cause wil be transported.
     */
    static Promise<Boolean> toBooleanPromise(Future<?> future) {
        final DefaultPromise<Boolean> result = new DefaultPromise<Boolean>(GlobalEventExecutor.INSTANCE);

        future.addListener(new GenericFutureListener<Future<Object>>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {

                if (future.isSuccess()) {
                    result.setSuccess(true);
                } else {
                    result.setFailure(future.cause());
                }
            }
        });
        return result;
    }

    /**
     * Promise aggregator that aggregates multiple promises into one {@link Promise}. The aggregator workflow is:
     * <ol>
     * <li>Create a new instance of {@link PromiseAggregator}</li>
     * <li>Call {@link #expectMore(int)} until the number of expected futures is reached</li>
     * <li>Arm the aggregator using {@link #arm()}</li>
     * <li>Add the number of futures using {@link #add(Promise[])} until the expectation is met. The added futures can be either
     * done or in progress.</li>
     * <li>The {@code aggregatePromise} is released/finished as soon as the last future/promise completes</li>
     * <p>
     * </ol>
     *
     * @param <V> Result value type
     * @param <F> Future type
     */
    static class PromiseAggregator<V, F extends Future<V>> implements GenericFutureListener<F> {

        private final Promise<?> aggregatePromise;
        private Set<Promise<V>> pendingPromises;
        private AtomicInteger expectedPromises = new AtomicInteger();
        private AtomicInteger processedPromises = new AtomicInteger();
        private boolean armed;

        /**
         * Creates a new instance.
         *
         * @param aggregatePromise the {@link Promise} to notify
         */
        public PromiseAggregator(Promise<V> aggregatePromise) {
            checkArgument(aggregatePromise != null, "aggregatePromise must not be null");
            this.aggregatePromise = aggregatePromise;
        }

        /**
         * Add the number of {@code count} to the count of expected promises.
         *
         * @param count number of futures/promises, that is added to the overall expectation count.
         * @throws IllegalStateException if the aggregator was armed
         */
        public void expectMore(int count) {
            checkState(!armed, "Aggregator is armed and does not allow any further expectations");

            expectedPromises.addAndGet(count);
        }

        /**
         * Arm the aggregator to expect completion of the futures.
         *
         * @throws IllegalStateException if the aggregator was armed
         */
        public void arm() {
            checkState(!armed, "Aggregator is already armed");
            armed = true;
        }

        /**
         * Add the given {@link Promise}s to the aggregator.
         *
         * @param promises the promises
         * @throws IllegalStateException if the aggregator was not armed
         */
        @SafeVarargs
        public final PromiseAggregator<V, F> add(Promise<V>... promises) {

            checkArgument(promises != null, "promises must not be null");
            checkState(armed, "Aggregator is not armed and does not allow adding promises in that state. Call arm() first.");

            if (promises.length == 0) {
                return this;
            }
            synchronized (this) {
                if (pendingPromises == null) {
                    int size;
                    if (promises.length > 1) {
                        size = promises.length;
                    } else {
                        size = 2;
                    }
                    pendingPromises = new LinkedHashSet<Promise<V>>(size);
                }
                for (Promise<V> p : promises) {
                    if (p == null) {
                        continue;
                    }
                    pendingPromises.add(p);
                    p.addListener(this);
                }
            }
            return this;
        }

        @Override
        public synchronized void operationComplete(F future) throws Exception {
            if (pendingPromises == null) {
                aggregatePromise.setSuccess(null);
            } else {
                pendingPromises.remove(future);
                processedPromises.incrementAndGet();
                if (!future.isSuccess()) {
                    Throwable cause = future.cause();
                    aggregatePromise.setFailure(cause);
                    for (Promise<V> pendingFuture : pendingPromises) {
                        pendingFuture.setFailure(cause);
                    }
                } else if (processedPromises.get() == expectedPromises.get()) {
                    if (pendingPromises.isEmpty()) {
                        aggregatePromise.setSuccess(null);
                    } else {
                        throw new IllegalStateException(
                                "Processed promises == expected promises but pending promises is not empty. This should not have happened!");
                    }
                }
            }
        }
    }
}
