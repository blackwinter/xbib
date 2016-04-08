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
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.xbib.common.settings.Settings;
import org.xbib.iri.IRI;
import org.xbib.metrics.Meter;
import org.xbib.tools.merge.holdingslicenses.entities.TitleRecord;
import org.xbib.util.ExceptionFormatter;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.Worker;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class ArticlesMergerWorker
        implements Worker<Pipeline<ArticlesMergerWorker, SerialItemRequest>, SerialItemRequest> {

    private final ArticlesMerger articlesMerger;

    private final static AtomicInteger medlineDuplicates = new AtomicInteger();

    private final static AtomicInteger xrefDuplicates = new AtomicInteger();

    private final int threadId;

    private final Logger logger;
    private final int scrollSize;
    private final long scrollMillis;

    private SerialItem serialItem;

    private Meter metric;

    public ArticlesMergerWorker(Settings settings, ArticlesMerger articlesMerger, int number) {
        this.threadId = number;
        this.articlesMerger = articlesMerger;
        this.logger = LogManager.getLogger(toString());
        this.scrollSize = settings.getAsInt("worker.scrollsize", 10); // per shard!
        this.scrollMillis = settings.getAsTime("worker.scrolltimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(360)).millis();
    }

    @Override
    public ArticlesMergerWorker setPipeline(Pipeline<ArticlesMergerWorker, SerialItemRequest> pipeline) {
        // unused
        return this;
    }

    @Override
    public Pipeline<ArticlesMergerWorker, SerialItemRequest> getPipeline() {
        return articlesMerger.getPipeline();
    }

    public ArticlesMergerWorker setMetric(Meter metric) {
        this.metric = metric;
        return this;
    }

    public Meter getMetric() {
        return metric;
    }

    public AtomicInteger getMedlineDuplicates() {
        return medlineDuplicates;
    }

    public AtomicInteger getXrefDuplicates() {
        return xrefDuplicates;
    }

    @Override
    public SerialItemRequest call() throws Exception {
        if (metric == null) {
            this.metric = new Meter();
            metric.spawn(5L);
        }
        SerialItemRequest element = null;
        try {
            element = getPipeline().getQueue().take();
            serialItem = element.get();
            while (serialItem != null) {
                process(serialItem);
                metric.mark();
                element = getPipeline().getQueue().take();
                serialItem = element.get();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            logger.error(ExceptionFormatter.format(e));
            logger.error("exiting, exception while processing {}", serialItem.getDate());
        } finally {
            metric.stop();
        }
        logger.info("medline dups = {}", medlineDuplicates);
        logger.info("xref dups = {}", xrefDuplicates);
        return element;
    }

    @Override
    public void close() throws IOException {
        if (!getPipeline().getQueue().isEmpty()) {
            logger.error("queue not empty?");
        }
    }

    @Override
    public String toString() {
        return ArticlesMergerWorker.class.getSimpleName() + "." + threadId;
    }

    private void process(SerialItem serialItem) throws IOException {
        Set<String> issns = new HashSet<>();
        for (TitleRecord titleRecord : serialItem.getTitleRecords()) {
            Collection<String> l = titleRecord.getISSNs();
            if (l != null) {
                issns.addAll(l);
            }
        }
        if (issns.isEmpty()) {
            logger.warn("no issn in {}", serialItem.getTitleRecords());
            return;
        }
        BoolQueryBuilder issnQuery = boolQuery();
        for (String issn : issns) {
            String s = issn.indexOf('-') > 0 ? issn : new StringBuilder(issn.toUpperCase()).insert(4, '-').toString();
            issnQuery.should(matchPhraseQuery("frbr:partOf.prism:issn", s));
        }
        QueryBuilder dateQuery = termQuery("dc:date", serialItem.getDate());
        BoolQueryBuilder queryBuilder = boolQuery();
        queryBuilder.must(dateQuery).must(issnQuery);
        QueryBuilder existsKey = existsQuery("xbib:key");
        QueryBuilder filteredQueryBuilder = boolQuery().must(queryBuilder).filter(existsKey);
        Map<String,Map<String,Object>> docs = new HashMap<>();
        if (articlesMerger.getMedlineIndex() != null) {
            fetchMedline(filteredQueryBuilder, docs);
        }
        if (articlesMerger.getXrefIndex() != null) {
            fetchXref(filteredQueryBuilder, docs);
        }
        postProcess(serialItem, docs);
    }

    private void fetchMedline(QueryBuilder queryBuilder, Map<String,Map<String,Object>> docs) throws IOException {
        /*
            // no ISSN, so we derive matching from title plus year
            // shorten title (series statement after '/' or ':') to raise probability of matching
            String t = serialItem.getManifestation().getTitle();
            // must include at least one space (= two words)
            int pos = t.indexOf(' ');
            if (pos < 0) {
                // no space, skip this
                search = false;
            }
            if (!search) {
                return;
            }
            BoolQueryBuilder abbrevQuery = boolQuery();
            if (serialItem.getAbbrevs() != null) {
                serialItem.getAbbrevs().forEach(abbrev -> abbrevQuery.should(matchPhraseQuery("fabio:hasShortTitle", abbrev)));
            }
            QueryBuilder titleQuery = matchPhrasePrefixQuery("prism:publicationName", t);
            QueryBuilder dateQuery = termQuery("prism:publicationDate", serialItem.getDate());
            queryBuilder.must(titleQuery).must(dateQuery).should(abbrevQuery);
        */
        SearchRequestBuilder searchRequest = articlesMerger.search().client().prepareSearch()
                .setIndices(articlesMerger.getMedlineIndex().getIndex())
                .setTypes(articlesMerger.getMedlineIndex().getType())
                .setQuery(queryBuilder)
                .setSize(scrollSize) // size() is per shard!
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        logger.debug("fetchMedline: hits={} query={}", searchResponse.getHits().getTotalHits(), searchRequest);
        while (searchResponse.getHits().getHits().length > 0) {
            for (int i = 0; i < searchResponse.getHits().getHits().length; i++) {
                SearchHit hit = searchResponse.getHits().getAt(i);
                String key = (String)hit.getSource().get("xbib:key");
                if (key == null) {
                    continue;
                }
                Map<String,Object> map = new HashMap<>();
                map.putAll(hit.getSource());
                if (docs.containsKey(key)) {
                    medlineDuplicates.incrementAndGet();
                    Map<String,Object> doc = docs.get(key);
                    List<String> pmids = new LinkedList<>();
                    pmids.add((String) map.get("fabio:hasPubMedId"));
                    Object o = doc.get("fabio:hasPubMedId");
                    if (o instanceof Collection) {
                        pmids.addAll((Collection<String>)o);
                    } else {
                        pmids.add((String)o);
                    }
                    map.put("fabio:hasPubMedId", pmids);
                    docs.put(key, map);
                } else {
                    docs.put(key, map);
                }
            }
            searchResponse = articlesMerger.search().client()
                    .prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet();
        }
        articlesMerger.search().client().prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet();
    }

    private void fetchXref(QueryBuilder queryBuilder, Map<String,Map<String,Object>> docs) throws IOException {
        /*
            // title matching, shorten title (series statement after '/' or ':') to raise probability of matching
            String t = serialItem.getManifestation().getTitle();
            // must include at least one space (= two words)
            int pos = t.indexOf(' ');
            if (pos < 0) {
                // no space, skip this
                search = false;
            }
            QueryBuilder titleQuery = matchPhrasePrefixQuery("prism:publicationName", t);
            QueryBuilder dateQuery = termQuery("prism:publicationDate", serialItem.getDate());
            queryBuilder.must(titleQuery).must(dateQuery);
            // filter to publisher, if exists
            String p = serialItem.getManifestation().getPublisher();
            QueryBuilder publisherQuery = p != null ? matchPhraseQuery("dc:publisher", p) : null;
            if (publisherQuery != null) {
                queryBuilder.must(publisherQuery);
            }
            if (!search) {
                logger.debug("skipped, title '{}' does not contain spaces", t);
                return;
            }
        */
        SearchRequestBuilder searchRequest = articlesMerger.search().client().prepareSearch()
                .setIndices(articlesMerger.getXrefIndex().getIndex())
                .setTypes(articlesMerger.getXrefIndex().getType())
                .setQuery(queryBuilder)
                .setSize(scrollSize) // size() is per shard
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .addSort(SortBuilders.fieldSort("_doc"));
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        logger.debug("fetchXRef: hits={} query={}", searchResponse.getHits().getTotalHits(), searchRequest);
        while (searchResponse.getHits().getHits().length > 0) {
            for (int i = 0; i < searchResponse.getHits().getHits().length; i++) {
                SearchHit hit = searchResponse.getHits().getAt(i);
                String key = (String)hit.getSource().get("xbib:key");
                if (key == null) {
                    continue;
                }
                if (docs.containsKey(key)) {
                    xrefDuplicates.incrementAndGet();
                    Map<String,Object> old = docs.get(key); // immutable
                    Map<String,Object> map = new HashMap<>();
                    map.putAll(old); // immutable -> mutable
                    merge(map, hit.getSource()); // merge new entries or append
                    docs.put(key, map);
                } else {
                    // new document
                    docs.put(key, hit.getSource());
                }
            }
            searchResponse = articlesMerger.search().client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet();
        }
        articlesMerger.search().client().prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet();
    }

    private void postProcess(SerialItem serialItem, Map<String,Map<String,Object>> docs) throws IOException {
        if (docs == null || docs.isEmpty()) {
            return;
        }
        Collection<Map<String,Object>> publications =  makePublications(serialItem);
        Collection<String> subjects = makeSubjects(serialItem);
        for (Map.Entry<String, Map<String, Object>> entry : docs.entrySet()) {
            Map<String, Object> doc = entry.getValue();
            doc.put("frbr:partOf", publications);
            if (!subjects.isEmpty()) {
                doc.put("dc:subject", subjects.size() > 1 ? subjects : subjects.iterator().next());
            }
            XContentBuilder builder = jsonBuilder();
            builder.value(doc);
            String index = articlesMerger.getArticlesIndex().getIndex();
            String type = articlesMerger.getArticlesIndex().getType();
            String id = entry.getKey();
            articlesMerger.ingest().index(index, type, id, builder.string());
        }
    }

    private Collection<Map<String,Object>> makePublications(SerialItem serialItem) {
        Collection<Map<String,Object>> publications = new HashSet<>();
        for (TitleRecord titleRecord : serialItem.getTitleRecords()) {
            Map<String, Object> publication = new HashMap<>();
            String zdbid = titleRecord.externalID();
            publication.put("xbib:zdbid", zdbid);
            // hyphenated form of ZDB ID for linking to ld.zdb-services.de
            String zdbIdWithHyphen = new StringBuilder(zdbid).insert(zdbid.length() - 1, "-").toString();
            IRI zdbserviceid = IRI.builder().scheme("http").host("ld.zdb-services.de").path("/resource/" + zdbIdWithHyphen).build();
            publication.put("rdfs:seeAlso", zdbserviceid.toString());
            publication.put("rdf:type", "fabio:Journal");
            publication.put("prism:publicationName", titleRecord.getTitle());
            publication.put("dc:publisher", titleRecord.getPublisherName());
            publication.put("prism:place", titleRecord.getPublisherPlace());
            publication.put("prism:issn", titleRecord.getIdentifiers().get("formattedissn"));
            publication.put("dc:rights", titleRecord.getLicense());
            publication.put("xbib:doaj", titleRecord.isOpenAccess());
            publication.put("xbib:media", titleRecord.mediaType());
            publication.put("xbib:content", titleRecord.contentType());
            publication.put("xbib:carrier", titleRecord.carrierType());
            publications.add(publication);
        }
        if (publications.size() > 1) {
            logger.info("publications = {}", publications);
        }
        return publications;
    }

    private Collection<String> makeSubjects(SerialItem serialItem) {
        Collection<String> subjects = new HashSet<>();
        for (TitleRecord titleRecord : serialItem.getTitleRecords()) {
            subjects.addAll(getDDC(titleRecord));
        }
        return subjects;
    }

    private void merge(Map<String,Object> map1, Map<String,Object> map2) {
        // pagination check: concatenate pages if pages are side by side
        if (map1.containsKey("frbr:embodiment") && map2.containsKey("frbr:embodiment")) {
            Object o1 = map1.get("frbr:embodiment");
            if (!(o1 instanceof Collection)) {
                o1 = Collections.singletonList(o1);
            }
            Collection<Map<String,Object>> l1 = (Collection<Map<String,Object>>)o1;
            Object o2 = map2.get("frbr:embodiment");
            if (!(o2 instanceof Collection)) {
                o2 = Collections.singletonList(o2);
            }
            Collection<Map<String,Object>> l2 = (Collection<Map<String,Object>>)o2;
            Integer b1 = null;
            Integer e1 = null;
            for (Map<String,Object> m1 : l1) {
                if (m1.containsKey("prism:startingPage")) {
                    try {
                        b1 = Integer.parseInt((String) m1.get("prism:startingPage"));
                    } catch (Exception e) {
                        // ignore
                    }
                }
                if (m1.containsKey("prism:endingPage")) {
                    try {
                        e1 = Integer.parseInt((String) m1.get("prism:endingPage"));
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
            Integer b2 = null;
            Integer e2 = null;
            for (Map<String,Object> m2 : l2) {
                if (m2.containsKey("prism:startingPage")) {
                    try {
                        b2 = Integer.parseInt((String) m2.get("prism:startingPage"));
                    } catch (Exception e) {
                        // ignore
                    }
                }
                if (m2.containsKey("prism:endingPage")) {
                    try {
                        e2 = Integer.parseInt((String) m2.get("prism:endingPage"));
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
            if (b1 != null && e1 != null && b2 != null && e2 != null) {
                // condition "1 follows 2"
                if (e2 + 1 == b1) {
                    // concatenate
                    logger.debug("concatenation of pages: {}-{}, {}-{}", b2, e2, b1, e1);
                    setPagination(map1, b2, e1);
                    mergeDOIs(map1, map2);
                    return;
                }
                // condition "2 follows 1"
                if (e1 + 1 == b2) {
                    // concatenate
                    logger.debug("concatenation of pages: {}-{}, {}-{}", b1, e1, b2, e2);
                    setPagination(map1, b1, e2);
                    mergeDOIs(map1, map2);
                    return;
                }
            } else if (b1 !=null && b2 != null) {
                if (b1 + 1 == b2) {
                    // concatenate
                    logger.debug("concatenation of pages: {}, {}", b1, b2);
                    setPagination(map1, b1, b2);
                    mergeDOIs(map1, map2);
                    return;
                }
                if (b2 + 1 == b1) {
                    // concatenate
                    logger.debug("concatenation of pages: {}, {}", b2, b1);
                    setPagination(map1, b2, b1);
                    mergeDOIs(map1, map2);
                    return;
                }
            }
        }
        // friendly merge: do not overwrite, instead append
        for (Map.Entry<String,Object> e : map2.entrySet()) {
            if (map1.containsKey(e.getKey())) {
                Object o = map1.get(e.getKey());
                if (o != null) {
                    if (!(o instanceof Collection)) {
                        o = Collections.singletonList(o);
                    }
                    Set<Object> set = new HashSet<>();
                    set.addAll((Collection<Object>) o);
                    if (e.getValue() instanceof Collection) {
                        set.addAll((Collection)e.getValue());
                    } else if (!(set.iterator().next() instanceof Map) || (e.getValue() instanceof Map)) {
                        set.add(e.getValue());
                    } else {
                        logger.warn("can't append value, wrong type: {}->{}", e.getKey(), e.getValue());
                    }
                    map1.put(e.getKey(), set.size() > 1 ? set : set.iterator().next());
                }
            } else {
                map1.put(e.getKey(), e.getValue());
            }
        }
    }

    private void setPagination(Map<String,Object> map, Integer begin, Integer end) {
        List<Map<String,Object>> newList = new LinkedList<>();
        Object o = map.get("frbr:embodiment");
        if (o != null) {
            if (!(o instanceof Collection)) {
                o = Collections.singletonList(o);
            }
            Collection<Map<String, Object>> l = (Collection<Map<String, Object>>) o;
            for (Map<String, Object> m : l) {
                if (m.containsKey("prism:startingPage")) {
                    m.put("prism:startingPage", begin);
                }
                if (m.containsKey("prism:endingPage")) {
                    m.put("prism:endingPage", end);
                }
                newList.add(m);
            }
            map.put("frbr:embodiment", newList);
        }
    }

    private void mergeDOIs(Map<String,Object> map1, Map<String,Object> map2) {
        List<String> newDOIS = new LinkedList<>();
        Object o = map1.get("prism:doi");
        if (o != null) {
            if (!(o instanceof Collection)) {
                o = Collections.singletonList(o);
            }
            newDOIS.addAll((Collection<String>)o);
        }
        o = map2.get("prism:doi");
        if (o != null) {
            if (!(o instanceof Collection)) {
                o = Collections.singletonList(o);
            }
            newDOIS.addAll((Collection<String>)o);
        }
        map1.put("prism:doi", newDOIS);
    }

    private Set<String> getDDC(TitleRecord titleRecord) {
        // DDC
        Set<String> subjects = new HashSet<>();
        Object o = titleRecord.map().get("DDCClassificationNumber");
        if (o != null) {
            if (!(o instanceof Collection)) {
                o = Collections.singletonList(o);
            }
            Collection<Map<String, Object>> l = (Collection<Map<String, Object>>) o;
            for (Map<String, Object> ddc : l) {
                Object oo = ddc.containsKey("ddcSource") ? ddc.get("ddcSource") : ddc.get("ddc");
                if (oo != null) {
                    if (!(oo instanceof Collection)) {
                        oo = Collections.singletonList(oo);
                    }
                    Collection<String> ll = (Collection<String>) oo;
                    for (String s : ll) {
                        subjects.add("http://xbib.info/dewey/class/" + s);
                    }
                }
            }
        }
        return subjects;
    }

    private Set<String> getAbbrev(TitleRecord titleRecord) {
        // Title abbrev
        Set<String> abbrevs = new HashSet<>();
        Object o = titleRecord.map().get("AbbreviatedTitle");
        if (o != null) {
            if (!(o instanceof Collection)) {
                o = Collections.singletonList(o);
            }
            for (Map<String, Object> abbrev : (Collection<Map<String, Object>>) o) {
                abbrevs.add(abbrev.get("titleMain").toString());
            }
        }
        return abbrevs;
    }

}
