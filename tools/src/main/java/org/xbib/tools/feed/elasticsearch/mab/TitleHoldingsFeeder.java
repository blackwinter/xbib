package org.xbib.tools.feed.elasticsearch.mab;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilders;
import org.xbib.etl.marc.dialects.mab.MABEntityBuilderState;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.etl.support.ValueMaps;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.FileInput;
import org.xbib.util.IndexDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer tool for title/holdings MAB entity queues
 */
public abstract class TitleHoldingsFeeder extends Feeder {

    private final static Logger logger = LogManager.getLogger(TitleHoldingsFeeder.class.getSimpleName());

    private final static String CATALOG_ID = "catalogid";

    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = FileInput.getInputStream(uri)) {
            String catalogId = settings.get(CATALOG_ID, "DE-605");
            // set identifier prefix (ISIL)
            Map<String, Object> params = new HashMap<>();
            params.put("catalogid", catalogId);
            params.put("_prefix", "(" + catalogId + ")");
            final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<>());
            final MABEntityQueue queue = createQueue(params);
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
        String catalogId = settings.get(CATALOG_ID, "DE-605");
        // simple index alias to the value in "identifier"
        elasticsearchOutput.switchIndex(ingest, def, Arrays.asList(def.getIndex(), catalogId));
        elasticsearchOutput.retention(ingest, def);
        if ("DE-605".equals(catalogId)) {
            // for union catalog, create additional aliases for "main ISILs" using xbib.identifier
            List<String> aliases = new LinkedList<>();
            ValueMaps valueMaps = new ValueMaps();
            Map<String, String> sigel2isil =
                    valueMaps.getAssocStringMap(settings.get("sigel2isil",
                            "org/xbib/analyzer/mab/sigel2isil.json"), "sigel2isil");
            // "main ISIL" = only one (or none) hyphen
            aliases.addAll(sigel2isil.values().stream()
                    .filter(isil -> isil.indexOf("-") == isil.lastIndexOf("-")).collect(Collectors.toList()));
            elasticsearchOutput.switchIndex(ingest, def, aliases,
                    (builder, index1, alias) -> builder.addAlias(index1, alias,
                            QueryBuilders.termsQuery("xbib.identifier", alias)));
            elasticsearchOutput.retention(ingest, def);
        }
        // holdings
        def = indexDefinitionMap.get("holdings");
        elasticsearchOutput.switchIndex(ingest, def, Collections.singletonList(def.getIndex()));
        elasticsearchOutput.retention(ingest, def);
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
            // write title resource
            String titleIndex = indexDefinitionMap.get("title").getConcreteIndex();
            String titleType = indexDefinitionMap.get("title").getType();
            RouteRdfXContentParams params = new RouteRdfXContentParams(titleIndex, titleType);
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getIdentifier(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            if (settings.get("collection") != null) {
                state.getResource().add("collection", settings.get("collection"));
            }
            builder.receive(state.getResource());
            if (settings.getAsBoolean("mock", false)) {
                logger.info("main id={} {}", state.getResource().id(), builder.string());
            }
            // write holdings resources
            String holdingsIndex = indexDefinitionMap.get("holdings").getConcreteIndex();
            String holdingsType = indexDefinitionMap.get("holdings").getType();
            Iterator<Resource> it = state.graph().getResources();
            while (it.hasNext()) {
                Resource resource  = it.next();
                if (resource.equals(state.getResource())) {
                    // skip main resource
                    continue;
                }
                params = new RouteRdfXContentParams(holdingsIndex, holdingsType);
                params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(),
                        state.getIdentifier() + "." + resource.id(), content));
                builder = routeRdfXContentBuilder(params);
                resource.newResource("xbib").add("uid", state.getIdentifier());
                resource.newResource("xbib").add("uid", state.getRecordIdentifier());
                builder.receive(resource);
            }
        }
    }
}
