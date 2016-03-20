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
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.xbib.common.settings.Settings;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.metrics.Meter;
import org.xbib.tools.merge.holdingslicenses.entities.Holding;
import org.xbib.tools.merge.holdingslicenses.entities.Indicator;
import org.xbib.tools.merge.holdingslicenses.entities.License;
import org.xbib.tools.merge.holdingslicenses.entities.MonographVolume;
import org.xbib.tools.merge.holdingslicenses.entities.MonographVolumeHolding;
import org.xbib.tools.merge.holdingslicenses.entities.SerialRecord;
import org.xbib.tools.merge.holdingslicenses.support.SerialRecordRequest;
import org.xbib.tools.merge.holdingslicenses.support.StatCounter;
import org.xbib.util.MultiMap;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.Worker;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

public class SimpleHoldingsLicensesWorker
        implements Worker<Pipeline<SimpleHoldingsLicensesWorker, SerialRecordRequest>, SerialRecordRequest> {

    private Pipeline<SimpleHoldingsLicensesWorker, SerialRecordRequest> pipeline;
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
    public Worker<Pipeline<SimpleHoldingsLicensesWorker, SerialRecordRequest>, SerialRecordRequest>
            setPipeline(Pipeline<SimpleHoldingsLicensesWorker, SerialRecordRequest> pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    @Override
    public Pipeline<SimpleHoldingsLicensesWorker, SerialRecordRequest> getPipeline() {
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
    public SerialRecordRequest call() throws Exception {
        logger.info("worker {} starting", this);
        SerialRecordRequest request = null;
        SerialRecord serialRecord = null;
        try {
            while ((request = getPipeline().getQueue().take()) != null) {
                serialRecord = request.get();
                if (serialRecord == null) {
                    break;
                }
                long t0 = System.nanoTime();
                process(serialRecord);
                long t1 = System.nanoTime();
                long delta = (t1 -t0) / 1000000;
                // warn if delta is longer than 10 secs
                if (delta > 10000) {
                    logger.warn("long processing of {}: {} ms", serialRecord.externalID(), delta);
                }
                metric.mark();
            }
            getPipeline().quit(this);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            logger.error("exiting, exception while processing {}", serialRecord);
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

    private void process(SerialRecord serialRecord) throws IOException {
        addSerialHoldings(serialRecord);
        addMonographs(serialRecord);
        if ("online resource".equals(serialRecord.carrierType())) {
            // EZB
            addLicenses(serialRecord, serialRecord.externalID());
            addIndicators(serialRecord, serialRecord.externalID());
        } else {
            if (serialRecord.getOnlineExternalID() != null) {
                // join EZB to print resource
                addLicenses(serialRecord, serialRecord.getOnlineExternalID());
                addIndicators(serialRecord, serialRecord.getOnlineExternalID());
            }
        }
        addOpenAccess(serialRecord);
        indexTitleRecord(serialRecord);
    }

    private void addSerialHoldings(SerialRecord serialRecord) throws IOException {
        QueryBuilder queryBuilder =
                termQuery("ParentRecordIdentifier.identifierForTheParentRecord",
                        "(DE-600)" + serialRecord.id());
        SearchRequestBuilder searchRequest = simpleHoldingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(simpleHoldingsLicensesMerger.getSourceHoldingsIndex())
                .setQuery(queryBuilder)
                .setSize(scrollSize)  // size is per shard!
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);

        logger.debug("search request = {} hits = {}",
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
                        serialRecord.addRelatedHolding(expandedisil, holding);
                    }
                } else {
                    // blacklisted ISIL?
                    if (simpleHoldingsLicensesMerger.blackListedISIL().lookup(isil)) {
                        continue;
                    }
                    holding.setName(simpleHoldingsLicensesMerger.bibdatLookup().lookupName().get(isil));
                    holding.setRegion(simpleHoldingsLicensesMerger.bibdatLookup().lookupRegion().get(isil));
                    holding.setOrganization(simpleHoldingsLicensesMerger.bibdatLookup().lookupOrganization().get(isil));
                    serialRecord.addRelatedHolding(isil, holding);
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

    private void addLicenses(SerialRecord serialRecord, String zdbId) throws IOException {
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
        logger.debug("search request = {} hits = {}",
                searchRequest.toString(),
                searchResponse.getHits().getTotalHits());
        while (searchResponse.getHits().getHits().length > 0) {
            getMetric().mark();
            for (SearchHit hit :  searchResponse.getHits()) {
                License license = new License(hit.getSource());
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
                        serialRecord.addRelatedHolding(expandedisil, license);
                    }
                } else {
                    // blacklisted ISIL?
                    if (simpleHoldingsLicensesMerger.blackListedISIL().lookup(isil)) {
                        continue;
                    }
                    license.setName(simpleHoldingsLicensesMerger.bibdatLookup().lookupName().get(isil));
                    license.setRegion(simpleHoldingsLicensesMerger.bibdatLookup().lookupRegion().get(isil));
                    license.setOrganization(simpleHoldingsLicensesMerger.bibdatLookup().lookupOrganization().get(isil));
                    serialRecord.addRelatedHolding(isil, license);
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

    private void addIndicators(SerialRecord serialRecord, String zdbId) throws IOException {
        QueryBuilder queryBuilder = termsQuery("xbib:identifier", zdbId);
        SearchRequestBuilder searchRequest = simpleHoldingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(simpleHoldingsLicensesMerger.getSourceIndicatorIndex())
                .setQuery(queryBuilder)
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        logger.debug("search request = {} hits = {}",
                searchRequest.toString(),
                searchResponse.getHits().getTotalHits());
        while (searchResponse.getHits().getHits().length > 0) {
            getMetric().mark();
            for (SearchHit hit :  searchResponse.getHits()) {
                Indicator indicator = new Indicator(hit.getSource());
                String isil = indicator.getISIL();
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
                        serialRecord.addRelatedIndicator(expandedisil, indicator);
                    }
                } else {
                    // blacklisted ISIL?
                    if (simpleHoldingsLicensesMerger.blackListedISIL().lookup(isil)) {
                        continue;
                    }
                    indicator.setName(simpleHoldingsLicensesMerger.bibdatLookup().lookupName().get(isil));
                    indicator.setRegion(simpleHoldingsLicensesMerger.bibdatLookup().lookupRegion().get(isil));
                    indicator.setOrganization(simpleHoldingsLicensesMerger.bibdatLookup().lookupOrganization().get(isil));
                    serialRecord.addRelatedIndicator(isil, indicator);
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

    private void addMonographs(SerialRecord serialRecord) throws IOException {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.should(termQuery("IdentifierZDB.identifierZDB", serialRecord.externalID()));
        for (String issn : serialRecord.getISSNs()) {
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
        logger.debug("search request={} hits={}",
                searchRequest.toString(), searchResponse.getHits().getTotalHits());
        while (searchResponse.getHits().getHits().length > 0) {
            getMetric().mark();
            for (SearchHit hit : searchResponse.getHits()) {
                Map<String, Object> m = hit.getSource();
                MonographVolume volume = new MonographVolume(m, serialRecord);
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
        SerialRecord serialRecord = volume.getSerialRecord();
        String key = volume.id();
        SearchRequestBuilder holdingsSearchRequest = simpleHoldingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(simpleHoldingsLicensesMerger.getSourceMonographicHoldingsIndex())
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .setQuery(termQuery("xbib.uid", key))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse holdingSearchResponse = holdingsSearchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        logger.debug("searchExtraHoldings search request = {} hits={}",
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
                        volumeHolding.setMediaType(serialRecord.mediaType());
                        volumeHolding.setCarrierType(serialRecord.carrierType());
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
        SerialRecord serialRecord = parent.getSerialRecord();
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
        logger.debug("searchSeriesVolumeHoldings search request={} hits={}",
                searchRequest.toString(), searchResponse.getHits().getTotalHits());
        while (searchResponse.getHits().getHits().length > 0) {
            getMetric().mark();
            for (SearchHit hit : searchResponse.getHits()) {
                MonographVolume volume = new MonographVolume(hit.getSource(), serialRecord);
                volume.addParent(serialRecord.externalID());
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
                logger.debug("searchSeriesVolumeHoldings search request={} hits={}",
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
                                volumeHolding.addParent(serialRecord.externalID());
                                volumeHolding.addParent(volume.externalID());
                                volumeHolding.setMediaType(serialRecord.mediaType());
                                volumeHolding.setCarrierType(serialRecord.carrierType());
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
                serialRecord.addVolume(volume);
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

    private void addOpenAccess(SerialRecord serialRecord) {
        // find at least one open access via ISSN
        boolean found = false;
        for (String issn : serialRecord.getISSNs()) {
            found = found || findOpenAccess(simpleHoldingsLicensesMerger.getSourceOpenAccessIndex(), issn);
        }
        serialRecord.setOpenAccess(found);
    }

    private boolean findOpenAccess(String index, String issn) {
        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(simpleHoldingsLicensesMerger.search().client(),
                SearchAction.INSTANCE);
        searchRequestBuilder
                .setSize(0)
                .setIndices(index)
                .setQuery(termQuery("dc:identifier", issn));
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        logger.debug("open access query {} ", searchRequestBuilder, searchResponse.getHits().getTotalHits());
        return searchResponse.getHits().getTotalHits() > 0;
    }

    private void indexTitleRecord(SerialRecord serialRecord) throws IOException {
        indexTitleRecord(serialRecord, null);
    }

    @SuppressWarnings("unchecked")
    private void indexTitleRecord(SerialRecord serialRecord, StatCounter statCounter) throws IOException {
        // first, index related conference/proceedings/abstracts/...
        if (!serialRecord.getMonographVolumes().isEmpty()) {
            for (MonographVolume volume : serialRecord.getMonographVolumes()) {
                XContentBuilder builder = jsonBuilder();
                buildMonographVolume(builder, volume, statCounter);
                index(simpleHoldingsLicensesMerger.getManifestationsIndex(),
                        simpleHoldingsLicensesMerger.getManifestationsIndexType(),
                        volume.externalID(), builder);
                MultiMap<String,Holding> mm = volume.getRelatedHoldings();
                for (String key : mm.keySet()) {
                    for (Holding volumeHolding : mm.get(key)){
                        builder = jsonBuilder();
                        buildMonographHolding(builder, volumeHolding);
                        // to holding index
                        String hid = volume.externalID();
                        index(simpleHoldingsLicensesMerger.getHoldingsIndex(),
                                simpleHoldingsLicensesMerger.getHoldingsIndexType(),
                                hid, builder);

                        if (statCounter != null) {
                            statCounter.increase("stat", "holdings", 1);
                        }
                        // extra entry by date
                        String vhid = "(" + volumeHolding.getServiceISIL() + ")" + volume.externalID()
                                + (volumeHolding.getFirstDate() != null ? "." + volumeHolding.getFirstDate() : null);
                        index(simpleHoldingsLicensesMerger.getVolumesIndex(),
                                simpleHoldingsLicensesMerger.getVolumesIndexType(),
                                vhid, builder);

                        if (statCounter != null) {
                            statCounter.increase("stat", "volumes", 1);
                        }
                    }
                }
            }
            int n = serialRecord.getMonographVolumes().size();
            if (statCounter != null) {
                statCounter.increase("stat", "manifestations", n);
            }
        }
        // write holdings and services
        if (!serialRecord.getRelatedHoldings().isEmpty()) {
            XContentBuilder builder = jsonBuilder();
            builder.startObject()
                    .field("parent", serialRecord.externalID());
            if (serialRecord.hasLinks()) {
                builder.field("links", serialRecord.getLinks());
            }
            builder.startArray("institution");
            int instcount = 0;
            final MultiMap<String, Holding> holdingsMap = serialRecord.getRelatedHoldings();
            for (String isil : holdingsMap.keySet()) {
                // blacklisted ISIL?
                if (simpleHoldingsLicensesMerger.blackListedISIL().lookup(isil)) {
                    continue;
                }
                Collection<Holding> holdings = holdingsMap.get(isil);
                if (holdings != null && !holdings.isEmpty()) {
                    instcount++;
                    builder.startObject().field("isil", isil);
                    builder.startArray("service");
                    int count = 0;
                    for (Holding holding : holdings) {
                        if (holding.isDeleted()) {
                            continue;
                        }
                        String serviceId = "(" + holding.getServiceISIL() + ")" + holding.identifier();
                        XContentBuilder serviceBuilder = jsonBuilder();
                        buildService(serviceBuilder, holding);
                        index(simpleHoldingsLicensesMerger.getServicesIndex(),
                                simpleHoldingsLicensesMerger.getServicesIndexType(),
                                serviceId,
                                serviceBuilder);
                        builder.value(serviceId);
                        count++;
                    }
                    builder.endArray()
                            .field("servicecount", count)
                            .endObject();
                    if (statCounter != null) {
                        statCounter.increase("stat", "services", count);
                    }
                }
            }
            builder.endArray();
            builder.field("institutioncount", instcount);
            builder.endObject();
            // now, build holdings per year
            MultiMap<Integer,Holding> map = serialRecord.getHoldingsByDate();
            for (Integer date : map.keySet()) {
                Collection<Holding> holdings = map.get(date);
                String volumeId = serialRecord.externalID() + (date != -1 ? "." + date : "");
                XContentBuilder volumeBuilder = jsonBuilder();
                buildVolume(volumeBuilder, serialRecord, date, holdings);
                index(simpleHoldingsLicensesMerger.getVolumesIndex(),
                        simpleHoldingsLicensesMerger.getVolumesIndexType(),
                        volumeId, volumeBuilder);
            }
            if (statCounter != null) {
                statCounter.increase("stat", "volumes", map.size());
            }
            // finally, add one holding per manifestation
            index(simpleHoldingsLicensesMerger.getHoldingsIndex(),
                    simpleHoldingsLicensesMerger.getHoldingsIndexType(),
                    serialRecord.externalID(),
                    builder);
            if (statCounter != null) {
                statCounter.increase("stat", "holdings", 1);
            }
        }
        if (statCounter != null) {
            statCounter.increase("stat", "manifestations", 1);
        }
        XContentBuilder builder = jsonBuilder();
        buildManifestation(builder, serialRecord, statCounter);
        index(simpleHoldingsLicensesMerger.getManifestationsIndex(),
                simpleHoldingsLicensesMerger.getManifestationsIndexType(),
                serialRecord.externalID(), builder);
    }

    private void buildManifestation(XContentBuilder builder,
                                    SerialRecord serialRecord,
                                    StatCounter statCounter) throws IOException {
        builder.startObject();
        builder.field("title", serialRecord.getExtendedTitle())
                .field("titlecomponents", serialRecord.getTitleComponents());
        String s = serialRecord.corporateName();
        if (s != null) {
            builder.field("corporatename", s);
        }
        s = serialRecord.meetingName();
        if (s != null) {
            builder.field("meetingname", s);
        }
        builder.field("country", serialRecord.country())
                .fieldIfNotNull("language", serialRecord.language())
                .field("publishedat", serialRecord.getPublisherPlace())
                .field("publishedby", serialRecord.getPublisher())
                .field("monographic", serialRecord.isMonographic())
                .field("openaccess", serialRecord.isOpenAccess())
                .fieldIfNotNull("license", serialRecord.getLicense())
                .field("contenttype", serialRecord.contentType())
                .field("mediatype", serialRecord.mediaType())
                .field("carriertype", serialRecord.carrierType())
                .fieldIfNotNull("firstdate", serialRecord.firstDate())
                .fieldIfNotNull("lastdate", serialRecord.lastDate());
        Set<Integer> missing = new HashSet<>(serialRecord.getDates());
        Set<Integer> set = serialRecord.getHoldingsByDate().keySet();
        builder.array("dates", set);
        builder.field("current", set.contains(currentYear));
        missing.removeAll(set);
        builder.array("missingdates", missing);
        builder.array("missingdatescount", missing.size());
        builder.field("greendate", serialRecord.getGreenDates());
        builder.field("greendatecount", serialRecord.getGreenDates().size());
        Set<String> isils = serialRecord.getRelatedHoldings().keySet();
        builder.array("isil", isils);
        builder.field("isilcount", isils.size());
        builder.field("identifiers", serialRecord.getIdentifiers());
        builder.field("subseries", serialRecord.isSubseries());
        builder.field("aggregate", serialRecord.isAggregate());
        builder.field("supplement", serialRecord.isSupplement());
        builder.fieldIfNotNull("resourcetype", serialRecord.resourceType());
        builder.fieldIfNotNull("genre", serialRecord.genre());
        MultiMap<String, SerialRecord> map = serialRecord.getRelated();
        if (!map.isEmpty()) {
            builder.startArray("relations");
            for (String rel : map.keySet()) {
                for (SerialRecord tr : map.get(rel)) {
                    builder.startObject()
                            .field("identifierForTheRelated", tr.externalID())
                            .field("label", rel)
                            .endObject();
                }
            }
            builder.endArray();
        }
        MultiMap<String, String> mm = serialRecord.getExternalRelations();
        if (!mm.isEmpty()) {
            builder.startArray("relations");
            for (String rel : mm.keySet()) {
                for (String relid : mm.get(rel)) {
                    builder.startObject()
                            .field("identifierForTheRelated", relid)
                            .field("label", rel)
                            .endObject();
                }
            }
            builder.endArray();
        }
        if (serialRecord.hasLinks()) {
            builder.array("links", serialRecord.getLinks());
        }
        builder.endObject();
        if (statCounter != null) {
            for (String country : serialRecord.country()) {
                statCounter.increase("country", country, 1);
            }
            statCounter.increase("language", serialRecord.language(), 1);
            statCounter.increase("contenttype", serialRecord.contentType(), 1);
            statCounter.increase("mediatype", serialRecord.mediaType(), 1);
            statCounter.increase("carriertype", serialRecord.carrierType(), 1);
            statCounter.increase("resourcetype", serialRecord.resourceType(), 1);
            statCounter.increase("genre", serialRecord.genre(), 1);
        }
    }

    public void buildService(XContentBuilder builder, Holding holding)
            throws IOException {
        builder.startObject()
                .array("parents", holding.parents());
        builder.field("mediatype", holding.mediaType())
                .field("carriertype", holding.carrierType())
                .field("name", holding.getName())
                .field("isil", holding.getISIL())
                .field("region", holding.getRegion())
                .fieldIfNotNull("organization", holding.getOrganization())
                .field("serviceisil", holding.getServiceISIL())
                .field("priority", holding.getPriority())
                .fieldIfNotNull("type", holding.getServiceType());
        Object o = holding.getServiceMode();
        if (o instanceof List) {
            builder.array("mode", (List) o);
        } else {
            builder.fieldIfNotNull("mode", o);
        }
        o = holding.getServiceDistribution();
        if (o instanceof List) {
            builder.array("distribution", (List) o);
        } else {
            builder.fieldIfNotNull("distribution", o);
        }
        builder.fieldIfNotNull("comment", holding.getServiceComment())
                .field("info", holding.getInfo())
                .field("current", holding.dates().contains(currentYear))
                .endObject();
    }

    private final static Integer currentYear = LocalDate.now().getYear();

    public void buildMonographVolume(XContentBuilder builder, MonographVolume monographVolume, StatCounter statCounter)
            throws IOException {
        builder.startObject()
            .array("parents", monographVolume.parents())
            .field("title", monographVolume.getTitle())
            .field("titlecomponents", monographVolume.getTitleComponents())
            .fieldIfNotNull("firstdate", monographVolume.firstDate());
        String s = monographVolume.corporateName();
        if (s != null) {
            builder.field("corporateName", s);
        }
        s = monographVolume.meetingName();
        if (s != null) {
            builder.field("meetingName", s);
        }
        if (monographVolume.conference() != null) {
            builder.field("conference");
            builder.map(monographVolume.conference());
        }
        builder.fieldIfNotNull("volume", monographVolume.getVolumeDesignation())
                .fieldIfNotNull("number", monographVolume.getNumbering())
                .fieldIfNotNull("resourcetype", monographVolume.resourceType())
                .fieldIfNotNull("genre", monographVolume.genre());
        if (monographVolume.country() != null && !monographVolume.country().isEmpty()) {
            builder.field("country", monographVolume.country());
        }
        builder.fieldIfNotNull("language", monographVolume.language())
                .fieldIfNotNull("publishedat", monographVolume.getPublisherPlace())
                .fieldIfNotNull("publishedby", monographVolume.getPublisher());
        if (monographVolume.hasIdentifiers()) {
            builder.field("identifiers", monographVolume.getIdentifiers());
        }
        builder.endObject();
        if (statCounter != null) {
            for (String country : monographVolume.country()) {
                statCounter.increase("country", country, 1);
            }
            statCounter.increase("language", monographVolume.language(), 1);
            // TODO
            //structCounter.increase("contenttype", contentType, 1);
            //structCounter.increase("mediatype", mediaType, 1);
            //structCounter.increase("carriertype", carrierType, 1);
            statCounter.increase("resourcetype", monographVolume.resourceType(), 1);
            for (String genre : monographVolume.genres()) {
                statCounter.increase("genre", genre, 1);
            }
        }
    }

    public void buildMonographHolding(XContentBuilder builder, Holding holding) throws IOException {
        builder.startObject();
        builder.array("parents", holding.parents())
                .array("date", holding.dates())
                .startObject("institution")
                .field("isil", holding.getISIL())
                .startObject("service")
                .field("mediatype", holding.mediaType())
                .field("carriertype", holding.carrierType())
                .field("region", holding.getRegion())
                .field("organization", holding.getOrganization())
                .field("name", holding.getName())
                .field("isil", holding.getServiceISIL())
                .field("serviceisil", holding.getServiceISIL())
                .field("priority", holding.getPriority())
                .field("type", holding.getServiceType());
        Object o = holding.getServiceMode();
        if (o instanceof List) {
            builder.array("mode", (List) o);
        } else {
            builder.field("mode", o);
        }
        o = holding.getServiceDistribution();
        if (o instanceof List) {
            builder.array("distribution", (List) o);
        } else {
            builder.field("distribution", o);
        }
        builder.startObject("info")
                .startObject("location")
                // https://www.hbz-nrw.de/dokumentencenter/produkte/verbunddatenbank/aktuell/plausi/Exemplar-Online-Kurzform.pdf
                .fieldIfNotNull("collection", holding.map().get("shelfmark")) // 088 b sublocation (Standort)
                .fieldIfNotNull("callnumber", holding.map().get("callnumber")) // 088 c (Signatur)
                //.fieldIfNotNull("collection", map.get("collection")) // 088 d zus. Bestandsangabe (nicht vorhanden)
                .endObject();
        builder.endObject();
        builder.field("current", holding.dates().contains(currentYear));
        builder.endObject();
    }

    private void buildVolume(XContentBuilder builder,
                             SerialRecord serialRecord,
                             Integer date,
                             Collection<Holding> holdings)
            throws IOException {
        builder.startObject();
        if (date != -1) {
            builder.field("date", date);
        }
        if (serialRecord.hasLinks()) {
            builder.field("links", serialRecord.getLinks());
        }
        Map<String, Set<Holding>> institutions = new HashMap<>();
        for (Holding holding : holdings) {
            // create holdings in order
            Set<Holding> set = institutions.containsKey(holding.getISIL()) ?
                    institutions.get(holding.getISIL()) : new TreeSet<>();
            set.add(holding);
            institutions.put(holding.getISIL(), set);
        }
        builder.field("institutioncount", institutions.size());
        builder.startArray("institution");
        for (Map.Entry<String,Set<Holding>> entry : institutions.entrySet()) {
            String isil = entry.getKey();
            Collection<Holding> set = entry.getValue();
            builder.startObject()
                    .field("isil", isil)
                    .field("servicecount", set.size());
            builder.startArray("service");
            for (Holding holding : set) {
                if (holding.isDeleted()) {
                    continue;
                }
                builder.value("(" + holding.getServiceISIL() + ")" + holding.identifier());
            }
            builder.endArray();
            builder.endObject();
        }
        builder.endArray();
        builder.endObject();
    }

    private XContentBuilder jsonBuilder() throws IOException {
        if (simpleHoldingsLicensesMerger.settings().getAsBoolean("mock", false)) {
            return org.xbib.common.xcontent.XContentService.jsonBuilder().prettyPrint();
        } else {
            return org.xbib.common.xcontent.XContentService.jsonBuilder();
        }
    }

    private void index(String index, String type, String id, XContentBuilder builder) throws IOException {
        if (simpleHoldingsLicensesMerger.settings().getAsBoolean("mock", false)) {
            logger.debug("{}/{}/{} {}", index, type, id, builder.string());
            return;
        }
        long len = builder.string().length();
        if (len > 1024 * 1024) {
            logger.warn("large document {}/{}/{} detected: {} bytes", index, type, id, len);
            return;
        }
        simpleHoldingsLicensesMerger.ingest().index(index, type, id, builder.string());
    }
}
