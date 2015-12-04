package org.xbib.tools.marc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse;
import org.elasticsearch.common.hppc.cursors.ObjectCursor;
import org.elasticsearch.index.query.FilterBuilders;
import org.xbib.common.unit.ByteSizeValue;
import org.xbib.etl.marc.MARCEntityBuilderState;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.etl.support.ValueMaps;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.TimewindowFeeder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer tool for MARC bibliographic data
 */
public abstract class BibliographicFeeder extends TimewindowFeeder {

    private final static Logger logger = LogManager.getLogger(BibliographicFeeder.class);

    @Override
    protected String getIndexParameterName() {
        return "bib-index";
    }

    @Override
    protected String getIndexTypeParameterName() {
        return "bib-type";
    }

    @Override
    protected String getIndexSettingsSpec() {
        return  "classpath:org/xbib/tools/feed/elasticsearch/marc/bib-settings.json";
    }

    @Override
    protected String getIndexMappingsSpec() {
        return "classpath:org/xbib/tools/feed/elasticsearch/marc/bib-mapping.json";
    }

    @Override
    public void process(URI uri) throws Exception {
        if (settings.getAsBoolean("onlyaliases", false)) {
            return;
        }
        // set identifier prefix (ISIL)
        Map<String,Object> params = new HashMap<>();
        if (settings.containsSetting("catalogid")) {
            params.put("catalogid", settings.get("catalogid"));
            params.put("_prefix", "(" + settings.get("catalogid") + ")");
        }
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<>());
        final MARCEntityQueue queue = createQueue(params);
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
    }

    @Override
    protected void updateAliases(String index, String concreteIndex) {
        super.updateAliases(index, concreteIndex);
        // identifier is alias
        if (settings.get("identifier") != null) {
            IndicesAliasesRequestBuilder requestBuilder = ingest.client().admin().indices().prepareAliases();
            logger.info("adding alias {} to index {}", settings.get("identifier"), getIndex());
            requestBuilder.addAlias(getIndex(), settings.get("identifier"));
            requestBuilder.execute().actionGet();
        }
        // for union catalog, create aliases for "main ISILs" using xbib.identifier
        if ("DE-605".equals(settings.get("identifier"))) {
            Map<String, String> sigel2isil = ValueMaps.getAssocStringMap(getClass().getClassLoader(),
                    settings.get("sigel2isil", "/org/xbib/analyzer/mab/sigel2isil.json"), "sigel2isil");
            final List<String> newAliases = new LinkedList<>();
            final List<String> switchedAliases = new LinkedList<>();
            IndicesAliasesRequestBuilder requestBuilder = ingest.client().admin().indices().prepareAliases();
            for (String isil : sigel2isil.values()) {
                // only one (or none) hyphen = "main ISIL"
                if (isil.indexOf("-") == isil.lastIndexOf("-")) {
                    GetAliasesResponse getAliasesResponse = ingest.client().admin().indices().prepareGetAliases(isil).execute().actionGet();
                    if (getAliasesResponse.getAliases().isEmpty()) {
                        requestBuilder.addAlias(concreteIndex, isil, FilterBuilders.termsFilter("xbib.identifier", isil));
                        newAliases.add(isil);
                    } else for (ObjectCursor<String> indexName : getAliasesResponse.getAliases().keys()) {
                        if (indexName.value.startsWith(getIndex())) {
                            requestBuilder.removeAlias(indexName.value, isil)
                                    .addAlias(concreteIndex, isil, FilterBuilders.termsFilter("xbib.identifier", isil));
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

    protected MARCEntityQueue createQueue(Map<String,Object> params) {
        return new MyQueue(params);
    }

    protected abstract void process(InputStream in, MARCEntityQueue queue) throws IOException;

    class MyQueue extends MARCEntityQueue {

        public MyQueue(Map<String,Object> params) {
            super(settings.get("package", "org.xbib.analyzer.marc.bib"),
                    params,
                    settings.getAsInt("pipelines", 1),
                    settings.get("elements",  "/org/xbib/analyzer/marc/bib.json")
            );
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(getConcreteIndex(), getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getRecordNumber(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            if (settings.get("collection") != null) {
                state.getResource().add("collection", settings.get("collection"));
            }
            builder.receive(state.getResource());
            if (settings.getAsBoolean("mock", false)) {
                logger.debug("{}", builder.string());
            }
        }
    }
}
