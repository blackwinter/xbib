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

import org.junit.Assert;
import org.junit.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.BlankMemoryResource;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;

import static org.xbib.rdf.content.RdfXContentFactory.rdfXContentBuilder;

public class RdfXContentGeneratorTest extends Assert {

    @Test
    public void testContentBuilder() throws Exception {
        Resource resource = new MemoryResource(IRI.create("urn:res"));
        MemoryLiteral l = new MemoryLiteral("2013")
                .type(IRI.create("xsd:gYear"));
        resource.add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("urn:link", IRI.create("urn:pointer"));
        RdfXContentParams params = new RdfXContentParams();
        RdfContentBuilder builder = rdfXContentBuilder(params);
        builder.receive(resource);
        String result = params.getGenerator().get();
        assertEquals(result,
                "{\"urn:property\":\"Hello World\",\"urn:date\":2013,\"urn:link\":\"urn:pointer\"}");
    }

    @Test
    public void testContentBuilderSingleEmbedded() throws Exception {
        Resource resource = new MemoryResource(IRI.create("urn:res"));
        MemoryLiteral l = new MemoryLiteral("2013")
                .type(IRI.create("xsd:gYear"));
        resource.add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("rdf:type", IRI.create("urn:type1"))
                .newResource("urn:embedded")
                .add("rdf:type", IRI.create("urn:type2"));
        RdfXContentParams params = new RdfXContentParams();
        RdfContentBuilder builder = rdfXContentBuilder(params);
        builder.receive(resource);
        String result = params.getGenerator().get();
        assertEquals(result,
                "{\"urn:property\":\"Hello World\",\"urn:date\":2013,\"rdf:type\":\"urn:type1\",\"urn:embedded\":{\"rdf:type\":\"urn:type2\"}}");
    }

    @Test
    public void testContentBuilderDoubleEmbedded() throws Exception {
        Resource resource = new MemoryResource(IRI.create("urn:res"));
        MemoryLiteral l = new MemoryLiteral("2013")
                .type(IRI.create("xsd:gYear"));
        resource.add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("rdf:type", IRI.create("urn:type1"))
                .newResource("urn:embedded")
                .add("rdf:type", IRI.create("urn:type2"));
        resource.newResource("urn:embedded2")
                .add("rdf:type", IRI.create("urn:type3"));
        RdfXContentParams params = new RdfXContentParams();
        RdfContentBuilder builder = rdfXContentBuilder(params);
        builder.receive(resource);
        String result = params.getGenerator().get();
        assertEquals(result,
                "{\"urn:property\":\"Hello World\",\"urn:date\":2013,\"rdf:type\":\"urn:type1\",\"urn:embedded\":{\"rdf:type\":\"urn:type2\"},\"urn:embedded2\":{\"rdf:type\":\"urn:type3\"}}");
    }

    @Test
    public void testContentBuilderEmptyEmbedded() throws Exception {
        Resource resource = new MemoryResource(IRI.create("urn:res"));
        MemoryLiteral l = new MemoryLiteral("2013")
                .type(IRI.create("xsd:gYear"));
        resource.add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("rdf:type", IRI.create("urn:type1"))
                .newResource("urn:embedded"); // empty resource, do not copy
        RdfXContentParams params = new RdfXContentParams();
        RdfContentBuilder builder = rdfXContentBuilder(params);
        builder.receive(resource);
        String result = params.getGenerator().get();
        assertEquals(result,
                "{\"urn:property\":\"Hello World\",\"urn:date\":2013,\"rdf:type\":\"urn:type1\"}");
    }
}
