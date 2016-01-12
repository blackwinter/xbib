package org.xbib.tools.feed.elasticsearch.mab;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesAction;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesAction;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.xbib.common.settings.Settings;
import org.xbib.common.unit.ByteSizeValue;
import org.xbib.elasticsearch.helper.client.LongAdderIngestMetric;
import org.xbib.etl.marc.dialects.mab.MABEntityBuilderState;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.etl.support.ClasspathURLStreamHandler;
import org.xbib.etl.support.ValueMaps;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.URIWorkerRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer tool for title/holdings MAB entity queues
 */
public abstract class TitleHoldingsFeeder extends Feeder {

    private final static Logger logger = LogManager.getLogger(TitleHoldingsFeeder.class.getSimpleName());

    private String index;

    private String concreteIndex;

    private String holdingsIndex;

    private String concreteHoldingsIndex;

    protected void setIndex(String index) {
        this.index = index;
    }

    @Override
    protected String getIndex() {
        return index;
    }

    protected void setConcreteIndex(String concreteIndex) {
        this.concreteIndex = concreteIndex;
    }

    @Override
    protected String getConcreteIndex() {
        return concreteIndex;
    }

    protected void setHoldingsIndex(String holdingsIndex) {
        this.holdingsIndex = holdingsIndex;
    }

    protected String getHoldingsIndex() {
        return holdingsIndex;
    }

    protected void setConcreteHoldingsIndex(String concreteHoldingsIndex) {
        this.concreteHoldingsIndex = concreteHoldingsIndex;
    }

    protected String getConcreteHoldingsIndex() {
        return concreteHoldingsIndex;
    }

    @Override
    protected String getType() {
        return settings.get("title-type");
    }
    protected String getHoldingsType() {
        return settings.get("holdings-type");
    }

    @Override
    protected ForkJoinPipeline<Converter, URIWorkerRequest> newPipeline() {
        return new TitleHoldingsFeederPipeline();
    }

    class TitleHoldingsFeederPipeline extends ForkJoinPipeline<Converter, URIWorkerRequest> {
        public String getIndex() {
            return index;
        }
        public String getConcreteIndex() {
            return concreteIndex;
        }
        public String getHoldingsIndex() {
            return holdingsIndex;
        }
        public String getConcreteHoldingsIndex() {
            return concreteHoldingsIndex;
        }
    }

    @Override
    public TitleHoldingsFeeder setPipeline(Pipeline<Converter,URIWorkerRequest> pipeline) {
        super.setPipeline(pipeline);
        if (pipeline instanceof TitleHoldingsFeederPipeline) {
            TitleHoldingsFeederPipeline configuredPipeline = (TitleHoldingsFeederPipeline) pipeline;
            setIndex(configuredPipeline.getIndex());
            setConcreteIndex(configuredPipeline.getConcreteIndex());
            setHoldingsIndex(configuredPipeline.getHoldingsIndex());
            setConcreteHoldingsIndex(configuredPipeline.getConcreteHoldingsIndex());
        }
        return this;
    }

    @Override
    public void prepareSink() throws IOException {
        setIndex(settings.get("title-index"));
        setHoldingsIndex(settings.get("holdings-index"));
        ingest = createIngest();
        Integer maxbulkactions = settings.getAsInt("maxbulkactions", 1000);
        Integer maxconcurrentbulkrequests = settings.getAsInt("maxconcurrentbulkrequests",
                Runtime.getRuntime().availableProcessors());
        ingest.maxActionsPerRequest(maxbulkactions)
                .maxConcurrentRequests(maxconcurrentbulkrequests);
        ingest.init(Settings.settingsBuilder()
                .put("cluster.name", settings.get("elasticsearch.cluster"))
                .put("host", settings.get("elasticsearch.host"))
                .put("port", settings.getAsInt("elasticsearch.port", 9300))
                .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                .build().getAsMap(), new LongAdderIngestMetric());
        String timeWindow = settings.get("timewindow") != null ?
                DateTimeFormat.forPattern(settings.get("timewindow")).print(new DateTime()) : "";
        setConcreteIndex(resolveAlias(getIndex() + timeWindow));
        setConcreteHoldingsIndex(resolveAlias(getHoldingsIndex() + timeWindow));
        logger.info("base index name = {}, concrete index name = {}, concrete holdings index name",
                getIndex(), getConcreteIndex(), getConcreteHoldingsIndex());
        super.prepareSink();
    }

    @Override
    protected TitleHoldingsFeeder createIndex(String index, String concreteIndex) throws IOException {
        if (ingest.client() == null) {
            return this;
        }
        ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        if (settings.getAsBoolean("onlyalias", false)) {
            updateAliases();
            return this;
        }
        try {
            String indexSettings = settings.get("title-index-settings",
                    "classpath:org/xbib/tools/feed/elasticsearch/mab/title-settings.json");
            InputStream indexSettingsInput = (indexSettings.startsWith("classpath:") ?
                    new URL(null, indexSettings, new ClasspathURLStreamHandler()) :
                    new URL(indexSettings)).openStream();
            String indexMappings = settings.get("title-index-mapping",
                    "classpath:org/xbib/tools/feed/elasticsearch/mab/title-mapping.json");
            InputStream indexMappingsInput = (indexMappings.startsWith("classpath:") ?
                    new URL(null, indexMappings, new ClasspathURLStreamHandler()) :
                    new URL(indexMappings)).openStream();
            ingest.newIndex(getConcreteIndex(), getType(), indexSettingsInput, indexMappingsInput);
            indexSettingsInput.close();
            indexMappingsInput.close();
            String indexHoldingsSettings = settings.get("holdings-index-settings",
                    "classpath:org/xbib/tools/feed/elasticsearch/mab/holdings-settings.json");
            InputStream indexHoldingsSettingsInput = (indexHoldingsSettings.startsWith("classpath:") ?
                    new URL(null, indexHoldingsSettings, new ClasspathURLStreamHandler()) :
                    new URL(indexHoldingsSettings)).openStream();
            String indexHoldingsMappings = settings.get("holdings-index-mapping",
                    "classpath:org/xbib/tools/feed/elasticsearch/mab/holdings-mapping.json");
            InputStream indexHoldingsMappingsInput = (indexHoldingsMappings.startsWith("classpath:") ?
                    new URL(null, indexHoldingsMappings, new ClasspathURLStreamHandler()) :
                    new URL(indexHoldingsMappings)).openStream();
            ingest.newIndex(getConcreteHoldingsIndex(), getHoldingsType(), indexHoldingsSettingsInput, indexHoldingsMappingsInput);
            indexHoldingsSettingsInput.close();
            indexHoldingsMappingsInput.close();

        } catch (Exception e) {
            if (!settings.getAsBoolean("ignoreindexcreationerror", false)) {
                throw e;
            } else {
                logger.warn("index creation error, but configured to ignore", e);
            }
        }
        ingest.startBulk(getConcreteIndex(), -1L, 1L);
        ingest.startBulk(getConcreteHoldingsIndex(), -1L, 1L);
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        if (settings.getAsBoolean("onlyalias", false)) {
            return;
        }
        // set identifier prefix (ISIL)
        Map<String,Object> params = new HashMap<>();
        params.put("identifier", settings.get("identifier", "DE-605"));
        params.put("_prefix", "(" + settings.get("identifier", "DE-605") + ")");
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        final MABEntityQueue queue = createQueue(params);
        for (String key : settings.getAsMap().keySet()) {
            if (key.startsWith("taxonomy-")) {
                String isil = key.substring("taxonomy-".length());
                queue.addClassifier("(" + settings.get("identifier", "DE-605") + ")", isil, settings.get(key));
            }
        }
        queue.setUnmappedKeyListener((id,key) -> {
                    if ((settings.getAsBoolean("detect-unknown", false))) {
                        logger.warn("record {} unmapped field {}", id, key);
                        unmapped.add("\"" + key + "\"");
                    }
                });
        queue.execute();
        String fileName = uri.getSchemeSpecificPart();
        InputStream in = new FileInputStream(fileName);
        ByteSizeValue bufferSize = settings.getAsBytesSize("buffersize", ByteSizeValue.parseBytesSizeValue("1m"));
        if (fileName.endsWith(".gz")) {
            in = bufferSize != null ? new GZIPInputStream(in, bufferSize.bytesAsInt()) : new GZIPInputStream(in);
        }
        process(in, queue);
        queue.close();
        if (settings.getAsBoolean("detect-unknown", false)) {
            logger.info("unknown keys={}", unmapped);
        }
        if (settings.getAsBoolean("aliases", false) && !settings.getAsBoolean("mock", false) && ingest.client() != null) {
            updateAliases();
            ingest.stopBulk(getConcreteIndex());
            ingest.stopBulk(getConcreteHoldingsIndex());
        } else {
            logger.info("not doing alias settings");
        }
    }

    protected String resolveAlias(String alias) {
        GetAliasesRequestBuilder getAliasesRequestBuilder = new GetAliasesRequestBuilder(ingest.client(), GetAliasesAction.INSTANCE);
        GetAliasesResponse getAliasesResponse = getAliasesRequestBuilder.setAliases(alias).execute().actionGet();
        if (!getAliasesResponse.getAliases().isEmpty()) {
            return getAliasesResponse.getAliases().keys().iterator().next().value;
        }
        return alias;
    }

    protected void updateAliases() {
        String titleIndex = getIndex();
        String concreteIndex = getConcreteIndex();
        String holdingsIndex = getHoldingsIndex();
        String concreteHoldingsIndex = getConcreteHoldingsIndex();

        if (!titleIndex.equals(concreteIndex)) {
            IndicesAliasesRequestBuilder requestBuilder = new IndicesAliasesRequestBuilder(ingest.client(), IndicesAliasesAction.INSTANCE);
            GetAliasesRequestBuilder getAliasesRequestBuilder = new GetAliasesRequestBuilder(ingest.client(), GetAliasesAction.INSTANCE);
            GetAliasesResponse getAliasesResponse = getAliasesRequestBuilder.setAliases(titleIndex).execute().actionGet();
            if (getAliasesResponse.getAliases().isEmpty()) {
                logger.info("adding alias {} to index {}", titleIndex, concreteIndex);
                requestBuilder.addAlias(concreteIndex, titleIndex);
                // identifier is alias for tite only
                if (settings.get("identifier") != null) {
                    requestBuilder.addAlias(concreteIndex, settings.get("identifier"));
                }
            } else {
                for (ObjectCursor<String> indexName : getAliasesResponse.getAliases().keys()) {
                    if (indexName.value.startsWith(titleIndex)) {
                        logger.info("switching alias {} from index {} to index {}", titleIndex, indexName.value, concreteIndex);
                        requestBuilder.removeAlias(indexName.value, titleIndex)
                                .addAlias(concreteIndex, titleIndex);
                        if (settings.get("identifier") != null) {
                            requestBuilder.removeAlias(indexName.value, settings.get("identifier"))
                                    .addAlias(concreteIndex, settings.get("identifier"));
                        }
                    }
                }
            }
            // holdings
            getAliasesResponse = getAliasesRequestBuilder.setAliases(holdingsIndex).execute().actionGet();
            if (getAliasesResponse.getAliases().isEmpty()) {
                logger.info("adding alias {} to index {}", holdingsIndex, concreteHoldingsIndex);
                requestBuilder.addAlias(concreteHoldingsIndex, holdingsIndex);
            } else for (ObjectCursor<String> indexName : getAliasesResponse.getAliases().keys()) {
                if (indexName.value.startsWith(holdingsIndex)) {
                    logger.info("switching alias {} from index {} to index {}", holdingsIndex, indexName.value, concreteHoldingsIndex);
                    requestBuilder.removeAlias(indexName.value, holdingsIndex)
                            .addAlias(concreteHoldingsIndex, holdingsIndex);
                }
            }
            requestBuilder.execute().actionGet();
        } else if (settings.get("identifier") != null) {
            // identifier is alias for title
            IndicesAliasesRequestBuilder requestBuilder = new IndicesAliasesRequestBuilder(ingest.client(), IndicesAliasesAction.INSTANCE);
            logger.debug("adding alias {} to index {}",  settings.get("identifier"), titleIndex);
            requestBuilder.addAlias(titleIndex, settings.get("identifier"));
            // identifier is alias fo holdings
            logger.debug("adding alias {} to index {}", settings.get("identifier"), holdingsIndex);
            requestBuilder.addAlias(holdingsIndex, settings.get("identifier"));
            requestBuilder.execute().actionGet();
        }
        if ("DE-605".equals(settings.get("identifier"))) {
            // for union catalog, create aliases for "main ISILs" using xbib.identifier
            Map<String, String> sigel2isil = ValueMaps.getAssocStringMap(getClass().getClassLoader(),
                    settings.get("sigel2isil", "/org/xbib/analyzer/mab/sigel2isil.json"), "sigel2isil");
            final List<String> newAliases = new LinkedList<>();
            final List<String> switchedAliases = new LinkedList<>();
            IndicesAliasesRequestBuilder requestBuilder = new IndicesAliasesRequestBuilder(ingest.client(), IndicesAliasesAction.INSTANCE);
            for (String isil : sigel2isil.values()) {
                // only one (or none) hyphen = "main ISIL"
                if (isil.indexOf("-") == isil.lastIndexOf("-")) {
                    GetAliasesRequestBuilder getAliasesRequestBuilder = new GetAliasesRequestBuilder(ingest.client(), GetAliasesAction.INSTANCE);
                    GetAliasesResponse getAliasesResponse = getAliasesRequestBuilder.setAliases(isil).execute().actionGet();
                    if (getAliasesResponse.getAliases().isEmpty()) {
                        requestBuilder.addAlias(concreteIndex, isil, termsQuery("xbib.identifier", isil));
                        newAliases.add(isil);
                    } else for (ObjectCursor<String> indexName : getAliasesResponse.getAliases().keys()) {
                        if (indexName.value.startsWith(titleIndex)) {
                            requestBuilder.removeAlias(indexName.value, isil)
                                    .addAlias(concreteIndex, isil, termsQuery("xbib.identifier", isil));
                            switchedAliases.add(isil);
                        }
                    }
                }
            }
            if (!newAliases.isEmpty() || !switchedAliases.isEmpty()) {
                requestBuilder.execute().actionGet();
                logger.info("{} new aliases created, {} aliases switched", newAliases.size(), switchedAliases.size());
            } else {
                logger.warn("no new or switched aliases");
            }
        }
    }

    protected MABEntityQueue createQueue(Map<String,Object> params) {
        return new MyQueue(params);
    }

    protected abstract void process(InputStream in, MABEntityQueue queue) throws IOException;

    class MyQueue extends MABEntityQueue {

        public MyQueue(Map<String,Object> params) {
            super(settings.get("package", "org.xbib.analyzer.mab.titel"),
                    params,
                    settings.getAsInt("pipelines", 1),
                    settings.get("elements")
            );
        }

        @Override
        public void afterCompletion(MABEntityBuilderState state) throws IOException {
            int docs = 0;
            // write title resource
            RouteRdfXContentParams params = new RouteRdfXContentParams(
                    getConcreteIndex(), settings.get("title-type"));
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getIdentifier(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            if (settings.get("collection") != null) {
                state.getResource().add("collection", settings.get("collection"));
            }
            builder.receive(state.getResource());
            docs++;
            if (settings.getAsBoolean("mock", false)) {
                logger.info("main id={} {}", state.getResource().id(), builder.string());
            }
            // write related resources (holdings etc.)
            Iterator<Resource> it = state.graph().getResources();
            while (it.hasNext()) {
                Resource resource  = it.next();
                if (resource.equals(state.getResource())) {
                    continue;
                }
                params = new RouteRdfXContentParams(
                        getConcreteHoldingsIndex(), settings.get("holdings-type"));
                params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(),
                        state.getIdentifier() + "." + resource.id(), content));
                builder = routeRdfXContentBuilder(params);
                resource.newResource("xbib").add("uid", state.getIdentifier());
                resource.newResource("xbib").add("uid", state.getRecordIdentifier());
                builder.receive(resource);
                docs++;
            }
        }
    }
}
