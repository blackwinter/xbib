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
package org.xbib.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.io.Connection;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;
import org.xbib.io.archive.file.Finder;
import org.xbib.io.archive.tar2.TarConnectionFactory;
import org.xbib.io.archive.tar2.TarSession;
import org.xbib.metric.MeterMetric;
import org.xbib.pipeline.AbstractPipeline;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineException;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.element.URIPipelineElement;
import org.xbib.pipeline.simple.MetricSimplePipelineExecutor;
import org.xbib.util.DurationFormatUtil;
import org.xbib.util.FormatUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Queue;

import static org.xbib.common.settings.Settings.settingsBuilder;

public abstract class Converter<T, P extends Pipeline<T, URIPipelineElement>>
        extends AbstractPipeline<URIPipelineElement, PipelineException> implements Provider {

    private final static Logger logger = LogManager.getLogger(Converter.class.getSimpleName());

    protected Reader reader;

    protected Writer writer;

    protected static Settings settings;

    protected static Session<StringPacket> session;

    protected MetricSimplePipelineExecutor<T, URIPipelineElement, Pipeline<T,URIPipelineElement>> executor;

    @Override
    public Converter<T, P> reader(Reader reader) {
        this.reader = reader;
        setSettings(settingsBuilder().loadFrom(reader).build());
        return this;
    }

    @Override
    public Converter<T, P> writer(Writer writer) {
        this.writer = writer;
        return this;
    }

    public void setSettings(Settings newSettings) {
        settings = newSettings;
    }

    public void prepareSource() throws IOException {
        logger.info("preparing input queue");
        try {// check if running is allowed only on a configured host
            if (settings.get("runhost") != null) {
                boolean found = false;
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
                    System.exit(1);
                }
            }
            if (settings.get("uri") != null) {
                URIPipelineElement element = new URIPipelineElement();
                element.set(URI.create(settings.get("uri")));
                queue.add(element);
                // parallel URI connection possible?
                if (settings.getAsBoolean("parallel", false)) {
                    for (int i = 1; i < settings.getAsInt("concurrency", 1); i++) {
                        element = new URIPipelineElement();
                        element.set(URI.create(settings.get("uri")));
                        queue.put(element);
                    }
                }
            } else if (settings.get("path") != null) {
                Queue<URI> uris = new Finder(settings.get("pattern"))
                        .find(settings.get("path"))
                        .pathSorted(settings.getAsBoolean("isPathSorted", false))
                        .chronologicallySorted(settings.getAsBoolean("isChronologicallySorted", false))
                        .getURIs();
                logger.info("input from path = {}", uris);
                for (URI uri : uris) {
                    URIPipelineElement element = new URIPipelineElement();
                    element.set(uri);
                    queue.put(element);
                }
            } else if (settings.get("archive") != null) {
                URIPipelineElement element = new URIPipelineElement();
                element.set(URI.create(settings.get("archive")));
                queue.put(element);
                TarConnectionFactory factory = new TarConnectionFactory();
                Connection<TarSession> connection = factory.getConnection(URI.create(settings.get("archive")));
                session = connection.createSession();
                session.open(Session.Mode.READ);
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void run() throws Exception {
        try {
            prepareSink();
            int concurrency = settings.getAsInt("concurrency", 1);
            //metric pipeline setExecutor only uses concurrency over different URIs
            // in the input queue, not with a single URI input
            executor = new MetricSimplePipelineExecutor<T, URIPipelineElement, Pipeline<T,URIPipelineElement>>()
                    .setConcurrency(concurrency)
                    .setQueue(queue)
                    .setPipelineProvider(pipelineProvider())
                    .prepare()
                    .execute();
            logger.info("preparing with settings {}", settings.getAsMap());
            prepareSource();
            logger.info("executing with concurrency={}", concurrency);
            executor.waitFor();
            logger.info("execution completed");
        } finally {
            cleanup();
            if (executor != null) {
                executor.shutdown();
                writeMetrics(executor.metric(), writer);
            }
        }
    }


    public Converter<T, P> cleanup() throws IOException {
        if (session != null) {
            session.close();
        }
        return this;
    }

    @Override
    public void close() throws IOException {
        logger.info("pipeline close (no op)");
    }

    @Override
    public void newRequest(Pipeline<MeterMetric, URIPipelineElement> pipeline, URIPipelineElement request) {
        try {
            process(request.get());
        } catch (Exception ex) {
            logger.error(request.get() + ": error while processing input: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void error(Pipeline<MeterMetric, URIPipelineElement> pipeline, URIPipelineElement request, PipelineException error) {
        logger.error(error.getMessage(), error);
    }

    protected void writeMetrics(MeterMetric metric, Writer writer) throws Exception {
        long docs = metric.count();
        long bytes = 0L;
        long elapsed = metric.elapsed() / 1000000;
        double dps = docs * 1000 / elapsed;
        double avg = bytes / (docs + 1); // avoid div by zero
        double mbps = (bytes * 1000 / elapsed) / (1024 * 1024);
        NumberFormat formatter = NumberFormat.getNumberInstance();
        logger.info("Converter complete. {} docs, {} = {} ms, {} = {} bytes, {} = {} avg size, {} dps, {} MB/s",
                docs,
                DurationFormatUtil.formatDurationWords(elapsed, true, true),
                elapsed,
                bytes,
                FormatUtil.convertFileSize(bytes),
                FormatUtil.convertFileSize(avg),
                formatter.format(avg),
                formatter.format(dps),
                formatter.format(mbps));
        if (writer != null) {
            String metrics = String.format("Converter complete. %d docs, %s = %d ms, %d = %s bytes, %s = %s avg getSize, %s dps, %s MB/s",
                    docs,
                    DurationFormatUtil.formatDurationWords(elapsed, true, true),
                    elapsed,
                    bytes,
                    FormatUtil.convertFileSize(bytes),
                    FormatUtil.convertFileSize(avg),
                    formatter.format(avg),
                    formatter.format(dps),
                    formatter.format(mbps));
            writer.append(metrics);
        }
    }

    protected void prepareSink() throws IOException {
    }

    protected abstract PipelineProvider<Pipeline<T,URIPipelineElement>> pipelineProvider();

    public abstract void process(URI uri) throws Exception;

}
