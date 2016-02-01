package org.xbib.tools.feed.elasticsearch.marc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilders;
import org.xbib.etl.marc.MARCEntityBuilderState;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.etl.support.ValueMaps;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.FileInput;
import org.xbib.tools.output.IndexDefinition;

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
import java.util.stream.Collectors;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer tool for MARC bibliographic data
 */
public abstract class BibliographicFeeder extends Feeder {

    private final static Logger logger = LogManager.getLogger(BibliographicFeeder.class);

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
        try (InputStream in = FileInput.getInputStream(uri)) {
            logger.info("start of processing {}", uri);
            process(in, queue);
            logger.info("end of processing {}", uri);
        }
        queue.close();
        if (settings.getAsBoolean("detect-unknown", false)) {
            logger.info("unknown keys={}", unmapped);
        }
    }

    @Override
    protected void performIndexSwitch() throws IOException {
        if (settings.getAsBoolean("mock", false)) {
            logger.warn("not doing alias when mock is active");
            return;
        }
        if (!settings.getAsBoolean("aliases", true)) {
            logger.warn("not doing alias settings because of configuration");
            return;
        }
        IndexDefinition def = indexDefinitionMap.get("bib");
        if (def == null ||  def.getTimeWindow() == null) {
            logger.warn("not doing index switch when index is not time windowed");
            return;
        }
        String identifier = settings.get("catalogid");
        List<String> aliases = new LinkedList<>();
        if (identifier != null) {
            aliases.add(identifier);
            if ("DE-605".equals(identifier)) {
                // only for DE-605, add special "sigel list" as aliases
                try {
                    ValueMaps valueMaps = new ValueMaps();
                    Map<String, String> sigel2isil = valueMaps.getAssocStringMap(settings.get("sigel2isil",
                            "org/xbib/analyzer/mab/sigel2isil.json"), "sigel2isil");
                    // only one (or none) hyphen = "main ISIL"
                    aliases.addAll(sigel2isil.values().stream()
                            .filter(isil -> isil.indexOf("-") == isil.lastIndexOf("-")).collect(Collectors.toList()));
                } catch (Exception e) {
                    logger.warn("error, sigel2isil not used for aliases");
                }
                ingest.switchAliases(def.getIndex(), def.getConcreteIndex(), aliases,
                        (builder, index1, alias) -> builder.addAlias(index1, alias, QueryBuilders.termsQuery("xbib.identifier", alias)));
            } else {
                ingest.switchAliases(def.getIndex(), def.getConcreteIndex(), aliases);
            }
            elasticsearchOutput.retention(ingest, def);
        } else {
            ingest.switchAliases(def.getIndex(), def.getConcreteIndex(), aliases);
            elasticsearchOutput.retention(ingest, def);
        }
    }

    protected MARCEntityQueue createQueue(Map<String,Object> params) {
        return new BibQueue(params);
    }

    protected abstract void process(InputStream in, MARCEntityQueue queue) throws IOException;

    class BibQueue extends MARCEntityQueue {

        public BibQueue(Map<String,Object> params) {
            super(settings.get("package", "org.xbib.analyzer.marc.bib"),
                    params,
                    settings.getAsInt("pipelines", 1),
                    settings.get("elements",  "/org/xbib/analyzer/marc/bib.json")
            );
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(indexDefinitionMap.get("bib").getConcreteIndex(),
                    indexDefinitionMap.get("bib").getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getRecordNumber(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            if (settings.get("collection") != null) {
                state.getResource().add("collection", settings.get("collection"));
            }
            builder.receive(state.getResource());
            getMetric().mark();
            if (indexDefinitionMap.get("bib").isMock()) {
                logger.debug("{}", builder.string());
            }
        }
    }
}
