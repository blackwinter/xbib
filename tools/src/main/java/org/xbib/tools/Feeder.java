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
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.support.client.ingest.IngestTransportClient;
import org.xbib.elasticsearch.support.client.mock.MockTransportClient;
import org.xbib.elasticsearch.support.client.transport.BulkTransportClient;
import org.xbib.entities.support.ClasspathURLStreamHandler;
import org.xbib.metric.MeterMetric;
import org.xbib.util.DurationFormatUtil;
import org.xbib.util.FormatUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.text.NumberFormat;

public abstract class Feeder extends Converter {

    private final static Logger logger = LogManager.getLogger(Feeder.class);

    protected static Ingest ingest;

    @Override
    public Feeder reader(Reader reader) {
        super.reader(reader);
        return this;
    }

    @Override
    public Feeder writer(Writer writer) {
        super.writer(writer);
        return this;
    }

    protected String getIndex() {
        return settings.get("index");
    }

    protected String getType() {
        return settings.get("type");
    }

    protected Ingest createIngest() {
        return settings.getAsBoolean("mock", false) ?
                new MockTransportClient() :
                "ingest".equals(settings.get("client")) ? new IngestTransportClient() :
                        new BulkTransportClient();
    }

    @Override
    protected void prepareSink() throws IOException {
        logger.info("preparing ingest");
        if (ingest == null) {
            Integer maxbulkactions = settings.getAsInt("maxbulkactions", 1000);
            Integer maxconcurrentbulkrequests = settings.getAsInt("maxconcurrentbulkrequests",
                    Runtime.getRuntime().availableProcessors());
            ingest = createIngest();
            ingest.maxActionsPerRequest(maxbulkactions)
                    .maxConcurrentRequests(maxconcurrentbulkrequests);
            ingest.init(ImmutableSettings.settingsBuilder()
                    .put("cluster.name", settings.get("elasticsearch.cluster", "elasticsearch"))
                    .put("host", settings.get("elasticsearch.host", "localhost"))
                    .put("port", settings.getAsInt("elasticsearch.port", 9300))
                    .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                    .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                    .build());
        }
        createIndex(getIndex());
    }

    @Override
    protected Feeder cleanup() throws IOException {
        super.cleanup();
        if (ingest != null) {
            try {
                logger.info("flush");
                ingest.flushIngest();
                logger.info("waiting for all responses");
                ingest.waitForResponses(TimeValue.timeValueSeconds(120));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error(e.getMessage(), e);
            }
            logger.info("shutdown");
            ingest.shutdown();
        }
        logger.info("done with run");
        return this;
    }

    @Override
    protected void writeMetrics(MeterMetric metric, Writer writer) throws Exception {
        if (metric ==null) {
            return;
        }
        long docs = metric.count();
        double mean = metric.meanRate();
        double oneminute = metric.oneMinuteRate();
        double fiveminute = metric.fiveMinuteRate();
        double fifteenminute = metric.fifteenMinuteRate();
        //long bytes = ingest != null && ingest.getMetric() != null ?
        //        ingest.getMetric().getTotalIngestSizeInBytes().count() : 0;
        long bytes = 0;
        long elapsed = metric.elapsed() / 1000000;
        String elapsedhuman = DurationFormatUtil.formatDurationWords(elapsed, true, true);
        double avg = bytes / (docs + 1); // avoid div by zero
        double mbps = (bytes * 1000.0 / elapsed) / (1024.0 * 1024.0);
        NumberFormat formatter = NumberFormat.getNumberInstance();
        logger.info("indexing metrics: elapsed {}, {} docs, {} bytes, {} avgsize, {} MB/s, {} ({} {} {})",
                elapsedhuman,
                docs,
                FormatUtil.convertFileSize(bytes),
                FormatUtil.convertFileSize(avg),
                formatter.format(mbps),
                mean,
                oneminute,
                fiveminute,
                fifteenminute
        );

        if (writer != null) {
            String metrics = String.format("indexing metrics: elapsed %s, %d docs, %s bytes, %s avgsize, %s MB/s, %f (%f %f %f)",
                    elapsedhuman,
                    docs,
                    FormatUtil.convertFileSize(bytes),
                    FormatUtil.convertFileSize(avg),
                    formatter.format(mbps),
                    mean,
                    oneminute,
                    fiveminute,
                    fifteenminute
            );
            writer.append(metrics);
        }
    }

    protected Feeder createIndex(String index) throws IOException {
        if (ingest == null) {
            return this;
        }
        if (settings.get("elasticsearch.cluster") != null) {
            Settings clientSettings = ImmutableSettings.settingsBuilder()
                    .put("cluster.name", settings.get("elasticsearch.cluster"))
                    .put("host", settings.get("elasticsearch.host"))
                    .put("port", settings.getAsInt("elasticsearch.port", 9300))
                    .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                    .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                    .build();
            ingest.init(clientSettings);
        }
        ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        try {
            String indexSettings = settings.get("index-settings",
                    "classpath:org/xbib/tools/feed/elasticsearch/settings.json");
            InputStream indexSettingsInput = (indexSettings.startsWith("classpath:") ?
                    new URL(null, indexSettings, new ClasspathURLStreamHandler()) :
                    new URL(indexSettings)).openStream();
            String indexMappings = settings.get("index-mapping",
                    "classpath:org/xbib/tools/feed/elasticsearch/mapping.json");
            InputStream indexMappingsInput = (indexMappings.startsWith("classpath:") ?
                    new URL(null, indexMappings, new ClasspathURLStreamHandler()) :
                    new URL(indexMappings)).openStream();
            ingest.newIndex(getIndex(), getType(),
                    indexSettingsInput, indexMappingsInput);
            beforeIndexCreation(ingest);
        } catch (Exception e) {
            if (!settings.getAsBoolean("ignoreindexcreationerror", false)) {
                throw e;
            } else {
                logger.warn("index creation error, but configured to ignore");
            }
        }
        return this;
    }

    protected Feeder beforeIndexCreation(Ingest output) throws IOException {
        return this;
    }
}
