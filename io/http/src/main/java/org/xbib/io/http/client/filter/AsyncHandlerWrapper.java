package org.xbib.io.http.client.filter;

import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.HttpResponseHeaders;
import org.xbib.io.http.client.HttpResponseStatus;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncHandlerWrapper<T> implements AsyncHandler<T> {

    private final AsyncHandler<T> asyncHandler;
    private final Semaphore available;
    private final AtomicBoolean complete = new AtomicBoolean(false);

    public AsyncHandlerWrapper(AsyncHandler<T> asyncHandler, Semaphore available) {
        this.asyncHandler = asyncHandler;
        this.available = available;
    }

    private void complete() {
        if (complete.compareAndSet(false, true)) {
            available.release();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onThrowable(Throwable t) {
        try {
            asyncHandler.onThrowable(t);
        } finally {
            complete();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        return asyncHandler.onBodyPartReceived(bodyPart);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
        return asyncHandler.onStatusReceived(responseStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State onHeadersReceived(HttpResponseHeaders headers) throws Exception {
        return asyncHandler.onHeadersReceived(headers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T onCompleted() throws Exception {
        try {
            return asyncHandler.onCompleted();
        } finally {
            complete();
        }
    }
}
