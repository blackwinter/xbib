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
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MockWorker extends AbstractWorker<Pipeline<MockWorker,URIWorkerRequest>,URIWorkerRequest> {

    private final static Logger logger = LogManager.getLogger(MockWorker.class.getName());

    private String config;

    private final static AtomicInteger count = new AtomicInteger();

    @SuppressWarnings("unchecked")
    public void bootstrap() throws Exception {
        try {
            config = "foobar";
            int concurrency = 2;
            logger.info("executing with concurrency {}", concurrency);
            ForkJoinPipeline pipeline = new ConfiguredPipeline()
                    .setQueue(new SynchronousQueue<>(true));
            setPipeline(pipeline);
            logger.info("preparing sink");
            prepareSink();
            getPipeline().setConcurrency(concurrency)
                    .setWorkerProvider(provider())
                    .prepare().execute();
            logger.info("push source");
            pushSource();
            getPipeline().waitFor(new URIWorkerRequest());
            logger.info("execution completed");
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        } finally {
            cleanup();
            if (getPipeline() != null) {
                getPipeline().shutdown();
                for (Worker worker : getPipeline().getWorkers()) {
                    logger.info(worker.getMetric());
                }
            }
        }
    }

    public Integer getCount() {
        return count.get();
    }

    @Override
    public MockWorker setPipeline(Pipeline<MockWorker,URIWorkerRequest> pipeline) {
        super.setPipeline(pipeline);
        logger.info("setPipeline: {}", pipeline.getClass());
        if (pipeline instanceof ConfiguredPipeline) {
            ConfiguredPipeline configuredPipeline = (ConfiguredPipeline)pipeline;
            config = configuredPipeline.getConfig();
        }
        return this;
    }

    protected void prepareSink() throws IOException {
    }

    @Override
    public void close() throws IOException {
        logger.info("worker close (no op)");
    }

    protected void pushSource() throws IOException, InterruptedException {
        logger.info("preparing input queue");
        Queue<URI> uris = new ConcurrentLinkedQueue<URI>() {{
            add(URI.create("a"));
            add(URI.create("b"));
            add(URI.create("c"));
        }};
        logger.info("input = {}", uris);
        for (URI uri : uris) {
            URIWorkerRequest element = new URIWorkerRequest();
            element.set(uri);
            getQueue().put(element);
        }
        logger.info("source prepared");
    }

    protected MockWorker cleanup() throws IOException {
        return this;
    }

    protected void process(URI uri) throws Exception {
        logger.info("start of processing {}", uri);
        logger.info("got config={}", config);
        // simlulate busy processing
        Thread.sleep(2000L);
        logger.info("end of processing {}", uri);
        count.incrementAndGet();
    }

    protected WorkerProvider provider() {
        return pipeline -> new MockWorker().setPipeline(pipeline);
    }

    @Override
    public void processRequest(Worker<Pipeline<MockWorker, URIWorkerRequest>, URIWorkerRequest> worker, URIWorkerRequest request) {
        try {
            URI uri = request.get();
            logger.info("new request for URI {}", uri);
            process(uri);
        } catch (Throwable ex) {
            logger.error(request.get() + ": error while processing input: " + ex.getMessage(), ex);
        }
    }

    class ConfiguredPipeline extends ForkJoinPipeline {

        public String getConfig() {
            return config;
        }

    }
}

