package org.xbib.tools.feed.elasticsearch.natliz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.marc.MARCEntityBuilderState;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.etl.marc.MARCDirectQueue;
import org.xbib.tools.convert.Converter;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchangeStream;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.FileInput;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

public class NatLizMarc extends Feeder {

    private final static Logger logger = LogManager.getLogger(NatLizMarc.class);

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new NatLizMarc().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = FileInput.getInputStream(uri)) {
            final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<>());
            // set identifier prefix (ISIL)
            Map<String, Object> params = new HashMap<>();
            params.put("identifier", settings.get("identifier", "NLZ"));
            params.put("_prefix", "(" + settings.get("identifier", "NLZ") + ")");
            final MARCEntityQueue queue = settings.getAsBoolean("direct", false) ?
                    new MyDirectQueue(params) :
                    new MyEntityQueue(params);
            queue.setUnmappedKeyListener((id, key) -> {
                if ((settings.getAsBoolean("detect-unknown", false))) {
                    logger.warn("unmapped field {}", key);
                    unmapped.add("\"" + key + "\"");
                }
            });
            queue.execute();
            final MarcXchangeStream kv = new MarcXchangeStream()
                    .setStringTransformer(value ->
                            Normalizer.normalize(new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8),
                                    Normalizer.Form.NFKC))
                    .add(queue);
            InputStreamReader r = new InputStreamReader(in, StandardCharsets.ISO_8859_1);
            final Iso2709Reader reader = new Iso2709Reader(r)
                    .setMarcXchangeListener(kv);
            reader.setProperty(Iso2709Reader.FORMAT, "MARC21");
            reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
            reader.setProperty(Iso2709Reader.FATAL_ERRORS, false);
            reader.parse();
            r.close();
            queue.close();
            if (settings.getAsBoolean("detect-unknown", false)) {
                logger.info("unknown keys={}", unmapped);
            }
        }
    }

    class MyEntityQueue extends MARCEntityQueue {

        public MyEntityQueue(Map<String,Object> params) throws Exception {
            super(settings.get("package", "org.xbib.analyzer.marc.bib"),
                    params,
                    settings.getAsInt("pipelines", 1),
                    findURL(settings.get("elements"))
            );
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(
                    settings.get("index"), settings.get("type"));
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), p.getId(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(state.getResource());
            if (settings.getAsBoolean("mock", false)) {
                logger.info("{}", params.getGenerator().get());
            }
        }
    }

    class MyDirectQueue extends MARCDirectQueue {

        public MyDirectQueue(Map<String,Object> params) throws Exception {
            super(settings.get("package", "org.xbib.analyzer.marc.bib"),
                    settings.getAsInt("pipelines", 1)
            );
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(
                    settings.get("index"), settings.get("type"));
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), p.getId(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(state.getResource());
            if (settings.getAsBoolean("mock", false)) {
                logger.info("{}", params.getGenerator().get());
            }
        }
    }
}
