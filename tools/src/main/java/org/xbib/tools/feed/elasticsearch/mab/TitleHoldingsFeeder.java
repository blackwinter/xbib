package org.xbib.tools.feed.elasticsearch.mab;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilders;
import org.xbib.etl.marc.dialects.mab.MABDirectQueue;
import org.xbib.etl.marc.dialects.mab.MABEntityBuilderState;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.etl.support.ValueMaps;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.FileInput;
import org.xbib.util.IndexDefinition;
import org.xbib.util.MockIndexDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.xbib.rdf.content.RdfXContentFactory.ntripleBuilder;
import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer tool for title/holdings MAB entity queues
 */
public abstract class TitleHoldingsFeeder extends Feeder {

    private final static Logger logger = LogManager.getLogger(TitleHoldingsFeeder.class.getSimpleName());

    private final static String CATALOG_ID = "catalogid";

    private String catalogId;

    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = FileInput.getInputStream(uri)) {
            // here settings can be passed as parameters to the entity mapper
            catalogId = settings.get(CATALOG_ID);
            Map<String, Object> params = new HashMap<>();
            params.put("catalogid", catalogId);
            params.put("_prefix", "(" + catalogId + ")");
            final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<>());
            final URL path = findURL(settings.get("elements"));
            final MABEntityQueue queue = createQueue(params, path);
            for (Map.Entry<String,List<String>> entry : fileInput.getFileMap().entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("taxonomy-")) {
                    String isil = key.substring("taxonomy-".length());
                    for (String taxFile : entry.getValue()) {
                        logger.info("adding classifier file from {} for ISIL {}", taxFile, isil);
                        queue.addClassifier("(" + catalogId + ")", isil, taxFile);
                    }
                }
            }
            queue.setUnmappedKeyListener((id, key) -> {
                if ((settings.getAsBoolean("detect-unknown", false))) {
                    logger.warn("record {} unmapped field {}", id, key);
                    unmapped.add("\"" + key + "\"");
                }
            });
            queue.execute();
            process(in, queue);
            queue.close();
            if (settings.getAsBoolean("detect-unknown", false)) {
                logger.info("unknown keys={}", unmapped);
            }
        }
    }

    @Override
    protected void performIndexSwitch() throws IOException {
        IndexDefinition def = indexDefinitionMap.get("title");
        if (def == null) {
            return;
        }
        // simple index alias to the value in "identifier"
        if (catalogId != null) {
            elasticsearchOutput.switchIndex(ingest, def, Arrays.asList(def.getIndex(), catalogId));
        }
        // union catalog?
        if ("DE-605".equals(catalogId)) {
            logger.info("special index aliasing for union catalog {}", catalogId);
            // for union catalog, create additional aliases for ISILs using xbib.identifier
            List<String> aliases = new LinkedList<>();
            ValueMaps valueMaps = new ValueMaps();
            Map<String, String> sigel2isil = valueMaps.getAssocStringMap(settings.get("sigel2isil",
                            "org/xbib/analyzer/mab/sigel2isil.json"), "sigel2isil");
            // "main ISIL" = only one (or none) hyphen
            aliases.addAll(sigel2isil.values().stream()
                    .filter(isil -> isil.indexOf("-") == isil.lastIndexOf("-")).collect(Collectors.toList()));
            elasticsearchOutput.switchIndex(ingest, def, aliases,
                    (builder, index1, alias) -> builder.addAlias(index1, alias,
                            QueryBuilders.termsQuery("xbib.identifier", alias)));
        } else {
            logger.info("no special index aliasing");
        }
        elasticsearchOutput.retention(ingest, def);
        // holdings
        def = indexDefinitionMap.get("holdings");
        elasticsearchOutput.switchIndex(ingest, def, Collections.singletonList(def.getIndex()));
        elasticsearchOutput.retention(ingest, def);
    }

    protected MABEntityQueue createQueue(Map<String,Object> params, URL path) throws Exception {
        return settings.getAsBoolean("direct", false) ? new MyDirectMABQueue() : new MyMABQueue(params, path);
    }

    protected abstract void process(InputStream in, MABEntityQueue queue) throws IOException;

    class MyMABQueue extends MABEntityQueue {

        public MyMABQueue(Map<String,Object> params, URL path) throws Exception {
            super(settings.get("package", "org.xbib.analyzer.mab.titel"),
                    params,
                    settings.getAsInt("pipelines", 1),
                    path
            );
        }

        @Override
        public void afterCompletion(MABEntityBuilderState state) throws IOException {
            // write title resource
            IndexDefinition indexDefinition = indexDefinitionMap.get("title");
            if (indexDefinition == null) {
                indexDefinition = new MockIndexDefinition();
            }
            String titleIndex = indexDefinition.getConcreteIndex();
            String titleType = indexDefinition.getType();
            RouteRdfXContentParams params = new RouteRdfXContentParams(titleIndex, titleType);
            params.setHandler((content, p) -> {
                if (ingest != null) {
                    ingest.index(p.getIndex(), p.getType(), state.getIdentifier(), content);
                }
            } ) ;
            if (settings.get("collection") != null) {
                state.getResource().add("collection", settings.get("collection"));
            }
            if (logger.isDebugEnabled()) {
                RdfContentBuilder builder = ntripleBuilder();
                builder.receive(state.getResource());
                logger.info("{}", builder.string());
            }

            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(state.getResource());
            if (settings.getAsBoolean("mock", false)) {
                logger.info("title: id={} builder={}", state.getIdentifier(), builder.string());
            }
            // write holdings resources
            indexDefinition = indexDefinitionMap.get("holdings");
            if (indexDefinition == null) {
                indexDefinition = new MockIndexDefinition();
            }
            String holdingsIndex = indexDefinition.getConcreteIndex();
            String holdingsType = indexDefinition.getType();
            Iterator<Resource> it = state.graph().getResources();
            while (it.hasNext()) {
                Resource resource  = it.next();
                if (resource.equals(state.getResource())) {
                    // skip main resource
                    continue;
                }
                params = new RouteRdfXContentParams(holdingsIndex, holdingsType);
                params.setHandler((content, p) -> {
                    if (ingest != null) {
                        ingest.index(p.getIndex(), p.getType(),
                                state.getIdentifier() + "." + resource.id(), content);
                    }
                });
                builder = routeRdfXContentBuilder(params);
                resource.newResource("xbib").add("uid", state.getIdentifier());
                resource.newResource("xbib").add("uid", state.getRecordIdentifier());
                builder.receive(resource);
                if (settings.getAsBoolean("mock", false)) {
                    logger.info("holdings: id={} builder={}", state.getIdentifier() + "." + resource.id(), builder.string());
                }
            }
        }
    }

    class MyDirectMABQueue extends MABDirectQueue {

        public MyDirectMABQueue() throws Exception {
            super(settings.get("package", "org.xbib.analyzer.mab.titel"),
                    settings.getAsInt("pipelines", 1)
            );
        }

        @Override
        public void afterCompletion(MABEntityBuilderState state) throws IOException {
            IndexDefinition indexDefinition = indexDefinitionMap.get("title");
            if (indexDefinition == null) {
                indexDefinition = new MockIndexDefinition();
            }
            String titleIndex = indexDefinition.getConcreteIndex();
            String titleType = indexDefinition.getType();
            RouteRdfXContentParams params = new RouteRdfXContentParams(titleIndex, titleType);
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), p.getId(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(state.getResource());
        }
    }
}
