package org.xbib.tools.marc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.unit.ByteSizeValue;
import org.xbib.etl.marc.MARCEntityBuilderState;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.feed.elasticsearch.TimewindowFeeder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Elasticsearch indexer tool for MARC holdings entity queues
 */
public abstract class HoldingsFeeder extends TimewindowFeeder {

    private final static Logger logger = LogManager.getLogger(HoldingsFeeder.class);

    @Override
    protected String getIndexParameterName() {
        return "hol-index";
    }

    @Override
    protected String getIndexTypeParameterName() {
        return "hol-type";
    }

    @Override
    protected String getIndexSettingsSpec() {
        return  "classpath:org/xbib/tools/feed/elasticsearch/marc/hol-settings.json";
    }

    @Override
    protected String getIndexMappingsSpec() {
        return "classpath:org/xbib/tools/feed/elasticsearch/marc/hol-mapping.json";
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

    protected MARCEntityQueue createQueue(Map<String,Object> params) {
        return new MyQueue(params);
    }

    protected abstract void process(InputStream in, MARCEntityQueue queue) throws IOException;

    class MyQueue extends MARCEntityQueue {

        public MyQueue(Map<String,Object> params) {
            super(settings.get("package", "org.xbib.analyzer.marc.hol"),
                    params,
                    settings.getAsInt("pipelines", 1),
                    settings.get("elements")
            );
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            // write resource
            RouteRdfXContentParams params = new RouteRdfXContentParams(
                    getConcreteIndex(), getType());
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
