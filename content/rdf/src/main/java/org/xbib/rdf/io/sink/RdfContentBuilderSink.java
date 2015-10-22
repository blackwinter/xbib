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
package org.xbib.rdf.io.sink;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentBuilderProvider;
import org.xbib.rdf.RdfGraph;
import org.xbib.rdf.RdfGraphParams;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.rdf.memory.MemoryTriple;

import java.io.IOException;

public class RdfContentBuilderSink implements QuadSink {

    private final static Logger logger = LogManager.getLogger(RdfContentBuilderSink.class);

    private final RdfGraph<RdfGraphParams> graph;

    private final RdfContentBuilderProvider provider;

    public RdfContentBuilderSink(RdfGraph<RdfGraphParams> graph,
                                 RdfContentBuilderProvider provider) {
        this.graph = graph;
        this.provider = provider;
    }

    @Override
    public void addNonLiteral(String subj, String pred, String obj) {
        try {
            Resource s = MemoryResource.create(graph.getParams().getNamespaceContext(), subj);
            IRI p = IRI.create(pred);
            Resource o = MemoryResource.create(graph.getParams().getNamespaceContext(), obj);
            Triple t = new MemoryTriple(s, p, o);
            graph.receive(t);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void addNonLiteral(String subj, String pred, String obj, String graphIRI) {
        try {
            Resource s = MemoryResource.create(graph.getParams().getNamespaceContext(), subj);
            IRI p = IRI.create(pred);
            Resource o = MemoryResource.create(graph.getParams().getNamespaceContext(), obj);
            Triple t = new MemoryTriple(s, p, o);
            graph.receive(t);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void addPlainLiteral(String subj, String pred, String content, String lang) {
        try {
            Resource s = MemoryResource.create(graph.getParams().getNamespaceContext(), subj);
            IRI p = IRI.create(pred);
            Literal o = new MemoryLiteral(content).language(lang);
            Triple t = new MemoryTriple(s, p, o);
            graph.receive(t);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void addPlainLiteral(String subj, String pred, String content, String lang, String graphIRI) {
        try {
            Resource s = MemoryResource.create(graph.getParams().getNamespaceContext(), subj);
            IRI p = IRI.create(pred);
            Literal o = new MemoryLiteral(content).language(lang);
            Triple t = new MemoryTriple(s, p, o);
            graph.receive(t);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void addTypedLiteral(String subj, String pred, String content, String type) {
        try {
            Resource s = MemoryResource.create(graph.getParams().getNamespaceContext(), subj);
            IRI p = IRI.create(pred);
            Literal o = new MemoryLiteral(content).type(IRI.create(type));
            Triple t = new MemoryTriple(s, p, o);
            graph.receive(t);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void addTypedLiteral(String subj, String pred, String content, String type, String graphIRI) {
        try {
            Resource s = MemoryResource.create(graph.getParams().getNamespaceContext(), subj);
            IRI p = IRI.create(pred);
            Literal o = new MemoryLiteral(content).type(IRI.create(type));
            Triple t = new MemoryTriple(s, p, o);
            graph.receive(t);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void setBaseUri(String baseUri) {
    }

    @Override
    public void startStream() throws IOException {
    }

    @Override
    public void endStream() throws IOException {
        if (graph.getResources() != null) {
            if (provider != null) {
                graph.getResources().forEachRemaining(resource -> {
                    RdfContentBuilder rdfContentBuilder;
                    try {
                        rdfContentBuilder = provider.newContentBuilder();
                        rdfContentBuilder.startStream();
                        rdfContentBuilder.receive(resource);
                        rdfContentBuilder.endStream();
                    } catch (IOException e) {
                       logger.error(e.getMessage(), e);
                    }
                });
            } else {
                logger.warn("no RDF content builder provider");
            }
        } else {
            logger.warn("no graph resources");
        }
    }

    @Override
    public void beginDocument(String id) throws IOException {
        IRI iri = graph.getParams().getNamespaceContext().expandIRI(id);
        graph.receive(iri);
    }

    @Override
    public void endDocument(String id) {
    }
}
