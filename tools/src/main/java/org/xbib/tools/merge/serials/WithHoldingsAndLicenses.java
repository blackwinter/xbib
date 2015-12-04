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
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.Ingest;
import org.xbib.elasticsearch.helper.client.LongAdderIngestMetric;
import org.xbib.elasticsearch.helper.client.ingest.IngestTransportClient;
import org.xbib.elasticsearch.helper.client.mock.MockTransportClient;
import org.xbib.elasticsearch.helper.client.search.SearchClient;
import org.xbib.elasticsearch.helper.client.transport.BulkTransportClient;
import org.xbib.etl.support.ClasspathURLStreamHandler;
import org.xbib.etl.support.StatusCodeMapper;
import org.xbib.etl.support.ValueMaps;
import org.xbib.metric.MeterMetric;
import org.xbib.tools.Bootstrap;
import org.xbib.tools.merge.serials.support.BibdatLookup;
import org.xbib.tools.merge.serials.support.BlackListedISIL;
import org.xbib.tools.merge.serials.entities.TitleRecord;
import org.xbib.tools.merge.serials.support.ConsortiaLookup;
import org.xbib.tools.merge.serials.support.MappedISIL;
import org.xbib.util.DateUtil;
import org.xbib.util.ExceptionFormatter;
import org.xbib.util.Strings;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.xbib.common.settings.Settings.settingsBuilder;

/**
 * Merge ZDB title and holdings and EZB licenses
 */
public class WithHoldingsAndLicenses
        extends ForkJoinPipeline<WithHoldingsAndLicensesWorker, TitelRecordRequest>
        implements Bootstrap {

    private final static Logger logger = LogManager.getLogger(WithHoldingsAndLicenses.class.getSimpleName());

    private WithHoldingsAndLicenses service;

    private Client client;

    private Ingest ingest;

    private String sourceTitleIndex;

    private String sourceTitleType;

    private int size;

    private long millis;

    private long total;

    private long count;

    private String identifier;

    private static MeterMetric queryMetric;

    private static MeterMetric indexMetric;

    private Settings settings;

    private BibdatLookup bibdatLookup;

    private ConsortiaLookup consortiaLookup;

    private BlackListedISIL isilbl;

    private MappedISIL isilMapped;

    private StatusCodeMapper statusCodeMapper;

    @Override
    public void bootstrap(Reader reader, Writer writer) throws Exception {
        settings = settingsBuilder().loadFromReader(reader).build();
        logger.info("run starts");
        this.sourceTitleIndex = settings.get("bib-index");

        if (Strings.isNullOrEmpty(sourceTitleIndex)) {
            throw new IllegalArgumentException("no bib-index parameter given");
        }
        this.sourceTitleType = settings.get("bib-type");

        SearchClient search = new SearchClient().newClient(ImmutableSettings.settingsBuilder()
                        .put("cluster.name", settings.get("elasticsearch.cluster"))
                        .put("host", settings.get("elasticsearch.host"))
                        .put("port", settings.getAsInt("elasticsearch.port", 9300))
                        .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                        .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                        .build()
        );
        this.service = this;
        this.client = search.client();
        this.size = settings.getAsInt("scrollsize", 10);
        this.millis = settings.getAsTime("scrolltimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(60)).millis();
        this.identifier = settings.get("identifier");

        this.ingest = settings.getAsBoolean("mock", false) ?
                new MockTransportClient() :
                "ingest".equals(settings.get("client")) ?
                        new IngestTransportClient() :
                        new BulkTransportClient();
        ingest.maxActionsPerRequest(settings.getAsInt("maxbulkactions", 1000))
                .maxConcurrentRequests(settings.getAsInt("maxConcurrentbulkrequests", Runtime.getRuntime().availableProcessors()));
        ingest.init(ImmutableSettings.settingsBuilder()
                .put("cluster.name", settings.get("elasticsearch.cluster"))
                .put("host", settings.get("elasticsearch.host"))
                .put("port", settings.getAsInt("elasticsearch.port", 9300))
                .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                .put("transport.sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                .put("transport.ping_timeout", TimeValue.timeValueSeconds(60))
                .put("transport.nodes_sampler_interval", TimeValue.timeValueSeconds(60))
                .build(), new LongAdderIngestMetric());

        String index = settings.get("index");
        try {
            String packageName = getClass().getPackage().getName().replace('.','/');
            String indexSettingsLocation = settings.get("index-settings",
                    "classpath:" + packageName + "/settings.json");
            logger.info("using index settings from {}", indexSettingsLocation);
            URL indexSettingsUrl = (indexSettingsLocation.startsWith("classpath:") ?
                    new URL(null, indexSettingsLocation, new ClasspathURLStreamHandler()) :
                    new URL(indexSettingsLocation));
            org.elasticsearch.common.settings.Settings indexSettings = ImmutableSettings.settingsBuilder()
                    .loadFromUrl(indexSettingsUrl).build();
            logger.info("creating index {}", index);
            ingest.newIndex(index, indexSettings, null);

            // add mappings
            String indexMappingsLocation = settings.get("index-mapping",
                    "classpath:" + packageName + "/mapping.json");
            logger.info("using index mappings from {}", indexMappingsLocation);
            URL indexMappingsUrl = (indexMappingsLocation.startsWith("classpath:") ?
                    new URL(null, indexMappingsLocation, new ClasspathURLStreamHandler()) :
                    new URL(indexMappingsLocation));
            Map<String,Object> indexMappings = ImmutableSettings.settingsBuilder()
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
        } catch (Exception e) {
            if (!settings.getAsBoolean("ignoreindexcreationerror", false)) {
                throw e;
            } else {
                logger.warn("index creation error, but configured to ignore", e);
            }
        }
        ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        ingest.startBulk(index);

        queryMetric = new MeterMetric(5L, TimeUnit.SECONDS);
        indexMetric = new MeterMetric(5L, TimeUnit.SECONDS);

        super.setWorkerProvider(new WorkerProvider<WithHoldingsAndLicensesWorker>() {
            int i = 0;

            @Override
            public WithHoldingsAndLicensesWorker get(Pipeline pipeline) {
                WithHoldingsAndLicensesWorker w = new WithHoldingsAndLicensesWorker(service, i++);
                //worker.setQueue(getQueue());
                return w;
            }
        });
        super.setConcurrency(settings.getAsInt("concurrency", 1));

        prepare();

        // here we do the work!
        try {
            execute();
            // poison elements
            waitFor(new TitelRecordRequest());
        } finally {
            logger.info("shutdown in progress");
            shutdown();
            logger.info("query: started {}, ended {}, took {}, count = {}",
                    DateUtil.formatDateISO(queryMetric.startedAt()),
                    DateUtil.formatDateISO(queryMetric.stoppedAt()),
                    TimeValue.timeValueMillis(queryMetric.elapsed() / 1000000).format(),
                    queryMetric.count());
            logger.info("index: started {}, ended {}, took {}, count = {}",
                    DateUtil.formatDateISO(indexMetric.startedAt()),
                    DateUtil.formatDateISO(indexMetric.stoppedAt()),
                    TimeValue.timeValueMillis(indexMetric.elapsed() / 1000000).format(),
                    indexMetric.count());

            logger.info("ingest shutdown in progress");
            ingest.flushIngest();
            ingest.waitForResponses(TimeValue.timeValueSeconds(60));
            ingest.shutdown();

            logger.info("search shutdown in progress");
            search.shutdown();

            logger.info("run complete");

        }
    }

    @Override
    public WithHoldingsAndLicenses prepare() {
        super.prepare();

        logger.info("preparing bibdat lookup...");
        bibdatLookup = new BibdatLookup();
        try {
            bibdatLookup.buildLookup(client, settings.get("index-bibdat", "bibdat"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("bibdat prepared, {} names, {} organizations, {} regions, {} other",
                bibdatLookup.lookupName().size(),
                bibdatLookup.lookupOrganization().size(),
                bibdatLookup.lookupRegion().size(),
                bibdatLookup.lookupOther().size());

        logger.info("preparing ISIL blacklist...");
        isilbl = new BlackListedISIL();
        try {
            isilbl.buildLookup(getClass().getResourceAsStream("isil.blacklist"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("ISIL blacklist prepared, size = {}", isilbl.lookup().size());

        logger.info("preparing mapped ISIL...");
        isilMapped = new MappedISIL();
        try {
            isilMapped.buildLookup(getClass().getResourceAsStream("isil.map"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("mapped ISILs prepared, size = {}", isilMapped.lookup().size());

        logger.info("preparing status code mapper...");
        Map<String,Object> statuscodes = ValueMaps.getMap(getClass().getClassLoader(),
                "org/xbib/analyzer/mab/status.json", "status");
        statusCodeMapper = new StatusCodeMapper();
        statusCodeMapper.add(statuscodes);
        logger.info("status code mapper prepared");

        // prepare "national license" / consortia ISIL expansion
        consortiaLookup = new ConsortiaLookup();
        try {
            consortiaLookup.buildLookup(client, settings.get("index-consortia", "nlzisil"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    @Override
    public WithHoldingsAndLicenses execute() {
        super.execute();
        logger.debug("executing");
        // enter loop over all title records
        boolean failure = false;
        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setSize(size)
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(millis));
        searchRequest.setIndices(sourceTitleIndex);
        if (sourceTitleType != null) {
            searchRequest.setTypes(sourceTitleType);
        }
        // single identifier?
        if (identifier != null) {
            searchRequest.setQuery(termQuery("IdentifierZDB.identifierZDB", identifier));
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        total = searchResponse.getHits().getTotalHits();
        count = 0L;
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        ScheduleThread scheduleThread = new ScheduleThread();
        scheduledExecutorService.scheduleAtFixedRate(scheduleThread, 0, 10, TimeUnit.SECONDS);
        //logger.debug("hits={}", searchResponse.getHits().getTotalHits());
        while (!failure && searchResponse.getScrollId() != null) {
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                    .execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                try {
                    if (canReceive() == 0L) {
                        logger.error("no more workers left to receive, aborting feed");
                        return this;
                    }
                    TitleRecord titleRecord = new TitleRecord(hit.getSource());
                    getQueue().put(new TitelRecordRequest().set(titleRecord));
                    count++;
                } catch (Throwable e) {
                    logger.error("error passing data to merge workers, exiting", e);
                    logger.error(ExceptionFormatter.format(e));
                    failure = true;
                    break;
                }
            }
        }
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
        scheduledExecutorService.shutdownNow();
        return this;
    }

    public boolean findOpenAccess(String issn) {
        CountRequestBuilder countRequestBuilder = client.prepareCount()
                .setIndices(settings.get("doaj-index", "doaj"))
                .setQuery(termQuery("dc:identifier", issn));
        CountResponse countResponse = countRequestBuilder.execute().actionGet();
        return countResponse.getCount() > 0;
    }

    public Client client() {
        return client;
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

    public MeterMetric queryMetric() {
        return queryMetric;
    }

    public MeterMetric indexMetric() {
        return indexMetric;
    }

    class ScheduleThread implements Runnable {

        public void run() {
            long percent = count * 100 / total;
            logger.info("=====> {}/{} = {}%, workers={}",
                    count,
                    total,
                    percent,
                    canReceive());
            logger.info("=====> query metric={} ({} {} {})",
                    queryMetric.meanRate(),
                    queryMetric.oneMinuteRate(),
                    queryMetric.fiveMinuteRate(),
                    queryMetric.fifteenMinuteRate());
            logger.info("=====> index metric={} ({} {} {})",
                    indexMetric.meanRate(),
                    indexMetric.oneMinuteRate(),
                    indexMetric.fiveMinuteRate(),
                    indexMetric.fifteenMinuteRate());
        }
    }

}
