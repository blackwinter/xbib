package org.xbib.cluster.service;

import io.netty.util.concurrent.EventExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.OperationContext;
import org.xbib.cluster.Request;
import org.xbib.cluster.transport.ThrowableNioEventLoopGroup;

public abstract class Service {

    private final static Logger logger = LogManager.getLogger(Service.class);

    public void handle(OperationContext ctx, Object object) {
        logger.warn("Discarded message {} because the service doesn't implement handle(OperationContext, Object)", object);
    }

    public void handle(ThrowableNioEventLoopGroup executor, OperationContext ctx, Object object) {
        int id = ctx.serviceId() % executor.executorCount();
        EventExecutor child = executor.getChild(id);
        if (child.inEventLoop()) {
            try {
                handle(ctx, object);
            } catch (Exception e) {
                logger.error("error while running throwable code block", e);
                // TODO: should we reply or wait for timeout?
            }
        } else {
            child.execute(() -> handle(ctx, object));
        }
    }

    public void handle(ThrowableNioEventLoopGroup executor, OperationContext ctx, Request request) {
        int id = ctx.serviceId() % executor.executorCount();
        EventExecutor child = executor.getChild(id);
        if (child.inEventLoop()) {
            // no need to create new runnable, just execute the request since we're already in event thread.
            try {
                request.run(this, ctx);
            } catch (Exception e) {
                logger.error("error while running throwable code block", e);
                // TODO: should we reply or wait for timeout?
            }
        } else {
            child.execute(() -> request.run(this, ctx));
        }
    }

    public abstract void onClose();
}