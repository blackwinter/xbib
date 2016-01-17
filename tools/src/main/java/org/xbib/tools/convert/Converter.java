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
import org.xbib.io.Connection;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;
import org.xbib.util.Finder;
import org.xbib.io.archive.tar2.TarConnectionFactory;
import org.xbib.io.archive.tar2.TarSession;
import org.xbib.metric.MeterMetric;
import org.xbib.tools.Program;
import org.xbib.time.DurationFormatUtil;
import org.xbib.util.FormatUtil;
import org.xbib.util.concurrent.AbstractWorker;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.URIWorkerRequest;
import org.xbib.util.concurrent.Worker;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.xbib.common.settings.Settings.settingsBuilder;

public class Converter
        extends AbstractWorker<Pipeline<Converter,URIWorkerRequest>,URIWorkerRequest>
        implements Program {

    private final static Logger logger = LogManager.getLogger(Converter.class.getSimpleName());

    protected Settings settings;

    protected Session<StringPacket> session;

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
        URL url = new URL(arg);
        try (Reader reader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"))) {
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
            prepareSink();
            pipeline.setConcurrency(concurrency)
                    .setWorkerProvider(provider())
                    .prepare()
                    .execute();
            prepareSource();
            pipeline.waitFor(new URIWorkerRequest());
        } finally {
            disposeSource();
            disposeSink();
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

    protected void prepareSink() throws IOException {
    }

    protected void prepareSource() throws IOException {
        try {
            // check if we only allowed to run on a certain host
            if (settings.get("runhost") != null) {
                logger.info("preparing input queue only on runhost={}", settings.get("runhost"));
                boolean found = false;
                // not very smart...
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface netint : Collections.list(nets)) {
                    Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                    for (InetAddress addr : Collections.list(inetAddresses)) {
                        if (addr.getHostName().equals(settings.get("runhost"))) {
                            found = true;
                        }
                    }
                }
                if (!found) {
                    logger.error("configured run host {} not found, exiting", settings.get("runhost"));
                    return;
                }
            }
            if (settings.getAsArray("source.uri").length > 0) {
                logger.info("preparing requests from uri array={}", Arrays.asList(settings.getAsArray("source.uri")));
                String[] inputs = settings.getAsArray("source.uri");
                for (String input : inputs) {
                    URIWorkerRequest request = new URIWorkerRequest();
                    request.set(URI.create(input));
                    getQueue().put(request);
                }
                logger.info("{} requests", inputs.length);
            } else if (settings.get("source.uri") != null) {
                logger.info("preparing request from uri={}", settings.get("source.uri"));
                String input = settings.get("uri");
                URIWorkerRequest element = new URIWorkerRequest();
                element.set(URI.create(input));
                getQueue().put(element);
                // parallel URI into queue?
                if (settings.getAsBoolean("source.parallel", false)) {
                    for (int i = 1; i < settings.getAsInt("source.concurrency", 1); i++) {
                        element = new URIWorkerRequest();
                        element.set(URI.create(input));
                        getQueue().put(element);
                    }
                }
                logger.info("put {} elements", settings.getAsBoolean("source.parallel", false) ? 1 + settings.getAsInt("source.concurrency", 1) : 1);
            } else if (settings.get("source.path") != null) {
                    logger.info("preparing input queue by path={}",
                            settings.get("source.path"));
                    Queue<URI> uris = new Finder()
                            .find(settings.get("source.base"), settings.get("source.basepattern"),
                                    settings.get("source.path"), settings.get("source.pattern"))
                            .sortByName(settings.getAsBoolean("source.sort_by_name", false))
                            .sortByLastModified(settings.getAsBoolean("source.sort_by_lastmodified", false))
                            .getURIs();
                    logger.info("input from URIs = {}", uris);
                    for (URI uri : uris) {
                        URIWorkerRequest element = new URIWorkerRequest();
                        element.set(uri);
                        getQueue().put(element);
                    }
                    logger.info("put {} elements", uris.size());
            } else if (settings.get("source.archive") != null) {
                logger.info("preparing input queue from archive={}", settings.get("source.archive"));
                URIWorkerRequest element = new URIWorkerRequest();
                element.set(URI.create(settings.get("source.archive")));
                getQueue().put(element);
                TarConnectionFactory factory = new TarConnectionFactory();
                Connection<TarSession> connection = factory.getConnection(URI.create(settings.get("source.archive")));
                Session<StringPacket> session = connection.createSession();
                session.open(Session.Mode.READ);
                setSession(session);
                logger.info("put 1 elements");
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    protected void process(URI uri) throws Exception {
    }

    protected void disposeSource() throws IOException {
    }

    protected void disposeSink() throws IOException {
        if (session != null) {
            session.close();
        }
    }

    protected Converter setSettings(Settings settings) {
        this.settings = settings;
        return this;
    }

    protected Settings getSettings() {
        return settings;
    }

    protected Converter setSession(Session<StringPacket> session) {
        this.session = session;
        return this;
    }

    protected Session<StringPacket> getSession() {
        return session;
    }

    protected void writeMetrics(MeterMetric metric) throws Exception {
        if (metric == null) {
            return;
        }
        long docs = metric.count();
        long bytes = 0L;
        long elapsed = metric.elapsed() / 1000000;
        double dps = docs * 1000 / elapsed;
        double avg = bytes / (docs + 1); // avoid div by zero
        double mbps = (bytes * 1000 / elapsed) / (1024 * 1024);
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
            setSession(converter.getSession());
            setNumber(threadCounter.getAndIncrement());
        }
        return this;
    }

    class ConverterPipeline extends ForkJoinPipeline<Converter, URIWorkerRequest> {

        public Settings getSettings() {
            return settings;
        }

        public Session<StringPacket> getSession() {
            return session;
        }
    }
}
