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

import java.io.IOException;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.io.ntriple.NTripleContentParams;

import static org.xbib.rdf.RdfContentFactory.ntripleBuilder;

public class ResourceTest extends Assert {

    @Test
    public void deleted() throws Exception {
        Resource r = new MemoryResource();
        assertEquals(r.isDeleted(), false);
        r.setDeleted(true);
        assertEquals(r.isDeleted(), true);
        r.setDeleted(false);
        assertEquals(r.isDeleted(), false);
    }

    @Test
    public void testResourceId() throws Exception {
        IRI iri = IRI.create("http://index?type#id");
        Resource r = new MemoryResource().id(iri);
        assertEquals("http", r.id().getScheme());
        assertEquals("index", r.id().getHost());
        assertEquals("type", r.id().getQuery());
        assertEquals("id", r.id().getFragment());
    }

    @Test
    public void testEmptyResources() throws Exception {
        Resource r = new MemoryResource().id(IRI.create("urn:root"));
        assertEquals(r.isEmpty(), true);
        assertEquals(r.toString(), "urn:root");
    }

    @Test
    public void testEmptyProperty() throws Exception {
        Resource r = new MemoryResource().id(IRI.create("urn:root"));
        r.add("urn:property", (String) null);
        assertEquals(r.isEmpty(), true);
    }

    @Test
    public void testStringLiteral() throws Exception {
        Resource r = new MemoryResource().id(IRI.create("urn:root"));
        r.add("urn:property", "Hello World");
        assertEquals(r.isEmpty(), false);
        assertEquals(r.triples().next().object().toString(), "Hello World");
    }

    @Test
    public void testIntegerLiteral() throws Exception {
        Resource r = new MemoryResource().id(IRI.create("urn:root"));
        MemoryLiteral literal = new MemoryLiteral(123).type(Literal.INT);
        r.add("urn:property", literal);
        assertEquals(r.isEmpty(), false);
        assertEquals(r.triples().next().object().toString(), "123^^xsd:int");
    }

    @Test
    public void testPredicateSet() throws Exception {
        Resource r = new MemoryResource().id(IRI.create("urn:doc1"))
                .add("urn:valueURI", "Hello World")
                .add("urn:creator", "Smith")
                .add("urn:creator", "Jones");
        Iterator<IRI> it = r.predicates().iterator();
        assertEquals("urn:valueURI", it.next().toString());
        assertEquals("urn:creator", it.next().toString());
        assertFalse(it.hasNext());
    }

    @Test
    public void testUniqueObjects() throws Exception {
        Resource r = new MemoryResource().id(IRI.create("urn:doc4"));
        r.add("urn:hasAttribute", "a")
                .add("urn:hasAttribute", "b")
                .add("urn:hasAttribute", "a") // another a, must be suppressed
                .add("urn:hasAttribute", "c");
        StringBuilder sb = new StringBuilder();
        r.objects("urn:hasAttribute").forEachRemaining(sb::append);
        assertEquals(sb.toString(), "abc");
    }

    @Test
    public void testPropertyIterator() throws Exception {
        Resource r = new MemoryResource();
        String id = "urn:doc2";
        r.id(IRI.create(id))
                .add("urn:valueURI", "Hello World")
                .add("urn:name", "Smith")
                .add("urn:name", "Jones");
        Iterator<Triple> it = r.properties();
        assertEquals("urn:doc2 urn:valueURI Hello World", it.next().toString());
        assertEquals("urn:doc2 urn:name Smith", it.next().toString());
        assertEquals("urn:doc2 urn:name Jones", it.next().toString());
    }

    @Test
    public void testIterator() throws Exception {
        MemoryResource.reset(); // for blank node counter
        Resource r = new MemoryResource();
        r.id(IRI.create("urn:doc1"))
                .add("urn:valueURI", "Hello World")
                .add("urn:name", "Smith")
                .add("urn:name", "Jones");
        // the first resource adds a resource value
        Resource r1 = r.newResource("urn:res1");
        r1.add("urn:has", "a first res value");
        // the second resource adds another resource value
        Resource r2 = r.newResource("urn:res1");
        r2.add("urn:has", "a second res value");

        assertEquals(r.predicates().size(), 3);
        
        Iterator<Triple> it = r.triples();
        assertEquals("urn:doc1 urn:valueURI Hello World", it.next().toString());
        assertEquals("urn:doc1 urn:name Smith", it.next().toString());
        assertEquals("urn:doc1 urn:name Jones", it.next().toString());
        assertEquals("urn:doc1 urn:res1 _:b1", it.next().toString());
        assertEquals("_:b1 urn:has a first res value", it.next().toString());
        assertEquals("urn:doc1 urn:res1 _:b2", it.next().toString());
        assertEquals("_:b2 urn:has a second res value", it.next().toString());
        assertFalse(it.hasNext());
        
        Iterator<IRI> itp = r.predicates().iterator();
        IRI pred = itp.next();
        assertEquals("urn:valueURI", pred.toString());
        Iterator<Node> values = r.objects(pred);
        assertEquals("Hello World", values.next().toString());
        assertFalse(values.hasNext());
        pred = itp.next();
        assertEquals("urn:name", pred.toString());
        values = r.objects(pred);
        assertEquals("Smith", values.next().toString());
        assertEquals("Jones", values.next().toString());
        assertFalse(values.hasNext());
        pred = itp.next();
        assertEquals("urn:res1", pred.toString());
        values = r.objects(pred);
        assertEquals("_:b1", values.next().toString());
        assertEquals("_:b2", values.next().toString());
        assertFalse(values.hasNext());
        assertFalse(itp.hasNext());
    }

    @Test
    public void testCompactPredicate() throws Exception {
        Resource r = new MemoryResource();
        r.id(IRI.create("urn:doc"))
                .add("urn:value1", "Hello World");
        IRI predicate = IRI.create("urn:pred");
        Resource r1 = r.newResource(predicate);
        r1.add(predicate, "a value");
        Iterator<Triple> it = r.triples();
        int cnt = 0;
        while (it.hasNext()) {
            it.next();
            cnt++;
        }
        assertEquals(cnt, 3);        
  
        r.compactPredicate(predicate);
        it = r.triples();
        assertEquals("urn:doc urn:value1 Hello World", it.next().toString());
        assertEquals("urn:doc urn:pred a value", it.next().toString());
        assertFalse(it.hasNext());
    }

    @Test
    public void testAddingResources() throws Exception {
        MemoryResource.reset(); // for blank node counter
        Resource r = new MemoryResource();
        r.id(IRI.create("urn:r"))
                .add("urn:value", "Hello R");

        // named ID
        Resource s = new MemoryResource();
        s.id(IRI.create("urn:s"))
                .add("urn:value", "Hello S");

        // another named ID
        Resource t = new MemoryResource();
        t.id(IRI.create("urn:t"))
                .add("urn:value", "Hello T");

        // a blank node resource ID
        IRI blank1 = new MemoryResource().blank().id();
        Resource u = new MemoryResource();
        u.id(blank1).add("urn:value", "Hello U");

        // another blank node resource ID
        IRI blank2 = new MemoryResource().blank().id();
        Resource v = new MemoryResource();
        v.id(blank2).add("urn:value", "Hello V");

        IRI predicate = IRI.create("dc:subject");
        r.add(predicate, s);
        r.add(predicate, t);
        r.add(predicate, u);
        r.add(predicate, v);

        Iterator<Triple> it = r.triples();
        assertEquals("urn:r urn:value Hello R", it.next().toString());
        assertEquals("urn:r dc:subject urn:s", it.next().toString());
        assertEquals("urn:s urn:value Hello S", it.next().toString());
        assertEquals("urn:r dc:subject urn:t", it.next().toString());
        assertEquals("urn:t urn:value Hello T", it.next().toString());
        assertEquals("urn:r dc:subject _:b1", it.next().toString());
        assertEquals("_:b1 urn:value Hello U", it.next().toString());
        assertEquals("urn:r dc:subject _:b2", it.next().toString());
        assertEquals("_:b2 urn:value Hello V", it.next().toString());
        assertFalse(it.hasNext());
    }


    @Test
    public void testTripleAdder() throws IOException {
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("vcard", "http://www.w3.org/2006/vcard/ns#");
        context.addNamespace("owl", "http://www.w3.org/2002/07/owl#");

        // ID with compact IRI, will be expanded
        Resource r = MemoryResource.create(context, "vcard:value");
        // triples with expanded IRIs
        Triple t1 = new MemoryTriple(MemoryResource.create("http://www.w3.org/2006/vcard/ns#value"),
                IRI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                IRI.create("http://www.w3.org/2002/07/owl#DatatypeProperty"));
        Triple t2 =  new MemoryTriple(MemoryResource.create("http://www.w3.org/2006/vcard/ns#value"),
                IRI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#label"),
                new MemoryLiteral("value@en"));
        r.add(t1).add(t2);
        NTripleContentParams params = new NTripleContentParams(context);
        RdfContentBuilder builder = ntripleBuilder(params);
        builder.receive(r);
        assertEquals("<http://www.w3.org/2006/vcard/ns#value> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty> .\n" +
                "<http://www.w3.org/2006/vcard/ns#value> <http://www.w3.org/1999/02/22-rdf-syntax-ns#label> \"value@en\" .",
                builder.string().trim());
    }
}
