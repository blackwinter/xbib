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

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.xbib.helper.StreamTester;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.io.xml.XmlHandler;

import javax.xml.namespace.QName;

import static org.xbib.rdf.RdfContentFactory.turtleBuilder;

public class JsonReaderTest extends StreamTester {

    @Test
    public void testGenericJsonReader() throws Exception {
        String filename = "dc.json";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }

        IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();
        namespaceContext.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        namespaceContext.addNamespace("dcterms", "http://purl.org/dc/terms/");
        namespaceContext.addNamespace("bib", "info:srw/cql-context-set/1/bib-v1/");
        namespaceContext.addNamespace("xbib", "http://xbib.org/");
        namespaceContext.addNamespace("lia", "http://xbib.org/lia/");

        JsonContentParams params = new JsonContentParams(namespaceContext);
        JsonResourceHandler jsonHandler = new JsonResourceHandler(params) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                return false;
            }

            @Override
            public boolean skip(QName name) {
                return false;
            }

            @Override
            public void identify(QName name, String value, IRI identifier) {
                if (identifier == null) {
                    getResource().id(IRI.create("id:doc1"));
                }
            }

            @Override
            public XmlHandler setNamespaceContext(IRINamespaceContext namespaceContext) {
                return this;
            }

            @Override
            public IRINamespaceContext getNamespaceContext() {
                return namespaceContext;
            }
        };
        RdfContentBuilder builder = turtleBuilder();
        jsonHandler.setBuilder(builder);
        new JsonContentParser(in)
                .setHandler(jsonHandler)
                .root(new QName("http://purl.org/dc/elements/1.1/", "root", "dc"))
                .parse();
    }

}
