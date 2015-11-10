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
package org.xbib.tools.feed.elasticsearch.dnb.title;

import org.xbib.elasticsearch.helper.client.Ingest;
import org.xbib.util.InputService;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.rdfxml.RdfXmlContentParser;
import org.xbib.tools.Feeder;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * Ingest DNB Title RDF/XML to Elasticsearch
 */
public class RdfXml extends Feeder {

    @Override
    public String getName() {
        return "dnb-title-rdfxml-elasticsearch";
    }

    @Override
    protected WorkerProvider provider() {
        return RdfXml::new;
    }

    @Override
    public RdfXml beforeIndexCreation(Ingest output) throws IOException {
        // TODO is this still necessary?
        output.setting(getClass().getResourceAsStream("settings.json"));
        output.mapping(settings.get("type"), getClass().getResourceAsStream("mapping.json"));
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
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
                settings.get("index", "dnb"),
                settings.get("type", "title"));
        params.setIdPredicate("gnd:gndIdentifier");
        InputStream in = InputService.getInputStream(uri);
        RdfXmlContentParser reader = new RdfXmlContentParser(in)
                .setRdfContentBuilderProvider(() -> routeRdfXContentBuilder(params))
                .setRdfContentBuilderHandler(builder ->
                        ingest.index(params.getIndex(), params.getType(), params.getId(), builder.string()))
                .parse();
        in.close();
    }

}

