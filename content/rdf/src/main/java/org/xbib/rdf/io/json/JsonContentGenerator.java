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
package org.xbib.rdf.io.json;

import org.xbib.iri.IRI;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.RdfContentGenerator;
import org.xbib.rdf.RdfGraph;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.memory.BlankMemoryResource;
import org.xbib.rdf.memory.MemoryResource;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

public class JsonContentGenerator
        implements RdfContentGenerator<JsonContentParams>, Flushable {

    private final Writer writer;

    private boolean nsWritten;

    private Resource resource;

    private JsonContentParams params = JsonContentParams.DEFAULT_PARAMS;

    JsonContentGenerator(OutputStream out) throws IOException {
        this(new OutputStreamWriter(out, "UTF-8"));
    }

    JsonContentGenerator(Writer writer) throws IOException {
        this.writer = writer;
        this.nsWritten = false;
        this.resource = new BlankMemoryResource();
    }

    public JsonContentParams getParams() {
        return params;
    }

    @Override
    public void close() throws IOException {
        // write last resource
        receive(resource);
    }

    @Override
    public JsonContentGenerator receive(IRI iri) {
        if (!iri.equals(resource.id())) {
            try {
                if (!nsWritten) {
                    writeNamespaces();
                }
                // TODO
                resource = new MemoryResource(iri);
            } catch (IOException e) {
                //
            }
        }
        return this;
    }

    @Override
    public RdfContentGenerator setParams(JsonContentParams rdfContentParams) {
        this.params = rdfContentParams;
        return this;
    }

    @Override
    public JsonContentGenerator startStream() {
        return this;
    }

    @Override
    public RdfContentGenerator setBaseUri(String baseUri) {
        return this;
    }

    @Override
    public JsonContentGenerator startPrefixMapping(String prefix, String uri) {
        return this;
    }

    @Override
    public JsonContentGenerator endPrefixMapping(String prefix) {
        return this;
    }

    @Override
    public JsonContentGenerator receive(Triple triple) {
        resource.add(triple);
        return this;
    }

    @Override
    public JsonContentGenerator endStream() {
        return this;
    }

    private JsonContentGenerator writeNamespaces() throws IOException {
        nsWritten = false;
        for (Map.Entry<String, String> entry : params.getNamespaceContext().getNamespaces().entrySet()) {
            if (entry.getValue().length() > 0) {
                String nsURI = entry.getValue();
                if (!RdfConstants.NS_URI.equals(nsURI)) {
                    //writeNamespace(entry.getKey(), nsURI);
                    nsWritten = true;
                }
            }
        }
        return this;
    }


    @Override
    public void flush() throws IOException {
    }

    @Override
    public JsonContentGenerator receive(Resource resource) throws IOException {
        return this;
    }
}
