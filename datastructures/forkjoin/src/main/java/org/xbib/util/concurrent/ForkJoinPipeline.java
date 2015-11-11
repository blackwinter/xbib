
package org.xbib.util.concurrent;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * A simple fork/join pipeline.
 *
 * @param <R> the request type
 * @param <W> the worker type
 */
public class ForkJoinPipeline<R extends WorkerRequest, W extends Worker<R>>
    implements Pipeline<R, W> {

    private ExecutorService executorService;

    private BlockingQueue<R> queue;

    private Collection<Worker<R>> workers;

    private Collection<Future<R>> futures;

    private WorkerProvider<W> provider;

    private CountDownLatch latch;

    private Sink<R> sink;

    private List<Throwable> exceptions;

    private int workerCount;

    private volatile boolean closed;

    @Override
    public ForkJoinPipeline<R, W> setConcurrency(int concurrency) {
        this.workerCount = concurrency;
        return this;
    }

    @Override
    public ForkJoinPipeline<R, W> setProvider(WorkerProvider<W> provider) {
        this.provider = provider;
        return this;
    }

    @Override
    public ForkJoinPipeline<R, W> setQueue(BlockingQueue<R> queue) {
        this.queue = queue;
        return this;
    }

    public BlockingQueue<R> getQueue() {
        return queue;
    }

    @Override
    public ForkJoinPipeline<R, W> setSink(Sink<R> sink) {
        this.sink = sink;
        return this;
    }

    @Override
    public ForkJoinPipeline<R, W> prepare() {
        if (provider == null) {
            throw new IllegalStateException("no provider set");
        }
        if (executorService == null) {
            this.executorService = Executors.newFixedThreadPool(workerCount);
        }
        if (queue == null) {
            this.queue = new SynchronousQueue<>(true);
        }
        if (workers == null) {
            this.workers = new LinkedList<>();
        }
        if (workerCount < 1) {
            workerCount = 1;
        }
        workerCount = Math.min(workerCount, 256);
        this.latch = new CountDownLatch(workerCount);
        for (int i = 0; i < workerCount; i++) {
            workers.add(provider.get().setQueue(queue));
        }
        return this;
    }

    @Override
    public ForkJoinPipeline<R, W> execute() {
        if (workers == null || workers.isEmpty()) {
            throw new IllegalStateException("no workers");
        }
        futures = new LinkedList<>();
        for (Worker<R> worker : workers) {
            futures.add(executorService.submit(worker));
        }
        return this;
    }

    /**
     * Wait for all results of the executions.
     *
     * @return this pipeline
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Override
    public ForkJoinPipeline<R, W> waitFor()
            throws InterruptedException, ExecutionException {
        if (executorService == null || workers == null || futures == null || futures.isEmpty()) {
            return this;
        }
        exceptions = new LinkedList<Throwable>();
        for (Future<R> future : futures) {
            try {
                R r = future.get();
                if (sink != null && !future.isCancelled()) {
                    sink.sink(r);
                }
            } catch (Throwable e) {
                exceptions.add(e);
            }
        }
        return this;
    }

    @Override
    public void shutdown() throws InterruptedException, IOException {
        if (executorService == null) {
            return;
        }
        executorService.shutdown();
        if (!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
            if (!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
                throw new IOException("executor service did not terminate");
            }
        }
    }

    public void shutdown(R poisonElement) throws InterruptedException, ExecutionException, IOException {
        if (closed) {
            return;
        }
        closed = true;
        for (int i = 0; i < workers.size(); i++) {
            queue.offer(poisonElement);
        }
        waitFor();
        shutdown();
    }

    /**
     * Get the pipelines of this executor.
     * @return the pipelines
     */
    @Override
    public Collection<Worker<R>> getWorkers() {
        return workers;
    }

    /**
     * Get the collected I/O exceptions that were thrown by the pipelines.
     * @return list of exceptions
     */
    public List<Throwable> getExceptions() {
        return exceptions;
    }

    /**
     * Count down the latch. Decreases the number of active workers.
     * Called from a worker when it terminates.
     * @return this pipeline
     */
    public ForkJoinPipeline<R, W> countDown() {
        latch.countDown();
        return this;
    }

    /**
     * Returns the number of workers.  If this pipeline can process requests,
     * the returned number is greater than 0.
     * @return number of workers ready to receive requests
     */
    public long canReceive() {
        return latch.getCount();
    }


}
