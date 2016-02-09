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

    private final Map<Thread,Map<String,MetricWriter>> metrics;

    private final ScheduledExecutorService service;

    public Metrics() {
        this.metrics = new HashMap<>();
        this.service = Executors.newScheduledThreadPool(2);
    }

    public void prepareMetrics(Settings settings) {
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
        metrics.put(Thread.currentThread(), thisMetrics);
        if (!thisMetrics.isEmpty()) {
            logger.info("metrics prepared for thread {}: {} entries", Thread.currentThread(), thisMetrics.size());
        }
    }

    public void scheduleWorkerMetrics(Settings settings, ForkJoinPipeline<Converter, URIWorkerRequest> pipeline) {
        if (settings == null) {
            return;
        }
        // run every 10 seconds by default
        long value = settings.getAsLong("schedule.metrics.seconds", 10L);
        if (pipeline.getWorkers() != null) {
            for (Worker worker : pipeline.getWorkers()) {
                service.scheduleAtFixedRate(new MeterMetricThread(worker.getMetric()), 0L, value, TimeUnit.SECONDS);
            }
        }
    }

    public void sceduleIngestMetrics(Settings settings, Ingest ingest) {
        if (settings == null) {
            return;
        }
        if (ingest == null) {
            return;
        }
        // run every 10 seconds by default
        long value = settings.getAsLong("schedule.metrics.seconds", 10L);
        service.scheduleAtFixedRate(new IngestMetricThread(ingest.getMetric()), 0L, value, TimeUnit.SECONDS);
    }

    public void append(MeterMetric metric) {
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

        Map<String,MetricWriter> thisMetrics = metrics.get(Thread.currentThread());
        if (thisMetrics != null) {
            for (Map.Entry<String, MetricWriter> entry : thisMetrics.entrySet()) {
                if (!"meter".equals(entry.getKey())) {
                    continue;
                }
                try {
                    MetricWriter writer = entry.getValue();
                    Settings settings = writer.settings;
                    Locale locale = writer.locale;
                    String format = settings.get("format", "%s\t%d\t%l");
                    String message = String.format(locale, format,  Thread.currentThread(), elapsed, docs);
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

    public void append(IngestMetric metric) {
        if (metric == null) {
            return;
        }
        long docs = metric.getSucceeded().count();
        long elapsed = metric.elapsed() / 1000000; // nano to millis
        double dps = docs * 1000.0 / elapsed;
        long bytes = metric.getTotalIngestSizeInBytes().count();
        double avg = bytes / (docs + 1.0); // avoid div by zero
        double bps = bytes * 1000.0 / elapsed;

        Map<String,MetricWriter> thisMetrics = metrics.get(Thread.currentThread());
        if (thisMetrics != null) {
            for (Map.Entry<String, MetricWriter> entry : thisMetrics.entrySet()) {
                if (!"ingest".equals(entry.getKey())) {
                    continue;
                }
                try {
                    MetricWriter writer = entry.getValue();
                    Settings settings = writer.settings;
                    Locale locale = writer.locale;
                    String format = settings.get("format", "%s\t%d\t%l\t%l");
                    String message = String.format(locale, format,  Thread.currentThread(), elapsed, bytes, docs);
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

    public void disposeMetrics() throws IOException {
        for (Map.Entry<Thread,Map<String,MetricWriter>> allMetricsEntry : metrics.entrySet()) {
            allMetricsEntry.getKey().interrupt();
            Map<String, MetricWriter> thisMetrics = allMetricsEntry.getValue();
            for (Map.Entry<String, MetricWriter> entry : thisMetrics.entrySet()) {
                try {
                    entry.getValue().writer.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        service.shutdownNow();
        logger.info("metrics disposed");
    }

    static class MetricWriter {
        BufferedWriter writer;
        Settings settings;
        Locale locale;
    }

    class MeterMetricThread extends Thread {

        private final MeterMetric metric;

        public MeterMetricThread(MeterMetric meterMetric) {
            this.metric = meterMetric;
        }

        @Override
        public void run() {
            append(metric);
        }
    }

    class IngestMetricThread extends Thread {

        private final IngestMetric metric;

        public IngestMetricThread(IngestMetric metric) {
            this.metric = metric;
        }

        @Override
        public void run() {
            append(metric);
        }
    }

}
