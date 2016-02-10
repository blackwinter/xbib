package org.xbib.tools.metrics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.Ingest;
import org.xbib.elasticsearch.helper.client.IngestMetric;
import org.xbib.graphics.chart.ChartBuilderXY;
import org.xbib.graphics.chart.ChartXY;
import org.xbib.graphics.chart.VectorGraphicsEncoder;
import org.xbib.graphics.chart.internal.style.Styler;
import org.xbib.metric.MeterMetric;
import org.xbib.tools.convert.Converter;
import org.xbib.util.FormatUtil;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.URIWorkerRequest;
import org.xbib.util.concurrent.Worker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Metrics {

    private final static Logger logger = LogManager.getLogger(Metrics.class);

    private final Map<String,MetricWriter> writers;

    private final ScheduledExecutorService service;

    public Metrics() {
        this.writers = new HashMap<>();
        this.service = Executors.newScheduledThreadPool(2);
    }

    public Map<String,MetricWriter> getWriters() {
        return writers;
    }

    public ScheduledExecutorService getService() {
        return service;
    }

    public void prepareMetrics(Settings settings) {
        if (settings == null) {
            return;
        }
        Map<String,Settings> metricSettings = settings.getGroups("metrics");
        for (Map.Entry<String,Settings> entry : metricSettings.entrySet()) {
            String type = entry.getKey();
            // ignore everything execpt "meter" and "ingest"
            if (!"meter".equals(type) && !"ingest".equals(type)) {
                continue;
            }
            String name = entry.getValue().get("name", entry.getKey());
            Path path = Paths.get(name);
            try {
                OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                Writer writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
                MetricWriter metricWriter = new MetricWriter();
                metricWriter.type = type;
                metricWriter.path = path;
                metricWriter.writer = writer;
                metricWriter.settings = entry.getValue();
                metricWriter.chart = Paths.get(metricWriter.settings.get("chart", name + ".svg"));
                metricWriter.title = metricWriter.settings.get("title", type);
                metricWriter.locale = metricWriter.settings.containsSetting("locale") ?
                        new Locale(metricWriter.settings.get("locale")) : Locale.getDefault();
                if (!writers.containsKey(name)) {
                    writers.put(name, metricWriter);
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (!writers.isEmpty()) {
            logger.info("metrics prepared: {}", writers.keySet());
        }
    }

    public void scheduleWorkerMetrics(Settings settings, ForkJoinPipeline<Converter, URIWorkerRequest> pipeline) {
        if (settings == null) {
            logger.warn("no settings");
            return;
        }
        // run every 10 seconds by default
        long value = settings.getAsLong("schedule.metrics.seconds", 10L);
        if (pipeline.getWorkers() == null || pipeline.getWorkers().isEmpty()) {
            logger.warn("no workers");
            return;
        }
        for (Worker worker : pipeline.getWorkers()) {
            service.scheduleAtFixedRate(new MeterMetricThread(worker.getMetric()), 0L, value, TimeUnit.SECONDS);
            logger.info("scheduled worker metrics at {} seconds", value);
        }
    }

    public void scheduleIngestMetrics(Settings settings, Ingest ingest) {
        if (settings == null) {
            logger.warn("no settings");
            return;
        }
        if (ingest == null) {
            logger.warn("no ingest");
            return;
        }
        // run every 10 seconds by default
        long value = settings.getAsLong("schedule.metrics.seconds", 10L);
        service.scheduleAtFixedRate(new IngestMetricThread(ingest.getMetric()), 0L, value, TimeUnit.SECONDS);
        logger.info("scheduled ingest metrics at {} seconds", value);
    }

    public synchronized void append(MeterMetric metric) {
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

        for (Map.Entry<String, MetricWriter> entry : writers.entrySet()) {
            try {
                MetricWriter writer = entry.getValue();
                if ("meter".equals(writer.type) && writer.writer != null) {
                    Settings settings = writer.settings;
                    Locale locale = writer.locale;
                    String format = settings.get("format", "%s\t%d\t%d\n");
                    String message = String.format(locale, format, writer.title, elapsed, docs);
                    writer.writer.write(message);
                    writer.writer.flush();
                }
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    public synchronized void append(IngestMetric metric) {
        if (metric == null) {
            return;
        }
        long docs = metric.getSucceeded().count();
        long elapsed = metric.elapsed() / 1000000; // nano to millis
        double dps = docs * 1000.0 / elapsed;
        long bytes = metric.getTotalIngestSizeInBytes().count();
        double avg = bytes / (docs + 1.0); // avoid div by zero
        double bps = bytes * 1000.0 / elapsed;

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

        for (Map.Entry<String, MetricWriter> entry : writers.entrySet()) {
            try {
                MetricWriter writer = entry.getValue();
                if ("ingest".equals(writer.type) && writer.writer != null) {
                    Settings settings = writer.settings;
                    Locale locale = writer.locale;
                    String format = settings.get("format", "%s\t%d\t%d\t%d\n");
                    String message = String.format(locale, format, writer.title, elapsed, bytes, docs);
                    writer.writer.write(message);
                    writer.writer.flush();
                }
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    public synchronized void disposeMetrics() throws IOException {
        service.shutdownNow();
        for (Map.Entry<String, MetricWriter> entry : writers.entrySet()) {
            try {
                if (entry.getValue().writer != null) {
                    entry.getValue().writer.close();
                    entry.getValue().writer = null;
                    logger.info("{} closed", entry.getKey());
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        toChart();
    }

    public void toChart() throws IOException {
        for (Map.Entry<String, MetricWriter> entry : writers.entrySet()) {
            MetricWriter writer = entry.getValue();
            if ("meter".equals(writer.type) && writer.chart != null) {
                ChartXY chart = new ChartBuilderXY().width(1024).height(800)
                        .theme(Styler.ChartTheme.Matlab)
                        .title(writer.title)
                        .xAxisTitle("t")
                        .yAxisTitle("dps")
                        .build();
                chart.getStyler().setPlotGridLinesVisible(false);
                chart.getStyler().setXAxisTickMarkSpacingHint(100);
                chart.getStyler().setDatePattern("HH:mm:ss");
                final List<Instant> xData = new ArrayList<>();
                final List<Double> yData = new ArrayList<>();
                try (Stream<String> stream = Files.lines(writer.path)) {
                    stream.map(line -> Arrays.asList(line.split("\t")))
                            .map(list -> {
                                long elapsed = Long.parseLong(list.get(1));
                                double docs = Double.parseDouble(list.get(2));
                                xData.add(Instant.ofEpochMilli(elapsed));
                                yData.add(docs * 1000.0 / elapsed);
                                return list;
                            });
                }
                chart.addSeries("Bulk index input rate", xData, yData);
                VectorGraphicsEncoder.write(chart, Files.newOutputStream(writer.chart),
                        VectorGraphicsEncoder.VectorGraphicsFormat.SVG );
            }
            if ("ingest".equals(writer.type) && writer.chart != null) {
                ChartXY chart = new ChartBuilderXY().width(1024).height(800)
                        .theme(Styler.ChartTheme.Matlab)
                        .title(writer.title)
                        .xAxisTitle("t")
                        .yAxisTitle("rate")
                        .build();
                chart.getStyler().setPlotGridLinesVisible(false);
                chart.getStyler().setXAxisTickMarkSpacingHint(100);
                chart.getStyler().setDatePattern("HH:mm:ss");
                final List<Instant> xData = new ArrayList<>();
                final List<Double> yData = new ArrayList<>();
                final List<Double> y2Data = new ArrayList<>();
                try (Stream<String> stream = Files.lines(writer.path)) {
                    stream.map(line -> Arrays.asList(line.split("\t")))
                            .map(list -> {
                                long elapsed = Long.parseLong(list.get(1));
                                double bytes = Double.parseDouble(list.get(2)) / 1024.0;
                                double docs = Double.parseDouble(list.get(3));
                                xData.add(Instant.ofEpochMilli(elapsed));
                                yData.add(docs * 1000.0 / elapsed);
                                y2Data.add(bytes * 1000.0 / elapsed);
                                return list;
                            });
                }
                chart.addSeries("Bulk index output rate", xData, yData);
                chart.addSeries("Bulk index volume rate (KBytes per sec)", xData, y2Data);
                VectorGraphicsEncoder.write(chart, Files.newOutputStream(writer.chart),
                        VectorGraphicsEncoder.VectorGraphicsFormat.SVG );
            }
        }
    }

    static class MetricWriter {
        String type;
        String title;
        Path path;
        Path chart;
        Writer writer;
        Settings settings;
        Locale locale;
    }

    class MeterMetricThread extends Thread {

        private final MeterMetric metric;

        public MeterMetricThread(MeterMetric meterMetric) {
            this.metric = meterMetric;
            setDaemon(true);
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
            setDaemon(true);
        }

        @Override
        public void run() {
            append(metric);
        }
    }

}
