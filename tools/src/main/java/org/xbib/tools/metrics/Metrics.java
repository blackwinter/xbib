package org.xbib.tools.metrics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.Ingest;
import org.xbib.elasticsearch.helper.client.IngestMetric;
import org.xbib.metric.MeterMetric;
import org.xbib.tools.convert.Converter;
import org.xbib.util.FormatUtil;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.URIWorkerRequest;
import org.xbib.util.concurrent.Worker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Metrics {

    private final static Logger logger = LogManager.getLogger(Metrics.class);

    private final Map<Integer,Map<String,MetricWriter>> metrics;

    private final ScheduledExecutorService service;

    public Metrics() {
        this.metrics = new HashMap<>();
        this.service = Executors.newScheduledThreadPool(2);
    }

    public ScheduledExecutorService getService() {
        return service;
    }

    public void prepareMetrics(int number, Settings settings) {
        if (settings == null) {
            return;
        }
        Map<String,Settings> metricSettings = settings.getGroups("metrics");
        Map<String,MetricWriter> thisMetrics = new HashMap<>();
        for (Map.Entry<String,Settings> entry : metricSettings.entrySet()) {
            // ignore everything execpt "meter" and "ingest"
            if (!"meter".equals(entry.getKey()) && !"ingest".equals(entry.getKey())) {
                continue;
            }
            String name = entry.getValue().get("name", entry.getKey());
            Path path = Paths.get(name);
            try {
                OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, Charset.forName("UTF-8")));
                MetricWriter writer = new MetricWriter();
                writer.writer = bufferedWriter;
                writer.settings = entry.getValue();
                writer.locale = writer.settings.containsSetting("locale") ?
                        new Locale(writer.settings.get("locale")) : Locale.getDefault();
                thisMetrics.put(name, writer);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        metrics.put(number, thisMetrics);
        if (!thisMetrics.isEmpty()) {
            logger.info("metrics prepared for {}: {} entries", number, thisMetrics.size());
        }
    }

    public void scheduleWorkerMetrics(int number, Settings settings, ForkJoinPipeline<Converter, URIWorkerRequest> pipeline) {
        if (settings == null) {
            return;
        }
        // run every 10 seconds by default
        long value = settings.getAsLong("schedule.metrics.seconds", 10L);
        if (pipeline.getWorkers() != null) {
            for (Worker worker : pipeline.getWorkers()) {
                service.scheduleAtFixedRate(new MeterMetricThread(number, worker.getMetric()), 0L, value, TimeUnit.SECONDS);
            }
        }
    }

    public void scheduleIngestMetrics(int number, Settings settings, Ingest ingest) {
        if (settings == null) {
            return;
        }
        if (ingest == null) {
            return;
        }
        // run every 10 seconds by default
        long value = settings.getAsLong("schedule.metrics.seconds", 10L);
        service.scheduleAtFixedRate(new IngestMetricThread(number, ingest.getMetric()), 0L, value, TimeUnit.SECONDS);
    }

    public synchronized void append(int number, MeterMetric metric) {
        if (metric == null) {
            return;
        }
        long docs = metric.count();
        long elapsed = metric.elapsed() / 1000000; // nanos to millis
        double dps = docs * 1000.0 / elapsed;
        long mean = Math.round(metric.meanRate());
        long oneminute = Math.round(metric.oneMinuteRate());
        long fiveminute = Math.round(metric.fiveMinuteRate());
        long fifteenminute = Math.round(metric.fifteenMinuteRate());

        Map<String,MetricWriter> thisMetrics = metrics.get(number);
        if (thisMetrics != null) {
            for (Map.Entry<String, MetricWriter> entry : thisMetrics.entrySet()) {
                if (!"meter".equals(entry.getKey())) {
                    continue;
                }
                try {
                    MetricWriter writer = entry.getValue();
                    Settings settings = writer.settings;
                    Locale locale = writer.locale;
                    String format = settings.get("format", "meter\t%d\t%l\t%l");
                    String message = String.format(locale, format, number, elapsed, docs);
                    writer.writer.write(message);
                    writer.writer.newLine();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        logger.info("meter: {} docs, {} ms = {}, {} = {}, {} ({} {} {})",
                docs,
                elapsed,
                FormatUtil.formatDurationWords(elapsed, true, true),
                dps,
                FormatUtil.formatDocumentSpeed(dps),
                mean,
                oneminute,
                fiveminute,
                fifteenminute
        );
    }

    public synchronized void append(int number, IngestMetric metric) {
        if (metric == null) {
            return;
        }
        long docs = metric.getSucceeded().count();
        long elapsed = metric.elapsed() / 1000000; // nano to millis
        double dps = docs * 1000.0 / elapsed;
        long bytes = metric.getTotalIngestSizeInBytes().count();
        double avg = bytes / (docs + 1.0); // avoid div by zero
        double bps = bytes * 1000.0 / elapsed;

        Map<String,MetricWriter> thisMetrics = metrics.get(number);
        if (thisMetrics != null) {
            for (Map.Entry<String, MetricWriter> entry : thisMetrics.entrySet()) {
                if (!"ingest".equals(entry.getKey())) {
                    continue;
                }
                try {
                    MetricWriter writer = entry.getValue();
                    Settings settings = writer.settings;
                    Locale locale = writer.locale;
                    String format = settings.get("format", "ingest\t%d\t%l\t%l\t%l");
                    String message = String.format(locale, format, number, elapsed, bytes, docs);
                    writer.writer.write(message);
                    writer.writer.newLine();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        logger.info("ingest: {} docs, {} ms = {}, {} = {}, {} = {} avg, {} = {}, {} = {}",
                docs,
                elapsed,
                FormatUtil.formatDurationWords(elapsed, true, true),
                bytes,
                FormatUtil.formatSize(bytes),
                avg,
                FormatUtil.formatSize(avg),
                dps,
                FormatUtil.formatDocumentSpeed(dps),
                bps,
                FormatUtil.formatSpeed(bps)
        );
    }

    public synchronized void disposeMetrics(int number) throws IOException {
        Map<String, MetricWriter> thisMetrics = metrics.get(number);
        if (thisMetrics == null) {
            return;
        }
        for (Map.Entry<String, MetricWriter> entry : thisMetrics.entrySet()) {
            try {
                if (entry.getValue().writer != null) {
                    entry.getValue().writer.close();
                    entry.getValue().writer = null;
                    logger.info("{}: {} closed", number, entry.getKey());
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    static class MetricWriter {
        BufferedWriter writer;
        Settings settings;
        Locale locale;
    }

    class MeterMetricThread extends Thread {

        private final int number;

        private final MeterMetric metric;

        public MeterMetricThread(int number, MeterMetric meterMetric) {
            this.number = number;
            this.metric = meterMetric;
            setDaemon(true);
        }

        @Override
        public void run() {
            append(number, metric);
        }
    }

    class IngestMetricThread extends Thread {

        private final int number;

        private final IngestMetric metric;

        public IngestMetricThread(int number, IngestMetric metric) {
            this.number = number;
            this.metric = metric;
            setDaemon(true);
        }

        @Override
        public void run() {
            append(number, metric);
        }
    }

}
