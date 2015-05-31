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

import com.google.common.collect.ImmutableList;
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
import org.xbib.tools.merge.zdb.entities.Cluster;
import org.xbib.tools.merge.zdb.entities.Holding;
import org.xbib.tools.merge.zdb.entities.Indicator;
import org.xbib.tools.merge.zdb.entities.License;
import org.xbib.tools.merge.zdb.entities.Manifestation;
import org.xbib.tools.merge.zdb.entities.Volume;
import org.xbib.tools.merge.zdb.entities.VolumeHolding;
import org.xbib.tools.util.SearchHitPipelineElement;
import org.xbib.util.ExceptionFormatter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newSetFromMap;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

public class WithHoldingsAndLicensesPipeline implements Pipeline<Boolean, Manifestation> {

    enum State {
        COLLECTING, PROCESSING, INDEXING
    }

    private final int number;

    private State state;

    private final WithHoldingsAndLicenses service;

    private final Logger logger;

    private final Queue<ClusterBuildContinuation> buildQueue;

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
    private String sourceVolumeIndex;
    private String sourceVolumeHoldingsIndex;

    private final String manifestationsIndex;
    private final String manifestationsIndexType;
    private final String holdingsIndex;
    private final String holdingsIndexType;
    private final String dateHoldingsIndex;
    private final String dateHoldingsIndexType;


    public WithHoldingsAndLicensesPipeline(WithHoldingsAndLicenses service, int number) {
        this.number = number;
        this.service = service;
        this.listeners = newHashMap();
        this.buildQueue = new ConcurrentLinkedQueue<ClusterBuildContinuation>();
        this.logger = LogManager.getLogger(toString());

        Settings settings = service.settings();
        this.sourceTitleIndex = settings.get("bib-index");
        this.sourceTitleType = settings.get("bib-type");
        this.sourceHoldingsIndex = settings.get("hol-index");
        this.sourceHoldingsType = settings.get("hol-type");
        this.sourceLicenseIndex = settings.get("xml-license-index");
        this.sourceLicenseType = settings.get("xml-license-type");
        this.sourceIndicatorIndex = settings.get("web-license-index");
        this.sourceIndicatorType = settings.get("web-license-type");
        this.sourceVolumeIndex = settings.get("volume-index");
        this.sourceVolumeHoldingsIndex = settings.get("volume-hol-index");

        String index = settings.get("index");
        if (index == null) {
            throw new IllegalArgumentException("no index given");
        }
        this.manifestationsIndex = settings.get("manifestations-index", index);
        this.manifestationsIndexType = settings.get("manifestations-type", "Manifestation");
        this.holdingsIndex = settings.get("holdings-index", index);
        this.holdingsIndexType = settings.get("holdings-type", "Holdings");
        this.dateHoldingsIndex = settings.get("date-holdings-index", index);
        this.dateHoldingsIndexType = settings.get("date-holdings-type", "DateHoldings");
    }

    public MeterMetric getMetric() {
        return null;
    }

    public Queue<ClusterBuildContinuation> getBuildQueue() {
        return buildQueue;
    }

    public Collection<Manifestation> getCluster() {
        return candidates;
    }

    @Override
    public boolean hasNext() {
        SearchHitPipelineElement element;
        try {
            element = service.queue().poll(60, TimeUnit.SECONDS);
            if (element != null && element.get() != null) {
                manifestation = new Manifestation(element.get().getSource());
                manifestation.setForced(element.getForced());
            } else {
                manifestation = null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            manifestation = null;
            logger.error("pipeline processing interrupted, queue inactive", e);
        }
        return manifestation != null;
    }

    @Override
    public Manifestation next() {
        return manifestation;
    }

    @Override
    public Boolean call() throws Exception {
        logger.info("pipeline starting");
        try {
            while (hasNext()) {
                manifestation = next();
                if (check(manifestation)) {
                    processWork(manifestation);
                }
                for (PipelineRequestListener listener : listeners.values()) {
                    listener.newRequest(this, manifestation);
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            logger.error(ExceptionFormatter.format(e));
            logger.error("exiting, exception while processing {}", manifestation);
        } finally {
            service.countDown();
        }
        logger.info("pipeline terminating");
        return true;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        if (!service.queue().isEmpty()) {
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

    public Pipeline<Boolean, Manifestation> add(String name, PipelineRequestListener listener) {
        this.listeners.put(name, listener);
        return this;
    }

    private boolean check(Manifestation manifestation) {
        if (manifestation.getForced()) {
            return true;
        }
        String id = manifestation.externalID();
        // Skip supplements and manifestations with "succeededBy" relation (= in timeline) here.
        // All of them are referenced by another manifestation.
        // We pull them in while cluster construction.
        // Note: we also check online/CD manifestations here. They may continue
        // print manifestations but without correct "succeededBy/precededBy" pair
        // maybe "OtherEditionEntry" see ZDB 20679373.
        if (manifestation.isInTimeline() || manifestation.isSupplement()) {
            service.skipped().add(id);
            return false;
        }
        // if we had this seen, skip
        return !service.processed().contains(id);
    }

    private void processWork(Manifestation manifestation) throws IOException {
        boolean dirty = false;
        Cluster cluster;
        do {
            // Work set algorithm
            // Candidates are unstructured, no timeline organization,
            // no relationship analysis, not ordered by ID
            this.candidates = newSetFromMap(new ConcurrentHashMap<Manifestation, Boolean>());
            candidates.add(manifestation);
            state = State.COLLECTING;
            // there are certain serial genres which are not fitting into our model of timelines
            if (dirty || (!manifestation.isDatabase() && !manifestation.isPacket())) {
                // retrieve all docs that are connected by relationships into the candidate set
                retrieveCandidates(manifestation, candidates);
            } else {
                logger.debug("{} skipped candidate retrieval isDatabase={} isNewspaper={} isPacket={}",
                        manifestation,
                        manifestation.isDatabase(),
                        manifestation.isPacket());
            }
            state = State.PROCESSING;

            if (candidates.size() > 100) {
                logger.warn("large list of candidates for {} ({})", manifestation, candidates.size());
            }

            // microforms et al?
            //candidates.addAll(extractOtherEditions(candidates));

            // Ensure all relationships in the cluster.
            for (Manifestation m : candidates) {
                setAllRelationsBetween(m, candidates);
            }

            cluster = new Cluster(candidates);

            // Now, this is expensive. Find holdings, licenses, indicators of candidates
            Set<Holding> holdings = searchHoldings(candidates);
            if (holdings.size() > 5000) {
                logger.warn("large list of holdings for {} ({})", manifestation, holdings.size());
            }
            cluster.setHoldings(holdings);
            Set<License> licenses = searchLicensesAndIndicators(candidates);
            if (licenses.size() > 5000) {
                logger.warn("large list of licenses for {} ({})", manifestation, licenses.size());
            }
            // search monographic volumes
            searchVolumes(cluster);
            for (Manifestation m : cluster) {
                service.count(m.getVolumes().size());
            }
            cluster.validateByDateRange(licenses);
            cluster.attachServicesToManifestations();
            dirty = !buildQueue.isEmpty();
            if (dirty) {
                logger.warn("{}: got build queue {}, retrying", manifestation, buildQueue);
            }
        } while (dirty);
        state = State.INDEXING;
        Set<String> visited = newHashSet();
        for (Manifestation m : cluster) {
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
        String tag = service.settings().get("tag");
        // first, index related volumes (conference/proceedings/abstracts/...)
        List<String> vids = newArrayList();
        if (!m.getVolumes().isEmpty()) {
            final ImmutableList<Volume> volumes;
            synchronized (m.getVolumes()) {
                volumes = ImmutableList.copyOf(m.getVolumes());
            }
            for (Volume volume : volumes) {
                XContentBuilder builder = jsonBuilder();
                String vid = volume.build(builder, tag, null);
                service.ingest().index(manifestationsIndex, manifestationsIndexType, vid, builder.string());
                vids.add(vid);
                for (VolumeHolding volumeHolding : volume.getHoldings()) {
                    builder = jsonBuilder();
                    vid = volumeHolding.build(builder, tag);
                    // by holding
                    service.ingest().index(holdingsIndex, holdingsIndexType, vid, builder.string());
                    // extra entry by date
                    service.ingest().index(dateHoldingsIndex, dateHoldingsIndexType, vid + "." + volumeHolding.dates().get(0), builder.string());
                }
                int n = 1 + 2 * volume.getHoldings().size();
                service.indexMetric().mark(n);
            }
            int n = m.getVolumes().size();
            service.indexMetric().mark(n);
        }
        m.addVolumeIDs(vids);

        // index this manifestation
        // holdings by date and the services for them
        if (!m.getVolumesByDate().isEmpty()) {
            SetMultimap<Integer, Holding> volumesByDate;
            synchronized (m.getVolumesByDate()) {
                volumesByDate = ImmutableSetMultimap.copyOf(m.getVolumesByDate());
            }
            for (Integer date : volumesByDate.keySet()) {
                String identifier = (tag != null ? tag + "." : "") + m.externalID() + (date != -1 ? "." + date : "");
                Set<Holding> holdings = volumesByDate.get(date);
                if (holdings != null && !holdings.isEmpty()) {
                    XContentBuilder builder = jsonBuilder();
                    String docid = m.buildHoldingsByDate(builder, tag, m.externalID(), date, holdings);
                    service.ingest().index(dateHoldingsIndex, dateHoldingsIndexType, identifier, builder.string());
                    service.indexMetric().mark(1);
                    logger.debug("indexed volume {} date {}", docid, date);
                }
            }
        }
        XContentBuilder builder = jsonBuilder();
        String docid = m.build(builder, tag, null);
        service.ingest().index(manifestationsIndex, manifestationsIndexType, docid, builder.string());
        service.indexMetric().mark(1);

        // holdings (list of institutions)
        if (!m.getVolumesByHolder().isEmpty()) {
            final SetMultimap<String, Holding> holdings;
            synchronized (m.getVolumesByHolder()) {
                holdings = ImmutableSetMultimap.copyOf(m.getVolumesByHolder());
            }
            builder = jsonBuilder();
            builder.startObject().startArray("holdings");
            for (String holder : holdings.keySet()) {
                docid = m.buildHoldingsByISIL(builder, tag, m.externalID(), holder, holdings.get(holder));
            }
            builder.endArray().endObject();
            service.ingest().index(holdingsIndex, holdingsIndexType, docid, builder.string());
            service.indexMetric().mark(1);
            logger.debug("indexed {} holdings for {}", holdings.size(), docid);
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

    /*private void retrieveOtherEdition(Manifestation manifestation, Collection<Manifestation> cluster) throws IOException {
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
        service.queryMetric().mark();
        searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(TimeValue.timeValueMillis(service.millis()))
                .execute().actionGet();
        service.queryMetric().mark();
        SearchHits hits = searchResponse.getHits();
        if (hits.getHits().length == 0) {
            return;
        }
        // we expect single hit, but we scroll although
        do {
            for (int i = 0; i < hits.getHits().length; i++) {
                SearchHit hit = hits.getAt(i);
                //Manifestation m = new Manifestation(mapper.readValue(hit.source(), Map.class));
                Manifestation m = new Manifestation(hit.getSource());
                cluster.add(m);
            }
            searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(service.millis()))
                    .execute().actionGet();
            service.queryMetric().mark();
            hits = searchResponse.getHits();
        } while (hits.getHits().length > 0);
    }*/

    private void retrieveCandidates(Manifestation manifestation, Collection<Manifestation> cluster)
            throws IOException {
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
        logger.debug("retrieveCandidates search request = {}", searchRequest.toString());
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(TimeValue.timeValueMillis(service.millis()))
                .execute().actionGet();
        service.queryMetric().mark();
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

    private void continueClusterBuild(Collection<Manifestation> cluster, ClusterBuildContinuation c)
            throws IOException {
        SearchResponse searchResponse = c.searchResponse;
        SearchHits hits;
        do {
            hits = searchResponse.getHits();
            for (int i = c.pos; i < hits.getHits().length; i++) {
                SearchHit hit = hits.getAt(i);
                Manifestation m = new Manifestation(hit.getSource());
                if (m.id().equals(c.manifestation.id())) {
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
                // If not database or newspaper:
                // look for more candidates for this manifestation iff temporal or carrier relation.
                // Also expand if there are any other print/online editions.
                boolean donotexpand = m.isDatabase() || m.isNewspaper();
                if (!donotexpand) {
                    if (temporalRelation || carrierRelation || m.hasCarrierRelations()) {
                        retrieveCandidates(m, cluster);
                    }
                }
            }
            searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(service.millis()))
                    .execute().actionGet();
            service.queryMetric().mark();
            hits = searchResponse.getHits();
        } while (hits.getHits().length > 0);
    }

    private boolean detectCollisionAndTransfer(Manifestation manifestation,
                                               ClusterBuildContinuation c, int pos) {
        for (WithHoldingsAndLicensesPipeline pipeline : service.getPipelines()) {
            if (this == pipeline) {
                continue;
            }
            if (pipeline.getCluster() != null && pipeline.getCluster().contains(manifestation)) {
                logger.warn("collision detected for {} with {} state={} cluster={} other cluster={}",
                        manifestation, pipeline, pipeline.state.name(),
                        getCluster(), pipeline.getCluster());
                c.pos = pos;
                pipeline.getBuildQueue().offer(c);
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
            QueryBuilder queryBuilder = termsQuery("identifierForTheParentRecord", subarray);
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
            logger.debug("searchHoldings search request = {}/{} {} hits={}",
                    sourceHoldingsIndex, sourceHoldingsType,
                    searchRequest.toString(), searchResponse.getHits().getTotalHits());
            while (searchResponse.getScrollId() != null) {
                searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(service.millis()))
                        .execute().actionGet();
                service.queryMetric().mark();
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
            for (Manifestation m : manifestations.values()) {
                logger.debug("found holdings of {} = {} ", m.externalID(), m.getVolumesByHolder().size());
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
            logger.debug("searching for licenses and indicators for {}", map);
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
            logger.debug("searchLicenses search request = {} hits={}",
                    searchRequest.toString(), searchResponse.getHits().getTotalHits());
            while (searchResponse.getScrollId() != null) {
                searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(service.millis()))
                        .execute().actionGet();
                service.queryMetric().mark();
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
                    if (service.blackListedISIL().lookup().contains(isil)) {
                        continue;
                    }
                    license.setRegion(service.bibdatLookup().lookupRegion().get(isil));
                    license.setOrganization(service.bibdatLookup().lookupOrganization().get(isil));
                    for (String parent : license.parents()) {
                        Manifestation m = manifestations.get(parent);
                        m.addRelatedHolding(isil, license);
                        logger.debug("license {} attached to manifestation {} print={} online={}",
                                license.identifier(), m.externalID(), m.getPrintExternalID(), m.getOnlineExternalID());
                        license.addManifestation(m);
                        // trick: add also to print manifestation if possible
                        if (m.hasPrint()) {
                            Manifestation p = manifestations.get(m.getPrintExternalID());
                            if (p != null) {
                                logger.debug("license {} attached to another manifestation {}",
                                            license.identifier(), p.externalID());
                                license.addManifestation(p);
                            }
                        }
                    }
                    licenses.add(license);
                }
            }
        }
        logger.debug("found {} licenses for manifestations {}",
                    licenses.size(), manifestations);
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
            logger.debug("searchIndicators search request = {} hits={}",
                    searchRequest.toString(), searchResponse.getHits().getTotalHits());
            while (searchResponse.getScrollId() != null) {
                searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(service.millis()))
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
                    indicator.setRegion(service.bibdatLookup().lookupRegion().get(isil));
                    indicator.setOrganization(service.bibdatLookup().lookupOrganization().get(isil));
                    if (service.blackListedISIL().lookup().contains(isil)) {
                        continue;
                    }
                    for (String parent : indicator.parents()) {
                        Manifestation m = manifestations.get(parent);
                        m.addRelatedHolding(isil, indicator);
                        indicator.addManifestation(m);
                        logger.debug("indicator {} parent {} attached to manifestation {}",
                                    indicator.identifier(), parent, m.externalID());
                        // trick: add also to print manifestation if possible
                        if (m.hasPrint()) {
                            Manifestation p = manifestations.get(m.getPrintExternalID());
                            if (p != null) {
                                indicator.addManifestation(p);
                                logger.debug("indicator {} parent {} attached to manifestation {}",
                                            indicator.identifier(), parent, p.externalID());
                            }
                        }
                    }
                    indicators.add(indicator);
                }
            }
        }
        logger.debug("found {} indicators for manifestations {}",
                    indicators.size(), manifestations);
    }

    private void searchVolumes(Collection<Manifestation> manifestations)
            throws IOException {
        // create a map of all manifestations that can have assigned a holding.
        Map<String, Manifestation> map = newHashMap();
        for (Manifestation m : manifestations) {
            map.put(m.externalID(), m);
            // add print if not already there...
            if (m.getPrintID() != null && !map.containsKey(m.getPrintID())) {
                map.put(m.getPrintExternalID(), m);
            }
        }
        searchVolumes(map);
    }

    private void searchVolumes(Map<String, Manifestation> manifestations) throws IOException {
        if (manifestations == null || manifestations.isEmpty()) {
            return;
        }
        for (String id : manifestations.keySet()) {
            Manifestation manifestation = manifestations.get(id);
            SearchRequestBuilder searchRequest = service.client().prepareSearch()
                    .setIndices(sourceVolumeIndex)
                    .setSize(service.size())
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(service.millis()))
                    .setQuery(termQuery("IdentifierZDB.identifierZDB", id));
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            service.queryMetric().mark();
            logger.debug("searchVolumes search request = {} hits={}",
                    searchRequest.toString(), searchResponse.getHits().getTotalHits());
            while (searchResponse.getScrollId() != null) {
                searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(service.millis()))
                        .execute().actionGet();
                service.queryMetric().mark();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    Map<String, Object> m = hit.getSource();
                    Volume volume = new Volume(m, manifestation);
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
    private void searchExtraHoldings(Volume volume) {
        Manifestation manifestation = volume.manifestation();
        String key = volume.id();
        SearchRequestBuilder holdingsSearchRequest = service.client().prepareSearch()
                .setIndices(sourceVolumeHoldingsIndex)
                .setSize(service.size())
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(service.millis()))
                .setQuery(termQuery("xbib.uid", key));
        SearchResponse holdingSearchResponse = holdingsSearchRequest.execute().actionGet();
        service.queryMetric().mark();
        logger.debug("searchExtraHoldings search request = {} hits={}",
                holdingsSearchRequest.toString(), holdingSearchResponse.getHits().getTotalHits());
        while (holdingSearchResponse.getScrollId() != null) {
            holdingSearchResponse = service.client().prepareSearchScroll(holdingSearchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(service.millis()))
                    .execute().actionGet();
            service.queryMetric().mark();
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
                        VolumeHolding volumeHolding = new VolumeHolding(item, volume);
                        volumeHolding.addParent(volume.externalID());
                        volumeHolding.setMediaType(manifestation.mediaType());
                        volumeHolding.setCarrierType(manifestation.carrierType());
                        volumeHolding.setDate(volume.firstDate(), volume.lastDate());
                        volumeHolding.setRegion(service.bibdatLookup().lookupRegion().get(volumeHolding.getISIL()));
                        volumeHolding.setOrganization(service.bibdatLookup().lookupOrganization().get(volumeHolding.getISIL()));
                        volumeHolding.setServiceMode(service.statusCodeMapper().lookup(volumeHolding.getStatus()));
                        if ("interlibrary".equals(volumeHolding.getServiceType()) && volumeHolding.getISIL() != null) {
                            volume.addHolding(volumeHolding);
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
    private void searchSeriesVolumeHoldings(Volume parent)
            throws IOException {
        Manifestation manifestation = parent.manifestation();
        // search children volumes of the series (conference, processing, abstract, ...)
        SearchRequestBuilder searchRequest = service.client().prepareSearch()
                .setIndices(sourceVolumeIndex)
                .setSize(service.size())
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(service.millis()))
                .setQuery(boolQuery().should(termQuery("SeriesAddedEntryUniformTitle.designation", parent.id()))
                        .should(termQuery("RecordIdentifierSuper.recordIdentifierSuper", parent.id())));
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        service.queryMetric().mark();
        //logger.debug("searchSeriesVolumeHoldings search request={} hits={}",
        //        searchRequest.toString(), searchResponse.getHits().getTotalHits());
        while (searchResponse.getScrollId() != null) {
            searchResponse = service.client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(service.millis()))
                    .execute().actionGet();
            service.queryMetric().mark();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                Volume volume = new Volume(hit.getSource(), manifestation);
                volume.addParent(manifestation.externalID());
                // for each conference/congress, search holdings
                SearchRequestBuilder holdingsSearchRequest = service.client().prepareSearch()
                        .setIndices(sourceVolumeHoldingsIndex)
                        .setSize(service.size())
                        .setSearchType(SearchType.SCAN)
                        .setScroll(TimeValue.timeValueMillis(service.millis()))
                        .setQuery(termQuery("xbib.uid", volume.id()));
                SearchResponse holdingSearchResponse = holdingsSearchRequest.execute().actionGet();
                service.queryMetric().mark();
                logger.debug("searchSeriesVolumeHoldings search request={} hits={}",
                        holdingsSearchRequest.toString(), holdingSearchResponse.getHits().getTotalHits());
                while (holdingSearchResponse.getScrollId() != null) {
                    holdingSearchResponse = service.client().prepareSearchScroll(holdingSearchResponse.getScrollId())
                            .setScroll(TimeValue.timeValueMillis(service.millis()))
                            .execute().actionGet();
                    service.queryMetric().mark();
                    SearchHits holdingHits = holdingSearchResponse.getHits();
                    if (holdingHits.getHits().length == 0) {
                        break;
                    }
                    for (SearchHit holdingHit : holdingHits) {
                        // one hit, many items. Iterate over items
                        Object o = holdingHit.getSource().get("Item");
                        if (!(o instanceof List)) {
                            o = Arrays.asList(o);
                        }
                        for (Map<String,Object> item : (List<Map<String,Object>>)o) {
                            if (item != null && !item.isEmpty()) {
                                VolumeHolding volumeHolding = new VolumeHolding(item, volume);
                                volumeHolding.addParent(manifestation.externalID());
                                volumeHolding.addParent(volume.externalID());
                                volumeHolding.setMediaType(manifestation.mediaType());
                                volumeHolding.setCarrierType(manifestation.carrierType());
                                volumeHolding.setDate(volume.firstDate(), volume.lastDate());
                                volumeHolding.setRegion(service.bibdatLookup().lookupRegion().get(volumeHolding.getISIL()));
                                volumeHolding.setOrganization(service.bibdatLookup().lookupOrganization().get(volumeHolding.getISIL()));
                                volumeHolding.setServiceMode(service.statusCodeMapper().lookup(volumeHolding.getStatus()));
                                if ("interlibrary".equals(volumeHolding.getServiceType()) && volumeHolding.getISIL() != null) {
                                    volume.addHolding(volumeHolding);
                                }
                            }
                        }
                    }
                }
                manifestation.addVolume(volume);
            }
        }
    }

    /*private void merge(Collection<Manifestation> manifestations, Set<Holding> holdings, Set<License> licenses,
                       Set<Volume> volumes, Set<VolumeHolding> extraholdings) {
        Set<String> isil = newTreeSet();
        isil.addAll(holdings.stream().map(Holding::getISIL).collect(Collectors.toList()));
        isil.addAll(holdings.stream().map(Holding::getServiceISIL).collect(Collectors.toList()));
        isil.addAll(licenses.stream().map(License::getISIL).collect(Collectors.toList()));
        isil.addAll(licenses.stream().map(License::getServiceISIL).collect(Collectors.toList()));
        Set<String> newisil = newTreeSet();
        newisil.addAll(extraholdings.stream()
                .filter(vh -> "interlibrary".equals(vh.getServiceType()))
                .map(VolumeHolding::getISIL).collect(Collectors.toList()));
        newisil.removeAll(isil);
        logger.info("cluster {}: {} holdings, {} licenses, {} extra volumes ({}), {} extra holdings, existing ISILs={}, new ISILs={}",
                manifestations, holdings.size(), licenses.size(), volumes.size(), volumes, extraholdings.size(), isil, newisil);
        for (Volume volume : volumes) {
            logger.info("title={} vol={} number={} date={}", volume.title(), volume.getVolumeDesignation(), volume.getNumbering(), volume.firstDate());
        }
        for (String s : newisil) {
            if (service.bibdatLookup().lookupLibrary().get(s) != null) {
                // ok
            } else if (service.bibdatLookup().lookupOther().get(s) != null) {
                logger.info("dubious ISIL {}", s);
            } else {
                logger.info("unknown (private?) ISIL {}", s);
            }
        }
    }*/

    private Set<String> findTheRelationsBetween(Manifestation manifestation, String id) {
        Set<String> relationNames = new HashSet<String>();
        for (String entry : Manifestation.relationEntries()) {
            Object o = manifestation.map().get(entry);
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

    private void setAllRelationsBetween(Manifestation manifestation, Collection<Manifestation> cluster) {
        for (String relation : Manifestation.relationEntries()) {
            Object o = manifestation.map().get(relation);
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

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Manifestation m : cluster) {
                if (sb.length() > 0 ) {
                    sb.append(",");
                }
                sb.append(m.toString());
            }
            return "Cont[" + sb.toString() + "][pos=" + pos + "]";
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
