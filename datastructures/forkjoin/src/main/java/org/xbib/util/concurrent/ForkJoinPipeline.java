
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
 * @param <W> the worker type
 */
public class ForkJoinPipeline<W extends Worker<Pipeline<W,R>, R>, R extends WorkerRequest>
    implements Pipeline<W,R> {

    private ExecutorService executorService;

    private BlockingQueue<R> queue;

    private Collection<W> workers;

    private Collection<Future<R>> futures;

    private WorkerProvider<W> workerProvider;

    private CountDownLatch latch;

    private Sink<R> sink;

    private List<Throwable> exceptions;

    private int workerCount;

    private volatile boolean closed;

    @Override
    public ForkJoinPipeline<W, R> setConcurrency(int concurrency) {
        this.workerCount = concurrency;
        return this;
    }

    @Override
    public ForkJoinPipeline<W, R> setWorkerProvider(WorkerProvider<W> workerProvider) {
        this.workerProvider = workerProvider;
        return this;
    }

    @Override
    public ForkJoinPipeline<W, R> setQueue(BlockingQueue<R> queue) {
        this.queue = queue;
        return this;
    }

    @Override
    public BlockingQueue<R> getQueue() {
        return queue;
    }

    @Override
    public ForkJoinPipeline<W,R> setSink(Sink<R> sink) {
        this.sink = sink;
        return this;
    }

    @Override
    public ForkJoinPipeline<W,R> prepare() {
        if (workerProvider == null) {
            throw new IllegalArgumentException("no worker provider set");
        }
        if (queue == null) {
            this.queue = new SynchronousQueue<>(true);
        }
        if (executorService == null) {
            if (workerCount < 1) {
                workerCount = 1;
            }
            this.workerCount = Math.min(workerCount, 256);
            this.executorService = Executors.newFixedThreadPool(workerCount, runnable -> {
                Thread t = Executors.defaultThreadFactory().newThread(runnable);
                t.setDaemon(true);
                return t;
            });
        }
        return this;
    }

    @Override
    public ForkJoinPipeline<W,R> execute() {
        if (executorService == null) {
            throw new IllegalStateException("no executor service");
        }
        if (workerCount == 0) {
            throw new IllegalStateException("no workers to create");
        }
        workers = new LinkedList<>();
        futures = new LinkedList<>();
        for (int i = 0; i < workerCount; i++) {
            W worker = workerProvider.get(this);
            workers.add(worker);
            futures.add(executorService.submit(worker));
        }
        this.latch = new CountDownLatch(workerCount);
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
    public ForkJoinPipeline<W, R> waitFor(R poisonElement)
            throws InterruptedException, ExecutionException, IOException {
        if (executorService == null || futures == null || futures.isEmpty()) {
            return this;
        }
        // send poison
        poison(poisonElement);
        exceptions = new LinkedList<>();
        for (Future<R> future : futures) {
            try {
                R r = future.get();
                if (sink != null && !future.isCancelled()) {
                    sink.sink(r);
                }
            } catch (Throwable e) {
                exceptions.add(e);
                latch.countDown();
            }
        }
        return this;
    }

    protected void poison(R poisonElement) throws InterruptedException, ExecutionException, IOException {
        if (closed) {
            return;
        }
        for (int i = 0; i < workerCount; i++) {
            queue.put(poisonElement);
        }
        closed = true;
    }

    @Override
    public void shutdown() throws InterruptedException, IOException {
        if (executorService == null) {
            return;
        }
        executorService.shutdown();
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                throw new IOException("pool did not terminate");
            }
        }
    }

    /**
     * Get the pipelines of this executor.
     * @return the pipelines
     */
    @Override
    public Collection<W> getWorkers() {
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
    public ForkJoinPipeline<W,R> countDown() {
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
