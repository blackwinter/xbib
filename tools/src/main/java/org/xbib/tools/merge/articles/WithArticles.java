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
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elasticsearch.support.client.ingest.IngestTransportClient;
import org.xbib.elasticsearch.support.client.mock.MockTransportClient;
import org.xbib.elasticsearch.support.client.search.SearchClient;
import org.xbib.elasticsearch.support.client.transport.BulkTransportClient;
import org.xbib.entities.support.ClasspathURLStreamHandler;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.QueuePipelineExecutor;
import org.xbib.tools.CommandLineInterpreter;
import org.xbib.tools.merge.serials.entities.TitleRecord;
import org.xbib.util.DateUtil;
import org.xbib.util.ExceptionFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.elasticsearch.index.query.FilterBuilders.boolFilter;
import static org.elasticsearch.index.query.FilterBuilders.existsFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.xbib.common.settings.ImmutableSettings.settingsBuilder;

/**
 * Merge serial manifestations with articles
 */
public class WithArticles
        extends QueuePipelineExecutor<SerialItemPipelineElement, WithArticlesPipeline>
        implements CommandLineInterpreter {

    private final static Logger logger = LogManager.getLogger(WithArticles.class.getName());

    private final static Set<String> docs = Collections.synchronizedSet(new HashSet<>());

    private static Settings settings;

    private Client client;

    private Ingest ingest;

    private WithArticles service;

    private int size;

    private long millis;

    private String identifier;

    public WithArticles reader(Reader reader) {
        settings = settingsBuilder().loadFromReader(reader).build();
        return this;
    }

    public WithArticles settings(Settings newSettings) {
        settings = newSettings;
        return this;
    }

    public WithArticles writer(Writer writer) {
        return this;
    }

    public WithArticles prepare() {
        super.prepare();
        return this;
    }

    @Override
    public void run() throws Exception {
        SearchClient search = new SearchClient().newClient(ImmutableSettings.settingsBuilder()
                .put("cluster.name", settings.get("source.cluster"))
                .put("host", settings.get("source.host"))
                .put("port", settings.getAsInt("source.port", 9300))
                .put("sniff", settings.getAsBoolean("source.sniff", false))
                .put("autodiscover", settings.getAsBoolean("source.autodiscover", false))
                .build());
        try {
            this.service = this;
            this.client = search.client();
            this.size = settings.getAsInt("scrollsize", 10);
            this.millis = settings.getAsTime("scrolltimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(600)).millis();
            this.identifier = settings.get("identifier");

            this.ingest = settings.getAsBoolean("mock", false) ?
                    new MockTransportClient() :
                    "ingest".equals(settings.get("client")) ?
                            new IngestTransportClient() :
                            new BulkTransportClient();
            ingest.maxActionsPerRequest(settings.getAsInt("maxbulkactions", 1000))
                    .maxConcurrentRequests(settings.getAsInt("maxconcurrentbulkrequests",
                            2 * Runtime.getRuntime().availableProcessors()));

            ingest.init(ImmutableSettings.settingsBuilder()
                    .put("cluster.name", settings.get("target.cluster"))
                    .put("host", settings.get("target.host"))
                    .put("port", settings.getAsInt("target.port", 9300))
                    .put("sniff", settings.getAsBoolean("target.sniff", false))
                    .put("autodiscover", settings.getAsBoolean("target.autodiscover", false))
                    .build());
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
            ingest.startBulk(settings.get("target-index"));
            super.setPipelineProvider(new PipelineProvider<WithArticlesPipeline>() {
                int i = 0;

                @Override
                public WithArticlesPipeline get() {
                    return new WithArticlesPipeline(service, i++);
                }
            });
            super.setConcurrency(settings.getAsInt("concurrency", 1));
            this.prepare();
            this.execute();
            logger.info("shutdown in progress");
            shutdown(new SerialItemPipelineElement().set(null));

            long total = 0L;
            for (Pipeline pipeline : getPipelines()) {
                WithArticlesPipeline p = (WithArticlesPipeline)pipeline;
                logger.info("pipeline {}, count {}, started {}, ended {}, took {}",
                        p,
                        p.getMetric().count(),
                        DateUtil.formatDateISO(p.getMetric().startedAt()),
                        DateUtil.formatDateISO(p.getMetric().stoppedAt()),
                        TimeValue.timeValueMillis(p.getMetric().elapsed() / 1000000).format());
                total += p.getMetric().count();
            }
            logger.info("total={}", total);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        } finally {
            search.shutdown();
            ingest.flushIngest();
            ingest.waitForResponses(TimeValue.timeValueSeconds(60));
            ingest.shutdown();
        }
    }

    @Override
    public WithArticles execute() {
        super.execute();
        logger.debug("executing");
        boolean failure = false;
        boolean complete = false;
        // strategy: iterate over ezdb/Manifestation, then iterate over existing dates,
        // pick the holdings, offer into queue
        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setIndices(settings.get("ezdb-index"))
                .setTypes(settings.get("ezdb-type"))
                .setSize(size)
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(millis));

        QueryBuilder queryBuilder = matchAllQuery();
        FilterBuilder filterBuilder = existsFilter("dates");
        if (identifier != null) {
            // execute on a single ID
            filterBuilder = null;
            queryBuilder = termQuery("_id", identifier);
        }
        // filter ISSN
        if (settings().getAsBoolean("issnonly", false)) {
            filterBuilder = boolFilter()
                    .must(existsFilter("dates"))
                    .must(existsFilter("identifiers.issn"));
        }
        if (settings().getAsBoolean("eonly", false)) {
            filterBuilder = boolFilter()
                    .must(existsFilter("dates"))
                    .must(termFilter("mediatype", "computer"));
        }
        queryBuilder = filterBuilder != null ? filteredQuery(queryBuilder, filterBuilder) : queryBuilder;
        searchRequest.setQuery(queryBuilder);

        SearchResponse searchResponse = searchRequest.execute().actionGet();
        long total = searchResponse.getHits().getTotalHits();
        long count = 0L;
        long lastpercent = -1L;
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
                    String id = hit.getId();
                    if (docs.contains(id)) {
                        continue;
                    }
                    docs.add(id);
                    Set<Integer> dates = newLinkedHashSet();
                    List<TitleRecord> titleRecords = newLinkedList();
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
                            getQueue().offer(new SerialItemPipelineElement().set(serialItem));
                        }
                    }
                    count++;
                    long percent = count * 100 / total;
                    if (percent != lastpercent && logger.isInfoEnabled()) {
                        logger.info("{}/{} {}%", count, total, percent);
                        for (Pipeline pipeline : getPipelines()) {
                            WithArticlesPipeline p = (WithArticlesPipeline)pipeline;
                            logger.info("{} throughput={} {} {} mean={} mldup={} xrefdup={}",
                                    p.toString(),
                                    p.getMetric().oneMinuteRate(),
                                    p.getMetric().fiveMinuteRate(),
                                    p.getMetric().fifteenMinuteRate(),
                                    p.getMetric().meanRate(),
                                    p.getMedlineDuplicates().get(),
                                    p.getXrefDuplicates().get()
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
        return this;
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

    public Set<String> docs() {
        return docs;
    }

    private TitleRecord expand(String id) throws IOException {
        QueryBuilder queryBuilder = termQuery("IdentifierZDB.identifierZDB", id);
        SearchRequestBuilder searchRequestBuilder = service.client().prepareSearch()
                .setIndices(service.settings().get("zdb-index", "zdb"))
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
        SearchRequestBuilder searchRequestBuilder = service.client().prepareSearch()
                .setIndices(service.settings().get("doaj-index", "doaj"))
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
