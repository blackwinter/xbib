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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
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
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineRequestListener;
import org.xbib.tools.merge.zdb.entities.TimelineCluster;
import org.xbib.tools.merge.zdb.entities.Holding;
import org.xbib.tools.merge.zdb.entities.Indicator;
import org.xbib.tools.merge.zdb.entities.License;
import org.xbib.tools.merge.zdb.entities.Manifestation;
import org.xbib.tools.merge.zdb.entities.TimeLine;
import org.xbib.tools.merge.zdb.licenseinfo.ManifestationPipelineElement;
import org.xbib.tools.merge.zdb.licenseinfo.WithHoldingsAndLicenses;
import org.xbib.util.ExceptionFormatter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newSetFromMap;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

public class WithHoldingsAndLicensesInTimelinePipeline
        implements Pipeline<Boolean, ManifestationPipelineElement> {

    private final int number;

    private final WithHoldingsAndLicensesInTimeline service;

    private final Logger logger;

    private final ObjectMapper mapper;

    private final Queue<ClusterBuildContinuation> buildQueue;

    private BlockingQueue<ManifestationPipelineElement> queue;

    private Map<String, PipelineRequestListener> listeners;

    private Set<Manifestation> candidates;

    private Manifestation manifestation;

    private String sourceTitleIndex;
    private String sourceTitleType;

    private String sourceHoldingsIndex;
    private String sourceHoldingsType;
    private String sourceLicenseIndex;
    private String sourceLicenseType;
    private String sourceIndicatorIndex;
    private String sourceIndicatorType;

    private final String worksIndex;
    private final String worksIndexType;
    private final String manifestationsIndex;
    private final String manifestationsIndexType;
    private final String holdingsIndex;
    private final String holdingsIndexType;
    private final String volumesIndex;
    private final String volumesIndexType;

    private MeterMetric metric;

    private MeterMetric serviceMetric;

    public WithHoldingsAndLicensesInTimelinePipeline(WithHoldingsAndLicensesInTimeline service, int number) {
        this.number = number;
        this.service = service;
        this.listeners = newHashMap();
        this.buildQueue = new ConcurrentLinkedQueue<ClusterBuildContinuation>();
        this.logger = LogManager.getLogger(toString());
        this.mapper = new ObjectMapper();

        Settings settings = service.settings();
        this.sourceTitleIndex = settings.get("bib-index");
        this.sourceTitleType = settings.get("bib-type");
        this.sourceHoldingsIndex = settings.get("hol-index");
        this.sourceHoldingsType = settings.get("hol-type");
        this.sourceLicenseIndex = settings.get("xml-license-index");
        this.sourceLicenseType = settings.get("xml-license-type");
        this.sourceIndicatorIndex = settings.get("web-license-index");
        this.sourceIndicatorType = settings.get("web-license-type");

        String index = settings.get("index");
        if (index == null) {
            throw new IllegalArgumentException("no index given");
        }
        this.worksIndex = settings.get("works-index", index);
        this.worksIndexType = settings.get("works-type", "Work");
        this.manifestationsIndex = settings.get("manifestation-index", index);
        this.manifestationsIndexType = settings.get("manifestation-type", "Manifestation");
        this.holdingsIndex = settings.get("holdings-index", index);
        this.holdingsIndexType = settings.get("holdings-type", "Holding");
        this.volumesIndex = settings.get("volumes-index", index);
        this.volumesIndexType = settings.get("volumes-type", "Volume");
    }

    public Queue<ClusterBuildContinuation> getBuildQueue() {
        return buildQueue;
    }

    public Collection<Manifestation> getCluster() {
        return candidates;
    }

    /*@Override
    public boolean hasNext() {
        SearchHitPipelineElement element;
        try {
            element = service.queue().poll(60, TimeUnit.SECONDS);
            if (element != null && element.get() != null) {
                manifestation = new Manifestation(mapper.readValue(element.get().source(), Map.class));
                manifestation.setForced(element.getForced());
            } else {
                manifestation = null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            manifestation = null;
            logger.error("pipeline processing interrupted, queue inactive", e);
        } catch (IOException e) {
            logger.error("exception while processing, exiting", e);
            manifestation = null;
        }
        return manifestation != null;
    }

    @Override
    public Manifestation next() {
        return manifestation;
    }*/

    @Override
    public Boolean call() throws Exception {
        logger.info("pipeline starting");
        try {
            this.metric = new MeterMetric(5L, TimeUnit.SECONDS);
            this.serviceMetric = new MeterMetric(5L, TimeUnit.SECONDS);
            ManifestationPipelineElement element = queue.poll();
            manifestation = element.get();
            while (manifestation != null) {
                if ("simple".equals(service.settings().get("mode", "standard"))) {
                    if (simpleCheck(manifestation)) {
                        simpleProcess(manifestation);
                    }
                } else {
                    if (check(manifestation)) {
                        processWork(manifestation);
                    }
                    for (PipelineRequestListener listener : listeners.values()) {
                        listener.newRequest(this, manifestation);
                    }
                }
                metric.mark();
                element = queue.poll();
                manifestation = element.get();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            logger.error(ExceptionFormatter.format(e));
            logger.error("exiting, exception while processing {}", manifestation);
        } finally {
            service.countDown();
            metric.stop();
        }
        logger.info("pipeline terminating");
        return true;
    }

    public MeterMetric getMetric() {
        return metric;
    }

    @Override
    public void close() throws IOException {
        if (!queue.isEmpty()) {
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

    public Pipeline<Boolean, ManifestationPipelineElement> add(String name, PipelineRequestListener listener) {
        this.listeners.put(name, listener);
        return this;
    }

    public MeterMetric getServiceMetric() {
        return serviceMetric;
    }

    private boolean check(Manifestation manifestation) {
        if (manifestation.getForced()) {
            return true;
        }
        String id = manifestation.externalID();
        // Skip supplements and manifestations with "succeededBy" relation (= in timeline) here.
        // All of them are referenced by another manifestation.
        // We pull them in later, while cluster construction.
        // Note: we also check online/CD manifestations here. They may continue
        // print manifestations but without correct "succeededBy/precededBy" pair
        // maybe "OtherEditionEntry" see ZDB 20679373.
        if (manifestation.isInTimeline() || manifestation.isSupplement()) {
            service.skipped().add(id);
            return false;
        }
        // if we had this seen, skip it
        return !service.processed().contains(id);
    }

    private boolean simpleCheck(Manifestation manifestation) {
        if (manifestation.getForced()) {
            return true;
        }
        String id = manifestation.externalID();
        if (!service.processed().contains(id)) {
            service.processed().add(id);
            return true;
        } else {
            return false;
        }
    }

    private void simpleProcess(Manifestation manifestation) throws IOException {
        // no Work set algorithm, only other edition (print/online)
        this.candidates = newSetFromMap(new ConcurrentHashMap<Manifestation, Boolean>());
        candidates.add(manifestation);
        if (manifestation.hasOnline() || manifestation.hasPrint()) {
            retrieveOtherEdition(manifestation, candidates);
        }
        Collection<Manifestation> manifestations = ImmutableSet.copyOf(candidates);
        candidates.clear();
        for (Manifestation m : manifestations) {
            setAllRelationsBetween(m, manifestations);
        }
        Set<Holding> holdings = searchHoldings(manifestations);
        Set<License> licenses = searchLicensesAndIndicators(manifestations);
        TimeLine timeline = new TimeLine(manifestations);
        timeline.setHoldings(holdings);
        timeline.setLicensesAndIndicators(licenses);
        timeline.makeServices();
        Set<String> visited = newHashSet();
        for (Manifestation m : timeline) {
            indexManifestation(m, visited);
        }
    }

    private void processWork(Manifestation manifestation) throws IOException {
        // Work set algorithm
        // Candidates are unstructured, no timeline organization,
        // no relationship analysis, not ordered by ID
        this.candidates = newSetFromMap(new ConcurrentHashMap<Manifestation, Boolean>());
        candidates.add(manifestation);
        // there are certain serial genres which are not fitting into our model of "few" timelines
        if (!manifestation.isDatabase() && !manifestation.isNewspaper() && !manifestation.isPacket()) {
            // retrieve all docs that are connected by relationships into the candidate set.
            retrieveCandidates(manifestation, candidates);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("{} skipped candidate retrieval because of genre", manifestation);
            }
        }

        if (candidates.size() > 100) {
            logger.warn("large list of candidates for {} ({})", manifestation, candidates.size());
        }

        // microforms et al?
        //candidates.addAll(extractOtherEditions(candidates));

        // Ensure all relationships in the cluster.
        for (Manifestation m : candidates) {
            setAllRelationsBetween(m, candidates);
        }

        // Create timelines, and process each timeline for services (holdings, licenses, indicators)
        TimelineCluster cluster = new TimelineCluster(candidates);

        // Now, this is expensive. Find holdings, licenses, indicators of candidates
        Set<Holding> holdings = searchHoldings(candidates);
        if (holdings.size() > 1000) {
            logger.warn("suspicious large list of holdings for {} ({})", manifestation, holdings.size());
        }
        Set<License> licenses = searchLicensesAndIndicators(candidates);
        if (licenses.size() > 1000) {
            logger.warn("suspicious large list of licenses for {} ({})", manifestation, licenses.size());
        }

        Set<TimeLine> timeLines = cluster.timeLines();

        if (logger.isDebugEnabled()) {
            logger.debug("cluster: candidates={} timelines={}", candidates, timeLines);
            for (TimeLine timeLine : timeLines) {
                logger.debug("timeline {} {}-{}", timeLine.getFingerprint(), timeLine.getFirstDate(), timeLine.getLastDate());
            }
        }

        Set<String> visited = newHashSet();
        for (TimeLine timeLine : timeLines) {
            String timelineId = timeLine.getFingerprint();
            // precaution against processing a timeline again. This should not happen.
            if (service.timelines().contains(timelineId)) {
                logger.warn("timeline {} already exists (while processing manifestation {})",
                        timelineId, manifestation);
                continue;
            }
            service.timelines().add(timelineId);
            //timeLine.setHoldings(searchHoldings(timeLine));
            timeLine.setHoldings(holdings);
            //timeLine.validateByDateRange(searchLicensesAndIndicators(timeLine));
            timeLine.setLicensesAndIndicators(licenses);
            // stratify and re-adjust the services in the timeline
            timeLine.makeServices();
            // index the time line object (it's a "serial line of publication events")
            XContentBuilder builder = jsonBuilder();
            timeLine.build(builder, service.settings().get("tag"), "Serial");
            service.ingest().index(worksIndex, worksIndexType, timelineId, builder.string());
            // index every manifestation in the timeline
            for (Manifestation m : timeLine) {
                indexManifestation(m, visited);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("indexed timeline {} of size {}: {} total manifestations",
                        timelineId, timeLine.size(), visited.size());
            }
        }
        // What about manifestations not in timeline? Index them as loners.
        // These loners indicate sources of errors in the relation configuration.
        for (Manifestation m : cluster.notInTimeLines()) {
            indexManifestation(m, visited);
        }
    }

    private void indexManifestation(Manifestation m, Set<String> visited) throws IOException {
        String id = m.externalID();
        // protection against recursion (should not happen)
        if (visited.contains(id)) {
            return;
        }
        visited.add(id);
        // make sure at other threads that we do never index a manifestation twice
        if (service.indexed().contains(id)) {
            return;
        }
        service.indexed().add(id);

        // index this manifestation
        String tag = service.settings().get("tag");
        XContentBuilder builder = jsonBuilder();
        String docid = m.build(builder, tag, null);
        if (docid != null) {
            service.ingest().index(manifestationsIndex, manifestationsIndexType, docid, builder.string());
        }
        // volumes by date and the services for them
        Integer volumeHoldingsCount = 0;
        if (!m.getVolumesByDate().isEmpty()) {
            SetMultimap<Integer, Holding> volumesByDate;
            synchronized (m.getVolumesByDate()) {
                volumesByDate = ImmutableSetMultimap.copyOf(m.getVolumesByDate());
            }
            for (Integer date : volumesByDate.keySet()) {
                String identifier = (tag != null ? tag + "." : "") + m.externalID() + (date != -1 ? "." + date : "");
                Set<Holding> holdings = volumesByDate.get(date);
                if (holdings != null && !holdings.isEmpty()) {
                    builder = jsonBuilder();
                    docid = m.buildHoldingsByDate(builder, tag, m.externalID(), date, holdings);
                    if (docid != null) {
                        service.ingest().index(volumesIndex, volumesIndexType, identifier, builder.string());
                        serviceMetric.mark(1);
                        if (logger.isDebugEnabled()) {
                            logger.debug("indexed volume identifier {}, date {}", docid, date);
                        }
                        volumeHoldingsCount++;
                    }
                }
            }
        }
        // holdings (list of institutions)
        if (!m.getVolumesByHolder().isEmpty()) {
            SetMultimap<String, Holding> holdings;
            synchronized (m.getVolumesByHolder()) {
                holdings = ImmutableSetMultimap.copyOf(m.getVolumesByHolder());
            }
            builder = jsonBuilder();
            builder.startObject().startArray("holdings");
            for (String holder : holdings.keySet()) {
                docid = m.buildHoldingsByISIL(builder, tag, m.externalID(), holder, holdings.get(holder));
            }
            builder.endArray().endObject();
            if (docid != null) {
                service.ingest().index(holdingsIndex, holdingsIndexType, docid, builder.string());
            }
            volumeHoldingsCount++;
            serviceMetric.mark(holdings.size());
            if (logger.isDebugEnabled()) {
                logger.debug("indexed {} holdings for {}", holdings.size(), docid);
            }
            if (volumeHoldingsCount == 0) {
                logger.warn("no volumes/holdings indexed for {}", m.externalID());
            }
        }
        // index related manifestations
        if (!m.getRelatedManifestations().isEmpty()) {
            SetMultimap<String, Manifestation> rels;
            synchronized (m.getRelatedManifestations()) {
                rels = ImmutableSetMultimap.copyOf(m.getRelatedManifestations());
            }
            for (String rel : rels.keys()) {
                for (Manifestation mm : rels.get(rel)) {
                    indexManifestation(mm, visited);
                }
            }
        }
    }

    private void retrieveOtherEdition(Manifestation manifestation, Collection<Manifestation> cluster) throws IOException {
        // only print/online edition. That's it.
        if (!manifestation.hasPrint() && !manifestation.hasOnline()) {
            return;
        }
        QueryBuilder queryBuilder = null;
        if (manifestation.getOnlineID() != null && manifestation.isPrint()) {
            queryBuilder = termQuery("IdentifierDNB.identifierDNB", manifestation.getOnlineID());
        }
        if (manifestation.getPrintID() != null && manifestation.isOnline()) {
            queryBuilder = termQuery("IdentifierDNB.identifierDNB", manifestation.getPrintID());
        }
        if (queryBuilder == null) {
            return;
        }
        SearchRequestBuilder searchRequest = service.client().prepareSearch()
                .setQuery(queryBuilder)
                .setSize(service.size()) // size is per shard!
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(service.millis()));
        searchRequest.setIndices(sourceTitleIndex);
        if (sourceTitleType != null) {
            searchRequest.setTypes(sourceTitleType);
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(TimeValue.timeValueMillis(service.millis()))
                .execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        if (hits.getHits().length == 0) {
            return;
        }
        // we expect single hit, but we scroll although.
        do {
            for (int i = 0; i < hits.getHits().length; i++) {
                SearchHit hit = hits.getAt(i);
                Manifestation m = new Manifestation(mapper.readValue(hit.source(), Map.class));
                cluster.add(m);
            }
            searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(service.millis()))
                    .execute().actionGet();
            hits = searchResponse.getHits();
        } while (hits.getHits().length > 0);
    }

    private void retrieveCandidates(Manifestation manifestation, Collection<Manifestation> cluster) throws IOException {
        SetMultimap<String, String> relations = ImmutableSetMultimap.copyOf(manifestation.getRelations());
        Set<String> neighbors = newHashSet(relations.values());
        QueryBuilder queryBuilder = neighbors.isEmpty() ?
                termQuery("_all", manifestation.id()) :
                boolQuery().should(termQuery("_all", manifestation.id()))
                        .should(termsQuery("IdentifierDNB.identifierDNB", neighbors.toArray()));
        SearchRequestBuilder searchRequest = service.client().prepareSearch()
                .setQuery(queryBuilder)
                .setSize(service.size()) // size is per shard!
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(service.millis()));
        searchRequest.setIndices(sourceTitleIndex);
        if (sourceTitleType != null) {
            searchRequest.setTypes(sourceTitleType);
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(TimeValue.timeValueMillis(service.millis()))
                .execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        if (hits.getHits().length == 0) {
            return;
        }
        ClusterBuildContinuation cont = new ClusterBuildContinuation(manifestation, searchResponse, cluster, 0);
        buildQueue.offer(cont);
        while (!buildQueue.isEmpty()) {
            cont = buildQueue.poll();
            cluster.addAll(cont.cluster);
            continueClusterBuild(cluster, cont);
        }
    }

    private void continueClusterBuild(Collection<Manifestation> cluster, ClusterBuildContinuation c) throws IOException {
        SearchResponse searchResponse = c.searchResponse;
        SearchHits hits;
        do {
            hits = searchResponse.getHits();
            for (int i = c.pos; i < hits.getHits().length; i++) {
                SearchHit hit = hits.getAt(i);
                Manifestation m = new Manifestation(mapper.readValue(hit.source(), Map.class));
                if (m.id().equals(c.manifestation.id())) {
                    continue;
                }
                if (m.isNewspaper()) {
                    continue;
                }
                if (m.isDatabase()) {
                    continue;
                }
                if (m.isWebsite()) {
                    continue;
                }
                if (cluster.contains(m)) {
                    continue;
                }
                cluster.add(m);
                service.processed().add(m.externalID());
                boolean collided = detectCollisionAndTransfer(m, c, i);
                if (collided) {
                    // break out
                    return;
                }
                boolean temporalRelation = false;
                boolean carrierRelation = false;
                Collection<String> rels = findTheRelationsBetween(c.manifestation, m.id());
                for (String relation : rels) {
                    if (relation == null) {
                        // shoud not happen
                        logger.debug("unknown relation {}", relation);
                        continue;
                    }
                    c.manifestation.addRelatedManifestation(relation, m);
                    String inverse = inverseRelations.get(relation);
                    if (inverse == null) {
                        logger.debug("no inverse relation for {}", relation);
                        //m.addRelatedManifestation("hasRelationTo", c.manifestation);
                    } else {
                        m.addRelatedManifestation(inverse, c.manifestation);
                    }
                    temporalRelation = temporalRelation
                            || "precededBy".equals(relation)
                            || "succeededBy".equals(relation);
                    carrierRelation = carrierRelation
                            || Manifestation.carrierEditions().contains(relation);
                }
                // other direction (missing entries in catalog are possible)
                for (String relation : rels) {
                    if (relation == null) {
                        logger.debug("unknown relation {}", relation);
                        continue;
                    }
                    m.addRelatedManifestation(relation, c.manifestation);
                    String inverse = inverseRelations.get(relation);
                    if (inverse == null) {
                        logger.debug("no inverse relation for {}", relation);
                        //c.manifestation.addRelatedManifestation("isRelatedTo", m);
                    } else {
                        c.manifestation.addRelatedManifestation(inverse, m);
                    }
                    temporalRelation = temporalRelation
                            || "precededBy".equals(relation)
                            || "succeededBy".equals(relation);
                    carrierRelation = carrierRelation
                            || Manifestation.carrierEditions().contains(relation);
                }
                // Look for more candidates for this manifestation iff temporal or carrier relation.
                // Also expand if there are any other print/online editions.
                if (temporalRelation || carrierRelation || m.hasCarrierRelations()) {
                    retrieveCandidates(m, cluster);
                }
            }
            searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(service.millis()))
                    .execute().actionGet();
            hits = searchResponse.getHits();
        } while (hits.getHits().length > 0);
    }

    private boolean detectCollisionAndTransfer(Manifestation manifestation,
                                               ClusterBuildContinuation c, int pos) {
        for (Pipeline pipeline : service.getPipelines()) {
            if (this == pipeline) {
                continue;
            }
            WithHoldingsAndLicensesInTimelinePipeline p = (WithHoldingsAndLicensesInTimelinePipeline)pipeline;
            if (p.getCluster() != null && p.getCluster().contains(manifestation)) {
                logger.warn("collision detected for {} with {} ", manifestation, pipeline);
                c.pos = pos;
                p.getBuildQueue().offer(c);
                return true;
            }
        }
        return false;
    }

    private Set<Holding> searchHoldings(Collection<Manifestation> manifestations) throws IOException {
        // create a map of all manifestations that can have assigned a holding.
        Map<String, Manifestation> map = newHashMap();
        for (Manifestation m : manifestations) {
            map.put(m.id(), m);
            // add print if not already there...
            if (m.getPrintID() != null && !map.containsKey(m.getPrintID())) {
                map.put(m.getPrintID(), m);
            }
        }
        Set<Holding> holdings = newHashSet();
        searchHoldings(holdings, map);
        return holdings;
    }

    private void searchHoldings(Set<Holding> holdings, Map<String, Manifestation> manifestations) throws IOException {
        if (sourceHoldingsIndex == null) {
            return;
        }
        if (manifestations == null || manifestations.isEmpty()) {
            return;
        }
        // split ids into portions of 1024 (default max clauses for Lucene)
        Object[] array = manifestations.keySet().toArray();
        for (int begin = 0; begin < array.length; begin += 1024) {
            int end = begin + 1024 > array.length ? array.length : begin + 1024;
            Object[] subarray = Arrays.copyOfRange(array, begin, end);
            QueryBuilder queryBuilder = termsQuery("identifierParent", subarray);
            // getSize is per shard
            SearchRequestBuilder searchRequest = service.client().prepareSearch()
                    .setQuery(queryBuilder)
                    .setSize(service.size())
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(service.millis()));
            searchRequest.setIndices(sourceHoldingsIndex);
            if (sourceHoldingsType != null) {
                searchRequest.setTypes(sourceHoldingsType);
            }
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            while (searchResponse.getScrollId() != null) {
                searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(service.millis()))
                        .execute().actionGet();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    Holding holding = new Holding(mapper.readValue(hit.source(), Map.class));
                    if (holding.isDeleted()) {
                        continue;
                    }
                    String isil = holding.getISIL();
                    if (isil == null) {
                        continue;
                    }
                    if (service.blackListedISIL().lookup().contains(isil)) {
                        continue;
                    }
                    holding.setRegion(service.bibdatLookup().lookupRegion().get(isil));
                    holding.setOrganization(service.bibdatLookup().lookupOrganization().get(isil));
                    for (String parent : holding.parents()) {
                        Manifestation parentManifestation = manifestations.get(parent);
                        parentManifestation.addRelatedHolding(isil, holding);
                        holding.addManifestation(parentManifestation);
                    }
                    holdings.add(holding);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            for (Manifestation m : manifestations.values()) {
                logger.info("found holdings of {} = {} ", m.externalID(), m.getVolumesByHolder().size());
            }
        }
    }

    private Set<License> searchLicensesAndIndicators(Collection<Manifestation> manifestations) throws IOException {
        // create a map of all manifestations that can have assigned a license.
        Map<String, Manifestation> map = newHashMap();
        boolean isOnline = false;
        for (Manifestation m : manifestations) {
            map.put(m.externalID(), m);
            // we really just rely on the carrier type. There may be licenses or indicators.
            isOnline = isOnline || "online resource".equals(m.carrierType());
            // copy print to the online manifestation in case it is not there
            if (m.getOnlineExternalID() != null && !map.containsKey(m.getOnlineExternalID())) {
                map.put(m.getOnlineExternalID(), m);
            }
        }
        Set<License> licenses = newHashSet();
        if (isOnline) {
            if (logger.isDebugEnabled()) {
                logger.debug("searching for licenses and indicators for {}", map);
            }
            searchLicenses(licenses, map);
            searchIndicators(licenses, map);
        }
        return licenses;
    }

    private void searchLicenses(Set<License> licenses, Map<String, Manifestation> manifestations) throws IOException {
        if (sourceLicenseIndex == null) {
            return;
        }
        if (manifestations == null || manifestations.isEmpty()) {
            return;
        }
        // split ids into portions of 1024 (default max clauses for Lucene)
        Object[] array = manifestations.keySet().toArray();
        for (int begin = 0; begin < array.length; begin += 1024) {
            int end = begin + 1024 > array.length ? array.length : begin + 1024;
            Object[] subarray = Arrays.copyOfRange(array, begin, end);
            QueryBuilder queryBuilder = termsQuery("ezb:zdbid", subarray);
            // getSize is per shard
            SearchRequestBuilder searchRequest = service.client().prepareSearch()
                    .setQuery(queryBuilder)
                    .setSize(service.size())
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(service.millis()));
            searchRequest.setIndices(sourceLicenseIndex);
            if (sourceLicenseType != null) {
                searchRequest.setTypes(sourceLicenseType);
            }
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            while (true) {
                searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(service.millis()))
                        .execute().actionGet();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    License license = new License(mapper.readValue(hit.source(), Map.class));
                    if (license.isDeleted()) {
                        continue;
                    }
                    String isil = license.getISIL();
                    if (isil == null) {
                        continue;
                    }
                    if (service.blackListedISIL().lookup().contains(isil)) {
                        continue;
                    }
                    license.setRegion(service.bibdatLookup().lookupRegion().get(isil));
                    license.setOrganization(service.bibdatLookup().lookupOrganization().get(isil));
                    for (String parent : license.parents()) {
                        Manifestation m = manifestations.get(parent);
                        m.addRelatedHolding(isil, license);
                        if (logger.isDebugEnabled()) {
                            logger.debug("license {} attached to manifestation {}", license.identifier(), m.externalID());
                        }
                        license.addManifestation(m);
                        // trick: add also to print manifestation if possible
                        if (m.hasPrint()) {
                            Manifestation p = manifestations.get(m.getPrintExternalID());
                            if (p != null) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("license {} attached to another manifestation {}", license.identifier(), p.externalID());
                                }
                                license.addManifestation(p);
                            }
                        }
                    }
                    licenses.add(license);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("found {} licenses for manifestations {}",
                    licenses.size(), manifestations);
        }
    }

    private void searchIndicators(Set<License> indicators, Map<String, Manifestation> manifestations) throws IOException {
        if (sourceIndicatorIndex == null) {
            return;
        }
        if (manifestations == null || manifestations.isEmpty()) {
            return;
        }
        // split ids into portions of 1024 (default max clauses for Lucene)
        Object[] array = manifestations.keySet().toArray();
        for (int begin = 0; begin < array.length; begin += 1024) {
            int end = begin + 1024 > array.length ? array.length : begin + 1024;
            Object[] subarray = Arrays.copyOfRange(array, begin, end);
            QueryBuilder queryBuilder = termsQuery("xbib:identifier", subarray);
            // getSize is per shard
            SearchRequestBuilder searchRequest = service.client().prepareSearch()
                    .setQuery(queryBuilder)
                    .setSize(service.size())
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(service.millis()));
            searchRequest.setIndices(sourceIndicatorIndex);
            if (sourceLicenseType != null) {
                searchRequest.setTypes(sourceIndicatorType);
            }
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            while (true) {
                searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(service.millis()))
                        .execute().actionGet();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    Indicator indicator = new Indicator(mapper.readValue(hit.source(), Map.class));
                    String isil = indicator.getISIL();
                    if (isil == null) {
                        continue;
                    }
                    indicator.setRegion(service.bibdatLookup().lookupRegion().get(isil));
                    indicator.setOrganization(service.bibdatLookup().lookupOrganization().get(isil));
                    if (service.blackListedISIL().lookup().contains(isil)) {
                        continue;
                    }
                    for (String parent : indicator.parents()) {
                        Manifestation m = manifestations.get(parent);
                        m.addRelatedHolding(isil, indicator);
                        indicator.addManifestation(m);
                        if (logger.isDebugEnabled()) {
                            logger.debug("indicator {} parents {} attached to manifestation {}",
                                    indicator.identifier(), indicator.parents(), m.externalID());
                        }
                        // trick: add also to print manifestation if possible
                        if (m.hasPrint()) {
                            Manifestation p = manifestations.get(m.getPrintExternalID());
                            if (p != null) {
                                indicator.addManifestation(p);
                                if (logger.isDebugEnabled()) {
                                    logger.debug("indicator {} parents {} attached to manifestation {}",
                                            indicator.identifier(), indicator.parents(), p.externalID());
                                }
                            }
                        }
                    }
                    indicators.add(indicator);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("found {} indicators for manifestations {}",
                    indicators.size(), manifestations);
        }
    }

    private Set<String> findTheRelationsBetween(Manifestation manifestation, String id) {
        Set<String> relationNames = new HashSet<String>();
        for (String entry : Manifestation.relationEntries()) {
            Object o = manifestation.map().get(entry);
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Arrays.asList(o);
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
                                oo = Arrays.asList(oo);
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

    private void setAllRelationsBetween(Manifestation manifestation, Collection<Manifestation> cluster) {
        for (String relation : Manifestation.relationEntries()) {
            Object o = manifestation.map().get(relation);
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Arrays.asList(o);
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
                                logger.trace("entry {} has no relation name in {}", entry, manifestation.externalID());
                            }
                            continue;
                        }
                    }
                    internalObj = entry.get("identifierDNB");
                    // take only first entry from list...
                    String value = internalObj == null ? null : internalObj instanceof List ?
                            ((List) internalObj).get(0).toString() : internalObj.toString();
                    for (Manifestation m : cluster) {
                        // self?
                        if (m.id().equals(manifestation.id())) {
                            continue;
                        }
                        if (m.id().equals(value)) {
                            manifestation.addRelatedManifestation(key, m);
                            // special trick: move over links from online to print
                            if ("hasPrintEdition".equals(key)) {
                                m.setLinks(manifestation.getLinks());
                            }
                            String inverse = inverseRelations.get(key);
                            if (inverse != null) {
                                m.addRelatedManifestation(inverse, manifestation);
                            } else {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("no inverse relation for {} in {}, using 'isRelatedTo'", key, manifestation.externalID());
                                }
                                m.addRelatedManifestation("isRelatedTo", manifestation);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Pipeline<Boolean, ManifestationPipelineElement> setQueue(BlockingQueue<ManifestationPipelineElement> queue) {
        this.queue = queue;
        return this;
    }

    private class ClusterBuildContinuation {
        final Manifestation manifestation;
        final SearchResponse searchResponse;
        final Collection<Manifestation> cluster;
        int pos;

        ClusterBuildContinuation(Manifestation manifestation,
                                 SearchResponse searchResponse,
                                 Collection<Manifestation> cluster,
                                 int pos) {
            this.manifestation = manifestation;
            this.searchResponse = searchResponse;
            this.cluster = cluster;
            this.pos = pos;
        }
    }

    private final Map<String, String> inverseRelations = new HashMap<String, String>() {{

        put("hasPart", "isPartOf");
        put("hasSupplement", "isSupplementOf");
        put("isPartOf", "hasPart");
        put("isSupplementOf", "hasSupplement");

        // temporal axis
        put("precededBy", "succeededBy");
        put("succeededBy", "precededBy");

        // "FRBR expression" relations
        put("hasLanguageEdition", "isLanguageEditionOf");
        put("hasTranslation", "isTranslationOf");
        put("isLanguageEditionOf", "hasLanguageEdition");
        put("isTranslationOf", "hasTranslation");

        // "FRBR manifestation" relations
        put("hasOriginalEdition", "isOriginalEditionOf");
        put("hasPrintEdition", "isPrintEditionOf");
        put("hasOnlineEdition", "isOnlineEditionOf");
        put("hasBrailleEdition", "isBrailleEditionOf");
        put("hasDVDEdition", "isDVDEditionOf");
        put("hasCDEdition", "isCDEditionOf");
        put("hasDiskEdition", "isDiskEditionOf");
        put("hasMicroformEdition", "isMicroformEditionOf");
        put("hasDigitizedEdition", "isDigitizedEditionOf");

        // edition relations
        put("hasSpatialEdition", "isSpatialEditionOf");
        put("hasTemporalEdition", "isTemporalEditionOf");
        put("hasPartialEdition", "isPartialEditionOf");
        put("hasTransientEdition", "isTransientEditionOf");
        put("hasLocalEdition", "isLocalEditionOf");
        put("hasAdditionalEdition", "isAdditionalEditionOf");
        put("hasAlternativeEdition", "isAdditionalEditionOf");
        put("hasDerivedEdition", "isDerivedEditionOf");
        put("hasHardcoverEdition", "isHardcoverEditionOf");
        put("hasManuscriptEdition", "isManuscriptEditionOf");
        put("hasBoxedEdition", "isBoxedEditionOf");
        put("hasReproduction", "isReproductionOf");
        put("hasSummary", "isSummaryOf");

        put("isOriginalEditionOf", "hasOriginalEdition");
        put("isPrintEditionOf", "hasPrintEdition");
        put("isOnlineEditionOf", "hasOnlineEdition");
        put("isBrailleEditionOf", "hasBrailleEdition");
        put("isDVDEditionOf", "hasDVDEdition");
        put("isCDEditionOf", "hasCDEdition");
        put("isDiskEditionOf", "hasDiskEdition");
        put("isMicroformEditionOf", "hasMicroformEdition");
        put("isDigitizedEditionOf", "hasMicroformEdition");
        put("isSpatialEditionOf", "hasSpatialEdition");
        put("isTemporalEditionOf", "hasTemporalEdition");
        put("isPartialEditionOf", "hasPartialEdition");
        put("isTransientEditionOf", "hasTransientEdition");
        put("isLocalEditionOf", "hasLocalEdition");
        put("isAdditionalEditionOf", "hasAdditionalEdition");
        put("isDerivedEditionOf", "hasDerivedEdition");
        put("isHardcoverEditionOf", "hasHardcoverEdition");
        put("isManuscriptEditionOf", "hasManuscriptEdition");
        put("isBoxedEditionOf", "hasBoxedEdition");
        put("isReproductionOf", "hasReproduction");
        put("isSummaryOf", "hasSummary");
    }};
}
