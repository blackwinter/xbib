package org.xbib.io.http.client.netty;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public abstract class SimpleGenericFutureListener<V> implements GenericFutureListener<Future<V>> {

    @Override
    public final void operationComplete(Future<V> future) throws Exception {
        if (future.isSuccess()) {
            onSuccess(future.get());
        } else {
            onFailure(future.cause());
        }
    }

    protected abstract void onSuccess(V value) throws Exception;

    protected abstract void onFailure(Throwable t) throws Exception;
}
