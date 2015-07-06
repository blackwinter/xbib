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
package org.xbib.tools.merge.zdb.licenseinfo.timeline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.support.client.transport.BulkTransportClient;
import org.xbib.elasticsearch.support.client.ingest.IngestTransportClient;
import org.xbib.elasticsearch.support.client.mock.MockTransportClient;
import org.xbib.elasticsearch.support.client.search.SearchClient;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.queue.QueuePipelineExecutor;
import org.xbib.tools.CommandLineInterpreter;
import org.xbib.tools.merge.zdb.entities.BibdatLookup;
import org.xbib.tools.merge.zdb.entities.BlackListedISIL;
import org.xbib.tools.merge.zdb.entities.Manifestation;
import org.xbib.tools.merge.zdb.licenseinfo.ManifestationPipelineElement;
import org.xbib.util.ExceptionFormatter;
import org.xbib.util.Strings;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Sets.newSetFromMap;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.xbib.common.settings.Settings.settingsBuilder;

/**
 * Merge ZDB title and holdings and EZB licenses
 */
public class WithHoldingsAndLicensesInTimeline
        extends QueuePipelineExecutor<Boolean, ManifestationPipelineElement, WithHoldingsAndLicensesInTimelinePipeline>
        implements CommandLineInterpreter {

    private final static Logger logger = LogManager.getLogger(WithHoldingsAndLicensesInTimeline.class.getSimpleName());

    private static Set<String> processed;

    private static Set<String> timelines;

    private static Set<String> indexed;

    private static Set<String> skipped;

    private WithHoldingsAndLicensesInTimeline service;

    private Client client;

    private Ingest ingest;

    private String sourceTitleIndex;

    private String sourceTitleType;

    private int size;

    private long millis;

    private String identifier;

    private static Settings settings;

    private static BibdatLookup bibdatLookup;

    private static BlackListedISIL isilbl;

    public WithHoldingsAndLicensesInTimeline reader(Reader reader) {
        settings = settingsBuilder().loadFrom(reader).build();
        return this;
    }

    public WithHoldingsAndLicensesInTimeline writer(Writer writer) {
        return this;
    }

    @Override
    public WithHoldingsAndLicensesInTimeline prepare() {
        super.prepare();

        logger.info("preparing bibdat lookup...");
        bibdatLookup = new BibdatLookup();
        try {
            bibdatLookup.buildLookup(client, "bibdat");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("bibdat prepared");

        logger.info("preparing ISIL blacklist...");
        isilbl = new BlackListedISIL();
        try {
            isilbl.buildLookup(getClass().getResourceAsStream("isil.blacklist"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("ISIL blacklist prepared");

        processed = newSetFromMap(new ConcurrentHashMap<String, Boolean>(16, 0.75f, settings.getAsInt("concurrency", 1)));
        timelines = newSetFromMap(new ConcurrentHashMap<String, Boolean>(16, 0.75f, settings.getAsInt("concurrency", 1)));
        indexed = newSetFromMap(new ConcurrentHashMap<String, Boolean>(16, 0.75f, settings.getAsInt("concurrency", 1)));
        skipped = newSetFromMap(new ConcurrentHashMap<String, Boolean>(16, 0.75f, settings.getAsInt("concurrency", 1)));

        return this;
    }

    @Override
    public void run() throws Exception {
        logger.info("run starts");
        this.sourceTitleIndex = settings.get("bib-index");
        this.sourceTitleType = settings.get("bib-type");
        if (Strings.isNullOrEmpty(sourceTitleIndex)) {
            throw new IllegalArgumentException("no bib-index parameter given");
        }
        if (Strings.isNullOrEmpty(sourceTitleType)) {
            throw new IllegalArgumentException("no bib-type parameter given");
        }
        SearchClient search = new SearchClient().init(Settings.settingsBuilder()
                .put("cluster.name", settings.get("source.cluster"))
                .put("host", settings.get("source.host"))
                .put("port", settings.getAsInt("source.port", 9300))
                .put("sniff", settings.getAsBoolean("source.sniff", false))
                .build().getAsMap());
        this.service = this;
        this.client = search.client();
        this.size = settings.getAsInt("scrollSize", 10);
        this.millis = settings.getAsTime("scrollTimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(60)).millis();
        this.identifier = settings.get("identifier");

        this.ingest = settings.getAsBoolean("mock", false) ?
                new MockTransportClient() :
                "ingest".equals(settings.get("client")) ?
                        new IngestTransportClient() :
                        new BulkTransportClient();

        ingest.maxActionsPerRequest(settings.getAsInt("maxBulkActions", 100))
                .maxConcurrentRequests(settings.getAsInt("maxConcurrentBulkRequests",
                        Runtime.getRuntime().availableProcessors()));
        ingest.setting(WithHoldingsAndLicensesInTimeline.class.getResourceAsStream("transport-client-settings.json"));
        ingest.init(Settings.settingsBuilder()
                .put("cluster.name", settings.get("target.cluster"))
                .put("host", settings.get("target.host"))
                .put("port", settings.getAsInt("target.port", 9300))
                .put("sniff", settings.getAsBoolean("target.sniff", false))
                .build().getAsMap());
        ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        ingest.setting(WithHoldingsAndLicensesInTimeline.class.getResourceAsStream("index-settings.json"));
        ingest.mapping("Work", WithHoldingsAndLicensesInTimeline.class.getResourceAsStream("mapping-Work.json"));
        ingest.mapping("Manifestation", WithHoldingsAndLicensesInTimeline.class.getResourceAsStream("mapping-Manifestation.json"));
        ingest.mapping("Volume", WithHoldingsAndLicensesInTimeline.class.getResourceAsStream("mapping-Volume.json"));
        ingest.mapping("Holding", WithHoldingsAndLicensesInTimeline.class.getResourceAsStream("mapping-Holding.json"));

        String index = settings.get("index");
        ingest.newIndex(index);
        ingest.startBulk(index, -1, 1000);

        super.setPipelineProvider(new PipelineProvider<WithHoldingsAndLicensesInTimelinePipeline>() {
            int i = 0;

            @Override
            public WithHoldingsAndLicensesInTimelinePipeline get() {
                return new WithHoldingsAndLicensesInTimelinePipeline(service, i++);
            }
        });
        super.setConcurrency(settings.getAsInt("concurrency", 1));

        this.prepare();

        // here we do the work!
        this.execute();

        logger.info("shutdown in progress");
        shutdown(new ManifestationPipelineElement().set(null));

        long total = 0L;
        /*for (Pipeline p : getPipelines()) {
            logger.info("pipeline {}, started {}, ended {}, took {}, count = {}, service count = {}",
                    p,
                    DateUtil.formatDateISO(p.getMetric().startedAt()),
                    DateUtil.formatDateISO(p.getMetric().stoppedAt()),
                    TimeValue.timeValueMillis(p.getMetric().elapsed() / 1000000).format(),
                    p.getMetric().count(),
                    p.getServiceMetric().count());
            total += p.getMetric().count();
        }*/
        logger.info("total={}", total);

        logger.info("ingest shutdown in progress");
        ingest.shutdown();

        logger.info("search shutdown in progress");
        search.shutdown();

        logger.info("run complete");
    }

    @Override
    public WithHoldingsAndLicensesInTimeline execute() {
        // execute pipelines
        super.execute();
        if (logger.isDebugEnabled()) {
            logger.debug("executing");
        }
        // enter loop over all manifestations, issue SCAN request
        boolean failure = false;
        boolean complete = false;
        boolean force = false;
        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setSize(size)
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(millis));
        searchRequest.setIndices(sourceTitleIndex);
        if (sourceTitleType != null) {
            searchRequest.setTypes(sourceTitleType);
        }
        // single identifier?
        if (identifier != null) {
            searchRequest.setQuery(termQuery("IdentifierZDB.identifierZDB", identifier));
            force = true;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("aggregate request = {}", searchRequest.toString());
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        long total = searchResponse.getHits().getTotalHits();
        long count = 0L;
        long lastpercent = -1L;
        if (logger.isDebugEnabled()) {
            logger.debug("hits={}", searchResponse.getHits().getTotalHits());
        }
        while (!failure && !complete && searchResponse.getScrollId() != null) {
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                    .execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                try {
                    if (canReceive() == 0L) {
                        logger.error("no more pipelines left to receive, aborting");
                        complete = true;
                        break;
                    }
                    Manifestation manifestation = new Manifestation(hit.getSource());
                    getQueue().offer(new ManifestationPipelineElement().set(manifestation).setForced(force));
                    count++;
                    long percent = count * 100 / total;
                    if (percent != lastpercent && logger.isInfoEnabled()) {
                        logger.info("{}/{} {}% processed={} timelines={} indexed={} skipped={}",
                                count, total, percent,
                                processed.size(), timelines.size(), indexed.size(), skipped.size());
                        /*for (WithHoldingsAndLicensesInTimelinePipeline p : getPipelines()) {
                            logger.info("{} throughput={} {} {} mean={}",
                                    p.toString(),
                                    p.getMetric().oneMinuteRate(),
                                    p.getMetric().fiveMinuteRate(),
                                    p.getMetric().fifteenMinuteRate(),
                                    p.getMetric().meanRate()
                            );
                        }*/
                    }
                    lastpercent = percent;
                } catch (Throwable e) {
                    logger.error("error passing data to merge pipelines, exiting", e);
                    logger.error(ExceptionFormatter.format(e));
                    failure = true;
                    break;
                }
            }
        }

        // post phase: add all "skipped" docs one by one. We do not know why they have been skipped.

        skipped.removeAll(indexed);
        logger.info("before indexing skipped: {}", skipped.size());

        // log skipped IDs file for analysis
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String filename = String.format("notindexed-before-%04d%02d%02d.txt", year, month + 1, day);
        try {
            FileWriter w = new FileWriter(filename);
            for (String s : skipped) {
                w.append(s).append("\n");
            }
            w.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        // post process, index the skipped IDs. Now with force.
        force = true;
        for (String skippedIdentifier : skipped) {
            searchRequest = client.prepareSearch()
                    .setIndices(sourceTitleIndex)
                    .setQuery(termQuery("IdentifierZDB.identifierZDB", skippedIdentifier));
            if (sourceTitleType != null) {
                searchRequest.setTypes(sourceTitleType);
            }
            searchResponse = searchRequest.execute().actionGet();
            if (logger.isDebugEnabled()) {
                logger.debug("hits={}", searchResponse.getHits().getTotalHits());
            }
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                continue;
            }
            for (SearchHit hit : hits) {
                try {
                    if (canReceive() == 0L) {
                        logger.error("no more pipelines left to receive, aborting");
                        break;
                    }
                    Manifestation manifestation = new Manifestation(hit.getSource());
                    getQueue().offer(new ManifestationPipelineElement().set(manifestation).setForced(force));
                } catch (Throwable e) {
                    logger.error("error passing data to merge pipelines, exiting", e);
                    logger.error(ExceptionFormatter.format(e));
                    break;
                }
            }
        }

        skipped.removeAll(indexed);
        logger.info("after indexing skipped: skipped = {}", skipped.size());
        filename = String.format("notindexed-after-%04d%02d%02d.txt", year, month + 1, day);
        try {
            FileWriter w = new FileWriter(filename);
            for (String s : skipped) {
                w.append(s).append("\n");
            }
            w.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return this;
    }

    public Set<Pipeline<Boolean, ManifestationPipelineElement>> getPipelines() {
        return super.getPipelines();
    }

    public Set<String> processed() {
        return processed;
    }

    public Set<String> timelines() {
        return timelines;
    }

    public Set<String> indexed() {
        return indexed;
    }

    public Set<String> skipped() {
        return skipped;
    }

    public Client client() {
        return client;
    }

    public Ingest ingest() {
        return ingest;
    }

    public Settings settings() {
        return settings;
    }

    public int size() {
        return size;
    }

    public long millis() {
        return millis;
    }

    public BibdatLookup bibdatLookup() {
        return bibdatLookup;
    }

    public BlackListedISIL blackListedISIL() {
        return isilbl;
    }

}
