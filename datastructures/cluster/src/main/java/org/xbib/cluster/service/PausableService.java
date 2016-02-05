package org.xbib.cluster.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.OperationContext;

import java.util.ArrayDeque;

public abstract class PausableService<T extends Service> extends Service {
    private final static Logger logger = LogManager.getLogger(PausableService.class);

    private final ServiceContext<T> ctx;
    ArrayDeque<FutureRequest> objectQueue = new ArrayDeque<>();
    ArrayDeque<Runnable> runnableQueue = new ArrayDeque<>();
    private volatile boolean paused = false;

    public PausableService(ServiceContext<T> ctx) {
        this.ctx = ctx;
    }

    @Override
    public void handle(OperationContext ctx, Object object) {
        if (paused) {
            logger.debug("Queued message {} for paused service {}", object, this);
            objectQueue.add(new FutureRequest(ctx, object));
        } else {
            safelyHandle(ctx, object);
        }
    }

    public boolean addQueueIfPaused(Runnable run) {
        if (paused) {
            logger.trace("Queued runnable {} for paused service {}", run, this);
            runnableQueue.add(run);
            return true;
        }
        return false;
    }

    public ServiceContext<T> getContext() {
        return ctx;
    }

    public boolean isPaused() {
        return paused;
    }


    public void safelyHandle(OperationContext ctx, Object object) {
        logger.warn("Discarded message {} because the service doesn't implement handle(OperationContext, object)", object);
    }

    public synchronized void pause() {
        logger.debug("Paused service {}", this);
        paused = true;
    }

    public synchronized void resume() {
        logger.debug("Resumed service {}", this);
        objectQueue.forEach(x -> safelyHandle(x.context, x.request));
        runnableQueue.forEach(Runnable::run);
        paused = false;
    }

    public static class FutureRequest {
        OperationContext context;
        Object request;

        public FutureRequest(OperationContext context, Object request) {
            this.context = context;
            this.request = request;
        }
    }
}
