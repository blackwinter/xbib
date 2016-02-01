package org.xbib.tools.feed.elasticsearch.marc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.marc.MARCEntityBuilderState;
import org.xbib.etl.marc.MARCEntityQueue;
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

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer tool for MARC holdings entity queues
 */
public abstract class HoldingsFeeder extends Feeder {

    private final static Logger logger = LogManager.getLogger(HoldingsFeeder.class);

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
        IndexDefinition def = indexDefinitionMap.get("hol");
        if (def == null ||  def.getTimeWindow() == null) {
            logger.warn("not doing index switch when index is not time windowed");
            return;
        }
        List<String> aliases = new LinkedList<>();
        ingest.switchAliases(def.getIndex(), def.getConcreteIndex(), aliases);
        elasticsearchOutput.retention(ingest, def);
    }

    protected MARCEntityQueue createQueue(Map<String,Object> params) {
        return new HolQueue(params);
    }

    protected abstract void process(InputStream in, MARCEntityQueue queue) throws IOException;

    class HolQueue extends MARCEntityQueue {

        public HolQueue(Map<String,Object> params) {
            super(settings.get("package", "org.xbib.analyzer.marc.hol"),
                    params,
                    settings.getAsInt("pipelines", 1),
                    settings.get("elements",  "/org/xbib/analyzer/marc/hol.json")
            );
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(indexDefinitionMap.get("hol").getConcreteIndex(),
                    indexDefinitionMap.get("hol").getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getRecordNumber(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            if (settings.get("collection") != null) {
                state.getResource().add("collection", settings.get("collection"));
            }
            builder.receive(state.getResource());
            getMetric().mark();
            if (indexDefinitionMap.get("hol").isMock()) {
                logger.debug("{}", builder.string());
            }
        }
    }
}
