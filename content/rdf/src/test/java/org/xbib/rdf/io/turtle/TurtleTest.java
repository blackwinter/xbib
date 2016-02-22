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
package org.xbib.rdf.io.turtle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;
import org.xbib.helper.StreamTester;
import org.xbib.iri.IRI;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentFactory;
import org.xbib.rdf.Resource;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.memory.MemoryResource;

import static org.xbib.rdf.RdfContentFactory.turtleBuilder;

public class TurtleTest extends StreamTester {

    @Test
    public void testTurtleGND() throws Exception {
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("gnd", "http://d-nb.info/gnd/");
        InputStream in = getClass().getResourceAsStream("GND.ttl");
        TurtleContentParser reader = new TurtleContentParser(in)
                .setBaseIRI(IRI.create("http://d-nb.info/gnd/"))
                .context(context);
        reader.parse();
        in.close();
    }

    @Test
    public void testTurtleGND2() throws Exception {
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        InputStream in = getClass().getResourceAsStream("gnd2.ttl");
        TurtleContentParser reader = new TurtleContentParser(in)
                .setBaseIRI(IRI.create("http://d-nb.info/gnd/"))
                .context(context);
        reader.parse();
        in.close();
    }

    @Test
    public void testTurtleGND3() throws Exception {
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        InputStream in = getClass().getResourceAsStream("gnd2.ttl");
        TurtleContentParser reader = new TurtleContentParser(in)
                .setBaseIRI(IRI.create("http://d-nb.info/gnd/"))
                .context(context);
        reader.setRdfContentBuilderProvider(RdfContentFactory::ntripleBuilder);
        reader.setRdfContentBuilderHandler(b -> {
            //logger.info("doc id={} content={}", b.getSubject(), b.string());
        });
        reader.parse();
        in.close();
    }

    @Test
    public void testTurtle() throws Exception {
        StringBuilder sb = new StringBuilder();
        String filename = "turtle-demo.ttl";
        InputStream in = getClass().getResource(filename).openStream();
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        String s1 = sb.toString().trim();
        Resource resource = createResource();

        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        context.addNamespace("dcterms", "http://purl.org/dc/terms/");

        TurtleContentParams params = new TurtleContentParams(context, true);
        RdfContentBuilder builder = turtleBuilder(params);
        builder.receive(resource);
        String s2 = builder.string().trim();
        assertEquals(s2, s1);
        in.close();
    }

    private Resource createResource() {
        Resource resource = new MemoryResource();
        resource.id(IRI.create("urn:doc1"));
        resource.add("dc:creator", "Smith");
        resource.add("dc:creator", "Jones");
        Resource r = resource.newResource("dcterms:hasPart")
                .add("dc:title", "This is a part")
                .add("dc:title", "of the sample title")
                .add("dc:creator", "Jörg Prante")
                .add("dc:date", "2009");
        resource.add("dc:title", "A sample title");
        resource.newResource("dcterms:isPartOf")
                .add("dc:title", "another")
                .add("dc:title", "title");
        return resource;
    }

    @Test
    public void testTurtleBuilder() throws Exception {
        Resource resource = createResource2();
        TurtleContentParams params = new TurtleContentParams(IRINamespaceContext.getInstance(), false);
        RdfContentBuilder builder = turtleBuilder(params);
        builder.receive(resource);
        assertStream(getClass().getResource("turtle-test.ttl").openStream(), builder.streamInput());
    }

    private Resource createResource2() {
        Resource r = new MemoryResource();
        r.id(IRI.create("urn:res"))
                .add("dc:title", "Hello")
                .add("dc:title", "World")
                .add("xbib:person", "Jörg Prante")
                .add("dc:subject", "An")
                .add("dc:subject", "example")
                .add("dc:subject", "for")
                .add("dc:subject", "a")
                .add("dc:subject", "sequence")
                .add("http://purl.org/dc/terms/place", "Köln");
        r.newResource("urn:res1")
                .add("property1", "value1")
                .add("property2", "value2");
        r.newResource("urn:res2")
                .add("property3", "value3")
                .add("property4", "value4");
        r.newResource("urn:res3")
                .add("property5", "value5")
                .add("property6", "value6");
        return r;
    }

    @Test
    public void testTurtleResourceIndent() throws Exception {
        Resource resource = createNestedResources();
        TurtleContentParams params = new TurtleContentParams(IRINamespaceContext.getInstance(), false);
        RdfContentBuilder builder = turtleBuilder(params);
        builder.receive(resource);
        assertStream(getClass().getResource("turtle-indent.ttl").openStream(),
                builder.streamInput());
    }

    private Resource createNestedResources() {
        Resource r = new MemoryResource();
        r.id(IRI.create("urn:res"))
                .add("dc:title", "Hello")
                .add("dc:title", "World")
                .add("xbib:person", "Jörg Prante")
                .add("dc:subject", "An")
                .add("dc:subject", "example")
                .add("dc:subject", "for")
                .add("dc:subject", "a")
                .add("dc:subject", "sequence")
                .add("http://purl.org/dc/terms/place", "Köln");
        Resource r1 = r.newResource("urn:res1")
                .add("property1", "value1")
                .add("property2", "value2");
        r1.newResource("urn:res2")
                .add("property3", "value3")
                .add("property4", "value4");
        r.newResource("urn:res3")
                .add("property5", "value5")
                .add("property6", "value6");
        return r;
    }


}
