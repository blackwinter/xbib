package org.xbib.tools.feed.elasticsearch.natliz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.marc.MARCEntityBuilderState;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.etl.marc.direct.MARCDirectQueue;
import org.xbib.util.InputService;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.Feeder;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

public class NatLizMarc extends Feeder {

    private final static Logger logger = LogManager.getLogger(NatLizMarc.class);

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final static Charset ISO88591 = Charset.forName("ISO-8859-1");

    @Override
    protected WorkerProvider provider() {
        return p -> new NatLizMarc().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        // set identifier prefix (ISIL)
        Map<String,Object> params = new HashMap<>();
        params.put("identifier", settings.get("identifier", "NLZ"));
        params.put("_prefix", "(" + settings.get("identifier", "NLZ") + ")");
        final MARCEntityQueue queue = settings.getAsBoolean("direct", false) ?
                new MyDirectQueue(params) :
                new MyEntityQueue(params) ;
        queue.setUnmappedKeyListener((id,key) -> {
            if ((settings.getAsBoolean("detect-unknown", false))) {
                logger.warn("unmapped field {}", key);
                unmapped.add("\"" + key + "\"");
            }
        });
        queue.execute();
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .setStringTransformer(value -> Normalizer.normalize(new String(value.getBytes(ISO88591), UTF8), Normalizer.Form.NFKC))
                .addListener(queue);
        InputStreamReader r = new InputStreamReader(InputService.getInputStream(uri), ISO88591);
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

    class MyEntityQueue extends MARCEntityQueue {

        public MyEntityQueue(Map<String,Object> params) {
            super(settings.get("package", "org.xbib.analyzer.marc.bib"),
                    params,
                    settings.getAsInt("pipelines", 1),
                    settings.get("elements")
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

        public MyDirectQueue(Map<String,Object> params) {
            super(settings.get("package", "org.xbib.analyzer.marc.bib"),
                    settings.getAsInt("pipelines", 1),
                    settings.get("elements")
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
