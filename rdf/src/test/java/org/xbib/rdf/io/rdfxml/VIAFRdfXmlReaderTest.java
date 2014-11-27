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

import org.testng.annotations.Test;
import org.xbib.helper.StreamTester;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.io.turtle.TurtleContentParams;

import java.io.IOException;
import java.io.InputStream;

import static org.xbib.rdf.RdfContentFactory.turtleBuilder;

public class VIAFRdfXmlReaderTest extends StreamTester {

    @Test
    public void testVIAF() throws Exception {
        InputStream in = getClass().getResource("VIAF.rdf").openStream();
        if (in == null) {
            throw new IOException("VIAF.rdf not found");
        }
        TurtleContentParams params = new TurtleContentParams(IRINamespaceContext.getInstance(), false);
        RdfContentBuilder builder = turtleBuilder(params);
        RdfXmlContentParser reader = new RdfXmlContentParser(in);
        reader.setBuilder(builder);
        reader.parse();
        assertStream(getClass().getResource("viaf.ttl").openStream(),
                builder.streamInput());
    }
}
