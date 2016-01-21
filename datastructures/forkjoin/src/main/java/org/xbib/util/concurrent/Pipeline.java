
package org.xbib.util.concurrent;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 * A pipeline
 *
 * @param <W> the worker type
 * @param <R> the request type
 */
public interface Pipeline<W extends Worker<Pipeline<W,R>, R>, R extends WorkerRequest> {

    /**
     * Set the concurrency of this pipeline setExecutor
     * @param concurrency the concurrency, must be a positive integer
     * @return this setExecutor
     */
    Pipeline<W,R> setConcurrency(int concurrency);

    Pipeline<W,R> setQueue(BlockingQueue<R> queue);

    Pipeline<W,R> setWorkerProvider(WorkerProvider<W> provider);

    BlockingQueue<R> getQueue();

    /**
     * Prepare the pipeline execution.
     * @return this setExecutor
     */
    Pipeline<W,R> prepare();

    /**
     * Execute the pipelines.
     * @return this setExecutor
     */
    Pipeline<W,R> execute();

    /**
     * Execute the pipelines.
     * @return this setExecutor
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    Pipeline<W,R> waitFor(R poison) throws InterruptedException, ExecutionException, IOException;

    /**
     * Shut down this pipeline executor.
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    void shutdown() throws InterruptedException, ExecutionException, IOException;

    /**
     * Return pipelines
     * @return the pipelines
     */
    Collection<W> getWorkers();
}
