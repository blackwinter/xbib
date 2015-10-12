
package org.xbib.pipeline;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * An Queue Pipeline executor. This executor can execute pipelines in parallel
 * and manage a queue that have to be processed by the pipelines.
 *
 * Pipelines are created by a pipeline provider.
 * The maximum number of concurrent pipelines is 256.
 * Each pipeline can receive archive entries, which are put into a blocking queue by
 * this setExecutor.
 *
 *@param <R> the pipeline request type
 * @param <P> the pipeline type
 */
public class QueuePipelineExecutor<R extends PipelineRequest, P extends Pipeline<R>>
     implements PipelineExecutor<R,P> {

    private ExecutorService executorService;

    private PipelineProvider<P> provider;

    private Set<Pipeline<R>> pipelines;

    private Collection<Future<R>> futures;

    private PipelineSink<R> sink;

    private CountDownLatch latch;

    private BlockingQueue<R> queue;

    private int concurrency;

    private volatile boolean closed;

    @Override
    public QueuePipelineExecutor<R,P> setConcurrency(int concurrency) {
        this.concurrency = concurrency;
        return this;
    }

    @Override
    public QueuePipelineExecutor<R,P> setPipelineProvider(PipelineProvider<P> provider) {
        this.provider = provider;
        return this;
    }

    @Override
    public QueuePipelineExecutor<R,P> setQueue(BlockingQueue<R> queue) {
        this.queue = queue;
        return this;
    }

    /**
     * Get queue
     * @return the queue
     */
    public BlockingQueue<R> getQueue() {
        return queue;
    }

    @Override
    public QueuePipelineExecutor<R,P> setSink(PipelineSink<R> sink) {
        this.sink = sink;
        return this;
    }

    @Override
    public QueuePipelineExecutor<R,P> prepare() {
        if (provider == null) {
            throw new IllegalArgumentException("no provider set");
        }
        if (executorService == null) {
            this.executorService = Executors.newFixedThreadPool(concurrency);
        }
        if (pipelines == null) {
            this.pipelines = new HashSet<>();
        }
        if (concurrency < 1) {
            concurrency = 1;
        }
        this.queue = new SynchronousQueue<>(true); // true means fair queue
        this.latch = new CountDownLatch(concurrency);
        for (int i = 0; i < Math.min(concurrency, 256); i++) {
            Pipeline<R> pipeline = provider.get();
            pipelines.add(pipeline);
        }
        return this;
    }

    /**
     * Usually, the pipelines receive elements from the setExecutor.
     * Here we ensure that all pipelines that receive elements are invoked.
     * This method returns immediately without waiting for the completion of pipelines.
     *
     * @return this setExecutor
     */
    @Override
    public QueuePipelineExecutor<R,P> execute() {
        futures = new LinkedList<>();
        for (Pipeline<R> pipeline : pipelines) {
            futures.add(executorService.submit(pipeline));
        }
        return this;
    }

    /**
     * Wait for all the pipelines executed.
     * @return this setExecutor
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public QueuePipelineExecutor<R,P> waitFor() throws InterruptedException, ExecutionException, IOException {
        for (Future<R> future : futures) {
            R r = future.get();
            if (sink != null)  {
                sink.sink(r);
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
                throw new IOException("pool did not terminate");
            }
        }
    }

    public void shutdown(R poisonElement) throws InterruptedException, ExecutionException, IOException {
        if (closed) {
            return;
        }
        closed = true;
        for (int i = 0; i < pipelines.size(); i++) {
            queue.offer(poisonElement);
        }
        waitFor();
        shutdown();
    }

    /**
     * Get pipelines
     * @return the pipelines
     */
    public Set<Pipeline<R>> getPipelines() {
        return pipelines;
    }

    /**
     * Count down the latch. Decreases the number of active pipelines.
     * Called from a pipeline when it terminates.
     * @return this setExecutor
     */
    public QueuePipelineExecutor countDown() {
        latch.countDown();
        return this;
    }

    /**
     * Returns the number of pipelines.  If this pipeline setExecutor can receive requests,
     * the returned number is greater than 0.
     * @return number of pipelines ready to receive requests
     */
    public long canReceive() {
        return latch.getCount();
    }

}