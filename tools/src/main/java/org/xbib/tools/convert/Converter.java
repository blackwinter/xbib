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
import org.xbib.common.settings.loader.SettingsLoader;
import org.xbib.common.settings.loader.SettingsLoaderFactory;
import org.xbib.tools.input.FileInput;
import org.xbib.tools.output.FileOutput;
import org.xbib.metric.MeterMetric;
import org.xbib.tools.Processor;
import org.xbib.time.DurationFormatUtil;
import org.xbib.util.FormatUtil;
import org.xbib.util.concurrent.AbstractWorker;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.URIWorkerRequest;
import org.xbib.util.concurrent.Worker;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.xbib.common.settings.Settings.settingsBuilder;

public class Converter
        extends AbstractWorker<Pipeline<Converter,URIWorkerRequest>,URIWorkerRequest>
        implements Processor {

    private final static Logger logger = LogManager.getLogger(Converter.class);

    protected final static Charset UTF8 = Charset.forName("UTF-8");

    protected final static Charset ISO88591 = Charset.forName("ISO-8859-1");

    protected Settings settings;

    protected FileInput fileInput = new FileInput();

    protected FileOutput fileOutput = new FileOutput();

    private final static AtomicInteger threadCounter = new AtomicInteger();

    private int number;

    public Converter setNumber(int number) {
        this.number = number;
        return this;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public int from(String arg) throws Exception {
        InputStream in;
        try {
            URL url = new URL(arg);
            in = url.openStream();
        } catch (MalformedURLException e) {
            in = new FileInputStream(arg);
        }
        try (Reader reader = new InputStreamReader(in, UTF8)) {
            return from(arg, reader);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public int from(String arg, Reader reader) throws Exception {
        try {
            SettingsLoader settingsLoader = SettingsLoaderFactory.loaderFromResource(arg);
            Settings settings = settingsBuilder()
                    .put(settingsLoader.load(Settings.copyToString(reader)))
                    .replacePropertyPlaceholders()
                    .build();
            logger.info("settings = {}", settings.getAsMap());
            run(settings);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return 1;
        } finally {
            if (getPipeline() != null) {
                getPipeline().shutdown();
                if (getPipeline().getWorkers() != null) {
                    for (Worker worker : getPipeline().getWorkers()) {
                        writeMetrics(worker.getMetric());
                    }
                }
            }
        }
        return 0;
    }

    public void run(Settings settings) throws Exception {
        this.settings = settings;
        int concurrency = settings.getAsInt("concurrency", Runtime.getRuntime().availableProcessors() * 2);
        logger.info("configuring fork/join pipeline with concurrency {}", concurrency);
        ForkJoinPipeline<Converter, URIWorkerRequest> pipeline = newPipeline();
        pipeline.setQueue(new SynchronousQueue<>(true));
        setPipeline(pipeline);
        try {
            prepareOutput();
            pipeline.setConcurrency(concurrency)
                    .setWorkerProvider(provider())
                    .prepare()
                    .execute();
            prepareInput();
            pipeline.waitFor(new URIWorkerRequest());
        } finally {
            disposeInput();
            disposeOutput();
        }
        logger.info("execution completed");
    }

    @Override
    public void close() throws IOException {
        logger.info("worker close (no op)");
    }

    @Override
    public void newRequest(Worker<Pipeline<Converter, URIWorkerRequest>, URIWorkerRequest> worker, URIWorkerRequest request) {
        try {
            URI uri = request.get();
            logger.info("processing URI {}", uri);
            process(uri);
        } catch (Throwable ex) {
            logger.error(request.get() + ": error while processing input: " + ex.getMessage(), ex);
        }
    }

    protected void prepareOutput() throws IOException {
    }

    protected void prepareInput() throws IOException, InterruptedException {
        fileInput.createQueue(settings, getQueue());
    }

    protected void process(URI uri) throws Exception {
    }

    protected void disposeInput() throws IOException {
    }

    protected void disposeOutput() throws IOException {
    }

    protected void setSettings(Settings settings) {
        this.settings = settings;
    }

    protected Settings getSettings() {
        return settings;
    }

    protected void writeMetrics(MeterMetric metric) throws Exception {
        if (metric == null) {
            return;
        }
        long docs = metric.count();
        long bytes = 0L;
        long elapsed = metric.elapsed() / 1000000;
        double dps = docs * 1000.0 / elapsed;
        double avg = bytes / (docs + 1.0); // avoid div by zero
        double mbps = (bytes * 1000.0 / elapsed) / (1024.0 * 1024.0);
        NumberFormat formatter = NumberFormat.getNumberInstance();
        logger.info("Worker complete. {} docs, {} = {} ms, {} = {} bytes, {} = {} avg size, {} dps, {} MB/s",
                docs,
                DurationFormatUtil.formatDurationWords(elapsed, true, true),
                elapsed,
                bytes,
                FormatUtil.convertFileSize(bytes),
                FormatUtil.convertFileSize(avg),
                formatter.format(avg),
                formatter.format(dps),
                formatter.format(mbps));
    }

    protected ForkJoinPipeline<Converter, URIWorkerRequest> newPipeline() {
        return new ConverterPipeline();
    }

    protected WorkerProvider<Converter> provider() {
        return null;
    }

    @Override
    public Converter setPipeline(Pipeline<Converter,URIWorkerRequest> pipeline) {
        super.setPipeline(pipeline);
        if (pipeline instanceof Converter) {
            Converter converter = (Converter)pipeline;
            setSettings(converter.getSettings());
            setNumber(threadCounter.getAndIncrement());
        }
        return this;
    }

    class ConverterPipeline extends ForkJoinPipeline<Converter, URIWorkerRequest> {

        public Settings getSettings() {
            return settings;
        }

    }
}
