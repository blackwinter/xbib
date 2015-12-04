package org.xbib.util.concurrent;

import org.xbib.metric.MeterMetric;

import java.io.Closeable;
import java.util.concurrent.Callable;

/**
 * A worker.
 *
 * @param <P> the pipeline type
 * @param <R> the request type for the worker
 */
public interface Worker<P extends Pipeline, R extends WorkerRequest> extends Callable<R>, Closeable {

    Worker<P, R> setPipeline(P pipeline);

    P getPipeline();

    Worker<P, R> setMetric(MeterMetric metric);

    MeterMetric getMetric();
}
