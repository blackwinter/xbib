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
package org.xbib.tools.convert.oai;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.StringPacket;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.oai.rdf.RdfResourceHandler;
import org.xbib.oai.xml.SimpleMetadataHandler;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.ntriple.NTripleContentParams;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.rdf.memory.MemoryLiteral;
import org.xbib.rdf.XSDResourceIdentifiers;
import org.xbib.tools.OAIHarvester;
import org.xbib.util.concurrent.WorkerProvider;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.text.Normalizer;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

/**
 * OAI harvester, write documents to RDF
 */
public class DOAJ extends OAIHarvester {

    private final static Logger logger = LogManager.getLogger(DOAJ.class);

    protected WorkerProvider provider() {
        return p -> new DOAJ().setPipeline(p);
    }

    protected SimpleMetadataHandler newMetadataHandler() {
        return new MySimpleMetadataHandler();
    }

    public class MySimpleMetadataHandler extends SimpleMetadataHandler {

        private final IRINamespaceContext namespaceContext;

        private RdfResourceHandler handler;

        public MySimpleMetadataHandler() {
            namespaceContext = IRINamespaceContext.newInstance();
            namespaceContext.addNamespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
            namespaceContext.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        }

        @Override
        public void startDocument() throws SAXException {
            this.handler = rdfResourceHandler();
            handler.setDefaultNamespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
            handler.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            handler.endDocument();
            try {
                RouteRdfXContentParams params = new RouteRdfXContentParams(namespaceContext);
                params.setHandler((content, p) -> {
                    if (settings.getAsBoolean("mock", false)) {
                        logger.info("{}", content);
                    } else {
                        StringPacket packet = session.newPacket();
                        packet.name();
                        String s = content;
                        // for Unicode in non-canonical form, normalize it here
                        s = Normalizer.normalize(s, Normalizer.Form.NFC);
                        packet.packet(s);
                        session.write(packet);
                    }
                });
                RdfContentBuilder builder = routeRdfXContentBuilder(params);
                builder.receive(handler.getResource());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new SAXException(e);
            }
        }

        @Override
        public void startPrefixMapping(String string, String string1) throws SAXException {
            handler.startPrefixMapping(string, string1);
        }

        @Override
        public void endPrefixMapping(String string) throws SAXException {
            handler.endPrefixMapping(string);
        }

        @Override
        public void startElement(String ns, String localname, String string2, Attributes atrbts) throws SAXException {
            handler.startElement(ns, localname, string2, atrbts);
        }

        @Override
        public void endElement(String ns, String localname, String string2) throws SAXException {
            handler.endElement(ns, localname, string2);
        }

        @Override
        public void characters(char[] chars, int i, int i1) throws SAXException {
            handler.characters(chars, i, i1);
        }
    }

    protected RdfResourceHandler rdfResourceHandler() {
        return resourceHandler;
    }

    private final static RdfResourceHandler resourceHandler = new DOAJResourceHandler(NTripleContentParams.DEFAULT_PARAMS);

    private static class DOAJResourceHandler extends RdfResourceHandler {

        public DOAJResourceHandler(RdfContentParams params) {
            super(params);
        }

        @Override
        public IRI toProperty(IRI property) {
            if ("issn".equals(property.getSchemeSpecificPart())) {
                return IRI.builder().curie("dc", "identifier").build();
            }
            if ("eissn".equals(property.getSchemeSpecificPart())) {
                return IRI.builder().curie("dc", "identifier").build();
            }
            return property;
        }

        @Override
        public Object toObject(QName name, String content) {
            switch (name.getLocalPart()) {
                case "identifier": {
                    if (content.startsWith("http://")) {
                        return new MemoryLiteral(content).type(XSDResourceIdentifiers.ANYURI);
                    }
                    if (content.startsWith("issn: ")) {
                        return new MemoryLiteral(content.substring(6)).type(ISSN);
                    }
                    if (content.startsWith("eissn: ")) {
                        return new MemoryLiteral(content.substring(7)).type(EISSN);
                    }
                    break;
                }
                case "subject": {
                    if (content.startsWith("LCC: ")) {
                        return new MemoryLiteral(content.substring(5)).type(LCCN);
                    }
                    break;
                }
                case "issn": {
                    return new MemoryLiteral(content.substring(0, 4) + "-" + content.substring(4)).type(ISSN);
                }
                case "eissn": {
                    return new MemoryLiteral(content.substring(0, 4) + "-" + content.substring(4)).type(EISSN);
                }
            }
            return super.toObject(name, content);
        }

        @Override
        public XmlHandler setNamespaceContext(IRINamespaceContext namespaceContext) {
            return null;
        }

        @Override
        public IRINamespaceContext getNamespaceContext() {
            return null;
        }
    }

    private final static IRI ISSN = IRI.create("urn:ISSN");

    private final static IRI EISSN = IRI.create("urn:EISSN");

    private final static IRI LCCN = IRI.create("urn:LCC");

}
