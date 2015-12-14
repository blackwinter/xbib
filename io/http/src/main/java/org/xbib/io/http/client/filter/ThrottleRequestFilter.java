package org.xbib.io.http.client.filter;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A {@link RequestFilter} throttles requests and block when the number of permits is reached, waiting for
 * the response to arrives before executing the next request.
 */
public class ThrottleRequestFilter implements RequestFilter {
    private final Semaphore available;
    private final int maxWait;

    public ThrottleRequestFilter(int maxConnections) {
        this(maxConnections, Integer.MAX_VALUE);
    }

    public ThrottleRequestFilter(int maxConnections, int maxWait) {
        this(maxConnections, maxWait, false);
    }

    public ThrottleRequestFilter(int maxConnections, int maxWait, boolean fair) {
        this.maxWait = maxWait;
        available = new Semaphore(maxConnections, fair);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {
        try {
            if (!available.tryAcquire(maxWait, TimeUnit.MILLISECONDS)) {
                throw new FilterException(String.format("No slot available for processing Request %s with AsyncHandler %s",
                        ctx.getRequest(), ctx.getAsyncHandler()));
            }
        } catch (InterruptedException e) {
            throw new FilterException(String.format("Interrupted Request %s with AsyncHandler %s", ctx.getRequest(), ctx.getAsyncHandler()));
        }

        return new FilterContext.FilterContextBuilder<>(ctx).asyncHandler(new AsyncHandlerWrapper<>(ctx.getAsyncHandler(), available))
                .build();
    }
}