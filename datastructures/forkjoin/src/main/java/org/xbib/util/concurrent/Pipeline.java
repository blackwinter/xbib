
package org.xbib.util.concurrent;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 *
 * @param <R> the request type
 * @param <W> the worker type
 */
public interface Pipeline<R extends WorkerRequest, W extends Worker<R>> {

    /**
     * Set the concurrency of this pipeline setExecutor
     * @param concurrency the concurrency, must be a positive integer
     * @return this setExecutor
     */
    Pipeline<R, W> setConcurrency(int concurrency);

    /**
     * Set the provider of this pipeline setExecutor
     * @param provider the pipeline provider
     * @return this setExecutor
     */
    Pipeline<R, W> setProvider(WorkerProvider<W> provider);

    Pipeline<R, W> setQueue(BlockingQueue<R> queue);

    BlockingQueue<R> getQueue();

    /**
     * Set pipeline sink
     * @param sink the pipeline sink
     * @return this setExecutor
     */
    Pipeline<R, W> setSink(Sink<R> sink);

    /**
     * Prepare the pipeline execution.
     * @return this setExecutor
     */
    Pipeline<R, W> prepare();

    /**
     * Execute the pipelines.
     * @return this setExecutor
     */
    Pipeline<R, W> execute();

    /**
     * Execute the pipelines.
     * @return this setExecutor
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    Pipeline<R, W> waitFor() throws InterruptedException, ExecutionException, IOException;

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
    Collection<Worker<R>> getWorkers();
}
