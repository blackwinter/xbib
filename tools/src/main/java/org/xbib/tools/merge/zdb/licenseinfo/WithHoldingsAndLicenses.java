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
package org.xbib.tools.merge.zdb.licenseinfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.support.client.transport.BulkTransportClient;
import org.xbib.elasticsearch.support.client.ingest.IngestTransportClient;
import org.xbib.elasticsearch.support.client.mock.MockTransportClient;
import org.xbib.elasticsearch.support.client.search.SearchClient;
import org.xbib.entities.support.StatusCodeMapper;
import org.xbib.entities.support.ValueMaps;
import org.xbib.metric.MeterMetric;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.queue.QueuePipelineExecutor;
import org.xbib.tools.CommandLineInterpreter;
import org.xbib.tools.merge.zdb.entities.BibdatLookup;
import org.xbib.tools.merge.zdb.entities.BlackListedISIL;
import org.xbib.tools.merge.zdb.entities.Manifestation;
import org.xbib.util.DateUtil;
import org.xbib.util.ExceptionFormatter;
import org.xbib.util.Strings;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.collect.Sets.newSetFromMap;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.xbib.common.settings.ImmutableSettings.settingsBuilder;

/**
 * Merge ZDB title and holdings and EZB licenses (without timeline)
 */
public class WithHoldingsAndLicenses
        extends QueuePipelineExecutor<Boolean, ManifestationPipelineElement, WithHoldingsAndLicensesPipeline>
        implements CommandLineInterpreter {

    private final static Logger logger = LogManager.getLogger(WithHoldingsAndLicenses.class.getSimpleName());

    private static Set<String> processed;

    private static Set<String> indexed;

    private static Set<String> skipped;

    private WithHoldingsAndLicenses service;

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

    private static MeterMetric queryMetric;

    private static MeterMetric indexMetric;

    private final static AtomicLong extraCounter = new AtomicLong();

    private long total;

    private long count;

    private StatusCodeMapper statusCodeMapper;

    public WithHoldingsAndLicenses reader(Reader reader) {
        settings = settingsBuilder().loadFromReader(reader).build();
        return this;
    }

    public WithHoldingsAndLicenses writer(Writer writer) {
        return this;
    }

    @Override
    public WithHoldingsAndLicenses prepare() {
        super.prepare();

        logger.info("preparing bibdat lookup...");
        bibdatLookup = new BibdatLookup();
        try {
            bibdatLookup.buildLookup(client, settings.get("index-bibdat", "bibdat"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("bibdat prepared, {} in organization lookup, {} in region lookup, {} in other",
                bibdatLookup.lookupOrganization().size(),
                bibdatLookup.lookupRegion().size(),
                bibdatLookup.lookupOther().size());

        logger.info("preparing ISIL blacklist...");
        isilbl = new BlackListedISIL();
        try {
            isilbl.buildLookup(getClass().getResourceAsStream("isil.blacklist"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("ISIL blacklist prepared, size = {}", isilbl.lookup().size());

        logger.info("preparing status code mapper...");
        Map<String,Object> statuscodes = ValueMaps.getMap(getClass().getClassLoader(),
                "org/xbib/analyzer/mab/status.json", "status");
        statusCodeMapper = new StatusCodeMapper();
        statusCodeMapper.add(statuscodes);
        logger.info("status code mapper prepared");

        processed = newSetFromMap(new ConcurrentHashMap<String, Boolean>(16, 0.75f, settings.getAsInt("concurrency", 1)));
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
        SearchClient search = new SearchClient().newClient(ImmutableSettings.settingsBuilder()
                        .put("cluster.name", settings.get("elasticsearch.cluster"))
                        .put("host", settings.get("elasticsearch.host"))
                        .put("port", settings.getAsInt("elasticsearch.port", 9300))
                        .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                        .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                        .build()
        );
        this.service = this;
        this.client = search.client();
        this.size = settings.getAsInt("scrollsize", 10);
        this.millis = settings.getAsTime("scrolltimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(60)).millis();
        this.identifier = settings.get("identifier");

        this.ingest = settings.getAsBoolean("mock", false) ?
                new MockTransportClient() :
                "ingest".equals(settings.get("client")) ?
                        new IngestTransportClient() :
                        new BulkTransportClient();

        ingest.maxActionsPerBulkRequest(settings.getAsInt("maxbulkactions", 100))
                .maxConcurrentBulkRequests(settings.getAsInt("maxConcurrentbulkrequests", Runtime.getRuntime().availableProcessors()));

        InputStream clientSettings = getClass().getResource(settings.get("transport-client-settings", "transport-client-settings.json")).openStream();
        ingest.setting(clientSettings);
        clientSettings.close();
        ingest.newClient(ImmutableSettings.settingsBuilder()
                .put("cluster.name", settings.get("elasticsearch.cluster"))
                .put("host", settings.get("elasticsearch.host"))
                .put("port", settings.getAsInt("elasticsearch.port", 9300))
                .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                .build());
        ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        ingest.setting(WithHoldingsAndLicenses.class.getResourceAsStream("index-settings.json"));
        ingest.mapping("Work", WithHoldingsAndLicenses.class.getResourceAsStream("mapping-Work.json"));
        ingest.mapping("Manifestation", WithHoldingsAndLicenses.class.getResourceAsStream("mapping-Manifestation.json"));
        ingest.mapping("Holdings", WithHoldingsAndLicenses.class.getResourceAsStream("mapping-Holdings.json"));
        ingest.mapping("DateHoldings", WithHoldingsAndLicenses.class.getResourceAsStream("mapping-DateHoldings.json"));

        String index = settings.get("index");
        try {
            ingest.newIndex(index);
        } catch (IndexAlreadyExistsException e) {
            logger.warn(e.getMessage(), e);
        }
        ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        ingest.startBulk(index, -1, 1000);

        queryMetric = new MeterMetric(5L, TimeUnit.SECONDS);
        indexMetric = new MeterMetric(5L, TimeUnit.SECONDS);

        super.setPipelineProvider(new PipelineProvider<WithHoldingsAndLicensesPipeline>() {
            int i = 0;

            @Override
            public WithHoldingsAndLicensesPipeline get() {
                return new WithHoldingsAndLicensesPipeline(service, i++);
            }
        });
        super.setConcurrency(settings.getAsInt("concurrency", 1));

        this.prepare();

        // here we do the work!
        this.execute();

        logger.info("shutdown in progress");
        shutdown(new ManifestationPipelineElement().set(null));

        logger.info("query: started {}, ended {}, took {}, count = {}",
                DateUtil.formatDateISO(queryMetric.startedAt()),
                DateUtil.formatDateISO(queryMetric.stoppedAt()),
                TimeValue.timeValueMillis(queryMetric.elapsed() / 1000000).format(),
                queryMetric.count());
        logger.info("index: started {}, ended {}, took {}, count = {}",
                DateUtil.formatDateISO(indexMetric.startedAt()),
                DateUtil.formatDateISO(indexMetric.stoppedAt()),
                TimeValue.timeValueMillis(indexMetric.elapsed() / 1000000).format(),
                indexMetric.count());

        logger.info("ingest shutdown in progress");
        ingest.flushIngest();
        ingest.waitForResponses(TimeValue.timeValueSeconds(60));
        ingest.shutdown();

        logger.info("search shutdown in progress");
        search.shutdown();

        logger.info("run complete");
    }

    @Override
    public WithHoldingsAndLicenses execute() {
        // execute pipelines
        super.execute();
        logger.debug("executing");
        // enter loop over all manifestations, issue SCAN request
        boolean failure = false;
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
            logger.debug("aggregate search request = {}", searchRequest.toString());
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        total = searchResponse.getHits().getTotalHits();
        count = 0L;
        ScheduleThread scheduleThread = new ScheduleThread();
        Executors.newSingleThreadExecutor().execute(scheduleThread);

        logger.debug("hits={}", searchResponse.getHits().getTotalHits());
        while (!failure && searchResponse.getScrollId() != null) {
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
                        logger.error("no more pipelines left to receive, aborting feed");
                        return this;
                    }
                    Manifestation manifestation = new Manifestation(hit.getSource());
                    getQueue().offer(new ManifestationPipelineElement().set(manifestation).setForced(force));
                    count++;
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
        String filename = String.format("notindexed-%04d%02d%02d-1.txt", year, month + 1, day);
        try {
            FileWriter w = new FileWriter(filename);
            for (String s : skipped) {
                w.append(s).append("\n");
            }
            w.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        // post process, try to index the skipped IDs. Now with force.
        force = true;
        for (String skippedIdentifier : skipped) {
            searchRequest = client.prepareSearch()
                    .setIndices(sourceTitleIndex)
                    .setQuery(termQuery("IdentifierZDB.identifierZDB", skippedIdentifier));
            if (sourceTitleType != null) {
                searchRequest.setTypes(sourceTitleType);
            }
            searchResponse = searchRequest.execute().actionGet();
            logger.debug("hits={}", searchResponse.getHits().getTotalHits());
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                continue;
            }
            for (SearchHit hit : hits) {
                try {
                    if (canReceive() == 0L) {
                        logger.error("no more pipelines left to receive, aborting");
                        return this;
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
        filename = String.format("notindexed-%04d%02d%02d-2.txt", year, month + 1, day);
        try {
            FileWriter w = new FileWriter(filename);
            for (String s : skipped) {
                w.append(s).append("\n");
            }
            w.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("extra counter = {}", extraCounter);

        scheduleThread.interrupt();

        return this;
    }

    public boolean findOpenAccess(String issn) {
        CountRequestBuilder countRequestBuilder = client.prepareCount()
                .setIndices(settings.get("doaj-index", "doaj"))
                .setQuery(termQuery("dc:identifier", issn));
        CountResponse countResponse = countRequestBuilder.execute().actionGet();
        return countResponse.getCount() > 0;
    }

    public Set<String> processed() {
        return processed;
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

    public StatusCodeMapper statusCodeMapper() {
        return statusCodeMapper;
    }

    public void count(long delta) {
        extraCounter.addAndGet(delta);
    }

    public MeterMetric queryMetric() {
        return queryMetric;
    }

    public MeterMetric indexMetric() {
        return indexMetric;
    }

    class ScheduleThread extends Thread {

        public void run() {
            while (!interrupted()) {
                long percent = count * 100 / total;
                logger.info("=====> {}/{} {}% processed={} indexed={} skipped={} extracounter={}",
                        count, total, percent,
                        processed.size(), indexed.size(), skipped.size(),
                        extraCounter);
                logger.info("query metric={} ({} {} {})",
                        queryMetric.meanRate(),
                        queryMetric.oneMinuteRate(),
                        queryMetric.fiveMinuteRate(),
                        queryMetric.fifteenMinuteRate());
                logger.info("index metric={} ({} {} {})",
                        indexMetric.meanRate(),
                        indexMetric.oneMinuteRate(),
                        indexMetric.fiveMinuteRate(),
                        indexMetric.fifteenMinuteRate());
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

}
