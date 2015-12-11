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
package org.xbib.rdf.io.rdfxml;

import org.junit.Test;
import org.xbib.helper.StreamTester;
import org.xbib.iri.IRI;
import org.xbib.rdf.Node;
import org.xbib.rdf.RdfContent;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.io.ntriple.NTripleContent;
import org.xbib.rdf.io.ntriple.NTripleContentParams;
import org.xbib.rdf.memory.MemoryRdfGraph;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryTriple;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static org.xbib.rdf.RdfContentFactory.ntripleBuilder;

public class EuropeanaEDMReaderTest extends StreamTester {

    @Test
    public void testEuropeana() throws Exception {
        String filename = "/org/xbib/rdf/io/rdfxml/oai_edm.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        MemoryRdfGraph graph = new MemoryRdfGraph();
        RdfXmlContentParser reader = new RdfXmlContentParser(in);
        reader.setRdfContentBuilderProvider(() -> new GeoJSONFilter(NTripleContent.nTripleContent, NTripleContentParams.DEFAULT_PARAMS, graph));
        reader.parse();
        RdfContentBuilder builder = ntripleBuilder();
        Iterator<Resource> resourceIterator = graph.getResources();
        while (resourceIterator.hasNext()) {
            Resource resource = resourceIterator.next();
            builder.receive(resource);
        }
        assertStream(getClass().getResource("edm.nt").openStream(),
                builder.streamInput());
    }

    private final static IRI GEO_LAT = IRI.create("http://www.w3.org/2003/01/geo/wgs84_pos#lat");

    private final static IRI GEO_LON = IRI.create("http://www.w3.org/2003/01/geo/wgs84_pos#long");

    private final static IRI location = IRI.create("location");

    class GeoJSONFilter extends RdfContentBuilder {

        MemoryRdfGraph graph;

        Node lat = null;

        Node lon = null;

        GeoJSONFilter(RdfContent content, RdfContentParams params, MemoryRdfGraph graph) throws IOException {
            super(content, params);
            this.graph = graph;
        }

        @Override
        public GeoJSONFilter receive(Triple triple) {
            graph.receive(triple);
            if (triple.predicate().equals(GEO_LAT)) {
                lat = triple.object();
            }
            if (triple.predicate().equals(GEO_LON)) {
                lon = triple.object();
            }
            if (lat != null && lon != null) {
                // create location string for Elasticsearch
                graph.receive(new MemoryTriple(triple.subject(), location, new MemoryLiteral(lat + "," + lon)));
                lon = null;
                lat = null;
            }
            return this;
        }

    }

}
