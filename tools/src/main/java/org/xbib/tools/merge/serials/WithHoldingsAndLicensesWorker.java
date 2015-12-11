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
package org.xbib.tools.merge.serials;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.settings.Settings;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.metric.MeterMetric;
import org.xbib.tools.merge.serials.support.StatCounter;
import org.xbib.tools.merge.serials.entities.Holding;
import org.xbib.tools.merge.serials.entities.Indicator;
import org.xbib.tools.merge.serials.entities.License;
import org.xbib.tools.merge.serials.entities.TitleRecord;
import org.xbib.tools.merge.serials.entities.MonographVolume;
import org.xbib.tools.merge.serials.entities.MonographVolumeHolding;
import org.xbib.util.ExceptionFormatter;
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
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.xbib.common.xcontent.XContentService.jsonBuilder;

public class WithHoldingsAndLicensesWorker implements Worker<Pipeline<WithHoldingsAndLicensesWorker, TitelRecordRequest>, TitelRecordRequest> {

    enum State {
        COLLECTING_CANDIDATES, PROCESSING, INDEXING
    }

    private final int number;
    private final WithHoldingsAndLicenses withHoldingsAndLicenses;
    private final Logger logger;
    private final Queue<ClusterBuildContinuation> buildQueue;

    private final String sourceTitleIndex;
    private final String sourceTitleType;
    private final String sourceHoldingsIndex;
    private final String sourceHoldingsType;
    private final String sourceLicenseIndex;
    private final String sourceLicenseType;
    private final String sourceIndicatorIndex;
    private final String sourceIndicatorType;
    private final String sourceMonographicIndex;
    private final String sourceMonographicHoldingsIndex;

    private final String manifestationsIndex;
    private final String manifestationsIndexType;
    private final String holdingsIndex;
    private final String holdingsIndexType;
    private final String volumesIndex;
    private final String volumesIndexType;
    private final String serviceIndex;
    private final String serviceIndexType;

    private State state;

    private Set<TitleRecord> candidates;


    public WithHoldingsAndLicensesWorker(WithHoldingsAndLicenses withHoldingsAndLicenses, int number) {
        this.number = number;
        this.withHoldingsAndLicenses = withHoldingsAndLicenses;
        this.buildQueue = new ConcurrentLinkedQueue<>();
        this.logger = LogManager.getLogger(toString());

        Settings settings = withHoldingsAndLicenses.settings();
        this.sourceTitleIndex = settings.get("bib-index");
        this.sourceTitleType = settings.get("bib-type");
        this.sourceHoldingsIndex = settings.get("hol-index");
        this.sourceHoldingsType = settings.get("hol-type");
        this.sourceLicenseIndex = settings.get("xml-license-index");
        this.sourceLicenseType = settings.get("xml-license-type");
        this.sourceIndicatorIndex = settings.get("web-license-index");
        this.sourceIndicatorType = settings.get("web-license-type");
        this.sourceMonographicIndex = settings.get("monographic-index");
        this.sourceMonographicHoldingsIndex = settings.get("monographic-hol-index");

        if (Strings.isNullOrEmpty(sourceTitleIndex)) {
            throw new IllegalArgumentException("no bib-index parameter given");
        }
        if (Strings.isNullOrEmpty(sourceHoldingsIndex)) {
            throw new IllegalArgumentException("no hol-index parameter given");
        }
        if (Strings.isNullOrEmpty(sourceLicenseIndex)) {
            throw new IllegalArgumentException("no xml-license-index parameter given");
        }
        if (Strings.isNullOrEmpty(sourceIndicatorIndex)) {
            throw new IllegalArgumentException("no web-license-index parameter given");
        }
        if (Strings.isNullOrEmpty(sourceMonographicIndex)) {
            throw new IllegalArgumentException("no monographic-index parameter given");
        }
        if (Strings.isNullOrEmpty(sourceMonographicHoldingsIndex)) {
            throw new IllegalArgumentException("no monographic-hol-index parameter given");
        }

        String index = settings.get("index");
        if (index == null) {
            throw new IllegalArgumentException("no index given");
        }
        this.manifestationsIndex = settings.get("manifestations-index", index);
        this.manifestationsIndexType = settings.get("manifestations-type", "Manifestations");
        this.holdingsIndex = settings.get("holdings-index", index);
        this.holdingsIndexType = settings.get("holdings-type", "Holdings");
        this.volumesIndex = settings.get("volumes-index", index);
        this.volumesIndexType = settings.get("volumes-type", "Volumes");
        this.serviceIndex = settings.get("service-index", index);
        this.serviceIndexType = settings.get("service-type", "Services");
    }

    @Override
    public Worker<Pipeline<WithHoldingsAndLicensesWorker, TitelRecordRequest>, TitelRecordRequest> setPipeline(Pipeline<WithHoldingsAndLicensesWorker, TitelRecordRequest> pipeline) {
        // unused
        return this;
    }

    public Pipeline<WithHoldingsAndLicensesWorker, TitelRecordRequest> getPipeline() {
        return withHoldingsAndLicenses;
    }

    @Override
    public WithHoldingsAndLicensesWorker setMetric(MeterMetric metric) {
        // ignore
        return this;
    }

    @Override
    public MeterMetric getMetric() {
        return null; // no metric
    }

    public Queue<ClusterBuildContinuation> getBuildQueue() {
        return buildQueue;
    }

    public Set<TitleRecord> getCandidates() {
        return candidates;
    }

    @Override
    public TitelRecordRequest call() throws Exception {
        logger.info("worker starting");
        TitelRecordRequest element = null;
        TitleRecord titleRecord = null;
        try {
            element = withHoldingsAndLicenses.getQueue().take();
            titleRecord = element != null ? element.get() : null;
            while (titleRecord != null) {
                process(titleRecord);
                element = withHoldingsAndLicenses.getQueue().take();
                titleRecord = element != null ? element.get() : null;
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            logger.error(ExceptionFormatter.format(e));
            logger.error("exiting, exception while processing {}", titleRecord);
        } finally {
            withHoldingsAndLicenses.countDown();
            logger.info("worker terminating");
        }
        return element;
    }

    @Override
    public void close() throws IOException {
        if (!withHoldingsAndLicenses.getQueue().isEmpty()) {
            logger.error("service queue not empty?");
        }
        if (!buildQueue.isEmpty()) {
            logger.error("complete queue not empty?");
        }
        logger.info("closing");
    }

    @Override
    public String toString() {
        return WithHoldingsAndLicenses.class.getSimpleName() + "." + number;
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
        boolean retry;
        do {
            // Ensure all relationships in the candidate set
            state = State.PROCESSING;
            for (TitleRecord m : candidates) {
                setAllRelationsBetween(m, candidates);
            }
            // Now, this is expensive. Find holdings, licenses, indicators of candidates
            Set<Holding> holdings = new HashSet<>();
            searchHoldings(candidates, holdings);
            Set<License> licenses = new HashSet<>();
            searchLicensesAndIndicators(candidates, licenses);
            searchMonographs(candidates);
            // before indexing, fetch build queue again
            retry = false;
            int before = candidates.size();
            while ((cont = buildQueue.poll()) != null) {
                for (TitleRecord tr : cont.cluster) {
                    candidates.add(tr);
                }
                int after = candidates.size();
                retry = after > before;
                continueClusterBuild(candidates, cont, 0);
            }
            if (retry) {
                logger.info("{}: retrying before indexing", titleRecord);
                continue;
            }
            state = State.INDEXING;
            for (TitleRecord tr : candidates) {
                indexTitleRecord(tr);
            }
            retry = false;
            before = candidates.size();
            while ((cont = buildQueue.poll()) != null) {
                for (TitleRecord tr : cont.cluster) {
                    candidates.add(tr);
                }
                int after = candidates.size();
                retry = after > before;
                continueClusterBuild(candidates, cont, 0);
            }
            if (retry) {
                logger.info("{}: retrying after indexing", titleRecord);
            }
        } while (retry);
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
        SearchRequestBuilder searchRequest = withHoldingsAndLicenses.client().prepareSearch()
                .setQuery(queryBuilder)
                .setSize(withHoldingsAndLicenses.size()) // size is per shard!
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()));
        searchRequest.setIndices(sourceTitleIndex);
        if (sourceTitleType != null) {
            searchRequest.setTypes(sourceTitleType);
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        searchResponse = withHoldingsAndLicenses.client().prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                .execute().actionGet();
        withHoldingsAndLicenses.queryMetric().mark();
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
        SearchHits hits;
        do {
            hits = searchResponse.getHits();
            for (int i = c.pos; i < hits.getHits().length; i++) {
                SearchHit hit = hits.getAt(i);
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
            searchResponse = withHoldingsAndLicenses.client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                    .execute().actionGet();
            withHoldingsAndLicenses.queryMetric().mark();
            hits = searchResponse.getHits();
        } while (hits.getHits().length > 0);
    }

    private boolean detectCollisionAndTransfer(TitleRecord titleRecord, ClusterBuildContinuation c, int pos) {
        for (Worker worker : withHoldingsAndLicenses.getWorkers()) {
            if (this == worker) {
                continue;
            }
            WithHoldingsAndLicensesWorker pipeline = (WithHoldingsAndLicensesWorker)worker;
            Set<TitleRecord> set = pipeline.getCandidates();
            if (set != null && set.contains(titleRecord)) {
                logger.warn("collision detected for {} with {} state={}",
                        titleRecord,
                        pipeline,
                        pipeline.state.name()
                );
                c.pos = pos;
                // remove from our candidates, because we pass them over to other thread
                candidates.remove(titleRecord);

                for (TitleRecord tr : c.cluster) {
                    candidates.remove(tr);
                }
                pipeline.getBuildQueue().offer(c);
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
            map.put(m.id(), m);
            // add print if not already there...
            if (m.getPrintID() != null && !map.containsKey(m.getPrintID())) {
                map.put(m.getPrintID(), m);
            }
        }
        searchHoldings(map, holdings);
    }

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
            QueryBuilder queryBuilder = termsQuery("identifierForTheParentRecord", subarray);
            // getSize is per shard
            SearchRequestBuilder searchRequest = withHoldingsAndLicenses.client().prepareSearch()
                    .setQuery(queryBuilder)
                    .setSize(withHoldingsAndLicenses.size())
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()));
            searchRequest.setIndices(sourceHoldingsIndex);
            if (sourceHoldingsType != null) {
                searchRequest.setTypes(sourceHoldingsType);
            }
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            /*logger.debug("searchHoldings search request = {}/{} {} hits={}",
                    sourceHoldingsIndex,
                    sourceHoldingsType,
                    searchRequest.toString(),
                    searchResponse.getHits().getTotalHits());*/
            while (searchResponse.getScrollId() != null) {
                searchResponse = withHoldingsAndLicenses.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                        .execute().actionGet();
                withHoldingsAndLicenses.queryMetric().mark();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    Holding holding = new Holding(hit.getSource());
                    if (holding.isDeleted()) {
                        continue;
                    }
                    String isil = holding.getISIL();
                    if (isil == null) {
                        continue;
                    }
                    // blacklisted ISIL?
                    if (withHoldingsAndLicenses.blackListedISIL().lookup().contains(isil)) {
                        continue;
                    }
                    // mapped ISIL?
                    if (withHoldingsAndLicenses.mappedISIL().lookup().containsKey(isil)) {
                        isil = (String)withHoldingsAndLicenses.mappedISIL().lookup().get(isil);
                    }
                    // consortia?
                    if (withHoldingsAndLicenses.consortiaLookup().lookupISILs().containsKey(isil)) {
                        List<String> list = withHoldingsAndLicenses.consortiaLookup().lookupISILs().get(isil);
                        for (String expandedisil : list) {
                            // new Holding for each ISIL
                            holding = new Holding(holding.map());
                            holding.setISIL(expandedisil);
                            holding.setName(withHoldingsAndLicenses.bibdatLookup().lookupName().get(expandedisil));
                            holding.setRegion(withHoldingsAndLicenses.bibdatLookup().lookupRegion().get(expandedisil));
                            holding.setOrganization(withHoldingsAndLicenses.bibdatLookup().lookupOrganization().get(expandedisil));
                            TitleRecord parentTitleRecord = titleRecordMap.get(holding.parentIdentifier());
                            parentTitleRecord.addRelatedHolding(expandedisil, holding);
                            holdings.add(holding);
                        }
                    } else {
                        holding.setName(withHoldingsAndLicenses.bibdatLookup().lookupName().get(isil));
                        holding.setRegion(withHoldingsAndLicenses.bibdatLookup().lookupRegion().get(isil));
                        holding.setOrganization(withHoldingsAndLicenses.bibdatLookup().lookupOrganization().get(isil));
                        TitleRecord parentTitleRecord = titleRecordMap.get(holding.parentIdentifier());
                        parentTitleRecord.addRelatedHolding(isil, holding);
                        holdings.add(holding);
                    }
                }
            }
        }
    }

    private void searchLicensesAndIndicators(Collection<TitleRecord> titleRecords, Set<License> licenses) throws IOException {
        // create a map of all title records that can have assigned a license.
        Map<String, TitleRecord> map = new HashMap<>();
        boolean isOnline = false;
        for (TitleRecord m : titleRecords) {
            map.put(m.externalID(), m);
            // we really just rely on the carrier type. There may be licenses or indicators.
            isOnline = isOnline || "online resource".equals(m.carrierType());
            // copy print to the online edition in case it is not there
            String id = m.getOnlineExternalID();
            if (id != null && !map.containsKey(id)) {
                map.put(id, m);
            }
        }
        if (isOnline) {
            searchLicenses(licenses, map);
            searchIndicators(licenses, map);
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
            SearchRequestBuilder searchRequest = withHoldingsAndLicenses.client().prepareSearch()
                    .setQuery(queryBuilder)
                    .setSize(withHoldingsAndLicenses.size())
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()));
            searchRequest.setIndices(sourceLicenseIndex);
            if (sourceLicenseType != null) {
                searchRequest.setTypes(sourceLicenseType);
            }
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            /*logger.debug("searchLicenses search request = {} hits={}",
                    searchRequest.toString(),
                    searchResponse.getHits().getTotalHits());*/
            while (searchResponse.getScrollId() != null) {
                searchResponse = withHoldingsAndLicenses.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                        .execute().actionGet();
                withHoldingsAndLicenses.queryMetric().mark();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    License license = new License(hit.getSource());
                    if (license.isDeleted()) {
                        continue;
                    }
                    String isil = license.getISIL();
                    if (isil == null) {
                        continue;
                    }
                    if (withHoldingsAndLicenses.blackListedISIL().lookup().contains(isil)) {
                        continue;
                    }
                    // mapped ISIL?
                    if (withHoldingsAndLicenses.mappedISIL().lookup().containsKey(isil)) {
                        isil = (String)withHoldingsAndLicenses.mappedISIL().lookup().get(isil);
                    }
                    license.setName(withHoldingsAndLicenses.bibdatLookup().lookupName().get(isil));
                    license.setRegion(withHoldingsAndLicenses.bibdatLookup().lookupRegion().get(isil));
                    license.setOrganization(withHoldingsAndLicenses.bibdatLookup().lookupOrganization().get(isil));
                    for (String parent : license.parents()) {
                        TitleRecord m = titleRecordMap.get(parent);
                        m.addRelatedHolding(isil, license);
                        /*logger.debug("license {} attached to {} print={} online={}",
                                license.identifier(),
                                m.externalID(),
                                m.getPrintExternalID(),
                                m.getOnlineExternalID());*/
                    }
                    licenses.add(license);
                }
            }
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
            // getSize is per shard
            SearchRequestBuilder searchRequest = withHoldingsAndLicenses.client().prepareSearch()
                    .setQuery(queryBuilder)
                    .setSize(withHoldingsAndLicenses.size())
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()));
            searchRequest.setIndices(sourceIndicatorIndex);
            if (sourceLicenseType != null) {
                searchRequest.setTypes(sourceIndicatorType);
            }
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            while (searchResponse.getScrollId() != null) {
                searchResponse = withHoldingsAndLicenses.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                        .execute().actionGet();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    Indicator indicator = new Indicator(hit.getSource());
                    String isil = indicator.getISIL();
                    if (isil == null) {
                        continue;
                    }
                    // blacklisted ISIL?
                    if (withHoldingsAndLicenses.blackListedISIL().lookup().contains(isil)) {
                        continue;
                    }
                    // mapped ISIL?
                    if (withHoldingsAndLicenses.mappedISIL().lookup().containsKey(isil)) {
                        isil = (String)withHoldingsAndLicenses.mappedISIL().lookup().get(isil);
                    }
                    indicator.setName(withHoldingsAndLicenses.bibdatLookup().lookupName().get(isil));
                    indicator.setRegion(withHoldingsAndLicenses.bibdatLookup().lookupRegion().get(isil));
                    indicator.setOrganization(withHoldingsAndLicenses.bibdatLookup().lookupOrganization().get(isil));
                    for (String parent : indicator.parents()) {
                        TitleRecord m = titleRecordMap.get(parent);
                        m.addRelatedIndicator(isil, indicator);
                        /*logger.debug("indicator {} attached to {} print={} online={}",
                                indicator.identifier(),
                                m.externalID(),
                                m.getPrintExternalID(),
                                m.getOnlineExternalID());*/
                    }
                    indicators.add(indicator);
                }
            }
        }
    }

    private void searchMonographs(Collection<TitleRecord> titleRecords) throws IOException {
        for (TitleRecord titleRecord : titleRecords) {
            SearchRequestBuilder searchRequest = withHoldingsAndLicenses.client().prepareSearch()
                    .setIndices(sourceMonographicIndex)
                    .setSize(withHoldingsAndLicenses.size())
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                    .setQuery(termQuery("IdentifierZDB.identifierZDB", titleRecord.externalID()));
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            withHoldingsAndLicenses.queryMetric().mark();
            /*logger.debug("searchMonographs search request = {} hits={}",
                    searchRequest.toString(), searchResponse.getHits().getTotalHits());*/
            while (searchResponse.getScrollId() != null) {
                searchResponse = withHoldingsAndLicenses.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                        .execute().actionGet();
                withHoldingsAndLicenses.queryMetric().mark();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    Map<String, Object> m = hit.getSource();
                    MonographVolume volume = new MonographVolume(m, titleRecord);
                    searchExtraHoldings(volume);
                    searchSeriesVolumeHoldings(volume);
                }
            }
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
        SearchRequestBuilder holdingsSearchRequest = withHoldingsAndLicenses.client().prepareSearch()
                .setIndices(sourceMonographicHoldingsIndex)
                .setSize(withHoldingsAndLicenses.size())
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                .setQuery(termQuery("xbib.uid", key));
        SearchResponse holdingSearchResponse = holdingsSearchRequest.execute().actionGet();
        withHoldingsAndLicenses.queryMetric().mark();
        //logger.debug("searchExtraHoldings search request = {} hits={}",
        //        holdingsSearchRequest.toString(), holdingSearchResponse.getHits().getTotalHits());
        while (holdingSearchResponse.getScrollId() != null) {
            holdingSearchResponse = withHoldingsAndLicenses.client().prepareSearchScroll(holdingSearchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                    .execute().actionGet();
            withHoldingsAndLicenses.queryMetric().mark();
            SearchHits holdingHits = holdingSearchResponse.getHits();
            if (holdingHits.getHits().length == 0) {
                break;
            }
            for (SearchHit holdingHit : holdingHits) {
                Object o = holdingHit.getSource().get("Item");
                if (!(o instanceof List)) {
                    o = Collections.singletonList(o);
                }
                for (Map<String,Object> item : (List<Map<String,Object>>)o) {
                    if (item != null && !item.isEmpty()) {
                        MonographVolumeHolding volumeHolding = new MonographVolumeHolding(item, volume);
                        volumeHolding.addParent(volume.externalID());
                        volumeHolding.setMediaType(titleRecord.mediaType());
                        volumeHolding.setCarrierType(titleRecord.carrierType());
                        volumeHolding.setDate(volume.firstDate(), volume.lastDate());
                        String isil = volumeHolding.getISIL();
                        volumeHolding.setName(withHoldingsAndLicenses.bibdatLookup().lookupName().get(isil));
                        volumeHolding.setRegion(withHoldingsAndLicenses.bibdatLookup().lookupRegion().get(isil));
                        volumeHolding.setOrganization(withHoldingsAndLicenses.bibdatLookup().lookupOrganization().get(isil));
                        volumeHolding.setServiceMode(withHoldingsAndLicenses.statusCodeMapper().lookup(volumeHolding.getStatus()));
                        if ("interlibrary".equals(volumeHolding.getServiceType()) && isil != null) {
                            volume.addRelatedHolding(isil, volumeHolding);
                        }
                    }
                }
            }
        }
    }

    /**
     * Search all holdings in this series, if it is a series
     * @param parent the parent volume
     * @throws IOException
     */
    private void searchSeriesVolumeHoldings(MonographVolume parent)
            throws IOException {
        TitleRecord titleRecord = parent.getTitleRecord();
        // search children volumes of the series (conference, processing, abstract, ...)
        SearchRequestBuilder searchRequest = withHoldingsAndLicenses.client().prepareSearch()
                .setIndices(sourceMonographicIndex)
                .setSize(withHoldingsAndLicenses.size())
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                .setQuery(boolQuery().should(termQuery("SeriesAddedEntryUniformTitle.designation", parent.id()))
                        .should(termQuery("RecordIdentifierSuper.recordIdentifierSuper", parent.id())));
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        withHoldingsAndLicenses.queryMetric().mark();
        //logger.debug("searchSeriesVolumeHoldings search request={} hits={}",
        //        searchRequest.toString(), searchResponse.getHits().getTotalHits());
        while (searchResponse.getScrollId() != null) {
            searchResponse = withHoldingsAndLicenses.client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                    .execute().actionGet();
            withHoldingsAndLicenses.queryMetric().mark();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                MonographVolume volume = new MonographVolume(hit.getSource(), titleRecord);
                volume.addParent(titleRecord.externalID());
                // for each conference/congress, search holdings
                SearchRequestBuilder holdingsSearchRequest = withHoldingsAndLicenses.client().prepareSearch()
                        .setIndices(sourceMonographicHoldingsIndex)
                        .setSize(withHoldingsAndLicenses.size())
                        .setSearchType(SearchType.SCAN)
                        .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                        .setQuery(termQuery("xbib.uid", volume.id()));
                SearchResponse holdingSearchResponse = holdingsSearchRequest.execute().actionGet();
                withHoldingsAndLicenses.queryMetric().mark();
                //logger.debug("searchSeriesVolumeHoldings search request={} hits={}",
                //        holdingsSearchRequest.toString(), holdingSearchResponse.getHits().getTotalHits());
                while (holdingSearchResponse.getScrollId() != null) {
                    holdingSearchResponse = withHoldingsAndLicenses.client().prepareSearchScroll(holdingSearchResponse.getScrollId())
                            .setScroll(TimeValue.timeValueMillis(withHoldingsAndLicenses.millis()))
                            .execute().actionGet();
                    withHoldingsAndLicenses.queryMetric().mark();
                    SearchHits holdingHits = holdingSearchResponse.getHits();
                    if (holdingHits.getHits().length == 0) {
                        break;
                    }
                    for (SearchHit holdingHit : holdingHits) {
                        // one hit, many items. Iterate over items
                        Object o = holdingHit.getSource().get("Item");
                        if (!(o instanceof List)) {
                            o = Collections.singletonList(o);
                        }
                        for (Map<String,Object> item : (List<Map<String,Object>>)o) {
                            if (item != null && !item.isEmpty()) {
                                MonographVolumeHolding volumeHolding = new MonographVolumeHolding(item, volume);
                                volumeHolding.addParent(titleRecord.externalID());
                                volumeHolding.addParent(volume.externalID());
                                volumeHolding.setMediaType(titleRecord.mediaType());
                                volumeHolding.setCarrierType(titleRecord.carrierType());
                                volumeHolding.setDate(volume.firstDate(), volume.lastDate());
                                String isil = volumeHolding.getISIL();
                                volumeHolding.setName(withHoldingsAndLicenses.bibdatLookup().lookupName().get(isil));
                                volumeHolding.setRegion(withHoldingsAndLicenses.bibdatLookup().lookupRegion().get(isil));
                                volumeHolding.setOrganization(withHoldingsAndLicenses.bibdatLookup().lookupOrganization().get(isil));
                                volumeHolding.setServiceMode(withHoldingsAndLicenses.statusCodeMapper().lookup(volumeHolding.getStatus()));
                                if ("interlibrary".equals(volumeHolding.getServiceType()) && isil != null) {
                                    volume.addRelatedHolding(isil, volumeHolding);
                                }
                            }
                        }
                    }
                }
                // this also copies holdings from the found volume to the title record
                titleRecord.addVolume(volume);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> findTheRelationsBetween(TitleRecord titleRecord, String id) {
        Set<String> relationNames = new HashSet<String>();
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
                                    logger.trace("no inverse relation for {} in {}, using 'isRelatedTo'", key, titleRecord.externalID());
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
        Collection<String> issns = m.hasIdentifiers() ? (Collection<String>) m.getIdentifiers().get("formattedissn") : null;
        if (issns != null) {
            boolean found = false;
            for (String issn : issns) {
                found = found || withHoldingsAndLicenses.findOpenAccess(issn);
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
                withHoldingsAndLicenses.ingest().index(manifestationsIndex, manifestationsIndexType, vid, builder.string());
                withHoldingsAndLicenses.indexMetric().mark(1);
                MultiMap<String,Holding> mm = volume.getRelatedHoldings();
                for (String key : mm.keySet()) {
                    for (Holding volumeHolding : mm.get(key)){
                        builder = jsonBuilder();
                        volumeHolding.toXContent(builder, XContentBuilder.EMPTY_PARAMS);
                        // to holding index
                        String hid = volume.externalID();
                        withHoldingsAndLicenses.ingest().index(holdingsIndex, holdingsIndexType, hid, builder.string());
                        withHoldingsAndLicenses.indexMetric().mark(1);
                        if (statCounter != null) {
                            statCounter.increase("stat", "holdings", 1);
                        }
                        // extra entry by date
                        String vhid = "(" + volumeHolding.getServiceISIL() + ")" + volume.externalID()
                                + (volumeHolding.getFirstDate() != null ? "." + volumeHolding.getFirstDate() : null);
                        withHoldingsAndLicenses.ingest().index(volumesIndex, volumesIndexType, vhid, builder.string());
                        withHoldingsAndLicenses.indexMetric().mark(1);
                        if (statCounter != null) {
                            statCounter.increase("stat", "volumes", 1);
                        }
                    }
                }
            }
            int n = m.getMonographVolumes().size();
            withHoldingsAndLicenses.indexMetric().mark(n);
            if (statCounter != null) {
                statCounter.increase("stat", "manifestations", n);
            }
        }
        // write holdings and services
        if (!m.getRelatedHoldings().isEmpty()) {
            final MultiMap<String, Holding> holdingsMap = m.getRelatedHoldings();
            XContentBuilder builder = jsonBuilder();
            builder.startObject()
                    .field("parent", m.externalID());
            if (m.hasLinks()) {
                builder.field("links", m.getLinks());
            }
            builder.startArray("institution");
            int instcount = 0;
            for (String isil : holdingsMap.keySet()) {
                Collection<Holding> holdings = holdingsMap.get(isil);
                if (holdings != null && !holdings.isEmpty()) {
                    instcount++;
                    builder.startObject()
                            .field("isil", isil);
                    builder.startArray("services");
                    int count = 0;
                    for (Holding holding : holdings) {
                        if (holding.isDeleted()) {
                            continue;
                        }
                        String serviceId = "(" + holding.getServiceISIL() + ")" + holding.identifier();
                        XContentBuilder serviceBuilder = jsonBuilder();
                        holding.toXContent(serviceBuilder, XContentBuilder.EMPTY_PARAMS);
                        withHoldingsAndLicenses.ingest().index(serviceIndex, serviceIndexType,
                                    serviceId, serviceBuilder.string());
                        withHoldingsAndLicenses.indexMetric().mark(1);
                        builder.startObject();
                        builder.field("identifierForTheService", serviceId);
                        builder.field("dates", holding.dates());
                        builder.endObject();
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
            builder.endArray().field("institutioncount", instcount).endObject();
            // one holding per manifestation
            withHoldingsAndLicenses.ingest().index(holdingsIndex, holdingsIndexType,
                    m.externalID(), builder.string());
            withHoldingsAndLicenses.indexMetric().mark(1);
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
                withHoldingsAndLicenses.ingest().index(volumesIndex, volumesIndexType,
                            volumeId, builder.string());
                withHoldingsAndLicenses.indexMetric().mark(1);
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
        withHoldingsAndLicenses.ingest().index(manifestationsIndex, manifestationsIndexType, m.externalID(), builder.string());
        withHoldingsAndLicenses.indexMetric().mark(1);
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
        // TODO we need better organization of holdings per ISIL per year
        Map<String, Set<Holding>> institutions = new HashMap<>();
        for (Holding holding : holdings) {
            // create holdings in order
            Set<Holding> set = institutions.containsKey(holding.getISIL()) ? institutions.get(holding.getISIL()) : new TreeSet<>();
            set.add(holding);
            institutions.put(holding.getISIL(), set);
        }
        builder.field("institutioncount", institutions.size());
        builder.startArray("institution");
        for (String isil : institutions.keySet()) {
            Collection<Holding> set = institutions.get(isil);
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
        builder.endArray().endObject();
    }

    private class ClusterBuildContinuation {
        final TitleRecord titleRecord;
        final SearchResponse searchResponse;
        final Collection<TitleRecord> cluster;
        Set<String> relations;
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
