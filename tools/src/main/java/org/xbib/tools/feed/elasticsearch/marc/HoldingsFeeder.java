package org.xbib.tools.feed.elasticsearch.marc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.marc.MARCDirectQueue;
import org.xbib.etl.marc.MARCEntityBuilderState;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchangeStream;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.FileInput;
import org.xbib.util.IndexDefinition;
import org.xbib.util.MockIndexDefinition;
import org.xbib.util.concurrent.WorkerProvider;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
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
public class HoldingsFeeder extends Feeder {

    private final static Logger logger = LogManager.getLogger(HoldingsFeeder.class);

    @Override
    @SuppressWarnings("unchecked")
    protected WorkerProvider<Converter> provider() {
        return p -> new HoldingsFeeder().setPipeline(p);
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
        final URL path = findURL(settings.get("elements",  "/org/xbib/analyzer/marc/hol.json"));
        final MARCEntityQueue queue = settings.getAsBoolean("direct", false) ?
                createDirectQueue() : createQueue(params, path);
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

    protected MARCEntityQueue createQueue(Map<String,Object> params, URL path) throws Exception {
        return new HolQueue(params, path);
    }

    protected MARCEntityQueue createDirectQueue() throws Exception {
        return new DirectQueue();
    }

    protected void process(InputStream in, MARCEntityQueue queue) throws IOException {
        final MarcXchangeStream marcXchangeStream = new MarcXchangeStream()
                .setStringTransformer(value ->
                        Normalizer.normalize(new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8),
                                Normalizer.Form.NFKC))
                .add(queue);
        try {
            InputStreamReader r = new InputStreamReader(in, StandardCharsets.ISO_8859_1);
            final Iso2709Reader reader = new Iso2709Reader(r)
                    .setMarcXchangeListener("Holdings", marcXchangeStream);
            reader.setProperty(Iso2709Reader.FORMAT, "MARC21");
            reader.setProperty(Iso2709Reader.TYPE, "Holdings");
            reader.setProperty(Iso2709Reader.FATAL_ERRORS, false);
            reader.parse();
            r.close();
        } catch (SAXNotSupportedException | SAXNotRecognizedException e) {
            throw new IOException(e);
        }
    }

    private class HolQueue extends MARCEntityQueue {

        HolQueue(Map<String,Object> params, URL path) throws Exception {
            super(settings.get("package", "org.xbib.analyzer.marc.hol"),
                    params,
                    settings.getAsInt("pipelines", 1),
                    path
            );
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            IndexDefinition indexDefinition = indexDefinitionMap.get("hol");
            if (indexDefinition == null) {
                throw new IOException("no 'hol' index definition configured");
            }
            RouteRdfXContentParams params = new RouteRdfXContentParams(indexDefinition.getConcreteIndex(),
                    indexDefinition.getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getRecordIdentifier(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            if (settings.get("collection") != null) {
                state.getResource().add("collection", settings.get("collection"));
            }
            builder.receive(state.getResource());
            getMetric().mark();
            if (indexDefinition.isMock()) {
                logger.debug("{}", builder.string());
            }
        }
    }

    private class DirectQueue extends MARCDirectQueue {

        private DirectQueue() throws Exception {
            super(settings.get("elements",  "/org/xbib/analyzer/marc/hol.json"),
                    settings.getAsInt("pipelines", 1)
            );
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            IndexDefinition indexDefinition = indexDefinitionMap.get("hol");
            if (indexDefinition == null) {
                indexDefinition = new MockIndexDefinition();
            }
            RouteRdfXContentParams params = new RouteRdfXContentParams(indexDefinition.getConcreteIndex(),
                    indexDefinition.getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getRecordIdentifier(), content));
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
