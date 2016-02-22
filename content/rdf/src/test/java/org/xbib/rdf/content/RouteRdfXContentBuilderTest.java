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

import org.junit.Test;
import org.xbib.helper.StreamTester;
import org.xbib.iri.IRI;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.io.rdfxml.RdfXmlContentParser;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.memory.MemoryResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

public class RouteRdfXContentBuilderTest extends StreamTester {

    @Test
    public void testRoute() throws Exception {
        Resource resource = new MemoryResource();
        MemoryLiteral l = new MemoryLiteral("2013")
                .type(IRI.create("xsd:gYear"));
        resource.id(IRI.create("urn:res"))
                .add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("urn:link", IRI.create("urn:pointer"));
        RouteRdfXContentParams params = new RouteRdfXContentParams("index", "type");
        params.setHandler((content, p) -> assertEquals(p.getIndex() + " " + p.getType() + " 1 " + content,
                "index type 1 {\"urn:property\":\"Hello World\",\"urn:date\":2013,\"urn:link\":\"urn:pointer\"}"
                ));
        RdfContentBuilder builder = routeRdfXContentBuilder(params);
        builder.receive(resource);
    }

    @Test
    public void testVIAF() throws Exception {
        InputStream in = getClass().getResource("VIAF.rdf").openStream();
        if (in == null) {
            throw new IOException("VIAF.rdf not found");
        }
        StringBuilder sb = new StringBuilder();
        RouteRdfXContentParams params = new RouteRdfXContentParams("index", "type");
        params.setHandler((content, p) -> {
            //logger.info("handle: {} {} {} {}", p.getIndex(), p.getType(), p.getId(), content);
        });
        new RdfXmlContentParser(in)
                .setRdfContentBuilderProvider(()-> routeRdfXContentBuilder(params))
                .setRdfContentBuilderHandler(builder -> {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(builder.string());
                })
                .parse();
        assertStream(new InputStreamReader(getClass().getResource("viaf.json").openStream()),
                new StringReader(sb.toString()));
    }
}
