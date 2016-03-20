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
    private final Integer currentYear;

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
        this.currentYear = LocalDate.now().getYear();
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
        // we really just rely on the carrier type to look for online access information
        if ("online resource".equals(serialRecord.carrierType())) {
            addLicenses(serialRecord);
            addIndicators(serialRecord);
        }
        // find open access
        boolean found = false;
        for (String issn : serialRecord.getISSNs()) {
            found = found || findOpenAccess(simpleHoldingsLicensesMerger.getSourceOpenAccessIndex(), issn);
        }
        serialRecord.setOpenAccess(found);
        indexTitleRecord(serialRecord);
    }

    /*private void process(SerialRecord serialRecord) throws IOException {
        this.candidates = new HashSet<>();
        candidates.add(serialRecord);
        state = State.COLLECTING_CANDIDATES;
        searchNeighbors(serialRecord, candidates, 0);
        // process build queue to get candidates
        ClusterBuildContinuation cont;
        while ((cont = buildQueue.poll()) != null) {
            for (SerialRecord tr : cont.cluster) {
                candidates.add(tr);
            }
            continueClusterBuild(candidates, cont, 0);
        }
        int retry;
        do {
            // Ensure all relationships in the candidate set
            state = State.PROCESSING;
            for (SerialRecord m : candidates) {
                setAllRelationsBetween(m, candidates);
            }
            // Now, this is expensive. Find holdings, licenses, indicators of candidates
            Set<Holding> holdings = new TreeSet<>();
            searchHoldings(candidates, holdings);
            Set<License> licenses = new HashSet<>();
            searchLicensesAndIndicators(candidates, licenses);
            searchMonographs(candidates);
            // before indexing, fetch build queue again
            retry = 0;
            int before = candidates.size();
            while ((cont = buildQueue.poll()) != null) {
                for (SerialRecord tr : cont.cluster) {
                    candidates.add(tr);
                }
                int after = candidates.size();
                if (after > before) {
                    retry++;
                }
                continueClusterBuild(candidates, cont, 0);
            }
            if (retry > 0 && retry < 10) {
                logger.info("{}: retrying {} before indexing, {} candidates",
                        serialRecord, retry, candidates.size());
                continue;
            }
            state = State.INDEXING;
            for (SerialRecord tr : candidates) {
                indexTitleRecord(tr);
            }
            retry = 0;
            before = candidates.size();
            while ((cont = buildQueue.poll()) != null) {
                for (SerialRecord tr : cont.cluster) {
                    candidates.add(tr);
                }
                int after = candidates.size();
                if (after > before) {
                    retry++;
                }
                continueClusterBuild(candidates, cont, 0);
            }
            if (retry > 0 && retry < 10) {
                logger.info("{}: retrying {} after indexing, {} candidates",
                        serialRecord, retry, candidates.size());
            }
        } while (retry > 0 && retry < 10);
        if (retry >= 10) {
            logger.warn("retry limit exceeded: {}, candidates = {}, buildqueue = {}",
                    serialRecord, candidates, buildQueue.size());
        }
    }*/

    /*private void searchNeighbors(SerialRecord serialRecord, Collection<SerialRecord> candidates, int level)
            throws IOException {
        Set<String> neighbors = new HashSet<>();
        MultiMap<String,String> m = serialRecord.getRelations();
        for (String key : m.keySet()) {
            neighbors.addAll(m.get(key).stream().collect(Collectors.toList()));
        }
        if (neighbors.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = termsQuery("IdentifierDNB.identifierDNB", neighbors.toArray());
        SearchRequestBuilder searchRequest = simpleHoldingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(simpleHoldingsLicensesMerger.getSourceTitleIndex())
                .setQuery(queryBuilder)
                .setSize(scrollSize) // size is per shard!
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .addSort(SortBuilders.fieldSort("_doc"));
        logger.debug("searchRequest {}", searchRequest);
        SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        getMetric().mark();
        SearchHits hits = searchResponse.getHits();
        if (hits.getHits().length == 0) {
            return;
        }
        // copy candidates to a new list for each continuation, may be used by other threads after a collision
        List<SerialRecord> list = new ArrayList<>(candidates);
        ClusterBuildContinuation carrierCont = new ClusterBuildContinuation(serialRecord, searchResponse,
                SerialRecord.getCarrierRelations(), list, level);
        buildQueue.offer(carrierCont);
    }*/

    /*private void continueClusterBuild(Set<SerialRecord> serialRecords, ClusterBuildContinuation c, int level)
            throws IOException {
        SearchResponse searchResponse = c.searchResponse;
        do {
            for (int i = c.pos; i < searchResponse.getHits().getHits().length; i++) {
                SearchHit hit = searchResponse.getHits().getAt(i);
                SerialRecord m = new SerialRecord(hit.getSource());
                if (m.id().equals(c.serialRecord.id())) {
                    continue;
                }
                if (serialRecords.contains(m)) {
                    continue;
                }
                serialRecords.add(m);
                boolean expand = false;
                Collection<String> relations = findTheRelationsBetween(c.serialRecord, m.id());
                for (String relation : relations) {
                    if (relation == null) {
                        continue;
                    }
                    String inverse = SerialRecord.getInverseRelations().get(relation);
                    c.serialRecord.addRelated(relation, m);
                    if (inverse != null) {
                        m.addRelated(inverse, c.serialRecord);
                    }
                    expand = expand || c.relations.contains(relation);
                }
                if (expand && level < 2) {
                    searchNeighbors(m, serialRecords, level + 1);
                }
            }
            searchResponse = simpleHoldingsLicensesMerger.search().client()
                    .prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
            getMetric().mark();
        } while (searchResponse.getHits().getHits().length > 0);
        simpleHoldingsLicensesMerger.search().client()
                .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
    }*/

    private void addSerialHoldings(SerialRecord serialRecord) throws IOException {
        QueryBuilder queryBuilder =
                termQuery("ParentRecordIdentifier.identifierForTheParentRecord", serialRecord.id());
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

    /*private void addLicensesAndIndicators(Collection<SerialRecord> serialRecords, Set<License> licenses)
            throws IOException {
        // create a map of all title records that can have assigned a license
        Map<String, SerialRecord> map = new HashMap<>();
        boolean isOnline = false;
        for (SerialRecord m : serialRecords) {
            map.put(m.externalID(), m);
            // we really just rely on the carrier type. There may be licenses or indicators
            isOnline = isOnline || "online resource".equals(m.carrierType());
            // copy print to the online edition in case it is not there
            String id = m.getOnlineExternalID();
            if (id != null && !map.containsKey(id)) {
                map.put(id, m);
            }
        }
        if (isOnline) {
            addLicenses(licenses, map);
            logger.debug("after license search: licenses={}", licenses.size());
            searchIndicators(licenses, map);
            logger.debug("after indicator search: licenses={}", licenses.size());
        }
    }*/

    private void addLicenses(SerialRecord serialRecord) throws IOException {
        QueryBuilder queryBuilder = termsQuery("ezb:zdbid", serialRecord.externalID());
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

    private void addIndicators(SerialRecord serialRecord) throws IOException {
        QueryBuilder queryBuilder = termsQuery("xbib:identifier", serialRecord.externalID());
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

    private boolean findOpenAccess(String index, String issn) {
        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(simpleHoldingsLicensesMerger.search().client(),
                SearchAction.INSTANCE);
        searchRequestBuilder
                .setSize(0)
                .setIndices(index)
                .setQuery(termQuery("dc:identifier", issn));
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet(30, TimeUnit.SECONDS);
        logger.debug("open access query {} ", searchRequestBuilder, searchResponse.getHits().getTotalHits());
        return searchResponse.getHits().getTotalHits() > 0;
    }

    /*@SuppressWarnings("unchecked")
    private Set<String> findTheRelationsBetween(SerialRecord serialRecord, String id) {
        Set<String> relationNames = new HashSet<>();
        for (String entry : SerialRecord.relationEntries()) {
            Object o = serialRecord.map().get(entry);
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Collections.singletonList(o);
                }
                for (Object obj : (List) o) {
                    Map<String, Object> m = (Map<String, Object>) obj;
                    Object internalObj = m.get("identifierDNB");
                    // take only first entry from list...
                    String value = internalObj == null ? null : internalObj instanceof List ?
                            ((List) internalObj).get(0).toString() : internalObj.toString();
                    if (id.equals(value)) {
                        // defined relation?
                        Object oo = m.get("relation");
                        if (oo != null) {
                            if (!(oo instanceof List)) {
                                oo = Collections.singletonList(oo);
                            }
                            for (Object relName : (List) oo) {
                                relationNames.add(relName.toString());
                            }
                        }
                    }
                }
            }
        }
        return relationNames;
    }*/

    /*@SuppressWarnings("unchecked")
    private void setAllRelationsBetween(SerialRecord serialRecord, Collection<SerialRecord> cluster) {
        for (String relation : SerialRecord.relationEntries()) {
            Object o = serialRecord.map().get(relation);
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Collections.singletonList(o);
                }
                for (Object s : (List) o) {
                    Map<String, Object> entry = (Map<String, Object>) s;
                    Object internalObj = entry.get("relation");
                    String key = internalObj == null ? null : internalObj instanceof List ?
                            ((List) internalObj).get(0).toString() : internalObj.toString();
                    if (key == null) {
                        internalObj = entry.get("relationshipInformation");
                        if (internalObj != null) {
                            //key = "hasRelationTo";
                            continue;
                        } else {
                            if (logger.isTraceEnabled()) {
                                logger.trace("entry {} has no relation name in {}", entry, serialRecord.externalID());
                            }
                            continue;
                        }
                    }
                    internalObj = entry.get("identifierDNB");
                    // take only first entry from list...
                    String value = internalObj == null ? null : internalObj instanceof List ?
                            ((List) internalObj).get(0).toString() : internalObj.toString();
                    for (SerialRecord m : cluster) {
                        // self?
                        if (m.id().equals(serialRecord.id())) {
                            continue;
                        }
                        if (m.id().equals(value)) {
                            serialRecord.addRelated(key, m);
                            // special trick: move over links from online to print
                            if ("hasPrintEdition".equals(key)) {
                                m.setLinks(serialRecord.getLinks());
                            }
                            String inverse = SerialRecord.getInverseRelations().get(key);
                            if (inverse != null) {
                                m.addRelated(inverse, serialRecord);
                            } else {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("no inverse relation for {} in {}, using 'isRelatedTo'", key,
                                            serialRecord.externalID());
                                }
                                m.addRelated("isRelatedTo", serialRecord);
                            }
                        }
                    }
                }
            }
        }
    }*/

    private void indexTitleRecord(SerialRecord serialRecord) throws IOException {
        indexTitleRecord(serialRecord, null);
    }

    @SuppressWarnings("unchecked")
    private void indexTitleRecord(SerialRecord serialRecord, StatCounter statCounter) throws IOException {
        // first, index related conference/proceedings/abstracts/...
        if (!serialRecord.getMonographVolumes().isEmpty()) {
            for (MonographVolume volume : serialRecord.getMonographVolumes()) {
                XContentBuilder builder = jsonBuilder();
                volume.toXContent(builder, XContentBuilder.EMPTY_PARAMS, statCounter);
                checkForIndex(simpleHoldingsLicensesMerger.getManifestationsIndex(),
                        simpleHoldingsLicensesMerger.getManifestationsIndexType(),
                        volume.externalID(), builder);
                MultiMap<String,Holding> mm = volume.getRelatedHoldings();
                for (String key : mm.keySet()) {
                    for (Holding volumeHolding : mm.get(key)){
                        builder = jsonBuilder();
                        volumeHolding.toXContent(builder, XContentBuilder.EMPTY_PARAMS);
                        // to holding index
                        String hid = volume.externalID();
                        checkForIndex(simpleHoldingsLicensesMerger.getHoldingsIndex(),
                                simpleHoldingsLicensesMerger.getHoldingsIndexType(),
                                hid, builder);

                        if (statCounter != null) {
                            statCounter.increase("stat", "holdings", 1);
                        }
                        // extra entry by date
                        String vhid = "(" + volumeHolding.getServiceISIL() + ")" + volume.externalID()
                                + (volumeHolding.getFirstDate() != null ? "." + volumeHolding.getFirstDate() : null);
                        checkForIndex(simpleHoldingsLicensesMerger.getVolumesIndex(),
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
                        holding.toXContent(serviceBuilder, XContentBuilder.EMPTY_PARAMS);
                        checkForIndex(simpleHoldingsLicensesMerger.getServicesIndex(),
                                simpleHoldingsLicensesMerger.getServicesIndexType(),
                                serviceId, serviceBuilder);
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
            // first, build holdings per year
            MultiMap<Integer,Holding> map = serialRecord.getHoldingsByDate();
            for (Integer date : map.keySet()) {
                Collection<Holding> holdings = map.get(date);
                String volumeId = serialRecord.externalID() + (date != -1 ? "." + date : "");
                builder = jsonBuilder();
                buildVolume(builder, serialRecord, serialRecord.externalID(), date, holdings);
                checkForIndex(simpleHoldingsLicensesMerger.getVolumesIndex(),
                        simpleHoldingsLicensesMerger.getVolumesIndexType(),
                        volumeId, builder);
            }
            if (statCounter != null) {
                statCounter.increase("stat", "volumes", map.size());
            }
            // second, add one holding per manifestation
            checkForIndex(simpleHoldingsLicensesMerger.getHoldingsIndex(),
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
        serialRecord.toXContent(builder, XContentBuilder.EMPTY_PARAMS, statCounter);
        checkForIndex(simpleHoldingsLicensesMerger.getManifestationsIndex(),
                simpleHoldingsLicensesMerger.getManifestationsIndexType(),
                serialRecord.externalID(), builder);
    }

    private void buildVolume(XContentBuilder builder,
                             SerialRecord serialRecord,
                             String parentIdentifier,
                             Integer date,
                             Collection<Holding> holdings)
            throws IOException {
        builder.startObject()
                .field("identifierForTheVolume", parentIdentifier + "." + date);
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

    private void checkForIndex(String index, String type, String id, XContentBuilder builder) throws IOException {
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
