/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2015 Jörg Prante and xbib
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
package org.xbib.tools.merge.articles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.ClientBuilder;
import org.xbib.elasticsearch.helper.client.Ingest;
import org.xbib.elasticsearch.helper.client.LongAdderIngestMetric;
import org.xbib.elasticsearch.helper.client.SearchTransportClient;
import org.xbib.etl.support.ClasspathURLStreamHandler;
import org.xbib.tools.merge.Merger;
import org.xbib.tools.merge.serials.entities.TitleRecord;
import org.xbib.time.DateUtil;
import org.xbib.util.ExceptionFormatter;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Merge serial manifestations with articles
 */
public class ArticlesMerger extends Merger {

    private final static Logger logger = LogManager.getLogger(ArticlesMerger.class.getSimpleName());

    private final static Set<String> docs = Collections.synchronizedSet(new HashSet<>());

    private Pipeline<ArticlesMergerWorker, SerialItemRequest> pipeline;

    private ArticlesMerger merger;

    private Settings settings;

    private SearchTransportClient search;

    private Ingest ingest;

    private int size;

    private long millis;

    @Override
    protected Pipeline<ArticlesMergerWorker, SerialItemRequest> newPipeline() {
        this.pipeline = new ForkJoinPipeline<>();
        return this.pipeline;
    }

    protected void setPipeline(Pipeline<ArticlesMergerWorker, SerialItemRequest> pipeline) {
        this.pipeline = pipeline;
    }

    protected Pipeline<ArticlesMergerWorker, SerialItemRequest> getPipeline() {
        return pipeline;
    }

    @Override
    public void run(Settings settings) throws Exception {
        this.merger = this;
        this.settings = settings;
        try {
            super.run(settings);
            // poison element
            getPipeline().waitFor(new SerialItemRequest());
            long total = 0L;
            for (ArticlesMergerWorker worker : getPipeline().getWorkers()) {
                logger.info("pipeline {}, count {}, started {}, ended {}, took {}",
                        worker,
                        worker.getMetric().count(),
                        DateUtil.formatDateISO(worker.getMetric().startedAt()),
                        DateUtil.formatDateISO(worker.getMetric().stoppedAt()),
                        TimeValue.timeValueMillis(worker.getMetric().elapsed() / 1000000).format());
                total += worker.getMetric().count();
            }
            logger.info("total={}", total);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        } finally {
            getPipeline().shutdown();
            search.shutdown();
            ingest.flushIngest();
            ingest.waitForResponses(TimeValue.timeValueSeconds(60));
            ingest.shutdown();
        }
    }

    protected void prepareSink() throws Exception {
        this.ingest = createIngest();
        ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        String indexSettings = settings.get("target-index-settings",
                "classpath:org/xbib/tools/merge/articles/settings.json");
        InputStream indexSettingsInput = (indexSettings.startsWith("classpath:") ?
                new URL(null, indexSettings, new ClasspathURLStreamHandler()) :
                new URL(indexSettings)).openStream();
        String indexMappings = settings.get("target-index-mapping",
                "classpath:org/xbib/tools/merge/articles/mapping.json");
        InputStream indexMappingsInput = (indexMappings.startsWith("classpath:") ?
                new URL(null, indexMappings, new ClasspathURLStreamHandler()) :
                new URL(indexMappings)).openStream();
        ingest.newIndex(settings.get("target-index"), settings.get("target-type"),
                indexSettingsInput, indexMappingsInput);
        ingest.startBulk(settings.get("target-index"), -1, 1);
    }

    protected Ingest createIngest() throws IOException {
        org.elasticsearch.common.settings.Settings clientSettings = org.elasticsearch.common.settings.Settings.settingsBuilder()
                .put("cluster.name", settings.get("elasticsearch.cluster", "elasticsearch"))
                .put("host", settings.get("elasticsearch.host", "localhost"))
                .put("port", settings.getAsInt("elasticsearch.port", 9300))
                .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                .build();
        ClientBuilder clientBuilder = ClientBuilder.builder()
                .put(clientSettings)
                .put(ClientBuilder.MAX_ACTIONS_PER_REQUEST, settings.getAsInt("maxbulkactions", 1000))
                .put(ClientBuilder.MAX_CONCURRENT_REQUESTS, settings.getAsInt("maxconcurrentbulkrequests",
                        Runtime.getRuntime().availableProcessors()))
                .setMetric(new LongAdderIngestMetric());
        if (settings.getAsBoolean("mock", false)) {
            return clientBuilder.toMockTransportClient();
        }
        if ("ingest".equals(settings.get("client"))) {
            return clientBuilder.toIngestTransportClient();
        }
        return clientBuilder.toBulkTransportClient();
    }

    @Override
    protected WorkerProvider provider() {
        return new WorkerProvider<ArticlesMergerWorker>() {
            int i = 0;

            @Override
            public ArticlesMergerWorker get(Pipeline pipeline) {
                return new ArticlesMergerWorker(merger, i++);
            }
        };
    }

    @Override
    protected void prepareSource() throws Exception {
        this.search = new SearchTransportClient().init(Settings.settingsBuilder()
                .put("cluster.name", settings.get("source.cluster"))
                .put("host", settings.get("source.host"))
                .put("port", settings.getAsInt("source.port", 9300))
                .put("sniff", settings.getAsBoolean("source.sniff", false))
                .put("autodiscover", settings.getAsBoolean("source.autodiscover", false))
                .build().getAsMap());
        this.size = settings.getAsInt("scrollsize", 10);
        this.millis = settings.getAsTime("scrolltimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(600)).millis();
        String identifier = settings.get("identifier");

        boolean failure = false;
        boolean complete = false;
        // strategy: iterate over ezdb/Manifestation, then iterate over existing dates,
        // pick the holdings, offer into queue
        SearchRequestBuilder searchRequest = search.client().prepareSearch()
                .setIndices(settings.get("ezdb-index"))
                .setTypes(settings.get("ezdb-type"))
                .setSize(size)
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(millis));

        QueryBuilder queryBuilder = matchAllQuery();
        QueryBuilder filterBuilder = existsQuery("dates");
        if (identifier != null) {
            // execute on a single ID
            filterBuilder = null;
            queryBuilder = termQuery("_id", identifier);
        }
        // filter ISSN
        if (settings().getAsBoolean("issnonly", false)) {
            filterBuilder = boolQuery()
                    .must(existsQuery("dates"))
                    .must(existsQuery("identifiers.issn"));
        }
        if (settings().getAsBoolean("eonly", false)) {
            filterBuilder = boolQuery()
                    .must(existsQuery("dates"))
                    .must(termQuery("mediatype", "computer"));
        }
        queryBuilder = filterBuilder != null ?
                boolQuery().must(queryBuilder).filter(filterBuilder) : queryBuilder;
        searchRequest.setQuery(queryBuilder);

        SearchResponse searchResponse = searchRequest.execute().actionGet();
        long total = searchResponse.getHits().getTotalHits();
        long count = 0L;
        long lastpercent = -1L;
        while (!failure && !complete && searchResponse.getScrollId() != null) {
            searchResponse = search.client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                    .execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                try {
                    if (getPipeline().getWorkers().size() == 0) {
                        logger.error("no more workers left to receive, aborting");
                        complete = true;
                        break;
                    }
                    String id = hit.getId();
                    if (docs.contains(id)) {
                        continue;
                    }
                    docs.add(id);
                    Set<Integer> dates = new LinkedHashSet<>();
                    List<TitleRecord> titleRecords = new LinkedList<>();
                    TitleRecord titleRecord = expand(id);
                    if (titleRecord == null) {
                        continue;
                    }
                    Collection<String> issns = (Collection<String>) titleRecord.getIdentifiers().get("formattedissn");
                    if (issns != null) {
                        for (String issn : issns) {
                            expandOA(titleRecord, issn);
                        }
                    }
                    titleRecords.add(titleRecord);
                    Collection<Integer> manifestationDates = titleRecord.getDates();
                    if (manifestationDates != null) {
                        dates.addAll(manifestationDates);
                    }
                    Collection<Map<String,Object>> relations = (Collection<Map<String,Object>>)hit.getSource().get("relations");
                    if (relations != null) {
                        for (Map<String, Object> relation : relations) {
                            String label = (String) relation.get("@label");
                            if ("hasOnlineEdition".equals(label) || "hasPrintEdition".equals(label)) {
                                String relid = (String) relation.get("@id");
                                if (docs.contains(relid)) {
                                    continue;
                                }
                                docs.add(relid);
                                TitleRecord m = expand(relid);
                                if (m != null) {
                                    titleRecords.add(m);
                                    logger.info("{} + {} added manifestation", titleRecord.externalID(), m.externalID());
                                    manifestationDates = m.getDates();
                                    if (manifestationDates != null) {
                                        dates.addAll(manifestationDates);
                                    }
                                }
                            }
                        }
                    }
                    for (Integer date : dates) {
                        SerialItem serialItem = new SerialItem();
                        serialItem.setDate(date);
                        for (TitleRecord m : titleRecords) {
                            if (m.firstDate() != null && m.lastDate() != null) {
                                if (m.firstDate() <= date && date <= m.lastDate()) {
                                    serialItem.addManifestation(titleRecord);
                                }
                            } else if (m.firstDate() != null) {
                                if (m.firstDate() <= date) {
                                    serialItem.addManifestation(titleRecord);
                                }
                            }
                        }
                        if (!serialItem.getTitleRecords().isEmpty()) {
                            getPipeline().getQueue().put(new SerialItemRequest().set(serialItem));
                        }
                    }
                    count++;
                    long percent = count * 100 / total;
                    if (percent != lastpercent && logger.isInfoEnabled()) {
                        logger.info("{}/{} {}%", count, total, percent);
                        for (ArticlesMergerWorker worker : getPipeline().getWorkers()) {
                            logger.info("{} throughput={} {} {} mean={} mldup={} xrefdup={}",
                                    worker.toString(),
                                    worker.getMetric().oneMinuteRate(),
                                    worker.getMetric().fiveMinuteRate(),
                                    worker.getMetric().fifteenMinuteRate(),
                                    worker.getMetric().meanRate(),
                                    worker.getMedlineDuplicates().get(),
                                    worker.getXrefDuplicates().get()
                            );
                        }
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
    }

    public SearchTransportClient search() {
        return search;
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

    private TitleRecord expand(String id) throws IOException {
        QueryBuilder queryBuilder = termQuery("IdentifierZDB.identifierZDB", id);
        SearchRequestBuilder searchRequestBuilder = search.client().prepareSearch()
                .setIndices(settings().get("zdb-index", "zdb"))
                .setQuery(queryBuilder)
                .setSize(1);
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        if (hits.getHits().length == 0) {
            logger.warn("ZDB-ID {} does not exist", id);
            return null;
        }
        return new TitleRecord(hits.getAt(0).getSource());
    }

    private TitleRecord expandOA(TitleRecord titleRecord, String issn) throws IOException {
        QueryBuilder queryBuilder = termQuery("dc:identifier", issn);
        SearchRequestBuilder searchRequestBuilder = search.client().prepareSearch()
                .setIndices(settings().get("doaj-index", "doaj"))
                .setQuery(queryBuilder)
                .setSize(1);
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        if (hits.getHits().length > 0) {
            titleRecord.setOpenAccess(true);
            String license = hits.getAt(0).getSource().containsKey("dc:rights") ?
                    hits.getAt(0).getSource().get("dc:rights").toString() : null;
            titleRecord.setLicense(license);
            logger.info("{} set to open access: {}", titleRecord.externalID(), license);
        }
        return titleRecord;
    }

}
