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
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Converter
        extends AbstractWorker<Pipeline<Converter,URIWorkerRequest>,URIWorkerRequest>
        implements Processor {

    private final static Logger logger = LogManager.getLogger(Converter.class);

    protected final static Charset UTF8 = Charset.forName("UTF-8");

    protected final static Charset ISO88591 = Charset.forName("ISO-8859-1");

    private final static AtomicInteger threadCounter = new AtomicInteger();

    protected Settings settings;

    protected FileInput fileInput;

    protected FileOutput fileOutput;

    protected Metrics metrics;

    private int number;

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
        int returnCode = 0;
        try {
            prepareOutput();
            pipeline.setConcurrency(concurrency)
                    .setWorkerProvider(provider())
                    .prepare()
                    .execute();
            prepareInput();
            scheduleMetrics();
            pipeline.waitFor(new URIWorkerRequest());
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            returnCode = 1;
        } finally {
            disposeInput();
            disposeOutput();
            disposeMetrics();
            pipeline.shutdown();
            Map<Converter, Throwable> throwables = pipeline.getWorkerErrors().getThrowables();
            if (!throwables.isEmpty()) {
                logger.error("found {} worker exceptions", throwables.size());
                for (Map.Entry<Converter, Throwable> entry : throwables.entrySet()) {
                    Converter w = entry.getKey();
                    Throwable t = entry.getValue();
                    logger.error(w + ": " + w.getElement() + ": " + t.getMessage(), t);
                }
                returnCode = 1;
            }
            // clear interrupt status, so Runner can continue
            Thread.interrupted();
        }
        return returnCode;
    }

    @Override
    public void close() throws IOException {
        if (metrics != null) {
            metrics.append(getMetric());
        }
        logger.info("worker closed");
    }

    @Override
    public void processRequest(Worker<Pipeline<Converter, URIWorkerRequest>, URIWorkerRequest> worker,
                               URIWorkerRequest request) throws Exception {
        URI uri = request.get();
        logger.info("processing URI {}", uri);
        process(uri);
    }

    protected void scheduleMetrics() {
        metrics.scheduleWorkerMetrics(settings, (ForkJoinPipeline<Converter, URIWorkerRequest>) getPipeline());
    }

    protected void prepareOutput() throws IOException {
        fileOutput.createFileMap(settings);
    }

    protected void prepareInput() throws IOException, InterruptedException {
        fileInput.createRequests(settings, getPipeline().getQueue());
    }

    protected void process(URI uri) throws Exception {
        // will be overridden
    }

    protected void disposeInput() throws IOException {
        // no need to close fileInput here
    }

    protected void disposeOutput() throws IOException {
        fileOutput.closeFileMap();
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

        public Metrics getMetrics() {
            return metrics;
        }
    }

}
