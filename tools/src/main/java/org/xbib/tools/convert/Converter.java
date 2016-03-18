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
package org.xbib.tools.convert;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.tools.input.FileInput;
import org.xbib.tools.metrics.Metrics;
import org.xbib.tools.output.FileOutput;
import org.xbib.tools.Processor;
import org.xbib.util.concurrent.AbstractWorker;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.URIWorkerRequest;
import org.xbib.util.concurrent.Worker;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Converter
        extends AbstractWorker<Pipeline<Converter,URIWorkerRequest>,URIWorkerRequest>
        implements Processor {

    private final static Logger logger = LogManager.getLogger(Converter.class);

    private final static AtomicInteger threadCounter = new AtomicInteger();

    protected Settings settings;

    protected FileInput fileInput;

    protected FileOutput fileOutput;

    protected Metrics metrics;

    private int number;

    private URIWorkerRequest request;

    public int run(Settings settings) throws Exception {
        this.fileInput = new FileInput();
        this.fileOutput = new FileOutput();
        this.metrics = new Metrics();
        this.settings = settings;
        logger.info("starting, settings = {}", settings.getAsMap());
        int concurrency = settings.getAsInt("concurrency", Runtime.getRuntime().availableProcessors());
        logger.info("configuring fork/join pipeline with concurrency {}", concurrency);
        ForkJoinPipeline<Converter, URIWorkerRequest> pipeline = newPipeline();
        pipeline.setQueue(new SynchronousQueue<>(true));
        setPipeline(pipeline);
        int returncode = 0;
        try {
            // order is important
            // open global resources before workers run
            prepareResources();
            // spawn worker threads and execute all workers
            pipeline.setConcurrency(concurrency)
                    .setWorkerProvider(provider())
                    .prepare()
                    .execute();
            // start to create input for workers on this thread
            prepareRequests();
            // now measure throughput
            scheduleMetrics();
            // send poison element to pipeline
            pipeline.waitFor(new URIWorkerRequest());
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            returncode = 1;
        } finally {
            // attention, order is important
            // close the request source, do not create more input for workers
            disposeRequests(returncode);
            // bring all workers to close down, let worker threads finish, global resources turn into a consistent state
            pipeline.shutdown();
            // close global resources, also the resources that were shared by the workers
            disposeResources(returncode);
            // shut down metric threads
            disposeMetrics();
            // evaluate error conditions
            Map<Converter, Throwable> throwables = pipeline.getWorkerErrors().getThrowables();
            if (!throwables.isEmpty()) {
                logger.error("found {} worker exceptions", throwables.size());
                for (Map.Entry<Converter, Throwable> entry : throwables.entrySet()) {
                    Converter w = entry.getKey();
                    Throwable t = entry.getValue();
                    logger.error(w + ": " + w.getRequest() + ": " + t.getMessage(), t);
                }
                returncode = 1;
            }
            // clear interrupt status, so next Runner incarnation on same JVM can continue
            Thread.interrupted();
        }
        return returncode;
    }

    @Override
    public void close() throws IOException {
        if (metrics != null) {
            metrics.append("append", getMetric());
        }
        logger.info("worker closed");
    }

    @Override
    protected void processRequest(URIWorkerRequest request) throws Exception {
        this.request = request;
        URI uri = request.get();
        logger.info("processing URI {}", uri);
        process(uri);
    }

    public URIWorkerRequest getRequest() {
        return request;
    }

    protected void scheduleMetrics() {
        if (getPipeline().getWorkers() == null || getPipeline().getWorkers().isEmpty()) {
            logger.warn("no workers for metrics");
            return;
        }
        for (Worker<Pipeline<Converter, URIWorkerRequest>, URIWorkerRequest> worker : getPipeline().getWorkers()) {
            metrics.scheduleMetrics(settings, "meter", worker.getMetric());
        }
    }

    protected void prepareResources() throws IOException {
        fileOutput.createFileMap(settings);
    }

    protected void prepareRequests() throws IOException, InterruptedException {
        fileInput.createRequests(settings, getPipeline().getQueue());
    }

    protected void process(URI uri) throws Exception {
        // will be overridden
    }

    protected void disposeResources(int returncode) throws IOException {
        fileOutput.closeFileMap(returncode);
    }

    protected void disposeRequests(int returncode) throws IOException {
        // no need to close fileInput here
    }

    protected void disposeMetrics() throws IOException {
        metrics.disposeMetrics();
    }

    protected Converter setNumber(int number) {
        this.number = number;
        return this;
    }

    protected int getNumber() {
        return number;
    }

    protected void setSettings(Settings settings) {
        this.settings = settings;
    }

    protected Settings getSettings() {
        return settings;
    }

    protected void setFileInput(FileInput fileInput) {
        this.fileInput = fileInput;
    }

    protected void setFileOutput(FileOutput fileOutput) {
        this.fileOutput = fileOutput;
    }

    protected void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    protected ForkJoinPipeline<Converter, URIWorkerRequest> newPipeline() {
        return new ConverterPipeline();
    }

    // must be overriden
    protected WorkerProvider<Converter> provider() {
        return null;
    }

    @Override
    public Converter setPipeline(Pipeline<Converter,URIWorkerRequest> pipeline) {
        super.setPipeline(pipeline);
        if (pipeline instanceof ConverterPipeline) {
            ConverterPipeline converterPipeline = (ConverterPipeline)pipeline;
            setNumber(threadCounter.getAndIncrement());
            setSettings(converterPipeline.getSettings());
            setFileInput(converterPipeline.getFileInput());
            setFileOutput(converterPipeline.getFileOutput());
            setMetrics(converterPipeline.getMetrics());
            metrics.prepareMetrics(getSettings());
        }
        return this;
    }

    public class ConverterPipeline extends ForkJoinPipeline<Converter, URIWorkerRequest> {

        public Settings getSettings() {
            return settings;
        }

        public FileInput getFileInput() {
            return fileInput;
        }

        public FileOutput getFileOutput() {
            return fileOutput;
        }

        public Metrics getMetrics() {
            return metrics;
        }
    }

}
