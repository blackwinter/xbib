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
package org.xbib.tools.merge.holdingslicenses.simple;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.xbib.common.settings.Settings;
import org.xbib.metrics.Meter;
import org.xbib.tools.merge.holdingslicenses.entities.Holding;
import org.xbib.tools.merge.holdingslicenses.entities.Indicator;
import org.xbib.tools.merge.holdingslicenses.entities.License;
import org.xbib.tools.merge.holdingslicenses.entities.MonographVolume;
import org.xbib.tools.merge.holdingslicenses.entities.MonographVolumeHolding;
import org.xbib.tools.merge.holdingslicenses.entities.TitleRecord;
import org.xbib.tools.merge.holdingslicenses.support.TitleRecordRequest;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.Worker;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

public class SimpleHoldingsLicensesWorker
        implements Worker<Pipeline<SimpleHoldingsLicensesWorker, TitleRecordRequest>, TitleRecordRequest> {

    private Pipeline<SimpleHoldingsLicensesWorker, TitleRecordRequest> pipeline;
    private Meter metric;
    private final int number;
    private final SimpleHoldingsLicensesMerger simpleHoldingsLicensesMerger;
    private final Logger logger;
    private final int scrollSize;
    private final long scrollMillis;
    private final long timeoutSeconds;

    @SuppressWarnings("unchecked")
    public SimpleHoldingsLicensesWorker(Settings settings,
                                        SimpleHoldingsLicensesMerger simpleHoldingsLicensesMerger,
                                        int number) {
        this.number = number;
        this.simpleHoldingsLicensesMerger = simpleHoldingsLicensesMerger;
        this.logger = LogManager.getLogger(toString());
        this.metric = new Meter();
        metric.spawn(5L);
        simpleHoldingsLicensesMerger.getMetrics().scheduleMetrics(settings, "meter" + number, metric);
        this.scrollSize = settings.getAsInt("worker.scrollsize", 10); // per shard!
        this.scrollMillis = settings.getAsTime("worker.scrolltimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(360)).millis();
        this.timeoutSeconds = settings.getAsTime("worker.timeout", org.xbib.common.unit.TimeValue.timeValueSeconds(60)).millis();
    }

    @Override
    public Worker<Pipeline<SimpleHoldingsLicensesWorker, TitleRecordRequest>, TitleRecordRequest>
            setPipeline(Pipeline<SimpleHoldingsLicensesWorker, TitleRecordRequest> pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    @Override
    public Pipeline<SimpleHoldingsLicensesWorker, TitleRecordRequest> getPipeline() {
        return pipeline;
    }

    @Override
    public SimpleHoldingsLicensesWorker setMetric(Meter metric) {
        this.metric = metric;
        return this;
    }

    @Override
    public Meter getMetric() {
        return metric;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TitleRecordRequest call() throws Exception {
        logger.info("worker {} starting", this);
        TitleRecordRequest request = null;
        TitleRecord titleRecord = null;
        try {
            while ((request = getPipeline().getQueue().take()) != null) {
                titleRecord = request.get();
                if (titleRecord == null) {
                    break;
                }
                long t0 = System.nanoTime();
                process(titleRecord);
                long t1 = System.nanoTime();
                long delta = (t1 -t0) / 1000000;
                // warn if delta is longer than 10 secs
                if (delta > 10000) {
                    logger.warn("long processing of {}: {} ms", titleRecord.externalID(), delta);
                }
                metric.mark();
            }
            getPipeline().quit(this);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            logger.error("exiting, exception while processing {}", titleRecord);
            getPipeline().quit(this, e);
        } finally {
            metric.stop();
        }
        return request;
    }

    @Override
    public void close() throws IOException {
        logger.info("worker {} closing", this);
    }

    @Override
    public String toString() {
        return SimpleHoldingsLicensesMerger.class.getSimpleName() + "." + number;
    }

    private void process(TitleRecord titleRecord) throws IOException {
        boolean isOnline = "online resource".equals(titleRecord.carrierType());
        // collect all IDs from all carrier relations that we want to integrate
        Collection<String> internalIDs = new HashSet<>();
        Collection<String> externalIDs = new HashSet<>();
        for (String relation : TitleRecord.getCarrierRelations()) {
            // internal for ZDB lookup
            if (titleRecord.getRelations().containsKey(relation)) {
                internalIDs.addAll(titleRecord.getRelations().get(relation));
            }
            // external for EZB/union catalog lookup
            if (titleRecord.getRelationsExternalIDs().containsKey(relation)) {
                externalIDs.addAll(titleRecord.getRelationsExternalIDs().get(relation));
            }
        }
        logger.debug("IDs: {}/{} -> {}/{} online={}",
                titleRecord.id(), titleRecord.externalID(),
                internalIDs, externalIDs, isOnline);
        addSerialHoldings(titleRecord, "(DE-600)" + titleRecord.id());
        for (String id : internalIDs) {
            addSerialHoldings(titleRecord, "(DE-600)" + id.toUpperCase());
        }
        for (String id : externalIDs) {
            addLicenses(titleRecord, id);
            addIndicators(titleRecord, id);
        }
        addMonographs(titleRecord);
        addOpenAccess(titleRecord);
        simpleHoldingsLicensesMerger.getHoldingsLicensesIndexer().index(titleRecord);
    }

    private void addSerialHoldings(TitleRecord titleRecord, String id) throws IOException {
        QueryBuilder queryBuilder = termQuery("ParentRecordIdentifier.identifierForTheParentRecord", id);
        SearchRequestBuilder searchRequest = simpleHoldingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(simpleHoldingsLicensesMerger.getSourceHoldingsIndex())
                .setQuery(queryBuilder)
                .setSize(scrollSize)  // size is per shard!
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        logger.debug("serial holdings: search request = {} hits = {}",
                searchRequest.toString(),
                searchResponse.getHits().getTotalHits());
        while (searchResponse.getHits().getHits().length > 0){
            getMetric().mark();
            for (SearchHit hit : searchResponse.getHits()) {
                Holding holding = new Holding(hit.getSource());
                if (holding.isDeleted()) {
                    continue;
                }
                String isil = holding.getISIL();
                if (isil == null) {
                    continue;
                }
                // mapped ISIL?
                if (simpleHoldingsLicensesMerger.mappedISIL().lookup().containsKey(isil)) {
                    isil = (String) simpleHoldingsLicensesMerger.mappedISIL().lookup().get(isil);
                }
                // consortia?
                if (simpleHoldingsLicensesMerger.consortiaLookup().lookupISILs().containsKey(isil)) {
                    List<String> list = simpleHoldingsLicensesMerger.consortiaLookup().lookupISILs().get(isil);
                    for (String expandedisil : list) {
                        // blacklisted expanded ISIL?
                        if (simpleHoldingsLicensesMerger.blackListedISIL().lookup(expandedisil)) {
                            continue;
                        }
                        // new Holding for each ISIL
                        holding = new Holding(holding.map());
                        holding.setISIL(isil);
                        holding.setServiceISIL(expandedisil);
                        holding.setName(simpleHoldingsLicensesMerger.bibdatLookup()
                                .lookupName().get(expandedisil));
                        holding.setRegion(simpleHoldingsLicensesMerger.bibdatLookup()
                                .lookupRegion().get(expandedisil));
                        holding.setOrganization(simpleHoldingsLicensesMerger.bibdatLookup()
                                .lookupOrganization().get(expandedisil));
                        titleRecord.addRelatedHolding(expandedisil, holding);
                    }
                } else {
                    // blacklisted ISIL?
                    if (simpleHoldingsLicensesMerger.blackListedISIL().lookup(isil)) {
                        continue;
                    }
                    holding.setName(simpleHoldingsLicensesMerger.bibdatLookup().lookupName().get(isil));
                    holding.setRegion(simpleHoldingsLicensesMerger.bibdatLookup().lookupRegion().get(isil));
                    holding.setOrganization(simpleHoldingsLicensesMerger.bibdatLookup().lookupOrganization().get(isil));
                    titleRecord.addRelatedHolding(isil, holding);
                }
            }
            searchResponse = simpleHoldingsLicensesMerger.search().client()
                    .prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        }
        simpleHoldingsLicensesMerger.search().client()
                .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
    }

    private void addLicenses(TitleRecord titleRecord, String zdbId) throws IOException {
        QueryBuilder queryBuilder = termsQuery("ezb:zdbid", zdbId);
        // getSize is per shard
        SearchRequestBuilder searchRequest = simpleHoldingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(simpleHoldingsLicensesMerger.getSourceLicenseIndex())
                .setQuery(queryBuilder)
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        logger.debug("licenses: search request = {} hits = {}",
                searchRequest.toString(),
                searchResponse.getHits().getTotalHits());
        while (searchResponse.getHits().getHits().length > 0) {
            getMetric().mark();
            for (SearchHit hit :  searchResponse.getHits()) {
                License license = new License(hit.getSource());
                logger.debug("processing license {}", license);
                if (license.isDeleted()) {
                    continue;
                }
                String isil = license.getISIL();
                if (isil == null) {
                    continue;
                }
                // mapped ISIL?
                if (simpleHoldingsLicensesMerger.mappedISIL().lookup().containsKey(isil)) {
                    isil = (String) simpleHoldingsLicensesMerger.mappedISIL().lookup().get(isil);
                }
                // consortia?
                if (simpleHoldingsLicensesMerger.consortiaLookup().lookupISILs().containsKey(isil)) {
                    List<String> list = simpleHoldingsLicensesMerger.consortiaLookup().lookupISILs().get(isil);
                    for (String expandedisil : list) {
                        // blacklisted expanded ISIL?
                        if (simpleHoldingsLicensesMerger.blackListedISIL().lookup(expandedisil)) {
                            continue;
                        }
                        // new License for each ISIL
                        license = new License(license.map());
                        license.setISIL(isil);
                        license.setServiceISIL(expandedisil);
                        license.setName(simpleHoldingsLicensesMerger.bibdatLookup().lookupName().get(expandedisil));
                        license.setRegion(simpleHoldingsLicensesMerger.bibdatLookup().lookupRegion().get(expandedisil));
                        license.setOrganization(simpleHoldingsLicensesMerger.bibdatLookup().lookupOrganization().get(expandedisil));
                        titleRecord.addRelatedHolding(expandedisil, license);
                    }
                } else {
                    // blacklisted ISIL?
                    if (simpleHoldingsLicensesMerger.blackListedISIL().lookup(isil)) {
                        continue;
                    }
                    license.setName(simpleHoldingsLicensesMerger.bibdatLookup().lookupName().get(isil));
                    license.setRegion(simpleHoldingsLicensesMerger.bibdatLookup().lookupRegion().get(isil));
                    license.setOrganization(simpleHoldingsLicensesMerger.bibdatLookup().lookupOrganization().get(isil));
                    titleRecord.addRelatedHolding(isil, license);
                }
            }
            searchResponse = simpleHoldingsLicensesMerger.search().client()
                   .prepareSearchScroll(searchResponse.getScrollId())
                   .setScroll(TimeValue.timeValueMillis(scrollMillis))
                   .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        }
        simpleHoldingsLicensesMerger.search().client()
                .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
    }

    private void addIndicators(TitleRecord titleRecord, String zdbId) throws IOException {
        QueryBuilder queryBuilder = termsQuery("xbib:identifier", zdbId);
        SearchRequestBuilder searchRequest = simpleHoldingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(simpleHoldingsLicensesMerger.getSourceIndicatorIndex())
                .setQuery(queryBuilder)
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        logger.debug("indicators: search request = {} hits = {}",
                searchRequest.toString(),
                searchResponse.getHits().getTotalHits());
        while (searchResponse.getHits().getHits().length > 0) {
            getMetric().mark();
            for (SearchHit hit :  searchResponse.getHits()) {
                Indicator indicator = new Indicator(hit.getSource());
                String isil = indicator.getISIL();
                // invalid/unknown institution in indicator
                if (isil == null) {
                    continue;
                }
                // mapped ISIL?
                if (simpleHoldingsLicensesMerger.mappedISIL().lookup().containsKey(isil)) {
                    isil = (String) simpleHoldingsLicensesMerger.mappedISIL().lookup().get(isil);
                }
                // consortia?
                if (simpleHoldingsLicensesMerger.consortiaLookup().lookupISILs().containsKey(isil)) {
                    List<String> list = simpleHoldingsLicensesMerger.consortiaLookup().lookupISILs().get(isil);
                    for (String expandedisil : list) {
                        // blacklisted expanded ISIL?
                        if (simpleHoldingsLicensesMerger.blackListedISIL().lookup(expandedisil)) {
                            continue;
                        }
                        indicator = new Indicator(indicator.map());
                        indicator.setISIL(isil);
                        indicator.setServiceISIL(expandedisil);
                        indicator.setName(simpleHoldingsLicensesMerger.bibdatLookup().lookupName().get(expandedisil));
                        indicator.setRegion(simpleHoldingsLicensesMerger.bibdatLookup().lookupRegion().get(expandedisil));
                        indicator.setOrganization(simpleHoldingsLicensesMerger.bibdatLookup().lookupOrganization().get(expandedisil));
                        titleRecord.addRelatedIndicator(expandedisil, indicator);
                    }
                } else {
                    // blacklisted ISIL?
                    if (simpleHoldingsLicensesMerger.blackListedISIL().lookup(isil)) {
                        continue;
                    }
                    indicator.setName(simpleHoldingsLicensesMerger.bibdatLookup().lookupName().get(isil));
                    indicator.setRegion(simpleHoldingsLicensesMerger.bibdatLookup().lookupRegion().get(isil));
                    indicator.setOrganization(simpleHoldingsLicensesMerger.bibdatLookup().lookupOrganization().get(isil));
                    titleRecord.addRelatedIndicator(isil, indicator);
                }
            }
            searchResponse = simpleHoldingsLicensesMerger.search().client()
                    .prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS); // must time out
        }
        simpleHoldingsLicensesMerger.search().client()
                .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
    }

    private void addMonographs(TitleRecord titleRecord) throws IOException {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.should(termQuery("IdentifierZDB.identifierZDB", titleRecord.externalID()));
        for (String issn : titleRecord.getISSNs()) {
            // not guaranteed which ISSN is really the print or online edition!
            queryBuilder.should(termQuery("IdentifierISSN.identifierISSN", issn));
            queryBuilder.should(termQuery("IdentifierSerial.identifierISSNOnline",issn));
        }
        SearchRequestBuilder searchRequest = simpleHoldingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(simpleHoldingsLicensesMerger.getSourceMonographicIndex())
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .setQuery(queryBuilder)
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        logger.debug("monographs: search request={} hits={}",
                searchRequest.toString(), searchResponse.getHits().getTotalHits());
        while (searchResponse.getHits().getHits().length > 0) {
            getMetric().mark();
            for (SearchHit hit : searchResponse.getHits()) {
                Map<String, Object> m = hit.getSource();
                MonographVolume volume = new MonographVolume(m, titleRecord);
                addExtraHoldings(volume);
                addSeriesVolumeHoldings(volume);
            }
            searchResponse = simpleHoldingsLicensesMerger.search().client()
                    .prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        }
        simpleHoldingsLicensesMerger.search().client()
                .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * Extra holdings are from a monographic catalog, but not in the base serials catalog.
     * @param volume the volume
     */
    @SuppressWarnings("unchecked")
    private void addExtraHoldings(MonographVolume volume) {
        TitleRecord titleRecord = volume.getTitleRecord();
        String key = volume.id();
        SearchRequestBuilder holdingsSearchRequest = simpleHoldingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(simpleHoldingsLicensesMerger.getSourceMonographicHoldingsIndex())
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .setQuery(termQuery("xbib.uid", key))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse holdingSearchResponse = holdingsSearchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        logger.debug("extraHoldings: search request = {} hits={}",
                holdingsSearchRequest.toString(), holdingSearchResponse.getHits().getTotalHits());
        while (holdingSearchResponse.getHits().getHits().length > 0) {
            getMetric().mark();
            for (SearchHit holdingHit :  holdingSearchResponse.getHits()) {
                Object o = holdingHit.getSource().get("Item");
                if (!(o instanceof List)) {
                    o = Collections.singletonList(o);
                }
                for (Map<String,Object> item : (List<Map<String,Object>>)o) {
                    if (item != null && !item.isEmpty()) {
                        MonographVolumeHolding volumeHolding = new MonographVolumeHolding(item, volume);
                        String isil = volumeHolding.getISIL();
                        if (isil == null) {
                            continue;
                        }
                        // mapped ISIL?
                        if (simpleHoldingsLicensesMerger.mappedISIL().lookup().containsKey(isil)) {
                            isil = (String) simpleHoldingsLicensesMerger.mappedISIL().lookup().get(isil);
                        }
                        // blacklisted ISIL?
                        if (simpleHoldingsLicensesMerger.blackListedISIL().lookup(isil)) {
                            continue;
                        }
                        volumeHolding.addParent(volume.externalID());
                        volumeHolding.setMediaType(titleRecord.mediaType());
                        volumeHolding.setCarrierType(titleRecord.carrierType());
                        volumeHolding.setDate(volume.firstDate(), volume.lastDate());
                        volumeHolding.setName(simpleHoldingsLicensesMerger.bibdatLookup()
                                .lookupName().get(isil));
                        volumeHolding.setRegion(simpleHoldingsLicensesMerger.bibdatLookup()
                                .lookupRegion().get(isil));
                        volumeHolding.setOrganization(simpleHoldingsLicensesMerger.bibdatLookup()
                                .lookupOrganization().get(isil));
                        volumeHolding.setServiceMode(simpleHoldingsLicensesMerger.statusCodeMapper()
                                .lookup(volumeHolding.getStatus()));
                        if ("interlibrary".equals(volumeHolding.getServiceType()) && isil != null) {
                            volume.addRelatedHolding(isil, volumeHolding);
                        }
                    }
                }
            }
            holdingSearchResponse = simpleHoldingsLicensesMerger.search().client()
                    .prepareSearchScroll(holdingSearchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        }
        simpleHoldingsLicensesMerger.search().client()
                .prepareClearScroll().addScrollId(holdingSearchResponse.getScrollId())
                .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * Search all holdings in this series, if it is a series
     * @param parent the parent volume
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private void addSeriesVolumeHoldings(MonographVolume parent) throws IOException {
        TitleRecord titleRecord = parent.getTitleRecord();
        if (parent.id() == null || parent.id().isEmpty()) {
            return;
        }
        // search children volumes of the series (conference, processing, abstract, ...)
        SearchRequestBuilder searchRequest = simpleHoldingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(simpleHoldingsLicensesMerger.getSourceMonographicIndex())
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .setQuery(boolQuery().should(termQuery("SeriesAddedEntryUniformTitle.designation", parent.id()))
                        .should(termQuery("RecordIdentifierSuper.recordIdentifierSuper", parent.id())))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        logger.debug("addSeriesVolumeHoldings search request={} hits={}",
                searchRequest.toString(), searchResponse.getHits().getTotalHits());
        while (searchResponse.getHits().getHits().length > 0) {
            getMetric().mark();
            for (SearchHit hit : searchResponse.getHits()) {
                MonographVolume volume = new MonographVolume(hit.getSource(), titleRecord);
                volume.addParent(titleRecord.externalID());
                // for each conference/congress, search holdings
                SearchRequestBuilder holdingsSearchRequest = simpleHoldingsLicensesMerger.search().client()
                        .prepareSearch()
                        .setIndices(simpleHoldingsLicensesMerger.getSourceMonographicHoldingsIndex())
                        .setSize(scrollSize)
                        .setScroll(TimeValue.timeValueMillis(scrollMillis))
                        .setQuery(termQuery("xbib.uid", volume.id()))
                        .addSort(SortBuilders.fieldSort("_doc"));
                SearchResponse holdingSearchResponse = holdingsSearchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
                getMetric().mark();
                logger.debug("addSeriesVolumeHoldings search request={} hits={}",
                        holdingsSearchRequest.toString(), holdingSearchResponse.getHits().getTotalHits());
                while (holdingSearchResponse.getHits().getHits().length > 0) {
                    getMetric().mark();
                    for (SearchHit holdingHit : holdingSearchResponse.getHits()) {
                        // one hit, many items. Iterate over items
                        Object o = holdingHit.getSource().get("Item");
                        if (!(o instanceof List)) {
                            o = Collections.singletonList(o);
                        }
                        for (Map<String,Object> item : (List<Map<String,Object>>)o) {
                            if (item != null && !item.isEmpty()) {
                                MonographVolumeHolding volumeHolding = new MonographVolumeHolding(item, volume);
                                String isil = volumeHolding.getISIL();
                                if (isil == null) {
                                    continue;
                                }
                                // mapped ISIL?
                                if (simpleHoldingsLicensesMerger.mappedISIL().lookup().containsKey(isil)) {
                                    isil = (String) simpleHoldingsLicensesMerger.mappedISIL().lookup().get(isil);
                                }
                                // blacklisted ISIL?
                                if (simpleHoldingsLicensesMerger.blackListedISIL().lookup(isil)) {
                                    continue;
                                }
                                volumeHolding.addParent(titleRecord.externalID());
                                volumeHolding.addParent(volume.externalID());
                                volumeHolding.setMediaType(titleRecord.mediaType());
                                volumeHolding.setCarrierType(titleRecord.carrierType());
                                volumeHolding.setDate(volume.firstDate(), volume.lastDate());
                                volumeHolding.setName(simpleHoldingsLicensesMerger.bibdatLookup()
                                        .lookupName().get(isil));
                                volumeHolding.setRegion(simpleHoldingsLicensesMerger.bibdatLookup()
                                        .lookupRegion().get(isil));
                                volumeHolding.setOrganization(simpleHoldingsLicensesMerger.bibdatLookup()
                                        .lookupOrganization().get(isil));
                                volumeHolding.setServiceMode(simpleHoldingsLicensesMerger.statusCodeMapper()
                                        .lookup(volumeHolding.getStatus()));
                                if ("interlibrary".equals(volumeHolding.getServiceType()) && isil != null) {
                                    volume.addRelatedHolding(isil, volumeHolding);
                                }
                            }
                        }
                    }
                    holdingSearchResponse = simpleHoldingsLicensesMerger.search().client()
                           .prepareSearchScroll(holdingSearchResponse.getScrollId())
                           .setScroll(TimeValue.timeValueMillis(scrollMillis))
                           .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
               }
                simpleHoldingsLicensesMerger.search().client()
                        .prepareClearScroll().addScrollId(holdingSearchResponse.getScrollId())
                        .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
                // this also copies holdings from the found volume to the title record
                titleRecord.addVolume(volume);
            }
            searchResponse = simpleHoldingsLicensesMerger.search().client()
                    .prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        }
        simpleHoldingsLicensesMerger.search().client()
                .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private void addOpenAccess(TitleRecord titleRecord) {
        if (titleRecord.hasIdentifiers()) {
            Collection<String> issns = (Collection<String>) titleRecord.getIdentifiers().get("formattedissn");
            if (issns != null) {
                titleRecord.setOpenAccess(searchOpenAccess(issns));
            }
        }
    }

    private boolean searchOpenAccess(Collection<String> issn) {
        SearchRequestBuilder searchRequestBuilder = simpleHoldingsLicensesMerger.search().client()
                .prepareSearch()
                .setSize(0)
                .setIndices(simpleHoldingsLicensesMerger.getSourceOpenAccessIndex())
                .setQuery(termsQuery("dc:identifier", issn));
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        long total = searchResponse.getHits().getTotalHits();
        logger.debug("openaccess: query {}", searchRequestBuilder, total);
        return total > 0;
    }

}
