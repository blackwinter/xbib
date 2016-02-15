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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.Ingest;
import org.xbib.elasticsearch.helper.client.SearchTransportClient;
import org.xbib.etl.support.StatusCodeMapper;
import org.xbib.etl.support.ValueMaps;
import org.xbib.metric.MeterMetric;
import org.xbib.tools.merge.Merger;
import org.xbib.tools.merge.holdingslicenses.support.BibdatLookup;
import org.xbib.tools.merge.holdingslicenses.support.BlackListedISIL;
import org.xbib.tools.merge.holdingslicenses.entities.TitleRecord;
import org.xbib.tools.merge.holdingslicenses.support.ConsortiaLookup;
import org.xbib.tools.merge.holdingslicenses.support.MappedISIL;
import org.xbib.util.ExceptionFormatter;
import org.xbib.util.IndexDefinition;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Merge holdings and licenses
 */
public class HoldingsLicensesMerger extends Merger {

    private final static Logger logger = LogManager.getLogger(HoldingsLicensesMerger.class.getSimpleName());

    private HoldingsLicensesMerger holdingsLicensesMerger;

    private int size;

    private long millis;

    private String identifier;

    private BibdatLookup bibdatLookup;

    private ConsortiaLookup consortiaLookup;

    private BlackListedISIL isilbl;

    private MappedISIL isilMapped;

    private StatusCodeMapper statusCodeMapper;

    private MeterMetric queryMetric;

    @Override
    @SuppressWarnings("unchecked")
    public int run(Settings settings) throws Exception {
        this.holdingsLicensesMerger = this;
        this.queryMetric = new MeterMetric(5L, TimeUnit.SECONDS);
        return super.run(settings);
    }

    protected void waitFor() throws IOException {
        metrics.scheduleMetrics(settings, "meterquery", queryMetric);
        // send poison elements and wait for completion
        getPipeline().waitFor(new TitelRecordRequest());
        getPipeline().shutdown();
        long total = 0L;
        for (HoldingsLicensesWorker worker : getPipeline().getWorkers()) {
            logger.info("worker {}, count {}, started {}, ended {}, took {}",
                    worker,
                    worker.getMetric().count(),
                    DateTimeFormatter.ISO_INSTANT.format(worker.getMetric().started()),
                    DateTimeFormatter.ISO_INSTANT.format(worker.getMetric().stopped()),
                    TimeValue.timeValueNanos(worker.getMetric().elapsed()).format());
            total += worker.getMetric().count();
        }
        logger.info("worker metric count total={}", total);
        metrics.append("meterquery", queryMetric);
    }

    @SuppressWarnings("unchecked")
    public Pipeline<HoldingsLicensesWorker, TitelRecordRequest> getPipeline() {
        return pipeline;
    }

    @Override
    protected WorkerProvider provider() {
        return new WorkerProvider<HoldingsLicensesWorker>() {
            int i = 0;

            @Override
            public HoldingsLicensesWorker get(Pipeline pipeline) {
                return (HoldingsLicensesWorker) new HoldingsLicensesWorker(holdingsLicensesMerger, i++)
                        .setPipeline(pipeline);
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void prepareRequests() throws Exception {
        super.prepareRequests();
        Map<String,IndexDefinition> indexDefinition = getInputIndexDefinitionMap();
        logger.info("preparing bibdat lookup...");
        bibdatLookup = new BibdatLookup();
        try {
            bibdatLookup.buildLookup(search.client(), indexDefinition.get("bibdat").getIndex());
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
            consortiaLookup.buildLookup(search.client(), indexDefinition.get("nlzisil").getIndex());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("preparing ISIL blacklist...");
        isilbl = new BlackListedISIL();
        try (InputStream in = getClass().getResourceAsStream("isil.blacklist")) {
            isilbl.buildLookup(in);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("ISIL blacklist prepared, size = {}", isilbl.lookup().size());

        logger.info("preparing mapped ISIL...");
        isilMapped = new MappedISIL();
        try (InputStream in = getClass().getResourceAsStream("isil.map")) {
            isilMapped.buildLookup(in);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("mapped ISILs prepared, size = {}", isilMapped.lookup().size());

        logger.info("preparing status code mapper...");
        ValueMaps valueMaps = new ValueMaps();
        Map<String,Object> statuscodes = valueMaps.getMap("org/xbib/analyzer/mab/status.json", "status");
        statusCodeMapper = new StatusCodeMapper();
        statusCodeMapper.add(statuscodes);
        logger.info("status code mapper prepared, size = {}", statusCodeMapper.getMap().size());

        // all prepared. Enter loop over all title records
        boolean failure = false;
        SearchRequestBuilder searchRequest = search.client().prepareSearch()
                .setSize(size)
                .setScroll(TimeValue.timeValueMillis(millis))
                .addSort(SortBuilders.fieldSort("_doc"));
        searchRequest.setIndices(indexDefinition.get("zdb").getIndex());
        // single identifier?
        if (identifier != null) {
            searchRequest.setQuery(termQuery("IdentifierZDB.identifierZDB", identifier));
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        logger.info("merging holdings/licenses for {} title records",
                searchResponse.getHits().getTotalHits());
        do {
            queryMetric.mark();
            for (SearchHit hit :  searchResponse.getHits()) {
                try {
                    if (getPipeline().getWorkers().isEmpty()) {
                        logger.error("no more workers left to receive, aborting feed");
                        return;
                    }
                    TitelRecordRequest titelRecordRequest = new TitelRecordRequest().set(new TitleRecord(hit.getSource()));
                    getPipeline().putQueue(titelRecordRequest);
                } catch (Throwable e) {
                    logger.error("error passing data to workers, exiting", e);
                    logger.error(ExceptionFormatter.format(e));
                    failure = true;
                    break;
                }
            }
            searchResponse = search.client()
                    .prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                    .execute().actionGet();
        } while (!failure && searchResponse.getHits().getHits().length > 0);
        logger.info("all title records processed");
        /*skipped.removeAll(indexed);
        logger.info("skipped: {}", skipped.size());

        // log skipped IDs file for analysis
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String filename = String.format("notindexed-%04d%02d%02d-1.txt", year, month + 1, day);
        try {
            FileWriter w = new FileWriter(filename);
            for (String s : skipped) {
                w.append(s).append("\n");
            }
            w.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }*/
        // post process, try to index the skipped IDs
        /*for (String skippedIdentifier : skipped) {
            searchRequest = client.prepareSearch()
                    .setIndices(sourceTitleIndex)
                    .setQuery(termQuery("IdentifierZDB.identifierZDB", skippedIdentifier));
            if (sourceTitleType != null) {
                searchRequest.setTypes(sourceTitleType);
            }
            searchResponse = searchRequest.execute().actionGet();
            logger.debug("hits={}", searchResponse.getHits().getTotalHits());
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                continue;
            }
            for (SearchHit hit : hits) {
                try {
                    if (canReceive() == 0L) {
                        logger.error("no more pipelines left to receive, aborting");
                        return this;
                    }
                    TitleRecord titleRecord = new TitleRecord(hit.getSource());
                    getQueue().offer(new TitelRecordPipelineElement().set(titleRecord).setCheck(false));
                } catch (Throwable e) {
                    logger.error("error passing data to merge pipelines, exiting", e);
                    logger.error(ExceptionFormatter.format(e));
                    break;
                }
            }
        }*/

        //skipped.removeAll(indexed);
        /*logger.info("after indexing skipped: skipped = {}", skipped.size());
        filename = String.format("notindexed-%04d%02d%02d-2.txt", year, month + 1, day);
        try {
            FileWriter w = new FileWriter(filename);
            for (String s : skipped) {
                w.append(s).append("\n");
            }
            w.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }*/
        //scheduledExecutorService.shutdownNow();
    }

    @Override
    protected void prepareResources() throws Exception {
        super.prepareResources();
        this.size = settings.getAsInt("scrollsize", 10);
        this.millis = settings.getAsTime("scrolltimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(3600)).millis();
        this.identifier = settings.get("identifier");

        /*this.sourceTitleIndex = settings.get("bib-index");
        if (Strings.isNullOrEmpty(sourceTitleIndex)) {
            throw new IllegalArgumentException("no bib-index parameter given");
        }

        String index = settings.get("index");
        String packageName = getClass().getPackage().getName().replace('.','/');
        String indexSettingsLocation = settings.get("index-settings",
                "classpath:" + packageName + "/settings.json");
        logger.info("using index settings from {}", indexSettingsLocation);
        URL indexSettingsUrl = new URL(indexSettingsLocation);
        logger.info("creating index {}", index);
        ingest.newIndex(index, org.elasticsearch.common.settings.Settings.settingsBuilder()
                .loadFromStream(indexSettingsUrl.toString(), indexSettingsUrl.openStream()).build(), null);

        // add mappings
        String indexMappingsLocation = settings.get("index-mapping",
                "classpath:" + packageName + "/mapping.json");
        logger.info("using index mappings from {}", indexMappingsLocation);
        URL indexMappingsUrl = new URL(indexMappingsLocation);
        Map<String,Object> indexMappings = Settings.settingsBuilder()
                .loadFromUrl(indexMappingsUrl).build().getAsStructuredMap();
        if (indexMappings != null) {
            for (Map.Entry<String,Object> me : indexMappings.entrySet()) {
                String type = me.getKey();
                Map<String, Object> mapping = (Map<String, Object>) me.getValue();
                logger.info("creating mapping for type {}: {}", type, mapping);
                if (mapping != null) {
                    ingest.newMapping(index, me.getKey(), mapping);
                } else {
                    logger.warn("no mapping found for {}", type);
                }
            }
        }
        ingest.waitForCluster("YELLOW", TimeValue.timeValueSeconds(30));
        ingest.startBulk(index, -1, 1);*/
    }

    protected void disposeRequests() throws IOException {
        super.disposeRequests();
    }

    protected void disposeResources() throws IOException {
        super.disposeResources();
    }

    public boolean findOpenAccess(String index, String issn) {
        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(search.client(), SearchAction.INSTANCE);
        searchRequestBuilder
                .setSize(0)
                .setIndices(index)
                .setQuery(termQuery("dc:identifier", issn));
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        logger.debug("open access query {} ", searchRequestBuilder, searchResponse.getHits().getTotalHits());
        queryMetric.mark();
        return searchResponse.getHits().getTotalHits() > 0;
    }

    public SearchTransportClient search() {
        return search;
    }

    public Ingest ingest() {
        return ingest;
    }

    public Settings settings() {
        return settings;
    }

    public int size() {
        return size;
    }

    public long millis() {
        return millis;
    }

    public BibdatLookup bibdatLookup() {
        return bibdatLookup;
    }

    public ConsortiaLookup consortiaLookup() {
        return consortiaLookup;
    }

    public BlackListedISIL blackListedISIL() {
        return isilbl;
    }

    public MappedISIL mappedISIL() {
        return isilMapped;
    }

    public StatusCodeMapper statusCodeMapper() {
        return statusCodeMapper;
    }

}
