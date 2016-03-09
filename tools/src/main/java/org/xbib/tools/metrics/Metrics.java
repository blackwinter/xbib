package org.xbib.tools.metrics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.IngestMetric;
import org.xbib.graphics.chart.ChartBuilderXY;
import org.xbib.graphics.chart.ChartXY;
import org.xbib.graphics.chart.VectorGraphicsEncoder;
import org.xbib.graphics.chart.internal.style.Styler;
import org.xbib.metrics.Meter;
import org.xbib.util.FormatUtil;

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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Metrics {

    private final static Logger logger = LogManager.getLogger(Metrics.class);

    private final Map<String,MetricWriter> writers;

    private final ScheduledExecutorService service;

    public Metrics() {
        this.writers = new HashMap<>();
        this.service = Executors.newScheduledThreadPool(2);
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

    public void scheduleMetrics(Settings settings, String type, Meter metric) {
        if (settings == null) {
            logger.warn("no settings");
            return;
        }
        if (type == null || !type.startsWith("meter")) {
            logger.warn("not a metric type that starts with meter*");
            return;
        }
        // run every 10 seconds by default
        long value = settings.getAsLong("schedule.metrics.seconds", 10L);
        service.scheduleAtFixedRate(new MeterMetricThread(type, metric), 0L, value, TimeUnit.SECONDS);
        logger.info("scheduled meter metrics at {} seconds", value);
    }

    public void scheduleIngestMetrics(Settings settings, IngestMetric ingestMetric) {
        if (settings == null) {
            logger.warn("no settings");
            return;
        }
        if (ingestMetric == null) {
            logger.warn("no ingest metric");
            return;
        }
        // run every 10 seconds by default
        long value = settings.getAsLong("schedule.metrics.seconds", 10L);
        service.scheduleAtFixedRate(new IngestMetricThread("ingest", ingestMetric), 0L, value, TimeUnit.SECONDS);
        logger.info("scheduled ingest metrics at {} seconds", value);
    }

    public synchronized void append(String type, Meter metric) {
        if (metric == null) {
            return;
        }
        long docs = metric.getCount();
        long elapsed = metric.elapsed() / 1000000; // nanos to millis
        double dps = docs * 1000.0 / elapsed;
        long mean = Math.round(metric.getMeanRate());
        long oneminute = Math.round(metric.getOneMinuteRate());
        long fiveminute = Math.round(metric.getFiveMinuteRate());
        long fifteenminute = Math.round(metric.getFifteenMinuteRate());

        logger.info("{}: {} docs, {} ms = {}, {} = {}, {} ({} {} {})",
                type,
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
                if (type.equals(writer.type) && writer.writer != null) {
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

    public synchronized void append(String type, IngestMetric metric) {
        if (metric == null) {
            return;
        }
        long docs = metric.getSucceeded().count();
        long elapsed = metric.elapsed() / 1000000; // nano to millis
        double dps = docs * 1000.0 / elapsed;
        long bytes = metric.getTotalIngestSizeInBytes().count();
        double avg = bytes / (docs + 1.0); // avoid div by zero
        double bps = bytes * 1000.0 / elapsed;

        logger.info("{}: {} docs, {} ms = {}, {} = {}, {} = {} avg, {} = {}, {} = {}",
                type,
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
                if (type.equals(writer.type) && writer.writer != null) {
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
            toChart(entry);
        }
    }

    public void toChart(Map.Entry<String, MetricWriter> entry) throws IOException {
        MetricWriter writer = entry.getValue();
        if (writer.type.startsWith("meter") && writer.chart != null) {
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
                        }).collect(Collectors.toList());
            }
            logger.info("{}: x={}, y={}",
                    writer.type, xData.size(), yData.size());
            if (!xData.isEmpty() && !yData.isEmpty()) {
                ChartXY chart = new ChartBuilderXY().width(1024).height(800)
                        .theme(Styler.ChartTheme.Matlab)
                        .title(writer.title)
                        .xAxisTitle("t")
                        .yAxisTitle("dps")
                        .build();
                chart.getStyler().setPlotGridLinesVisible(false);
                chart.getStyler().setXAxisTickMarkSpacingHint(100);
                chart.getStyler().setDatePattern("HH:mm:ss");
                chart.getStyler().setZoneId(ZoneId.of("UTC"));
                chart.addSeries("Bulk index input rate", xData, yData);
                VectorGraphicsEncoder.write(chart, Files.newOutputStream(writer.chart),
                        VectorGraphicsEncoder.VectorGraphicsFormat.SVG);
            }
        } else if (writer.type.startsWith("ingest") && writer.chart != null) {
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
                        }).collect(Collectors.toList());
            }
            logger.info("{}: x={}, y1={}, y2={}",
                    writer.type, xData.size(), yData.size(), y2Data.size());
            if (!xData.isEmpty() && !yData.isEmpty() && !y2Data.isEmpty()) {
                ChartXY chart = new ChartBuilderXY().width(1024).height(800)
                        .theme(Styler.ChartTheme.Matlab)
                        .title(writer.title)
                        .xAxisTitle("t")
                        .yAxisTitle("rate")
                        .build();
                chart.getStyler().setPlotGridLinesVisible(false);
                chart.getStyler().setXAxisTickMarkSpacingHint(100);
                chart.getStyler().setDatePattern("HH:mm:ss");
                chart.addSeries("Bulk index output rate", xData, yData);
                chart.addSeries("Bulk index volume rate", xData, y2Data);
                VectorGraphicsEncoder.write(chart, Files.newOutputStream(writer.chart),
                        VectorGraphicsEncoder.VectorGraphicsFormat.SVG);
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

        private final String type;

        private final Meter metric;

        public MeterMetricThread(String type, Meter meterMetric) {
            this.type = type;
            this.metric = meterMetric;
            setDaemon(true);
        }

        @Override
        public void run() {
            append(type, metric);
        }
    }

    class IngestMetricThread extends Thread {

        private final String type;

        private final IngestMetric metric;

        public IngestMetricThread(String type, IngestMetric metric) {
            this.type = type;
            this.metric = metric;
            setDaemon(true);
        }

        @Override
        public void run() {
            append(type, metric);
        }
    }

}
