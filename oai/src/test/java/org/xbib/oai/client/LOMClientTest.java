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
package org.xbib.oai.client;

import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.OAIDateResolution;
import org.xbib.oai.client.listrecords.ListRecordsListener;
import org.xbib.oai.client.listrecords.ListRecordsRequest;
import org.xbib.oai.rdf.RdfResourceHandler;
import org.xbib.oai.xml.MetadataHandler;
import org.xbib.oai.xml.XmlMetadataHandler;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.DefaultResourceContentBuilder;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.util.DateUtil;
import org.xbib.xml.XMLNS;
import org.xbib.xml.XSI;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.io.StringWriter;

public class LOMClientTest {

    private final Logger logger = LoggerFactory.getLogger(LOMClientTest.class.getName());

    @Test
    public void testListRecordsLOM() throws Exception {

        OAIClient client = OAIClientFactory.newClient("http://www.melt.fwu.de/oai2.php");
        ListRecordsRequest request = client.newListRecordsRequest()
                .setFrom(DateUtil.parseDateISO("2008-04-04T00:00:00Z"), OAIDateResolution.DAY)
                .setUntil(DateUtil.parseDateISO("2014-04-05T00:00:00Z"), OAIDateResolution.DAY)
                .setMetadataPrefix("oai_lom");

        do {
            try {
                ListRecordsListener listener = new ListRecordsListener(request);
                request.addHandler(xmlMetadataHandler());
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    StringWriter sw = new StringWriter();
                    listener.getResponse().to(sw);
                } else {
                    logger.warn("no response");
                }
                request = client.resume(request, listener.getResumptionToken());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        } while (request != null);
        client.close();
    }

    protected MetadataHandler xmlMetadataHandler() {
        IRINamespaceContext namespaceContext = IRINamespaceContext.getInstance();
        ResourceContext<Resource> resourceContext = new SimpleResourceContext()
                .setContentBuilder(new DefaultResourceContentBuilder())
                .setNamespaceContext(namespaceContext);
        resourceContext.setNamespaceContext(IRINamespaceContext.getInstance());
        RdfResourceHandler handler = new RdfResourceHandler(resourceContext);
        return new LOMHandler()
                .setHandler(handler)
                .setResourceContext(handler.resourceContext());
    }

    class LOMHandler extends XmlMetadataHandler {

        private XmlHandler handler;

        private ResourceContext<Resource> resourceContext;

        private boolean attributes;

        private String uri;

        private String qname;

        public LOMHandler setHandler(XmlHandler handler) {
            this.handler = handler;
            this.handler.setDefaultNamespace("oai_dc","http://www.openarchives.org/OAI/2.0/oai_dc/");
            return this;
        }

        public LOMHandler setResourceContext(ResourceContext<Resource> resourceContext) {
            this.resourceContext = resourceContext;
            return this;
        }

        @Override
        public void startDocument() throws SAXException {
            handler.startDocument();
        }

        public void endDocument() throws SAXException {
            handler.endDocument();
            String identifier = getHeader().getIdentifier();
                if (resourceContext.getResource() != null) {
                    IRI iri = IRI.builder().scheme("http")
                            .host("test")
                            .query("test")
                            .fragment(identifier).build();
                    resourceContext.getResource().id(iri);
                }
                //output.write(resourceContext);
                if (resourceContext.getResources() == null) {
                    // single document
                    //mock.output(resourceContext, resourceContext.getResource(), resourceContext.getContentBuilder());
                } else for (Resource resource : resourceContext.getResources()) {
                    // multiple documents. Rewrite IRI for ES index/type addressing
                    String index = "test";
                    String type = "test";
                    if (index.equals(resource.id().getHost())) {
                        IRI iri = IRI.builder().scheme("http").host(index).query(type)
                                .fragment(resource.id().getFragment()).build();
                        resource.add("iri", resource.id().getFragment());
                        resource.id(iri);
                    } else {
                        IRI iri = IRI.builder().scheme("http").host(index).query(type)
                                .fragment(resource.id().toString()).build();
                        resource.add("iri", resource.id().toString());
                        resource.id(iri);
                    }
                    //mock.output(resourceContext, resource, resourceContext.getContentBuilder());
                }
            try {
                StringWriter sw = new StringWriter();
                NTripleWriter writer = new NTripleWriter(sw);
                writer.write(resourceContext);
                logger.info("{}", sw.toString());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public void startPrefixMapping(String prefix, String nsURI) throws SAXException {
            handler.startPrefixMapping(prefix, nsURI);
            resourceContext.getNamespaceContext().addNamespace(prefix, nsURI);
            if ("".equals(prefix)) {
                handler.setDefaultNamespace("oai_lom", nsURI);
                resourceContext.getNamespaceContext().addNamespace("oai_lom", nsURI);
            }
        }

        @Override
        public void endPrefixMapping(String string) throws SAXException {
            handler.endPrefixMapping(string);
        }

        @Override
        public void startElement(String ns, String localname, String string2, Attributes atrbts) throws SAXException {
            handler.startElement(ns, localname, string2, atrbts);
            attributes = false;
            uri = ns;
            this.qname = string2;
            for (int i = 0; i < atrbts.getLength(); i++) {
                String uri = atrbts.getURI(i);
                String qname = atrbts.getQName(i);
                char[] ch = atrbts.getValue(i).toCharArray();
                if (!qname.startsWith(XMLNS.NS_PREFIX) && !XSI.NS_URI.equals(uri)) {
                    String localName = "attr_" + atrbts.getLocalName(i);
                    int pos = qname.indexOf(':');
                    String attr_qname = pos > 0 ? qname.substring(0,pos+1) + localName : localName;
                    handler.startElement(uri, localName, attr_qname, emptyAttributes);
                    handler.characters(ch, 0, ch.length);
                    handler.endElement(uri, localName, attr_qname);
                    attributes = true;
                } else if (qname.startsWith(XMLNS.NS_PREFIX) && !XSI.NS_URI.equals(uri)) {
                    int pos = qname.indexOf(':');
                    String prefix = pos > 0 ? qname.substring(pos+1) : "xmlns";
                    handler.startElement(XMLNS.NS_URI, "_namespace", XMLNS.NS_PREFIX + ":_namespace", emptyAttributes);
                    handler.startElement(XMLNS.NS_URI, "_prefix", XMLNS.NS_PREFIX + ":_prefix", emptyAttributes);
                    char[] p = prefix.toCharArray();
                    handler.characters(p, 0, p.length);
                    handler.endElement(XMLNS.NS_URI, "_prefix", XMLNS.NS_PREFIX + ":_prefix");
                    handler.startElement(XMLNS.NS_URI, "_uri", XMLNS.NS_PREFIX + ":_uri", emptyAttributes);
                    handler.characters(ch, 0, ch.length);
                    handler.endElement(XMLNS.NS_URI, "_uri", XMLNS.NS_PREFIX + ":_uri");
                    handler.endElement(XMLNS.NS_URI, "_namespace", XMLNS.NS_PREFIX + ":_namespace");
                    attributes = true;
                }
            }
        }

        @Override
        public void endElement(String ns, String localname, String string2) throws SAXException {
            handler.endElement(ns, localname, string2);
        }

        @Override
        public void characters(char[] chars, int i, int i1) throws SAXException {
            if (attributes) {
                handler.startElement(uri, "_value", qname, emptyAttributes);
            }
            handler.characters(chars, i, i1);
            if (attributes) {
                handler.endElement(uri, "_value", qname);
            }
        }

        private final Attributes emptyAttributes = new AttributesImpl();
    }

}