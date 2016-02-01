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
import org.xbib.tools.convert.Converter;
import org.xbib.tools.input.FileInput;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.turtle.TurtleContentParser;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.InputStream;
import java.net.URI;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * GND ingest from Turtle format
 */
public class Turtle extends Feeder {

    private final static Logger logger = LogManager.getLogger(Turtle.class);

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new Turtle().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = FileInput.getInputStream(uri)) {
            IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();
            namespaceContext.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
            namespaceContext.addNamespace("geo", "http://rdvocab.info/");
            namespaceContext.addNamespace("rda", "http://purl.org/dc/elements/1.1/");
            namespaceContext.addNamespace("foaf", "http://xmlns.com/foaf/0.1/");
            namespaceContext.addNamespace("sf", "http://www.opengis.net/ont/sf#");
            namespaceContext.addNamespace("isbd", "http://iflastandards.info/ns/isbd/elements/");
            namespaceContext.addNamespace("gndo", "http://d-nb.info/standards/elementset/gnd#");
            namespaceContext.addNamespace("dcterms", "http://purl.org/dc/terms/");
            namespaceContext.addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
            namespaceContext.addNamespace("marcRole", "http://id.loc.gov/vocabulary/relators/");
            namespaceContext.addNamespace("lib", "http://purl.org/library/");
            namespaceContext.addNamespace("umbel", "http://umbel.org/umbel#");
            namespaceContext.addNamespace("bibo", "http://purl.org/ontology/bibo/");
            namespaceContext.addNamespace("owl", "http://www.w3.org/2002/07/owl#");
            namespaceContext.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            namespaceContext.addNamespace("skos", "http://www.w3.org/2004/02/skos/core#");

            RouteRdfXContentParams params = new RouteRdfXContentParams(namespaceContext,
                    indexDefinitionMap.get("bib").getConcreteIndex(),
                    indexDefinitionMap.get("bib").getType());
            params.setIdPredicate("gndo:gndIdentifier");
            params.setHandler((content, p) -> {
                int pos = p.getId().lastIndexOf('/');
                String docid = p.getId().substring(pos + 1);
                if (settings.getAsBoolean("mock", false)) {
                    logger.info("{}", content);
                } else {
                    ingest.index(p.getIndex(), p.getType(), docid, content);
                }
            });
            IRI base = IRI.builder().scheme("http").host("d-nb.info").path("/gnd/").build();
            TurtleContentParser reader = new TurtleContentParser(in)
                    .setBaseIRI(base)
                    .context(namespaceContext);
            reader.setRdfContentBuilderProvider(() -> routeRdfXContentBuilder(params));
            reader.setRdfContentBuilderHandler(b -> {
                IRI iri = b.getSubject();
                String s = b.string();
            });
            reader.parse();
        }
    }

}
