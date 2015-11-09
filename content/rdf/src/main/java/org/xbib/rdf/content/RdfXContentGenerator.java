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
package org.xbib.rdf.content;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.RdfContentGenerator;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.memory.MemoryResource;

import static org.xbib.common.xcontent.XContentService.jsonBuilder;

public class RdfXContentGenerator<R extends RdfXContentParams> implements RdfContentGenerator<R> {

    private R params;

    protected final OutputStream out;

    protected Resource resource;

    protected XContentBuilder builder;

    private boolean flushed;

    RdfXContentGenerator(OutputStream out) throws IOException {
        this.out = out;
    }

    @Override
    public RdfXContentGenerator setParams(R rdfContentParams) {
        this.params = rdfContentParams;
        params.setGenerator(this);
        return null;
    }

    public R getParams() {
        return params;
    }

    @Override
    public RdfXContentGenerator startStream() {
        this.resource = new MemoryResource();
        flushed = false;
        return this;
    }

    @Override
    public RdfContentGenerator setBaseUri(String baseUri) {
        startPrefixMapping("", baseUri);
        return this;
    }

    @Override
    public RdfXContentGenerator startPrefixMapping(String prefix, String uri) {
        params.getNamespaceContext().addNamespace(prefix, uri);
        return this;
    }

    @Override
    public RdfXContentGenerator endPrefixMapping(String prefix) {
        return this;
    }

    @Override
    public RdfXContentGenerator receive(IRI identifier) throws IOException {
        this.resource = new MemoryResource().id(identifier);
        flushed = false;
        return this;
    }

    @Override
    public RdfXContentGenerator receive(Triple triple) {
        this.resource.add(triple);
        return this;
    }

    @Override
    public RdfXContentGenerator endStream() throws IOException {
        if (resource != null && !resource.isEmpty()) {
            flush();
        }
        return this;
    }

    @Override
    public void close() throws IOException {
        flush();
    }

    @Override
    public void flush() throws IOException {
        if (this.resource == null || this.resource.isEmpty() || flushed) {
            return;
        }
        flushed = true;
        builder = jsonBuilder(out);
        builder.startObject();
        build(this.resource);
        builder.endObject();
    }

    @Override
    public RdfXContentGenerator receive(Resource resource)  throws IOException {
        if (resource != null) {
            this.resource = resource;
        }
        flush();
        return this;
    }

    public String string() throws IOException {
        if (builder != null) {
            return builder.string();
        }
        return null;
    }

    public String get() throws IOException {
        return string();
    }

    public void filter(IRI predicate, Node object) {
        // empty
    }

    protected void build(Resource resource) throws IOException {
        if (resource == null) {
            return;
        }
        for (IRI predicate : resource.predicates()) {
            // first, the values
            final List<Object> values = new ArrayList<Object>(32);
            final Iterator<Node> it = resource.visibleObjects(predicate);
            while (it.hasNext()) {
                Node node = it.next();
                if (node instanceof Resource) {
                    values.add(((Resource) node).id().toString()); // URLs
                } else if (node instanceof Literal) {
                    Object o = ((Literal)node).object();
                    if (o != null) {
                        values.add(o);
                    }
                }
                filter(predicate, node);
            }
            if (values.size() == 1) {
                builder.field(params.getNamespaceContext().compact(predicate), values.get(0));
            } else if (values.size() > 1) {
                builder.array(params.getNamespaceContext().compact(predicate), values);
            }
            // second, the embedded resources
            final Collection<Resource> resources = resource.embeddedResources(predicate);
            if (resources.size() == 1) {
                Resource res = resources.iterator().next();
                if (!res.isEmpty()) {
                    builder.field(params.getNamespaceContext().compact(predicate));
                    builder.startObject();
                    build(res);
                    builder.endObject();
                }
            } else if (resources.size() > 1) {
                builder.field(params.getNamespaceContext().compact(predicate));
                builder.startArray();
                for (Resource res : resources) {
                    if (!res.isEmpty()) {
                        builder.startObject();
                        build(res);
                        builder.endObject();
                    }
                }
                builder.endArray();
            }
        }
    }
}