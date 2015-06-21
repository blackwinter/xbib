package org.xbib.pipeline;

import java.io.Closeable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * A pipeline.
 *
 * @param <T> the pipeline result type
 * @param <R> the pipeline request type
 */
public interface Pipeline<T,R extends PipelineRequest>
        extends Callable<T>, Closeable {

    Pipeline<T,R> setQueue(BlockingQueue<R> queue);
}
