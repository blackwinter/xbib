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
package org.xbib.rdf.io.ntriple;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.XSDResourceIdentifiers;
import org.xbib.rdf.memory.MemoryResource;

import static org.xbib.rdf.RdfContentFactory.ntripleBuilder;

public class NTripleTest extends Assert {

    @Test
    public void testNTripleBuilder() throws Exception {
        RdfContentBuilder builder = ntripleBuilder();
        Resource resource = createResource();
        builder.receive(resource);
        assertTrue(builder.string().length() > 0);
    }

    @Test
    public void testNTripleWriteInt() throws Exception {
        Resource resource = new MemoryResource();
        resource.id(IRI.create("urn:doc1"));
        resource.add("http://purl.org/dc/elements/1.1/date",new MemoryLiteral("2010").type(XSDResourceIdentifiers.INTEGER));
        RdfContentBuilder builder = ntripleBuilder();
        builder.receive(resource);
        assertEquals(builder.string(),
                "<urn:doc1> <http://purl.org/dc/elements/1.1/date> \"2010\"^^<xsd:integer> .\n");
    }

    private Resource createResource() {
        Resource resource = new MemoryResource();
        String id = "urn:doc1";
        resource.id(IRI.create(id));
        resource.add("http://purl.org/dc/elements/1.1/creator", "Smith");
        resource.add("http://purl.org/dc/elements/1.1/creator", "Jones");
        Resource r = resource.newResource("dcterms:hasPart");
        r.add("http://purl.org/dc/elements/1.1/title", "This is a part");
        r.add("http://purl.org/dc/elements/1.1/title", "of a title");
        r.add("http://purl.org/dc/elements/1.1/creator", "Jörg Prante");
        r.add("http://purl.org/dc/elements/1.1/date", "2009");
        resource.add("http://purl.org/dc/elements/1.1/title", "A sample title");
        r = resource.newResource("http://purl.org/dc/terms/isPartOf");
        r.add("http://purl.org/dc/elements/1.1/title", "another");
        r.add("http://purl.org/dc/elements/1.1/title", "title");
        return resource;
    }
}
