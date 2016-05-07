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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.xbib.common.settings.Settings;
import org.xbib.etl.support.StatusCodeMapper;
import org.xbib.etl.support.ValueMaps;
import org.xbib.metrics.Meter;
import org.xbib.tools.merge.Merger;
import org.xbib.tools.merge.holdingslicenses.HoldingsLicensesIndexer;
import org.xbib.tools.merge.holdingslicenses.entities.Monograph;
import org.xbib.tools.merge.holdingslicenses.entities.TitleRecord;
import org.xbib.tools.merge.holdingslicenses.support.BibdatLookup;
import org.xbib.tools.merge.holdingslicenses.support.BlackListedISIL;
import org.xbib.tools.merge.holdingslicenses.support.ConsortiaLookup;
import org.xbib.tools.merge.holdingslicenses.support.MappedISIL;
import org.xbib.tools.merge.holdingslicenses.support.TitleRecordRequest;
import org.xbib.tools.metrics.Metrics;
import org.xbib.util.IndexDefinition;
import org.xbib.common.Strings;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class SimpleHoldingsLicensesMerger extends Merger {

    private final static Logger logger = LogManager.getLogger(SimpleHoldingsLicensesMerger.class);

    private SimpleHoldingsLicensesMerger simpleHoldingsLicensesMerger;

    private BibdatLookup bibdatLookup;

    private ConsortiaLookup consortiaLookup;

    private BlackListedISIL isilbl;

    private MappedISIL isilMapped;

    private StatusCodeMapper statusCodeMapper;

    private Metrics metrics;

    private Meter queryMetric;

    private String sourceTitleIndex;
    private String sourceHoldingsIndex;
    private String sourceLicenseIndex;
    private String sourceIndicatorIndex;
    private String sourceMonographicIndex;
    private String sourceMonographicHoldingsIndex;
    private String sourceOpenAccessIndex;

    private HoldingsLicensesIndexer holdingsLicensesIndexer;

    @Override
    @SuppressWarnings("unchecked")
    public int run(Settings settings) throws Exception {
        this.simpleHoldingsLicensesMerger = this;
        this.metrics = new Metrics();
        this.queryMetric = new Meter();
        queryMetric.spawn(5L);
        metrics.scheduleMetrics(settings, "meterquery", queryMetric);
        return super.run(settings);
    }

    @SuppressWarnings("unchecked")
    public Pipeline<SimpleHoldingsLicensesWorker, TitleRecordRequest> getPipeline() {
        return pipeline;
    }

    @Override
    protected WorkerProvider provider() {
        return new WorkerProvider<SimpleHoldingsLicensesWorker>() {
            int i = 0;

            @Override
            @SuppressWarnings("unchecked")
            public SimpleHoldingsLicensesWorker get(Pipeline pipeline) {
                return (SimpleHoldingsLicensesWorker) new SimpleHoldingsLicensesWorker(settings,
                        simpleHoldingsLicensesMerger, i++)
                        .setPipeline(pipeline);
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void prepareRequests() throws Exception {
        super.prepareRequests();
        Map<String, IndexDefinition> indexDefinitionMap = getInputIndexDefinitionMap();
        this.sourceTitleIndex = indexDefinitionMap.get("zdb").getIndex();
        checkIndex("zdb", sourceTitleIndex);
        this.sourceHoldingsIndex = indexDefinitionMap.get("zdbholdings").getIndex();
        checkIndex("zdbholdings", sourceHoldingsIndex);
        this.sourceLicenseIndex = indexDefinitionMap.get("ezbxml").getIndex();
        checkIndex("ezbxml", sourceLicenseIndex);
        this.sourceIndicatorIndex = indexDefinitionMap.get("ezbweb").getIndex();
        checkIndex("ezbweb", sourceIndicatorIndex);
        this.sourceMonographicIndex = indexDefinitionMap.get("hbz").getIndex();
        checkIndex("hbz", sourceMonographicIndex);
        this.sourceMonographicHoldingsIndex = indexDefinitionMap.get("hbzholdings").getIndex();
        checkIndex("hbzholdings", sourceMonographicHoldingsIndex);
        this.sourceOpenAccessIndex = indexDefinitionMap.get("doaj").getIndex();
        checkIndex("doaj", sourceOpenAccessIndex);

        logger.info("preparing bibdat lookup...");
        bibdatLookup = new BibdatLookup();
        try {
            bibdatLookup.buildLookup(search.client(), indexDefinitionMap.get("bibdat").getIndex());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("bibdat prepared, {} names, {} organizations, {} regions, {} other",
                bibdatLookup.lookupName().size(),
                bibdatLookup.lookupOrganization().size(),
                bibdatLookup.lookupRegion().size(),
                bibdatLookup.lookupOther().size());

        // prepare "national license" / consortia ISIL expansion
        consortiaLookup = new ConsortiaLookup();
        try {
            consortiaLookup.buildLookup(search.client(), indexDefinitionMap.get("nlzisil").getIndex());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("preparing ISIL blacklist...");
        isilbl = new BlackListedISIL();
        try (InputStream in = getClass().getResourceAsStream(settings.get("isil.blacklist", "/org/xbib/tools/merge/holdingslicenses/isil.blacklist"))) {
            isilbl.buildLookup(in);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("ISIL blacklist prepared, size = {}", isilbl.lookup().size());

        logger.info("preparing mapped ISIL...");
        isilMapped = new MappedISIL();
        try (InputStream in = getClass().getResourceAsStream(settings.get("isil.map", "/org/xbib/tools/merge/holdingslicenses/isil.map"))) {
            isilMapped.buildLookup(in);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("mapped ISILs prepared, size = {}", isilMapped.lookup().size());

        logger.info("preparing status code mapper...");
        ValueMaps valueMaps = new ValueMaps();
        Map<String, Object> statuscodes = valueMaps.getMap("org/xbib/analyzer/mab/status.json", "status");
        statusCodeMapper = new StatusCodeMapper();
        statusCodeMapper.add(statuscodes);
        logger.info("status code mapper prepared, size = {}", statusCodeMapper.getMap().size());

        this.holdingsLicensesIndexer = new HoldingsLicensesIndexer(this);

        if (settings.getAsBoolean("withserials", true)) {
            processSerialTitles();
        }
        if (settings.getAsBoolean("withmonographs", true)) {
            processMonographTitles();
        }
    }

    private void processSerialTitles() {
        Map<String, IndexDefinition> indexDefinitionMap = getInputIndexDefinitionMap();
        int scrollSize = settings.getAsInt("scrollsize", 10);
        long scrollMillis = settings.getAsTime("scrolltimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(60)).millis();
        boolean failure = false;
        SearchRequestBuilder searchRequest = search.client().prepareSearch()
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .addSort(SortBuilders.fieldSort("_doc"));
        searchRequest.setIndices(indexDefinitionMap.get("zdb").getIndex());
        // single identifier?
        String identifier = settings.get("identifier");
        if (identifier != null) {
            searchRequest.setQuery(termQuery("IdentifierZDB.identifierZDB", identifier));
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        long total =  searchResponse.getHits().getTotalHits();
        logger.info("merging holdings/licenses for {} serial title records", total);
        while (!failure && searchResponse.getHits().getHits().length > 0) {
            queryMetric.mark();
            for (SearchHit hit : searchResponse.getHits()) {
                try {
                    if (getPipeline().getWorkers().isEmpty()) {
                        logger.error("no more workers left to receive, aborting");
                        return;
                    }
                    TitleRecord titleRecord = new TitleRecord(hit.getSource());
                    TitleRecordRequest titleRecordRequest = new TitleRecordRequest().set(titleRecord);
                    getPipeline().putQueue(titleRecordRequest);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                    failure = true;
                    break;
                }
            }
            searchResponse = search.client()
                    .prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet();
        }
        search.client()
                .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet();
        logger.info("{} serial title records processed", total);
    }

    private void processMonographTitles() {
        Map<String, IndexDefinition> indexDefinitionMap = getInputIndexDefinitionMap();
        IndexDefinition indexDefinition = indexDefinitionMap.get("hbz");
        if (indexDefinition == null) {
            return;
        }
        int scrollSize = settings.getAsInt("scrollsize", 10);
        long scrollMillis = settings.getAsTime("scrolltimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(60)).millis();
        boolean failure = false;
        // only ISBN (for now)
        SearchRequestBuilder searchRequest = search.client().prepareSearch()
                .setIndices(indexDefinition.getIndex())
                .setQuery(existsQuery("IdentifierISBN.identifierISBN"))
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .addSort(SortBuilders.fieldSort("_doc"));
        // single identifier?
        String identifier = settings.get("identifier");
        if (identifier != null) {
            searchRequest.setQuery(termQuery("RecordIdentifier.identifierForTheRecord", identifier));
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        long total =  searchResponse.getHits().getTotalHits();
        logger.info("iterating over {} monograph records", total);
        while (!failure && searchResponse.getHits().getHits().length > 0) {
            queryMetric.mark();
            for (SearchHit hit : searchResponse.getHits()) {
                try {
                    if (getPipeline().getWorkers().isEmpty()) {
                        logger.error("no more workers left to receive, aborting");
                        return;
                    }
                    Monograph monograph = new Monograph(hit.getSource());
                    TitleRecordRequest titleRecordRequest = new TitleRecordRequest().set(monograph);
                    getPipeline().putQueue(titleRecordRequest);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                    failure = true;
                    break;
                }
            }
            searchResponse = search.client()
                    .prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet();
        }
        search.client()
                .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet();
        logger.info("{} monograph records processed", total);
    }


    protected void waitFor() throws IOException {
        try {
            // send poison elements and wait for completion
            getPipeline().waitFor(new TitleRecordRequest());
        } finally {
            long total = 0L;
            for (SimpleHoldingsLicensesWorker worker : getPipeline().getWorkers()) {
                logger.info("worker {}, count {}, took {}",
                        worker,
                        worker.getMetric().getCount(),
                        TimeValue.timeValueNanos(worker.getMetric().elapsed()).format());
                total += worker.getMetric().getCount();
            }
            logger.info("worker metric count total = {}", total);
            metrics.append("meterquery", queryMetric);
        }
    }

    @Override
    protected void disposeRequests(int returncode) throws IOException {
        super.disposeRequests(returncode);
    }

    @Override
    protected void disposeResources(int returncode) throws IOException {
        super.disposeResources(returncode);
    }

    private void checkIndex(String displayName, String index) throws IOException {
        if (Strings.isNullOrEmpty(index)) {
            throw new IllegalArgumentException("no index given for " + displayName);
        }
        SearchRequestBuilder searchRequest = search.client().prepareSearch()
                .setIndices(index)
                .setSize(0);
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        if (searchResponse.getHits().getTotalHits() == 0L) {
            throw new IllegalArgumentException("no documents given in index " + displayName);
        }
        Long l = ingest.mostRecentDocument(index);
        if (l != null) {
            logger.info("most recent document of {} is from {}", displayName,
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.systemDefault()));
        } else {
            throw new IllegalArgumentException("no most recent document given in index " + displayName);
        }
    }

    public Metrics getMetrics() {
        return metrics;
    }

    BibdatLookup bibdatLookup() {
        return bibdatLookup;
    }

    ConsortiaLookup consortiaLookup() {
        return consortiaLookup;
    }

    BlackListedISIL blackListedISIL() {
        return isilbl;
    }

    MappedISIL mappedISIL() {
        return isilMapped;
    }

    StatusCodeMapper statusCodeMapper() {
        return statusCodeMapper;
    }

    String getSourceHoldingsIndex() {
        return sourceHoldingsIndex;
    }

    String getSourceLicenseIndex() {
        return sourceLicenseIndex;
    }

    String getSourceIndicatorIndex() {
        return sourceIndicatorIndex;
    }

    String getSourceMonographicIndex() {
        return sourceMonographicIndex;
    }

    String getSourceMonographicHoldingsIndex() {
        return sourceMonographicHoldingsIndex;
    }

    String getSourceOpenAccessIndex() {
        return sourceOpenAccessIndex;
    }

    HoldingsLicensesIndexer getHoldingsLicensesIndexer() {
        return holdingsLicensesIndexer;
    }
}
