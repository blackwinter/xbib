package org.xbib.tools.feed.elasticsearch.natliz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.xcontent.XContentHelper;
import org.xbib.etl.marc.dialects.mab.MABEntityBuilderState;
import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.tools.convert.Converter;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.marc.dialects.mab.xml.MabXMLReader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.FileInput;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

public class NatLiz extends Feeder {

    private final static Logger logger = LogManager.getLogger(NatLiz.class.getName());

    private final IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new NatLiz().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = FileInput.getInputStream(uri)) {
            namespaceContext.add(new HashMap<String, String>() {{
                put(RdfConstants.NS_PREFIX, RdfConstants.NS_URI);
                put("dc", "http://purl.org/dc/elements/1.1/");
                put("dcterms", "http://purl.org/dc/terms/");
                put("foaf", "http://xmlns.com/foaf/0.1/");
                put("frbr", "http://purl.org/vocab/frbr/core#");
                put("fabio", "http://purl.org/spar/fabio/");
                put("prism", "http://prismstandard.org/namespaces/basic/3.0/");
            }});

            final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
            Map<String, Object> params = new HashMap<>();
            params.put("identifier", settings.get("identifier", "DE-NLZ"));
            params.put("_prefix", "(" + settings.get("identifier", "DE-NLZ") + ")");
            final MABEntityQueue queue = new MyEntityQueue(params);
            queue.setUnmappedKeyListener((id, key) -> {
                if ((settings.getAsBoolean("detect-unknown", false))) {
                    logger.warn("record {} unmapped field {}", id, key);
                    unmapped.add("\"" + key + "\"");
                }
            });
            queue.execute();
            final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                    .addListener(queue);
            MabXMLReader reader = new MabXMLReader(in)
                    .setMarcXchangeListener(kv);
            reader.parse();
            queue.close();
            if (settings.getAsBoolean("detect-unknown", false)) {
                logger.info("unknown keys={}", unmapped);
            }
        }
    }

    class MyEntityQueue extends MABEntityQueue {

        final RouteRdfXContentParams params;

        public MyEntityQueue(Map<String,Object> map) throws Exception {
            super(settings.get("package", "org.xbib.analyzer.mab.titel"),
                    map,
                    settings.getAsInt("pipelines", 1),
                    settings.get("elements"));
            this.params = new RouteRdfXContentParams(namespaceContext,
                    indexDefinitionMap.get("bib").getConcreteIndex(),
                    indexDefinitionMap.get("bib").getType());
        }

        @Override
        public void afterCompletion(MABEntityBuilderState state) throws IOException {
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            if (settings.get("collection") != null) {
                state.getResource().add("collection", settings.get("collection"));
            }
            builder.receive(state.getResource());
            if (settings.getAsBoolean("mock", false)) {
                logger.info("id={} {}", state.getResource().id(), params.getGenerator().get());
            }
            Map<String,Object> map = XContentHelper.convertToMap(params.getGenerator().get());
            NatLizMapper natLizMapper = new NatLizMapper();
            builder = routeRdfXContentBuilder(params);
            Resource resource = natLizMapper.map(map);
            builder.receive(natLizMapper.map(map));
            if (settings.getAsBoolean("mock", false)) {
                logger.info("mapper: {}", params.getGenerator().get());
            } else {
                ingest.index(params.getIndex(), params.getType(), resource.id().toString(), params.getGenerator().get());
            }
        }
    }

}