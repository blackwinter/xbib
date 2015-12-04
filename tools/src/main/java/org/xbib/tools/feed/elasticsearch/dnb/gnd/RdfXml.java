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
package org.xbib.tools.feed.elasticsearch.dnb.gnd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.util.InputService;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.rdfxml.RdfXmlContentParser;
import org.xbib.tools.Feeder;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Ingest DNB GND RDF/XML to Elasticsearch
 */
public class RdfXml extends Feeder {

    private final static Logger logger = LogManager.getLogger(RdfXml.class);

    final static IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();

    static {
        namespaceContext.add(new HashMap<String, String>() {{
            put("dc", "http://purl.org/dc/elements/1.1/");
            put("geo", "http://rdvocab.info/");
            put("rda", "http://purl.org/dc/elements/1.1/");
            put("foaf", "http://xmlns.com/foaf/0.1/");
            put("sf", "http://www.opengis.net/ont/sf#");
            put("isbd", "http://iflastandards.info/ns/isbd/elements/");
            put("gnd", "http://d-nb.info/standards/elementset/gnd#");
            put("dcterms", "http://purl.org/dc/terms/");
            put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
            put("marcRole", "http://id.loc.gov/vocabulary/relators/");
            put("lib", "http://purl.org/library/");
            put("umbel", "http://umbel.org/umbel#");
            put("bibo", "http://purl.org/ontology/bibo/");
            put("owl", "http://www.w3.org/2002/07/owl#");
            put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            put("skos", "http://www.w3.org/2004/02/skos/core#");
            put("geosparql", "http://www.opengis.net/ont/geosparql#");
        }});
    }

    @Override
    protected WorkerProvider provider() {
        return p -> new RdfXml().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        logger.debug("processing URI {}", uri);
        InputStream in = InputService.getInputStream(uri);
        GNDRdfXmlContentParser reader = new GNDRdfXmlContentParser(in);
        reader.parse();
        reader.flush();
        in.close();
    }

    /*
     * For reasons I don't find, a normal routeRdfXContentBuilder did not work.
     * This is a workaround.
     */

    class GNDRdfXmlContentParser extends RdfXmlContentParser {

        private Resource lastSubject;

        private RdfContentBuilder builder;

        private RouteRdfXContentParams params = new RouteRdfXContentParams(namespaceContext);

        private String id;

        public GNDRdfXmlContentParser(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected void yield(Triple t) throws IOException {
            if (lastSubject == null || !t.subject().equals(lastSubject)) {
                if (builder != null) {
                    builder.endStream();
                    if (settings.getAsBoolean("mock", false)) {
                        logger.info("builder = {}", params.getGenerator().get());
                    } else {
                        ingest.index(getIndex(), getType(), id, params.getGenerator().get());
                    }
                    builder.close();
                }
                builder = routeRdfXContentBuilder(params);
                builder.startStream();
                builder.receive(t.subject().id());
                lastSubject = t.subject();
            }
            if (t.predicate().toString().equals("http://d-nb.info/standards/elementset/gnd#gndIdentifier")) {
                id = t.object().toString();
            }
            builder.receive(t);
        }

        public void flush() throws IOException {
            if (builder != null && lastSubject != null) {
                builder.endStream();
                if (settings.getAsBoolean("mock", false)) {
                    logger.info("builder = {}", params.getGenerator().get());
                } else {
                    ingest.index(getIndex(), getType(), id, params.getGenerator().get());
                }
                builder.close();
            }
        }
    }
}

