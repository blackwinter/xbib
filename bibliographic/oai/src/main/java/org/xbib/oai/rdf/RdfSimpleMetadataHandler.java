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
package org.xbib.oai.rdf;

import org.xbib.iri.IRI;
import org.xbib.oai.OAIConstants;
import org.xbib.oai.xml.SimpleMetadataHandler;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.Resource;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.io.xml.XmlResourceHandler;
import org.xbib.rdf.memory.MemoryResource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 *  RDF metadata handler
 */
public class RdfSimpleMetadataHandler extends SimpleMetadataHandler implements OAIConstants {

    private RdfResourceHandler handler;

    private Resource resource;

    private RdfContentBuilder builder;

    private RdfContentParams params;

    private IRINamespaceContext namespaceContext;

    public static IRINamespaceContext getDefaultContext() {
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace(DC_PREFIX, DC_NS_URI);
        context.addNamespace(OAIDC_NS_PREFIX, OAIDC_NS_URI);
        return context;
    }

    public RdfSimpleMetadataHandler() {
        this(RdfSimpleMetadataHandler::getDefaultContext);
    }

    public RdfSimpleMetadataHandler(RdfContentParams params) {
        this.params = params;
        this.resource = new MemoryResource();
        //resourceContext.setNamespaceContext(context);
        //resourceContext.newResource();
        // set up our default handler
        this.handler = new RdfResourceHandler(params);
        handler.setDefaultNamespace(NS_PREFIX, NS_URI);
    }

    public IRINamespaceContext getContext() {
        return params.getNamespaceContext();
    }

    public Resource getResource() {
        return resource;
    }

    /*public RdfMetadataHandler setResourceContext(Context<Resource> context) {
        this.resourceContext = context;
        return this;
    }

    public Context<Resource> getResourceContext() {
        return resourceContext;
    }
    */

    public RdfSimpleMetadataHandler setHandler(RdfResourceHandler handler) {
        handler.setDefaultNamespace(NS_PREFIX, NS_URI);
        this.handler = handler;
        /*if (resourceWriter != null) {
            //handler.setBuilder(resourceContextWriter);
        }*/
        return this;
    }

    public XmlResourceHandler getHandler() {
        return handler;
    }

    /*public RdfMetadataHandler setResourceWriter(ResourceWriter resourceWriter) {
        if (resourceWriter != null) {
            this.resourceWriter = resourceWriter;
            //handler.setBuilder(resourceContextWriter);
        }
        return this;
    }*/

    public RdfSimpleMetadataHandler setBuilder(RdfContentBuilder builder) {
        this.builder = builder;
        return this;
    }

    @Override
    public void startDocument() throws SAXException {
        if (handler != null) {
            handler.startDocument();
        }
    }

    /**
     * At the endStream of each OAI metadata, the resource context receives the identifier from
     * the metadata header. The resource context is pushed to the RDF output.
     * Any IOException is converted to a SAXException.
     *
     * @throws SAXException
     */
    @Override
    public void endDocument() throws SAXException {
        String id = getHeader().getIdentifier().trim();
        if (handler != null) {
            handler.identify(null, id, null);
            resource.id(IRI.create(id));
            handler.endDocument();
            try {
                if (builder != null) {
                    builder.receive(resource);
                }
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String namespaceURI) throws SAXException {
        if (handler != null) {
            handler.startPrefixMapping(prefix, namespaceURI);
            if (prefix.isEmpty()) {
                handler.setDefaultNamespace("oai", namespaceURI);
            }
        }
    }

    @Override
    public void endPrefixMapping(String string) throws SAXException {
        if (handler != null) {
            handler.endPrefixMapping(string);
        }
    }

    @Override
    public void startElement(String ns, String localname, String string2, Attributes atrbts) throws SAXException {
        if (handler != null) {
            handler.startElement(ns, localname, string2, atrbts);
        }
    }

    @Override
    public void endElement(String ns, String localname, String string2) throws SAXException {
        if (handler != null) {
            handler.endElement(ns, localname, string2);
        }
    }

    @Override
    public void characters(char[] chars, int i, int i1) throws SAXException {
        if (handler != null) {
            handler.characters(chars, i, i1);
        }
    }

}
