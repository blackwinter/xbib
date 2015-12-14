package org.xbib.io.http.client.netty;

public abstract class Callback {

    protected final NettyResponseFuture<?> future;

    public Callback(NettyResponseFuture<?> future) {
        this.future = future;
    }

    abstract public void call() throws Exception;

    public NettyResponseFuture<?> future() {
        return future;
    }
}
