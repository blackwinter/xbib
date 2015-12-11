package org.xbib.rdf.io.sink;

import org.junit.Test;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentBuilderProvider;
import org.xbib.rdf.RdfGraph;
import org.xbib.rdf.RdfGraphParams;
import org.xbib.rdf.io.ntriple.NTripleContent;
import org.xbib.rdf.io.ntriple.NTripleContentParams;
import org.xbib.rdf.io.source.StreamProcessor;
import org.xbib.rdf.jsonld.JsonLdReader;
import org.xbib.rdf.memory.MemoryRdfGraph;
import org.xbib.rdf.memory.MemoryRdfGraphParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class RdfContentBuilderSinkTest {

    @Test
    public void ntripleBuilderSink() throws Exception {
        InputStream in = getClass().getResourceAsStream("/org/xbib/rdf/jsonld/Vcard.jsonld");
        if (in != null) {
            IRINamespaceContext context = IRINamespaceContext.newInstance();
            context.addNamespace("vcard", "http://www.w3.org/2006/vcard/ns#");
            context.addNamespace("owl", "http://www.w3.org/2002/07/owl#");
            RdfGraphParams params = new MemoryRdfGraphParams(context, false);
            RdfGraph<RdfGraphParams> graph = new MemoryRdfGraph().setParams(params);
            NTripleContentParams nTripleContentParams = new NTripleContentParams(context);
            RdfContentBuilderProvider provider = new RdfContentBuilderProvider() {
                @Override
                public RdfContentBuilder newContentBuilder() throws IOException {
                    return new RdfContentBuilder(NTripleContent.nTripleContent, nTripleContentParams) {
                        @Override
                        public RdfContentBuilder endStream() throws IOException {
                            super.endStream();
                            //logger.info("ntriples={}", string());
                            return this;
                        }
                    };
                }
            };
            RdfContentBuilderSink sink = new RdfContentBuilderSink(graph, provider);
            StreamProcessor streamProcessor = new StreamProcessor(JsonLdReader.connect(sink));
            Reader input = new InputStreamReader(in, "UTF-8");
            streamProcessor.process(input);
            input.close();
        }
    }
}
