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

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;

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

    /**
     * Set the worker provider. With this method, all worker threads are created.
     * @param provider the provider
     * @return this pipeline
     */
    Pipeline<W,R> setWorkerProvider(WorkerProvider<W> provider);

    /**
     * Set the request queue. The queue is blocking.
     * @param queue the blocking request queue
     * @return this pipeline
     */
    Pipeline<W,R> setQueue(BlockingQueue<R> queue);

    /**
     * Get teh request queue.
     * @return the requests queue
     */
    BlockingQueue<R> getQueue();

    /**
     * Put element to request qeueu, but only if workers are still available.
     * @param element the element
     * @return this pipeline
     */
    Pipeline<W,R> putQueue(R element);

    /**
     * Prepare the workers' execution.
     * @return this setExecutor
     */
    Pipeline<W,R> prepare();

    /**
     * Execute the workers.
     * @return this setExecutor
     */
    Pipeline<W,R> execute();

    /**
     * Execute the workers.
     * @return this pipeline
     * @throws IOException
     */
    Pipeline<W,R> waitFor(R poison) throws IOException;

    /**
     * Shut down this pipeline.
     * @throws IOException
     */
    void shutdown() throws IOException;

    /**
     * Quit a worker.
     * @param worker the worker to be quit
     */
    void quit(W worker);

    /**
     * Quit a worker, interrupt pipeline thread and queue if necessary.
     * This will end the pipeline execution
     * @param worker the worker to be quit
     */
    void quit(W worker, Throwable throwable);

    /**
     * Return current active workers
     * @return the workers
     */
    Collection<W> getWorkers();

    /**
     * Return workers' errors.
     * @return the workers' errors
     */
    WorkerErrors<W> getWorkerErrors();
}
