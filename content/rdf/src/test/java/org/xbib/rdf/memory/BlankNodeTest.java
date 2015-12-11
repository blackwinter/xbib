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

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;

public class BlankNodeTest extends Assert {

    @Test
    public void testBlankNodeRenumbering() throws Exception {
        MemoryResource.reset();

        Resource r = new MemoryResource().id(IRI.create("urn:meta1"));

        // test order of adding
        Resource r1 = r.newResource("urn:res1");
        r1.add("urn:has", "a first res");
        r.add("urn:has", "a first property");
        Resource q = new MemoryResource().id(IRI.create("urn:meta2"));
        Resource r2 = q.newResource("urn:res2");
        r2.add("urn:has", "a second res");
        q.add("urn:has", "a second property");
        // we test here resource adding
        r.add("a:res", q);

        Iterator<Triple> it = r.properties();
        assertEquals(it.next().toString(), "urn:meta1 urn:res1 _:b1");
        assertEquals(it.next().toString(), "urn:meta1 urn:has a first property");
        assertEquals(it.next().toString(), "urn:meta1 a:res urn:meta2");
    }

    @Test
    public void testIterator() throws Exception {
        MemoryResource.reset();

        Resource r = new MemoryResource()
                .id(IRI.create("res1"));
        r.add("p0", "l0")
            .newResource("res2")
                .add("p1", "l1")
                .add("p2", "l2")
            .newResource("res3")
                .add("p1", "l1")
                .add("p2", "l2")
            .newResource("res4")
                .add("p1", "l1")
                .add("p2", "l2");

        Iterator<Triple> it = r.triples();
        assertEquals(it.next().toString(), "res1 p0 l0");
        assertEquals(it.next().toString(), "res1 res2 _:b1");
        assertEquals(it.next().toString(), "_:b1 p1 l1");
        assertEquals(it.next().toString(), "_:b1 p2 l2");
        assertEquals(it.next().toString(), "_:b1 res3 _:b2");
        assertEquals(it.next().toString(), "_:b2 p1 l1");
        assertEquals(it.next().toString(), "_:b2 p2 l2");
        assertEquals(it.next().toString(), "_:b2 res4 _:b3");
        assertEquals(it.next().toString(), "_:b3 p1 l1");
        assertEquals(it.next().toString(), "_:b3 p2 l2");
    }

    @Test
    public void testResIterator() throws Exception {
        MemoryResource.reset();

        Resource r = new MemoryResource()
                .id(IRI.create("res0"));
        r.add("p0", "l0")
                .newResource("res")
                .add("p1", "l1")
                .add("p2", "l2")
                .newResource("res")
                .add("p1", "l1")
                .add("p2", "l2")
                .newResource("res")
                .add("p1", "l1")
                .add("p2", "l2");

        Iterator<Triple> it = r.triples();
        assertEquals(it.next().toString(), "res0 p0 l0");
        assertEquals(it.next().toString(), "res0 res _:b1");
        assertEquals(it.next().toString(), "_:b1 p1 l1");
        assertEquals(it.next().toString(), "_:b1 p2 l2");
        assertEquals(it.next().toString(), "_:b1 res _:b2");
        assertEquals(it.next().toString(), "_:b2 p1 l1");
        assertEquals(it.next().toString(), "_:b2 p2 l2");
        assertEquals(it.next().toString(), "_:b2 res _:b3");
        assertEquals(it.next().toString(), "_:b3 p1 l1");
        assertEquals(it.next().toString(), "_:b3 p2 l2");
    }
}
