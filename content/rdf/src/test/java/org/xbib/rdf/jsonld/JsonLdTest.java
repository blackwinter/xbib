/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.rdf.jsonld;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentBuilderProvider;
import org.xbib.rdf.RdfGraph;
import org.xbib.rdf.RdfGraphParams;
import org.xbib.rdf.io.ntriple.NTripleContent;
import org.xbib.rdf.io.ntriple.NTripleContentParams;
import org.xbib.rdf.io.sink.RdfContentBuilderSink;
import org.xbib.rdf.io.source.StreamProcessor;
import org.xbib.rdf.memory.MemoryRdfGraph;
import org.xbib.rdf.memory.MemoryRdfGraphParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;


public class JsonLdTest {
    private final static Logger logger = LogManager.getLogger(JsonLdTest.class);

    @Test
    public void ntripleBuilderSink() throws Exception {
        InputStream in = getClass().getResourceAsStream("/org/xbib/rdf/jsonld/schema.jsonld");
        if (in != null) {
            IRINamespaceContext context = IRINamespaceContext.newInstance();
            context.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
            context.addNamespace("foaf", "http://xmlns.com/foaf/0.1/");
            context.addNamespace("bibo", "http://purl.org/ontology/bibo/");
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
                            logger.info("ntriples={}", string());
                            return this;
                        }
                    };
                }
            };
            RdfContentBuilderSink sink = new RdfContentBuilderSink(graph, provider);
            StreamProcessor streamProcessor = new StreamProcessor(JsonLdReader.connect(sink));
            Reader input = new InputStreamReader(in, "UTF-8");
            streamProcessor.process(input, "http://xbib.org/");
            input.close();
        }
    }
}
