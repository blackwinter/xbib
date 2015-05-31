package org.xbib.rdf.xcontent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentBuilderProvider;
import org.xbib.rdf.RdfGraph;
import org.xbib.rdf.RdfGraphParams;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.RdfXContent;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.sink.RdfContentBuilderSink;
import org.xbib.rdf.io.source.StreamProcessor;
import org.xbib.rdf.jsonld.JsonLdReader;
import org.xbib.rdf.memory.MemoryRdfGraph;
import org.xbib.rdf.memory.MemoryRdfGraphParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class RdfXContentJsonLdTest {

    private final static Logger logger = LogManager.getLogger(RdfXContentJsonLdTest.class);

    public void jsonld() throws Exception {
        InputStream in = getClass().getResourceAsStream("/org/xbib/rdf/xcontent/Vcard.jsonld");
        if (in != null) {
            IRINamespaceContext context = IRINamespaceContext.newInstance();
            context.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            context.addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
            context.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
            context.addNamespace("vcard", "http://www.w3.org/2006/vcard/ns#");
            context.addNamespace("owl", "http://www.w3.org/2002/07/owl#");
            RdfGraphParams params = new MemoryRdfGraphParams(context, false);
            RdfGraph<RdfGraphParams> graph = new MemoryRdfGraph().setParams(params);
            RouteRdfXContentParams rdfXContentParams = new RouteRdfXContentParams(context, "index", "type");
            RdfContentBuilderProvider provider = new RdfContentBuilderProvider() {
                @Override
                public RdfContentBuilder newContentBuilder() throws IOException {
                    return new RdfContentBuilder(RdfXContent.rdfXContent, rdfXContentParams) {
                        private Resource resource;

                        @Override
                        public RdfContentBuilder receive(Resource resource) throws IOException {
                            super.receive(resource);
                            this.resource = resource;
                            return this;
                        }

                        @Override
                        public RdfContentBuilder endStream() throws IOException {
                            super.endStream();
                            logger.info("ES document: id={} json={}",
                                    context.compact(resource.id()),
                                    rdfXContentParams.getGenerator().get());
                            return this;
                        }
                    };
                }
            };
            RdfContentBuilderSink sink = new RdfContentBuilderSink(graph, provider);
            StreamProcessor streamProcessor = new StreamProcessor(JsonLdReader.connect(sink));
            String uri = "http://xbib.org";
            Reader input = new InputStreamReader(in, "UTF-8");
            streamProcessor.process(input, uri);
            input.close();
        }
    }
}

