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
package org.xbib.rdf.io.xml;

import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.RdfContentGenerator;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.memory.MemoryResource;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.util.XMLEventConsumer;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

/**
 * Write resource as XML to stream
 */
public class XmlContentGenerator
        implements RdfContentGenerator<XmlContentParams>, Flushable, XmlConstants {

    private final Writer writer;

    private final static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    private final static XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    private Resource resource;

    private XmlContentParams params = XmlContentParams.DEFAULT_PARAMS;

    public XmlContentGenerator(OutputStream out) throws IOException{
        this(new OutputStreamWriter(out, "UTF-8"));
    }
    public XmlContentGenerator(Writer writer) throws IOException {
        this.writer = writer;
    }

    public XmlContentParams getParams() {
        return params;
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public XmlContentGenerator receive(IRI iri) throws IOException {
        if (!iri.equals(resource.id())) {
            receive(resource);
            resource = new MemoryResource(iri);
        }
        return this;
    }

    @Override
    public RdfContentGenerator setParams(XmlContentParams rdfContentParams) {
        this.params = rdfContentParams;
        return this;
    }

    @Override
    public XmlContentGenerator startStream() {
        return this;
    }

    @Override
    public RdfContentGenerator setBaseUri(String baseUri) {
        startPrefixMapping("", baseUri);
        return this;
    }

    @Override
    public XmlContentGenerator receive(Triple triple) {
        resource.add(triple);
        return this;
    }

    @Override
    public XmlContentGenerator endStream() {
        return this;
    }

    @Override
    public XmlContentGenerator startPrefixMapping(String prefix, String uri) {
        if (prefix == null || prefix.isEmpty() || XML_SCHEMA_URI.equals(uri)) {
            return this;
        }
        params.getNamespaceContext().addNamespace(prefix, uri);
        return this;
    }

    @Override
    public XmlContentGenerator endPrefixMapping(String prefix) {
        // we don't remove name spaces. It's troubling RDF serializations.
        //namespaceContext.removeNamespace(prefix);
        return this;
    }

    @Override
    public XmlContentGenerator receive(Resource resource) throws IOException {
        if (resource == null) {
            return this;
        }
        try {
            XMLEventWriter xew = outputFactory.createXMLEventWriter(writer);
            IRI resourceURI = resource.id();
            String nsPrefix = resourceURI.getScheme();
            String name = resourceURI.getSchemeSpecificPart();
            String nsURI = params.getNamespaceContext().getNamespaceURI(nsPrefix);
            writeResource(xew, resource, new QName(nsURI, name, nsPrefix));
            xew.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        return this;
    }


    private void writeResource(XMLEventConsumer consumer, Resource resource, QName parent)
            throws XMLStreamException {
        boolean startElementWritten = false;
        List<Triple> triples = resource.properties();
        for (Triple triple : triples) {
            if (!startElementWritten) {
                if (parent != null) {
                    consumer.add(eventFactory.createStartElement(parent, null, null));
                }
                startElementWritten = true;
            }
            write(consumer, triple);
        }
        if (!startElementWritten) {
            if (parent != null) {
                consumer.add(eventFactory.createStartElement(parent, null, null));
            }
        }
        if (parent != null) {
            consumer.add(eventFactory.createEndElement(parent, null));
        }
    }

    private void write(XMLEventConsumer consumer, Triple triple)
            throws XMLStreamException {
        IRI predicate = triple.predicate();
        Node object = triple.object();
        String nsPrefix = predicate.getScheme();
        String name = predicate.getSchemeSpecificPart();
        String nsURI = params.getNamespaceContext().getNamespaceURI(nsPrefix);
        if (object instanceof Resource) {
            writeResource(consumer, (Resource) object, new QName(nsURI, name, nsPrefix));
        } else if (object instanceof Literal) {
            String literal = ((Literal) object).object().toString();
            consumer.add(eventFactory.createStartElement(nsPrefix, nsURI, name));
            consumer.add(eventFactory.createCharacters(literal));
            consumer.add(eventFactory.createEndElement(nsPrefix, nsURI, name));
        } else {
            throw new XMLStreamException("can't write object class: " + object.getClass().getName());
        }

    }
}
