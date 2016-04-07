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
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.settings.Settings;
import org.xbib.metrics.Meter;
import org.xbib.tools.merge.Merger;
import org.xbib.tools.merge.holdingslicenses.entities.TitleRecord;
import org.xbib.tools.metrics.Metrics;
import org.xbib.util.ExceptionFormatter;
import org.xbib.util.IndexDefinition;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
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

    private ArticlesMerger merger;

    private Meter queryMetric;

    @Override
    @SuppressWarnings("unchecked")
    public int run(Settings settings) throws Exception {
        this.merger = this;
        this.metrics = new Metrics();
        this.queryMetric = new Meter();
        queryMetric.spawn(5L);
        metrics.scheduleMetrics(settings, "meterquery", queryMetric);
        return super.run(settings);
    }

    @SuppressWarnings("unchecked")
    public Pipeline<ArticlesMergerWorker, SerialItemRequest> getPipeline() {
        return pipeline;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected WorkerProvider provider() {
        return new WorkerProvider<ArticlesMergerWorker>() {
            int i = 0;

            @Override
            public ArticlesMergerWorker get(Pipeline pipeline) {
                return new ArticlesMergerWorker(settings, merger, i++).setPipeline(pipeline);
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void prepareRequests() throws Exception {
        super.prepareRequests();
        Map<String,IndexDefinition> indexDefinitionMap = getInputIndexDefinitionMap();

        boolean failure = false;
        boolean complete = false;
        int scrollSize = settings.getAsInt("scrollsize", 10);
        long scrollMillis = settings.getAsTime("scrolltimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(60)).millis();

        // strategy: iterate over ezdb manifestations,
        // then iterate over existing dates,
        // pick the holdings, offer to workers

        QueryBuilder queryBuilder = matchAllQuery();
        QueryBuilder filterBuilder = existsQuery("dates");
        String identifier = settings.get("identifier");
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
        IndexDefinition indexDefinition = indexDefinitionMap.get("ezdb");
        SearchRequestBuilder searchRequest = search.client().prepareSearch()
                .setIndices(indexDefinition.getIndex())
                .setTypes(indexDefinition.getType())
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .setQuery(queryBuilder);
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        while (!failure && !complete && searchResponse.getHits().getHits().length > 0) {
            for (SearchHit hit : searchResponse.getHits()) {
                try {
                    if (getPipeline().getWorkers().isEmpty()) {
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
                    TitleRecord titleRecord = expandZdbId(indexDefinitionMap, id);
                    if (titleRecord == null) {
                        continue;
                    }
                    Collection<String> issns = (Collection<String>) titleRecord.getIdentifiers().get("formattedissn");
                    if (issns != null) {
                        for (String issn : issns) {
                            expandOA(indexDefinitionMap, titleRecord, issn);
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
                                TitleRecord m = expandZdbId(indexDefinitionMap, relid);
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
                } catch (Throwable e) {
                    logger.error("error passing data to worker, exiting", e);
                    logger.error(ExceptionFormatter.format(e));
                    failure = true;
                    break;
                }
            }
            searchResponse = search.client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet();
        }
        search().client().prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet();
    }

    @Override
    protected void disposeRequests(int returncode) throws IOException {
    }

    @Override
    protected void disposeResources(int returncode) throws IOException {
    }

    @Override
    protected void waitFor() throws IOException {
        getPipeline().waitFor(new SerialItemRequest());
        long total = 0L;
        for (ArticlesMergerWorker worker : getPipeline().getWorkers()) {
            logger.info("worker {}, count {}, took {}",
                    worker,
                    worker.getMetric().getCount(),
                    TimeValue.timeValueMillis(worker.getMetric().elapsed()).format());
            total += worker.getMetric().getCount();
        }
        logger.info("total={}", total);
    }

    private TitleRecord expandZdbId(Map<String,IndexDefinition> indexDefinitionMap, String zdbId)
            throws IOException {
        IndexDefinition indexDefinition = indexDefinitionMap.get("zdb");
        SearchRequestBuilder searchRequestBuilder = search.client().prepareSearch()
                .setIndices(indexDefinition.getIndex())
                .setQuery(termQuery("IdentifierZDB.identifierZDB", zdbId))
                .setSize(1);
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        if (hits.getHits().length == 0) {
            logger.warn("ZDB-ID {} does not exist", zdbId);
            return null;
        }
        return new TitleRecord(hits.getAt(0).getSource());
    }

    private TitleRecord expandOA(Map<String,IndexDefinition> indexDefinitionMap, TitleRecord titleRecord, String issn)
            throws IOException {
        IndexDefinition indexDefinition = indexDefinitionMap.get("doaj");
        SearchRequestBuilder searchRequestBuilder = search.client().prepareSearch()
                .setIndices(indexDefinition.getIndex())
                .setQuery(termQuery("dc:identifier", issn))
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

    IndexDefinition getMedlineIndex() {
        Map<String,IndexDefinition> indexDefinitionMap = getInputIndexDefinitionMap();
        return indexDefinitionMap.get("medline");
    }

    IndexDefinition getXrefIndex() {
        Map<String,IndexDefinition> indexDefinitionMap = getInputIndexDefinitionMap();
        return indexDefinitionMap.get("xref");
    }

    IndexDefinition getArticlesIndex() {
        Map<String,IndexDefinition> indexDefinitionMap = getOutputIndexDefinitionMap();
        return indexDefinitionMap.get("articles");
    }
}
