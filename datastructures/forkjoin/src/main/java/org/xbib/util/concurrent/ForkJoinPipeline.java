/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.util.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
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

    private final static Logger logger = LogManager.getLogger(ForkJoinPipeline.class);

    private volatile Thread thread;

    private ExecutorService executorService;

    private BlockingQueue<R> queue;

    private Map<W,Future<R>> futures;

    private WorkerProvider<W> workerProvider;

    private CountDownLatch latch;

    private WorkerErrors<W> workerErrors;

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
    public ForkJoinPipeline<W,R> putQueue(R element) {
        if (latch.getCount() > 0) {
            try {
                queue.put(element);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("interrupted while putting element " + element, e);
            }
        }
        return this;
    }

    @Override
    public BlockingQueue<R> getQueue() {
        return queue;
    }

    @Override
    public ForkJoinPipeline<W,R> prepare() {
        if (workerProvider == null) {
            throw new IllegalArgumentException("no worker provider set");
        }
        if (queue == null) {
            this.queue = new SynchronousQueue<>(true);
        }
        if (thread == null) {
            thread = Thread.currentThread();
        }
        if (workerErrors == null) {
            workerErrors = new WorkerErrors<>();
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
        this.futures = new LinkedHashMap<>();
        for (int i = 0; i < workerCount; i++) {
            W worker = workerProvider.get(this);
            futures.put(worker, executorService.submit(worker));
        }
        this.latch = new CountDownLatch(workerCount);
        return this;
    }

    /**
     * Wait for all results of the executions.
     *
     * @return this pipeline
     */
    @Override
    public ForkJoinPipeline<W, R> waitFor(R poisonElement) throws IOException {
        if (executorService == null || futures == null || futures.isEmpty()) {
            return this;
        }
        // send poison elements
        poison(poisonElement);
        for (Map.Entry<W, Future<R>> entry : futures.entrySet()) {
            W worker = entry.getKey();
            try {
                Future<R> future = entry.getValue();
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("interrupted while getting future for worker " + worker);
            } catch (CancellationException e) {
                Thread.currentThread().interrupt();
                logger.error("cancelled while getting future for worker " + worker);
            } catch (ExecutionException e) {
                logger.error("execution error in worker " + worker, e);
            }
        }
        return this;
    }

    protected void poison(R poisonElement) throws IOException {
        if (closed) {
            return;
        }
        try {
            if (workerCount > 0) {
                for (int i = 0; i < workerCount; i++) {
                    if (latch != null && latch.getCount() > 0) {
                        queue.put(poisonElement);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("interrupted while sending poison elements");
        }
        closed = true;
    }

    @Override
    public void shutdown() throws IOException {
        if (executorService == null) {
            return;
        }
        try {
            if (latch != null && latch.getCount() > 0) {
                logger.info("waiting for termination");
                executorService.awaitTermination(60L, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("interrupted while waiting for termination");
        }
        logger.info("shutdown");
        executorService.shutdown();
    }

    /**
     * Get the pipelines of this executor.
     * @return the pipelines
     */
    @Override
    public Collection<W> getWorkers() {
        return futures.keySet();
    }

    /**
     * Get the collected I/O exceptions that were thrown by the pipelines.
     * @return list of exceptions
     */
    @Override
    public WorkerErrors<W> getWorkerErrors() {
        return workerErrors;
    }

    /**
     * This worker has quit.
     * Called from a worker when it terminates.
     */
    public void quit(W worker) {
        latch.countDown();
    }

    public void quit(W worker, Throwable throwable) {
        logger.info("quit after exception", throwable);
        latch.countDown();
        workerErrors.add(worker, throwable);
        futures.get(worker).cancel(true);
        if (!thread.isInterrupted()) {
            logger.info("interrupting");
            thread.interrupt();
        }
    }

}
