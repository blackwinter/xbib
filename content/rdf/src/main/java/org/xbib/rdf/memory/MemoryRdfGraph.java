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
package org.xbib.rdf.memory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.iri.IRI;
import org.xbib.rdf.Node;
import org.xbib.rdf.RdfContentGenerator;
import org.xbib.rdf.RdfGraph;
import org.xbib.rdf.RdfGraphParams;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MemoryRdfGraph implements RdfGraph<RdfGraphParams> {

    private final static Logger logger = LogManager.getLogger(MemoryRdfGraph.class);

    private RdfGraphParams params = MemoryRdfGraphParams.DEFAULT_PARAMS;

    private Map<IRI, Resource> resources = new LinkedHashMap<IRI, Resource>();

    @Override
    public Iterator<Resource> getResources() {
        return resources.values()
                .stream()
                //.filter(r -> !r.isEmbedded())
                //.map(this::expand)
                .iterator();
    }

    @Override
    public RdfGraph<RdfGraphParams> putResource(IRI id, Resource resource) {
        resources.put(id, resource);
        return this;
    }

    @Override
    public Resource getResource(IRI predicate) {
        return resources.get(predicate);
    }

    @Override
    public Resource removeResource(IRI predicate) {
        return resources.remove(predicate);
    }

    @Override
    public boolean hasResource(IRI predicate) {
        return resources.containsKey(predicate);
    }

    @Override
    public MemoryRdfGraph setParams(RdfGraphParams params) {
        this.params = params;
        return this;
    }

    @Override
    public RdfGraphParams getParams() {
        return params;
    }

    @Override
    public MemoryRdfGraph startStream() {
        return this;
    }

    @Override
    public RdfContentGenerator setBaseUri(String baseUri) {
        startPrefixMapping("", baseUri);
        return this;
    }

    @Override
    public MemoryRdfGraph startPrefixMapping(String prefix, String uri) {
        params.getNamespaceContext().addNamespace(prefix, uri);
        return this;
    }

    @Override
    public MemoryRdfGraph endPrefixMapping(String prefix) {
        // ignore
        return this;
    }

    @Override
    public MemoryRdfGraph receive(IRI identifier) {
        // ignore
        return this;
    }

    @Override
    public MemoryRdfGraph receive(Triple triple) {
        IRI subject = triple.subject().id();
        if (!resources.containsKey(subject)) {
            resources.put(subject, new MemoryResource(subject));
        }
        resources.get(subject).add(triple);
        return this;
    }

    @Override
    public MemoryRdfGraph endStream() {
        return this;
    }

    @Override
    public MemoryRdfGraph receive(Resource resource) throws IOException {
        resources.put(resource.id(), resource);
        return this;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    private Resource expand(Resource resource) {
        Resource expanded = new MemoryResource(resource.id());
        new GraphTriples(resource).triples.stream().forEach(expanded::add);
        return expanded;
    }

    private class GraphTriples {

        private final List<Triple> triples;

        GraphTriples(Resource resource) {
            this.triples = unfold(resource);
        }

        private List<Triple> unfold(final Resource resource) {
            List<Triple> list = new LinkedList<>();
            if (resource == null) {
                return list;
            }
            resource.predicates().forEach(pred -> resource.objects(pred).stream().forEach(node -> {
                        if (node instanceof Resource) {
                            Resource resource1 = (Resource)node;
                            if (resource1.isEmbedded()) {
                                Resource r = getResource(resource1.id());
                                if (r != null) {
                                    list.add(new MemoryTriple(resource1, pred, r.id()));
                                    list.addAll(unfold(r));
                                } else {
                                    logger.error("huh? {}", resource1.id());
                                }
                            } else {
                                list.addAll(unfold(resource1));
                            }
                        } else {
                            list.add(new MemoryTriple(resource, pred, node));
                        }
                    }
                    ));
            return list;
        }
    }


}
