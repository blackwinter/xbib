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
package org.xbib.rdf;

import org.xbib.io.BytesArray;
import org.xbib.io.BytesReference;
import org.xbib.io.FastByteArrayOutputStream;
import org.xbib.io.stream.BytesStream;
import org.xbib.iri.IRI;
import org.xbib.rdf.io.ntriple.NTripleContent;
import org.xbib.rdf.io.ntriple.NTripleContentParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RdfContentBuilder implements RdfContentGenerator {

    private final RdfContentGenerator generator;

    private final OutputStream out;

    private IRI subject;

    public RdfContentBuilder() throws IOException {
        this(NTripleContent.nTripleContent, NTripleContentParams.DEFAULT_PARAMS);
    }

    public RdfContentBuilder(RdfContent rdfContent, RdfContentParams rdfParams) throws IOException {
        this(rdfContent, rdfParams, new FastByteArrayOutputStream());
    }

    @SuppressWarnings("unchecked")
    public RdfContentBuilder(RdfContent rdfContent, RdfContentParams rdfContentParams, OutputStream out) throws IOException {
        this.out = out;
        this.generator = rdfContent.createGenerator(out);
        this.generator.setParams(rdfContentParams);
    }

    public RdfContentParams getParams() {
        return generator.getParams();
    }

    @Override
    public void flush() throws IOException {
        generator.flush();
    }

    @Override
    public void close() throws IOException {
        generator.close();
    }

    public BytesReference bytes() throws IOException {
        close();
        return ((BytesStream) out).bytes();
    }

    public InputStream streamInput() throws IOException {
        close();
        return bytes().streamInput();
    }

    public String string() throws IOException {
        close();
        BytesArray bytesArray = bytes().toBytesArray();
        return new String(bytesArray.array(), bytesArray.arrayOffset(), bytesArray.length(), "UTF-8");
    }

    @Override
    public RdfContentBuilder setParams(RdfContentParams rdfContentParams) {
        generator.setParams(rdfContentParams);
        return this;
    }

    @Override
    public RdfContentBuilder startStream() throws IOException {
        generator.startStream();
        return this;
    }

    @Override
    public RdfContentBuilder endStream() throws IOException {
        generator.endStream();
        return this;
    }

    @Override
    public RdfContentBuilder setBaseUri(String baseUri) {
        generator.startPrefixMapping("", baseUri);
        return this;
    }

    @Override
    public RdfContentBuilder startPrefixMapping(String prefix, String uri) {
        generator.startPrefixMapping(prefix, uri);
        return this;
    }

    @Override
    public RdfContentBuilder endPrefixMapping(String prefix) {
        generator.endPrefixMapping(prefix);
        return this;
    }

    @Override
    public RdfContentBuilder receive(IRI identifier) throws IOException {
        this.subject = identifier;
        generator.receive(identifier);
        return this;
    }

    @Override
    public RdfContentBuilder receive(Triple triple) throws IOException {
        generator.receive(triple);
        return this;
    }

    @Override
    public RdfContentBuilder receive(Resource resource) throws IOException {
        generator.receive(resource);
        return this;
    }

    public IRI getSubject() {
        return subject;
    }

}
