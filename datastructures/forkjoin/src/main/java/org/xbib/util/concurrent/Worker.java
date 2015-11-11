package org.xbib.util.concurrent;

import org.xbib.metric.MeterMetric;

import java.io.Closeable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * A worker.
 *
 * @param <R> the request type for the worker
 */
public interface Worker<R extends WorkerRequest> extends Callable<R>, Closeable {

    Worker<R> setQueue(BlockingQueue<R> queue);

    BlockingQueue<R> getQueue();

    MeterMetric getMetric();
}
