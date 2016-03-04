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
package org.xbib.tools.merge.holdingslicenses;

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
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.xbib.common.settings.Settings;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.metric.MeterMetric;
import org.xbib.tools.merge.holdingslicenses.support.StatCounter;
import org.xbib.tools.merge.holdingslicenses.entities.Holding;
import org.xbib.tools.merge.holdingslicenses.entities.Indicator;
import org.xbib.tools.merge.holdingslicenses.entities.License;
import org.xbib.tools.merge.holdingslicenses.entities.TitleRecord;
import org.xbib.tools.merge.holdingslicenses.entities.MonographVolume;
import org.xbib.tools.merge.holdingslicenses.entities.MonographVolumeHolding;
import org.xbib.util.IndexDefinition;
import org.xbib.util.MultiMap;
import org.xbib.util.Strings;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.Worker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

public class HoldingsLicensesWorker
        implements Worker<Pipeline<HoldingsLicensesWorker, TitelRecordRequest>, TitelRecordRequest> {

    enum State {
        COLLECTING_CANDIDATES, PROCESSING, INDEXING
    }

    private Pipeline<HoldingsLicensesWorker, TitelRecordRequest> pipeline;
    private MeterMetric metric;
    private final int number;
    private final HoldingsLicensesMerger holdingsLicensesMerger;
    private final Logger logger;
    private final Queue<ClusterBuildContinuation> buildQueue;
    private final int scrollSize;
    private final long scrollMillis;
    private final long timeoutSeconds;

    private String sourceTitleIndex;
    private String sourceHoldingsIndex;
    private String sourceLicenseIndex;
    private String sourceIndicatorIndex;
    private String sourceMonographicIndex;
    private String sourceMonographicHoldingsIndex;
    private String sourceOpenAccessIndex;

    private String manifestationsIndex;
    private String manifestationsIndexType;
    private String holdingsIndex;
    private String holdingsIndexType;
    private String volumesIndex;
    private String volumesIndexType;
    private String servicesIndex;
    private String servicesIndexType;

    private State state;

    private Set<TitleRecord> candidates;

    @SuppressWarnings("unchecked")
    public HoldingsLicensesWorker(Settings settings,
                                  HoldingsLicensesMerger holdingsLicensesMerger,
                                  int scrollSize, long scrollMillis, int number) {
        this.number = number;
        this.holdingsLicensesMerger = holdingsLicensesMerger;
        this.buildQueue = new ConcurrentLinkedQueue<>();
        this.logger = LogManager.getLogger(toString());
        this.metric = new MeterMetric(5L, TimeUnit.SECONDS);
        holdingsLicensesMerger.getMetrics().scheduleMetrics(settings, "meter" + number, metric);
        this.scrollSize = scrollSize;
        this.scrollMillis = scrollMillis;
        this.timeoutSeconds = 60;
        logger.info("scrollSize= {} scrollMillis={}", scrollSize, scrollMillis);
    }

    @Override
    public Worker<Pipeline<HoldingsLicensesWorker, TitelRecordRequest>, TitelRecordRequest>
            setPipeline(Pipeline<HoldingsLicensesWorker, TitelRecordRequest> pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    @Override
    public Pipeline<HoldingsLicensesWorker, TitelRecordRequest> getPipeline() {
        return pipeline;
    }

    @Override
    public HoldingsLicensesWorker setMetric(MeterMetric metric) {
        this.metric = metric;
        return this;
    }

    @Override
    public MeterMetric getMetric() {
        return metric;
    }

    public Queue<ClusterBuildContinuation> getBuildQueue() {
        return buildQueue;
    }

    public Set<TitleRecord> getCandidates() {
        return candidates;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TitelRecordRequest call() throws Exception {
        logger.info("worker {} starting", this);
        TitelRecordRequest element = null;
        TitleRecord titleRecord = null;
        try {
            Map<String,IndexDefinition> indexDefinitionMap = holdingsLicensesMerger.getInputIndexDefinitionMap();
            this.sourceTitleIndex = indexDefinitionMap.get("zdb").getIndex();
            if (Strings.isNullOrEmpty(sourceTitleIndex)) {
                throw new IllegalArgumentException("no zdb index given");
            }
            this.sourceHoldingsIndex = indexDefinitionMap.get("zdbholdings").getIndex();
            if (Strings.isNullOrEmpty(sourceHoldingsIndex)) {
                throw new IllegalArgumentException("no zdbholdings index given");
            }
            this.sourceLicenseIndex = indexDefinitionMap.get("ezbxml").getIndex();
            if (Strings.isNullOrEmpty(sourceLicenseIndex)) {
                throw new IllegalArgumentException("no ezbxml index given");
            }
            this.sourceIndicatorIndex = indexDefinitionMap.get("ezbweb").getIndex();
            if (Strings.isNullOrEmpty(sourceIndicatorIndex)) {
                throw new IllegalArgumentException("no ezbweb index given");
            }
            this.sourceMonographicIndex = indexDefinitionMap.get("hbz").getIndex();
            if (Strings.isNullOrEmpty(sourceMonographicIndex)) {
                throw new IllegalArgumentException("no hbz index given");
            }
            this.sourceMonographicHoldingsIndex = indexDefinitionMap.get("hbzholdings").getIndex();
            if (Strings.isNullOrEmpty(sourceMonographicHoldingsIndex)) {
                throw new IllegalArgumentException("no hbzholdings index given");
            }
            this.sourceOpenAccessIndex = indexDefinitionMap.get("doaj").getIndex();
            if (Strings.isNullOrEmpty(sourceOpenAccessIndex)) {
                throw new IllegalArgumentException("no doaj index given");
            }
            indexDefinitionMap = holdingsLicensesMerger.getOutputIndexDefinitionMap();
            String indexName = indexDefinitionMap.get("holdingslicenses").getConcreteIndex();
            this.manifestationsIndex = indexName;
            this.manifestationsIndexType = "manifestations";
            this.holdingsIndex = indexName;
            this.holdingsIndexType = "holdings";
            this.volumesIndex = indexName;
            this.volumesIndexType = "volumes";
            this.servicesIndex = indexName;
            this.servicesIndexType = "services";
            element = getPipeline().getQueue().take();
            titleRecord = element != null ? element.get() : null;
            while (titleRecord != null) {
                long t0 = System.nanoTime();
                process(titleRecord);
                long t1 = System.nanoTime();
                long delta = (t1 -t0) / 1000000;
                // warn if delta is longer than 10 secs
                if (delta > 10000) {
                    logger.warn("long processing of {}: {} ms", titleRecord.externalID(), delta);
                }
                metric.mark();
                element = getPipeline().getQueue().take();
                titleRecord = element != null ? element.get() : null;
            }
            getPipeline().quit(this);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            logger.error("exiting, exception while processing {}", titleRecord);
            getPipeline().quit(this, e);
        } finally {
            metric.stop();
        }
        return element;
    }

    @Override
    public void close() throws IOException {
        logger.info("worker closing");
    }

    @Override
    public String toString() {
        return HoldingsLicensesMerger.class.getSimpleName() + "." + number;
    }

    private void process(TitleRecord titleRecord) throws IOException {
        // Candidates are unstructured, no timeline organization,
        // no relationship analysis, not ordered by ID
        this.candidates = new HashSet<>();
        candidates.add(titleRecord);
        state = State.COLLECTING_CANDIDATES;
        searchNeighbors(titleRecord, candidates, 0);
        // process build queue to get candidates
        ClusterBuildContinuation cont;
        while ((cont = buildQueue.poll()) != null) {
            for (TitleRecord tr : cont.cluster) {
                candidates.add(tr);
            }
            continueClusterBuild(candidates, cont, 0);
        }
        int retry;
        do {
            // Ensure all relationships in the candidate set
            state = State.PROCESSING;
            for (TitleRecord m : candidates) {
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
                for (TitleRecord tr : cont.cluster) {
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
                        titleRecord, retry, candidates.size());
                continue;
            }
            state = State.INDEXING;
            for (TitleRecord tr : candidates) {
                indexTitleRecord(tr);
            }
            retry = 0;
            before = candidates.size();
            while ((cont = buildQueue.poll()) != null) {
                for (TitleRecord tr : cont.cluster) {
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
                        titleRecord, retry, candidates.size());
            }
        } while (retry > 0 && retry < 10);
        if (retry >= 10) {
            logger.warn("retry limit exceeded: {}, candidates = {}, buildqueue = {}",
                    titleRecord, candidates, buildQueue.size());
        }
    }

    private void searchNeighbors(TitleRecord titleRecord, Collection<TitleRecord> candidates, int level)
            throws IOException {
        Set<String> neighbors = new HashSet<>();
        MultiMap<String,String> m = titleRecord.getRelations();
        for (String key : m.keySet()) {
            neighbors.addAll(m.get(key).stream().collect(Collectors.toList()));
        }
        if (neighbors.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = termsQuery("IdentifierDNB.identifierDNB", neighbors.toArray());
        SearchRequestBuilder searchRequest = holdingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(sourceTitleIndex)
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
        List<TitleRecord> list = new ArrayList<>(candidates);
        ClusterBuildContinuation carrierCont = new ClusterBuildContinuation(titleRecord, searchResponse,
                TitleRecord.getCarrierRelations(), list, level);
        buildQueue.offer(carrierCont);
    }

    private void continueClusterBuild(Set<TitleRecord> titleRecords, ClusterBuildContinuation c, int level)
            throws IOException {
        SearchResponse searchResponse = c.searchResponse;
        do {
            for (int i = c.pos; i < searchResponse.getHits().getHits().length; i++) {
                SearchHit hit = searchResponse.getHits().getAt(i);
                TitleRecord m = new TitleRecord(hit.getSource());
                if (m.id().equals(c.titleRecord.id())) {
                    continue;
                }
                if (titleRecords.contains(m)) {
                    continue;
                }
                titleRecords.add(m);
                boolean collided = detectCollisionAndTransfer(m, c, i);
                if (collided) {
                    // abort immediately
                    return;
                }
                boolean expand = false;
                Collection<String> relations = findTheRelationsBetween(c.titleRecord, m.id());
                for (String relation : relations) {
                    if (relation == null) {
                        continue;
                    }
                    String inverse = TitleRecord.getInverseRelations().get(relation);
                    c.titleRecord.addRelated(relation, m);
                    if (inverse != null) {
                        m.addRelated(inverse, c.titleRecord);
                    }
                    expand = expand || c.relations.contains(relation);
                }
                if (expand && level < 2) {
                    searchNeighbors(m, titleRecords, level + 1);
                }
            }
            searchResponse = holdingsLicensesMerger.search().client()
                    .prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
            getMetric().mark();
        } while (searchResponse.getHits().getHits().length > 0);
        holdingsLicensesMerger.search().client()
                .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
    }

    private boolean detectCollisionAndTransfer(TitleRecord titleRecord, ClusterBuildContinuation c, int pos) {
        for (HoldingsLicensesWorker worker : getPipeline().getWorkers()) {
            if (this == worker) {
                continue;
            }
            Set<TitleRecord> set = worker.getCandidates();
            if (set != null && set.contains(titleRecord)) {
                logger.warn("collision detected for {} with {} state={}", titleRecord, worker, worker.state.name());
                c.pos = pos;
                // remove from our candidates, because we pass over to other thread
                candidates.remove(titleRecord);
                for (TitleRecord tr : c.cluster) {
                    candidates.remove(tr);
                }
                // pass it over
                worker.getBuildQueue().offer(c);
                return true;
            }
        }
        return false;
    }

    private void searchHoldings(Collection<TitleRecord> titleRecords, Set<Holding> holdings)
            throws IOException {
        if (sourceHoldingsIndex == null) {
            return;
        }
        // create a map of all title records that can have assigned a holding
        Map<String, TitleRecord> map = new HashMap<>();
        for (TitleRecord m : titleRecords) {
            map.put("(DE-600)" + m.id(), m);
            // add print if not already there...
            if (m.getPrintID() != null && !map.containsKey(m.getPrintID())) {
                map.put("(DE-600)" + m.getPrintID(), m);
            }
        }
        searchHoldings(map, holdings);
    }

    @SuppressWarnings("unchecked")
    private void searchHoldings(Map<String, TitleRecord> titleRecordMap, Set<Holding> holdings)
            throws IOException {
        if (sourceHoldingsIndex == null) {
            return;
        }
        if (titleRecordMap == null || titleRecordMap.isEmpty()) {
            return;
        }
        // split ids into portions of 1024 (default max clauses for Lucene)
        Object[] array = titleRecordMap.keySet().toArray();
        for (int begin = 0; begin < array.length; begin += 1024) {
            int end = begin + 1024 > array.length ? array.length : begin + 1024;
            Object[] subarray = Arrays.copyOfRange(array, begin, end);
            QueryBuilder queryBuilder = termsQuery("ParentRecordIdentifier.identifierForTheParentRecord", subarray);
            // getSize is per shard
            SearchRequestBuilder searchRequest = holdingsLicensesMerger.search().client()
                    .prepareSearch()
                    .setIndices(sourceHoldingsIndex)
                    .setQuery(queryBuilder)
                    .setSize(scrollSize)
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .addSort(SortBuilders.fieldSort("_doc"));
            SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
            logger.debug("searchHoldings search request = {} hits = {}",
                    searchRequest.toString(),
                    searchResponse.getHits().getTotalHits());
            do {
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
                    if (holdingsLicensesMerger.mappedISIL().lookup().containsKey(isil)) {
                        isil = (String) holdingsLicensesMerger.mappedISIL().lookup().get(isil);
                    }
                    // consortia?
                    if (holdingsLicensesMerger.consortiaLookup().lookupISILs().containsKey(isil)) {
                        List<String> list = holdingsLicensesMerger.consortiaLookup().lookupISILs().get(isil);
                        for (String expandedisil : list) {
                            // blacklisted expanded ISIL?
                            if (holdingsLicensesMerger.blackListedISIL().lookup(expandedisil)) {
                                continue;
                            }
                            // new Holding for each ISIL
                            holding = new Holding(holding.map());
                            holding.setISIL(isil);
                            holding.setServiceISIL(expandedisil);
                            holding.setName(holdingsLicensesMerger.bibdatLookup()
                                    .lookupName().get(expandedisil));
                            holding.setRegion(holdingsLicensesMerger.bibdatLookup()
                                    .lookupRegion().get(expandedisil));
                            holding.setOrganization(holdingsLicensesMerger.bibdatLookup()
                                    .lookupOrganization().get(expandedisil));
                            TitleRecord parentTitleRecord = titleRecordMap.get(holding.parentIdentifier());
                            parentTitleRecord.addRelatedHolding(expandedisil, holding);
                            holdings.add(holding);
                        }
                    } else {
                        // blacklisted ISIL?
                        if (holdingsLicensesMerger.blackListedISIL().lookup(isil)) {
                            continue;
                        }
                        holding.setName(holdingsLicensesMerger.bibdatLookup().lookupName().get(isil));
                        holding.setRegion(holdingsLicensesMerger.bibdatLookup().lookupRegion().get(isil));
                        holding.setOrganization(holdingsLicensesMerger.bibdatLookup().lookupOrganization().get(isil));
                        TitleRecord parentTitleRecord = titleRecordMap.get(holding.parentIdentifier());
                        parentTitleRecord.addRelatedHolding(isil, holding);
                        holdings.add(holding);
                    }
                }
                searchResponse = holdingsLicensesMerger.search().client()
                        .prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(scrollMillis))
                        .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
            } while (searchResponse.getHits().getHits().length > 0);
            holdingsLicensesMerger.search().client()
                    .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        }
    }

    private void searchLicensesAndIndicators(Collection<TitleRecord> titleRecords, Set<License> licenses)
            throws IOException {
        // create a map of all title records that can have assigned a license
        Map<String, TitleRecord> map = new HashMap<>();
        boolean isOnline = false;
        for (TitleRecord m : titleRecords) {
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
            searchLicenses(licenses, map);
            logger.debug("after license search: licenses={}", licenses.size());
            searchIndicators(licenses, map);
            logger.debug("after indicator search: licenses={}", licenses.size());
        }
    }

    private void searchLicenses(Set<License> licenses, Map<String, TitleRecord> titleRecordMap) throws IOException {
        if (sourceLicenseIndex == null) {
            return;
        }
        if (titleRecordMap == null || titleRecordMap.isEmpty()) {
            return;
        }
        // split ids into portions of 1024 (default max clauses for Lucene)
        Object[] array = titleRecordMap.keySet().toArray();
        for (int begin = 0; begin < array.length; begin += 1024) {
            int end = begin + 1024 > array.length ? array.length : begin + 1024;
            Object[] subarray = Arrays.copyOfRange(array, begin, end);
            QueryBuilder queryBuilder = termsQuery("ezb:zdbid", subarray);
            // getSize is per shard
            SearchRequestBuilder searchRequest = holdingsLicensesMerger.search().client()
                    .prepareSearch()
                    .setIndices(sourceLicenseIndex)
                    .setQuery(queryBuilder)
                    .setSize(scrollSize)
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .addSort(SortBuilders.fieldSort("_doc"));
            SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
            logger.debug("searchLicenses search request = {} hits = {}",
                    searchRequest.toString(),
                    searchResponse.getHits().getTotalHits());
            do {
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
                    if (holdingsLicensesMerger.mappedISIL().lookup().containsKey(isil)) {
                        isil = (String) holdingsLicensesMerger.mappedISIL().lookup().get(isil);
                    }
                    // consortia?
                    if (holdingsLicensesMerger.consortiaLookup().lookupISILs().containsKey(isil)) {
                        List<String> list = holdingsLicensesMerger.consortiaLookup().lookupISILs().get(isil);
                        for (String expandedisil : list) {
                            // blacklisted expanded ISIL?
                            if (holdingsLicensesMerger.blackListedISIL().lookup(expandedisil)) {
                                continue;
                            }
                            // new License for each ISIL
                            license = new License(license.map());
                            license.setISIL(isil);
                            license.setServiceISIL(expandedisil);
                            license.setName(holdingsLicensesMerger.bibdatLookup().lookupName().get(expandedisil));
                            license.setRegion(holdingsLicensesMerger.bibdatLookup().lookupRegion().get(expandedisil));
                            license.setOrganization(holdingsLicensesMerger.bibdatLookup().lookupOrganization().get(expandedisil));
                            for (String parent : license.parents()) {
                                TitleRecord m = titleRecordMap.get(parent);
                                m.addRelatedHolding(expandedisil, license);
                            }
                            licenses.add(license);
                        }
                    } else {
                        // blacklisted ISIL?
                        if (holdingsLicensesMerger.blackListedISIL().lookup(isil)) {
                            continue;
                        }
                        license.setName(holdingsLicensesMerger.bibdatLookup().lookupName().get(isil));
                        license.setRegion(holdingsLicensesMerger.bibdatLookup().lookupRegion().get(isil));
                        license.setOrganization(holdingsLicensesMerger.bibdatLookup().lookupOrganization().get(isil));
                        for (String parent : license.parents()) {
                            TitleRecord m = titleRecordMap.get(parent);
                            m.addRelatedHolding(isil, license);
                        }
                        licenses.add(license);
                    }
                }
                searchResponse = holdingsLicensesMerger.search().client()
                       .prepareSearchScroll(searchResponse.getScrollId())
                       .setScroll(TimeValue.timeValueMillis(scrollMillis))
                       .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
            } while (searchResponse.getHits().getHits().length > 0);
            holdingsLicensesMerger.search().client()
                    .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        }
    }

    private void searchIndicators(Set<License> indicators, Map<String, TitleRecord> titleRecordMap) throws IOException {
        if (sourceIndicatorIndex == null) {
            return;
        }
        if (titleRecordMap == null || titleRecordMap.isEmpty()) {
            return;
        }
        // split ids into portions of 1024 (default max clauses for Lucene)
        Object[] array = titleRecordMap.keySet().toArray();
        for (int begin = 0; begin < array.length; begin += 1024) {
            int end = begin + 1024 > array.length ? array.length : begin + 1024;
            Object[] subarray = Arrays.copyOfRange(array, begin, end);
            QueryBuilder queryBuilder = termsQuery("xbib:identifier", subarray);
            SearchRequestBuilder searchRequest = holdingsLicensesMerger.search().client()
                    .prepareSearch()
                    .setIndices(sourceIndicatorIndex)
                    .setQuery(queryBuilder)
                    .setSize(scrollSize)
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .addSort(SortBuilders.fieldSort("_doc"));
            SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
            logger.debug("searchIndicators search request = {} hits = {}",
                    searchRequest.toString(),
                    searchResponse.getHits().getTotalHits());
            do {
                for (SearchHit hit :  searchResponse.getHits()) {
                    Indicator indicator = new Indicator(hit.getSource());
                    String isil = indicator.getISIL();
                    if (isil == null) {
                        continue;
                    }
                    // mapped ISIL?
                    if (holdingsLicensesMerger.mappedISIL().lookup().containsKey(isil)) {
                        isil = (String) holdingsLicensesMerger.mappedISIL().lookup().get(isil);
                    }
                    // consortia?
                    if (holdingsLicensesMerger.consortiaLookup().lookupISILs().containsKey(isil)) {
                        List<String> list = holdingsLicensesMerger.consortiaLookup().lookupISILs().get(isil);
                        for (String expandedisil : list) {
                            // blacklisted expanded ISIL?
                            if (holdingsLicensesMerger.blackListedISIL().lookup(expandedisil)) {
                                continue;
                            }
                            indicator = new Indicator(indicator.map());
                            indicator.setISIL(isil);
                            indicator.setServiceISIL(expandedisil);
                            indicator.setName(holdingsLicensesMerger.bibdatLookup().lookupName().get(expandedisil));
                            indicator.setRegion(holdingsLicensesMerger.bibdatLookup().lookupRegion().get(expandedisil));
                            indicator.setOrganization(holdingsLicensesMerger.bibdatLookup().lookupOrganization().get(expandedisil));
                            for (String parent : indicator.parents()) {
                                TitleRecord m = titleRecordMap.get(parent);
                                m.addRelatedIndicator(expandedisil, indicator);
                            }
                            indicators.add(indicator);
                        }
                    } else {
                        // blacklisted ISIL?
                        if (holdingsLicensesMerger.blackListedISIL().lookup(isil)) {
                            continue;
                        }
                        indicator.setName(holdingsLicensesMerger.bibdatLookup().lookupName().get(isil));
                        indicator.setRegion(holdingsLicensesMerger.bibdatLookup().lookupRegion().get(isil));
                        indicator.setOrganization(holdingsLicensesMerger.bibdatLookup().lookupOrganization().get(isil));
                        for (String parent : indicator.parents()) {
                            TitleRecord m = titleRecordMap.get(parent);
                            m.addRelatedIndicator(isil, indicator);
                        }
                        indicators.add(indicator);
                    }
                }
                searchResponse = holdingsLicensesMerger.search().client()
                        .prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(scrollMillis))
                        .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS); // must time out
            } while (searchResponse.getHits().getHits().length > 0);
            holdingsLicensesMerger.search().client()
                    .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        }
    }

    @SuppressWarnings("unchecked")
    private void searchMonographs(Collection<TitleRecord> titleRecords) throws IOException {
        for (TitleRecord titleRecord : titleRecords) {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.should(termQuery("IdentifierZDB.identifierZDB", titleRecord.externalID()));
            Collection<String> issns = (Collection<String>) titleRecord.getIdentifiers().get("issn");
            if (issns != null) {
                for (String issn : issns) {
                    // not known which ISSN is print or online
                    queryBuilder.should(termQuery("IdentifierISSN.identifierISSN", issn));
                    queryBuilder.should(termQuery("IdentifierSerial.identifierISSNOnline",issn));
                }
            }
            SearchRequestBuilder searchRequest = holdingsLicensesMerger.search().client()
                    .prepareSearch()
                    .setIndices(sourceMonographicIndex)
                    .setSize(scrollSize)
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .setQuery(queryBuilder)
                    .addSort(SortBuilders.fieldSort("_doc"));
            SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
            logger.debug("searchMonographs search request={} hits={}",
                    searchRequest.toString(), searchResponse.getHits().getTotalHits());
            do {
                getMetric().mark();
                for (SearchHit hit : searchResponse.getHits()) {
                    Map<String, Object> m = hit.getSource();
                    MonographVolume volume = new MonographVolume(m, titleRecord);
                    searchExtraHoldings(volume);
                    searchSeriesVolumeHoldings(volume);
                }
                searchResponse = holdingsLicensesMerger.search().client()
                        .prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(scrollMillis))
                        .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
            } while (searchResponse.getHits().getHits().length > 0);
            holdingsLicensesMerger.search().client()
                    .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Extra holdings are from a monographic catalog, but not in the base serials catalog.
     * @param volume the volume
     */
    @SuppressWarnings("unchecked")
    private void searchExtraHoldings(MonographVolume volume) {
        TitleRecord titleRecord = volume.getTitleRecord();
        String key = volume.id();
        SearchRequestBuilder holdingsSearchRequest = holdingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(sourceMonographicHoldingsIndex)
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .setQuery(termQuery("xbib.uid", key))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse holdingSearchResponse = holdingsSearchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        logger.debug("searchExtraHoldings search request = {} hits={}",
                holdingsSearchRequest.toString(), holdingSearchResponse.getHits().getTotalHits());
        do {
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
                        if (holdingsLicensesMerger.mappedISIL().lookup().containsKey(isil)) {
                            isil = (String) holdingsLicensesMerger.mappedISIL().lookup().get(isil);
                        }
                        // blacklisted ISIL?
                        if (holdingsLicensesMerger.blackListedISIL().lookup(isil)) {
                            continue;
                        }
                        volumeHolding.addParent(volume.externalID());
                        volumeHolding.setMediaType(titleRecord.mediaType());
                        volumeHolding.setCarrierType(titleRecord.carrierType());
                        volumeHolding.setDate(volume.firstDate(), volume.lastDate());
                        volumeHolding.setName(holdingsLicensesMerger.bibdatLookup()
                                .lookupName().get(isil));
                        volumeHolding.setRegion(holdingsLicensesMerger.bibdatLookup()
                                .lookupRegion().get(isil));
                        volumeHolding.setOrganization(holdingsLicensesMerger.bibdatLookup()
                                .lookupOrganization().get(isil));
                        volumeHolding.setServiceMode(holdingsLicensesMerger.statusCodeMapper()
                                .lookup(volumeHolding.getStatus()));
                        if ("interlibrary".equals(volumeHolding.getServiceType()) && isil != null) {
                            volume.addRelatedHolding(isil, volumeHolding);
                        }
                    }
                }
            }
            holdingSearchResponse = holdingsLicensesMerger.search().client()
                    .prepareSearchScroll(holdingSearchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        } while (holdingSearchResponse.getHits().getHits().length > 0);
        holdingsLicensesMerger.search().client()
                .prepareClearScroll().addScrollId(holdingSearchResponse.getScrollId())
                .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * Search all holdings in this series, if it is a series
     * @param parent the parent volume
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private void searchSeriesVolumeHoldings(MonographVolume parent)
            throws IOException {
        TitleRecord titleRecord = parent.getTitleRecord();
        if (parent.id() == null || parent.id().isEmpty()) {
            return;
        }
        // search children volumes of the series (conference, processing, abstract, ...)
        SearchRequestBuilder searchRequest = holdingsLicensesMerger.search().client()
                .prepareSearch()
                .setIndices(sourceMonographicIndex)
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .setQuery(boolQuery().should(termQuery("SeriesAddedEntryUniformTitle.designation", parent.id()))
                        .should(termQuery("RecordIdentifierSuper.recordIdentifierSuper", parent.id())))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse searchResponse = searchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        logger.debug("searchSeriesVolumeHoldings search request={} hits={}",
                searchRequest.toString(), searchResponse.getHits().getTotalHits());
        do {
            getMetric().mark();
            for (SearchHit hit : searchResponse.getHits()) {
                MonographVolume volume = new MonographVolume(hit.getSource(), titleRecord);
                volume.addParent(titleRecord.externalID());
                // for each conference/congress, search holdings
                SearchRequestBuilder holdingsSearchRequest = holdingsLicensesMerger.search().client()
                        .prepareSearch()
                        .setIndices(sourceMonographicHoldingsIndex)
                        .setSize(scrollSize)
                        .setScroll(TimeValue.timeValueMillis(scrollMillis))
                        .setQuery(termQuery("xbib.uid", volume.id()))
                        .addSort(SortBuilders.fieldSort("_doc"));
                SearchResponse holdingSearchResponse = holdingsSearchRequest.execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
                getMetric().mark();
                logger.debug("searchSeriesVolumeHoldings search request={} hits={}",
                        holdingsSearchRequest.toString(), holdingSearchResponse.getHits().getTotalHits());
               do {
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
                                if (holdingsLicensesMerger.mappedISIL().lookup().containsKey(isil)) {
                                    isil = (String) holdingsLicensesMerger.mappedISIL().lookup().get(isil);
                                }
                                // blacklisted ISIL?
                                if (holdingsLicensesMerger.blackListedISIL().lookup(isil)) {
                                    continue;
                                }
                                volumeHolding.addParent(titleRecord.externalID());
                                volumeHolding.addParent(volume.externalID());
                                volumeHolding.setMediaType(titleRecord.mediaType());
                                volumeHolding.setCarrierType(titleRecord.carrierType());
                                volumeHolding.setDate(volume.firstDate(), volume.lastDate());
                                volumeHolding.setName(holdingsLicensesMerger.bibdatLookup()
                                        .lookupName().get(isil));
                                volumeHolding.setRegion(holdingsLicensesMerger.bibdatLookup()
                                        .lookupRegion().get(isil));
                                volumeHolding.setOrganization(holdingsLicensesMerger.bibdatLookup()
                                        .lookupOrganization().get(isil));
                                volumeHolding.setServiceMode(holdingsLicensesMerger.statusCodeMapper()
                                        .lookup(volumeHolding.getStatus()));
                                if ("interlibrary".equals(volumeHolding.getServiceType()) && isil != null) {
                                    volume.addRelatedHolding(isil, volumeHolding);
                                }
                            }
                        }
                    }
                    holdingSearchResponse = holdingsLicensesMerger.search().client()
                           .prepareSearchScroll(holdingSearchResponse.getScrollId())
                           .setScroll(TimeValue.timeValueMillis(scrollMillis))
                           .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
               } while (holdingSearchResponse.getHits().getHits().length > 0);
                holdingsLicensesMerger.search().client()
                        .prepareClearScroll().addScrollId(holdingSearchResponse.getScrollId())
                        .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
                // this also copies holdings from the found volume to the title record
                titleRecord.addVolume(volume);
            }
            searchResponse = holdingsLicensesMerger.search().client()
                    .prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
        } while (searchResponse.getHits().getHits().length > 0);
        holdingsLicensesMerger.search().client()
                .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet(timeoutSeconds, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private Set<String> findTheRelationsBetween(TitleRecord titleRecord, String id) {
        Set<String> relationNames = new HashSet<>();
        for (String entry : TitleRecord.relationEntries()) {
            Object o = titleRecord.map().get(entry);
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
    }

    @SuppressWarnings("unchecked")
    private void setAllRelationsBetween(TitleRecord titleRecord, Collection<TitleRecord> cluster) {
        for (String relation : TitleRecord.relationEntries()) {
            Object o = titleRecord.map().get(relation);
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
                                logger.trace("entry {} has no relation name in {}", entry, titleRecord.externalID());
                            }
                            continue;
                        }
                    }
                    internalObj = entry.get("identifierDNB");
                    // take only first entry from list...
                    String value = internalObj == null ? null : internalObj instanceof List ?
                            ((List) internalObj).get(0).toString() : internalObj.toString();
                    for (TitleRecord m : cluster) {
                        // self?
                        if (m.id().equals(titleRecord.id())) {
                            continue;
                        }
                        if (m.id().equals(value)) {
                            titleRecord.addRelated(key, m);
                            // special trick: move over links from online to print
                            if ("hasPrintEdition".equals(key)) {
                                m.setLinks(titleRecord.getLinks());
                            }
                            String inverse = TitleRecord.getInverseRelations().get(key);
                            if (inverse != null) {
                                m.addRelated(inverse, titleRecord);
                            } else {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("no inverse relation for {} in {}, using 'isRelatedTo'", key,
                                            titleRecord.externalID());
                                }
                                m.addRelated("isRelatedTo", titleRecord);
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void indexTitleRecord(TitleRecord m) throws IOException {
        indexTitleRecord(m, null);
    }

    @SuppressWarnings("unchecked")
    private void indexTitleRecord(TitleRecord m, StatCounter statCounter) throws IOException {
        // find open access
        Collection<String> issns = m.hasIdentifiers() ?
                (Collection<String>) m.getIdentifiers().get("formattedissn") : null;
        if (issns != null) {
            boolean found = false;
            for (String issn : issns) {
                found = found || findOpenAccess(sourceOpenAccessIndex, issn);
            }
            m.setOpenAccess(found);
            if (found) {
                if (statCounter != null) {
                    statCounter.increase("stat", "openaccess", 1);
                }
            }
        }
        // first, index related conference/proceedings/abstracts/...
        if (!m.getMonographVolumes().isEmpty()) {
            for (MonographVolume volume : m.getMonographVolumes()) {
                XContentBuilder builder = jsonBuilder();
                volume.toXContent(builder, XContentBuilder.EMPTY_PARAMS, statCounter);
                String vid = volume.externalID();
                if (checkSize(manifestationsIndex, manifestationsIndexType, vid, builder)) {
                    holdingsLicensesMerger.ingest().index(manifestationsIndex, manifestationsIndexType, vid,
                            builder.string());
                }
                MultiMap<String,Holding> mm = volume.getRelatedHoldings();
                for (String key : mm.keySet()) {
                    for (Holding volumeHolding : mm.get(key)){
                        builder = jsonBuilder();
                        volumeHolding.toXContent(builder, XContentBuilder.EMPTY_PARAMS);
                        // to holding index
                        String hid = volume.externalID();
                        if (checkSize(holdingsIndex, holdingsIndexType, hid, builder)) {
                            holdingsLicensesMerger.ingest().index(holdingsIndex, holdingsIndexType, hid, builder.string());
                        }
                        if (statCounter != null) {
                            statCounter.increase("stat", "holdings", 1);
                        }
                        // extra entry by date
                        String vhid = "(" + volumeHolding.getServiceISIL() + ")" + volume.externalID()
                                + (volumeHolding.getFirstDate() != null ? "." + volumeHolding.getFirstDate() : null);
                        if (checkSize(volumesIndex, volumesIndexType, vhid, builder)) {
                            holdingsLicensesMerger.ingest().index(volumesIndex, volumesIndexType, vhid, builder.string());
                        }
                        if (statCounter != null) {
                            statCounter.increase("stat", "volumes", 1);
                        }
                    }
                }
            }
            int n = m.getMonographVolumes().size();
            if (statCounter != null) {
                statCounter.increase("stat", "manifestations", n);
            }
        }
        // write holdings and services
        if (!m.getRelatedHoldings().isEmpty()) {
            XContentBuilder builder = jsonBuilder();
            builder.startObject()
                    .field("parent", m.externalID());
            if (m.hasLinks()) {
                builder.field("links", m.getLinks());
            }
            builder.startArray("institution");
            int instcount = 0;
            final MultiMap<String, Holding> holdingsMap = m.getRelatedHoldings();
            for (String isil : holdingsMap.keySet()) {
                // blacklisted ISIL?
                if (holdingsLicensesMerger.blackListedISIL().lookup(isil)) {
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
                        if (checkSize(servicesIndex, servicesIndexType, serviceId, serviceBuilder)) {
                            holdingsLicensesMerger.ingest().index(servicesIndex, servicesIndexType,
                                    serviceId, serviceBuilder.string());
                        }
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
            // one holding per manifestation
            if (checkSize(holdingsIndex, holdingsIndexType, m.externalID(), builder)) {
                holdingsLicensesMerger.ingest().index(holdingsIndex, holdingsIndexType,
                        m.externalID(), builder.string());
            }
            if (statCounter != null) {
                statCounter.increase("stat", "holdings", 1);
            }
            // holdings per year
            MultiMap<Integer,Holding> map = m.getHoldingsByDate();
            for (Integer date : map.keySet()) {
                Collection<Holding> holdings = map.get(date);
                String volumeId = m.externalID() + (date != -1 ? "." + date : "");
                builder = jsonBuilder();
                buildVolume(builder, m, m.externalID(), date, holdings);
                if (checkSize(volumesIndex, volumesIndexType, volumeId, builder)) {
                    holdingsLicensesMerger.ingest().index(volumesIndex, volumesIndexType,
                            volumeId, builder.string());
                }
            }
            if (statCounter != null) {
                statCounter.increase("stat", "volumes", map.size());
            }
        }
        if (statCounter != null) {
            statCounter.increase("stat", "manifestations", 1);
        }
        XContentBuilder builder = jsonBuilder();
        m.toXContent(builder, XContentBuilder.EMPTY_PARAMS, statCounter);
        if (checkSize(manifestationsIndex, manifestationsIndexType, m.externalID(), builder)) {
            holdingsLicensesMerger.ingest().index(manifestationsIndex, manifestationsIndexType, m.externalID(),
                    builder.string());
        }
    }

    private void buildVolume(XContentBuilder builder,
                             TitleRecord titleRecord,
                             String parentIdentifier,
                             Integer date,
                             Collection<Holding> holdings)
            throws IOException {
        builder.startObject()
                .field("identifierForTheVolume", parentIdentifier + "." + date);
        if (date != -1) {
            builder.field("date", date);
        }
        if (titleRecord.hasLinks()) {
            builder.field("links", titleRecord.getLinks());
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

    private boolean findOpenAccess(String index, String issn) {
        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(holdingsLicensesMerger.search().client(),
                SearchAction.INSTANCE);
        searchRequestBuilder
                .setSize(0)
                .setIndices(index)
                .setQuery(termQuery("dc:identifier", issn));
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet(30, TimeUnit.SECONDS);
        logger.debug("open access query {} ", searchRequestBuilder, searchResponse.getHits().getTotalHits());
        return searchResponse.getHits().getTotalHits() > 0;
    }

    private XContentBuilder jsonBuilder() throws IOException {
        if (holdingsLicensesMerger.settings().getAsBoolean("mock", false)) {
            return org.xbib.common.xcontent.XContentService.jsonBuilder().prettyPrint();
        } else {
            return org.xbib.common.xcontent.XContentService.jsonBuilder();
        }
    }

    private boolean checkSize(String index, String type, String id, XContentBuilder builder) throws IOException {
        if (holdingsLicensesMerger.settings().getAsBoolean("mock", false)) {
            logger.debug("{}/{}/{} {}", index, type, id, builder.string());
        }
        long len = builder.string().length();
        if (len > 1024 * 1024) {
            logger.warn("large document {}/{}/{} detected: {} bytes", index, type, id, len);
            return false;
        }
        return true;
    }

    private static class ClusterBuildContinuation {
        final TitleRecord titleRecord;
        final SearchResponse searchResponse;
        final Collection<TitleRecord> cluster;
        final Set<String> relations;
        int pos;

        ClusterBuildContinuation(TitleRecord titleRecord,
                                 SearchResponse searchResponse,
                                 Set<String> relations,
                                 Collection<TitleRecord> cluster,
                                 int pos) {
            this.titleRecord = titleRecord;
            this.searchResponse = searchResponse;
            this.relations = relations;
            this.cluster = cluster;
            this.pos = pos;
        }
    }
}
