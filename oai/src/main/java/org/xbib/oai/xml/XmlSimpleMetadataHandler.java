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
package org.xbib.oai.xml;

import org.xbib.oai.OAIConstants;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class XmlSimpleMetadataHandler extends SimpleMetadataHandler implements OAIConstants {

    private final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    private List namespaces = new ArrayList();

    private Stack nsStack = new Stack();

    private Locator locator;

    private XMLEventWriter eventWriter;

    private Writer writer;

    private String id;

    private boolean needToCallStartDocument = false;

    public XmlSimpleMetadataHandler setWriter(Writer writer) {
        this.writer = writer;
        try {
            outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);
            this.eventWriter = outputFactory.createXMLEventWriter(writer);
        } catch (XMLStreamException e) {
            // ignore
        }
        return this;
    }

    public Writer getWriter() {
        return writer;
    }

    public XmlSimpleMetadataHandler setEventWriter(XMLEventWriter eventWriter) {
        this.eventWriter = eventWriter;
        return this;
    }

    public XMLEventWriter getEventWriter() {
        return eventWriter;
    }

    public String getIdentifier() {
        return id;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public Location getCurrentLocation() {
        if (locator != null) {
            return new SAXLocation(locator);
        } else {
            return null;
        }
    }

    @Override
    public void startDocument() throws SAXException {
        if (eventWriter == null) {
            return;
        }
        namespaces.clear();
        nsStack.clear();
        eventFactory.setLocation(getCurrentLocation());
        needToCallStartDocument = true;
    }

    @Override
    public void endDocument() throws SAXException {
        if (eventWriter == null) {
            return;
        }
        this.id = getHeader().getIdentifier().trim();
        try {
            eventFactory.setLocation(getCurrentLocation());
            eventWriter.add(eventFactory.createEndDocument());
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
        namespaces.clear();
        nsStack.clear();
    }

    @Override
    public void startPrefixMapping(String prefix, String namespaceURI) throws SAXException {
        if (eventWriter == null) {
            return;
        }
        if (prefix == null) {
            prefix = "";
        } else if (prefix.equals("xml")) {
            return;
        }
        if (namespaces == null) {
            namespaces = new ArrayList();
        }
        namespaces.add(prefix);
        namespaces.add(namespaceURI);
    }

    @Override
    public void endPrefixMapping(String string) throws SAXException {
    }

    @Override
    public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
        if (eventWriter == null) {
            return;
        }
        if (needToCallStartDocument) {
            try {
                eventWriter.add(eventFactory.createStartDocument());
            } catch (XMLStreamException e) {
                // is thrown because of document encoding - commented out
                //throw new SAXException(e);
            }
            needToCallStartDocument = false;
        }
        Collection[] events = {null, null};
        createStartEvents(attributes, events);
        nsStack.add(events[0]);
        try {
            String[] q = {null, null};
            parseQName(qname, q);
            eventFactory.setLocation(getCurrentLocation());
            eventWriter.add(eventFactory.createStartElement(q[0], uri,
                    q[1], events[1].iterator(), events[0].iterator()));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localname, String qname) throws SAXException {
        if (eventWriter == null) {
            return;
        }
        String[] q = {null, null};
        parseQName(qname, q);
        Collection nsList = (Collection) nsStack.remove(nsStack.size() - 1);
        Iterator nsIter = nsList.iterator();
        try {
            eventFactory.setLocation(getCurrentLocation());
            eventWriter.add(eventFactory.createEndElement(q[0], uri, q[1], nsIter));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void characters(char[] chars, int i, int i1) throws SAXException {
        if (eventWriter == null) {
            return;
        }
        try {
            eventFactory.setLocation(getCurrentLocation());
            eventWriter.add(eventFactory.createCharacters(new String(chars, i, i1)));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    private void createStartEvents(Attributes attributes, Collection[] events) {
        Map nsMap = null;
        List attrs = null;
        if (namespaces != null) {
            final int nDecls = namespaces.size();
            for (int i = 0; i < nDecls; i++) {
                final String prefix = (String) namespaces.get(i);
                String uri = (String) namespaces.get(i++);
                Namespace ns = createNamespace(prefix, uri);
                if (nsMap == null) {
                    nsMap = new HashMap();
                }
                nsMap.put(prefix, ns);
            }
        }
        String[] qname = {null, null};
        for (int i = 0, s = attributes.getLength(); i < s; i++) {
            parseQName(attributes.getQName(i), qname);
            String attrPrefix = qname[0];
            String attrLocal = qname[1];
            String attrQName = attributes.getQName(i);
            String attrValue = attributes.getValue(i);
            String attrURI = attributes.getURI(i);
            if ("xmlns".equals(attrQName) || "xmlns".equals(attrPrefix)) {
                if (!attrValue.isEmpty() && !nsMap.containsKey(attrPrefix)) {
                    Namespace ns = createNamespace(attrPrefix, attrValue);
                    if (nsMap == null) {
                        nsMap = new HashMap();
                    }
                    nsMap.put(attrPrefix, ns);
                }
            } else {
                Attribute attribute;
                if (attrPrefix.length() > 0 && !attrValue.isEmpty()) {
                    attribute = eventFactory.createAttribute(attrPrefix, attrURI, attrLocal, attrValue);
                } else {
                    attribute = eventFactory.createAttribute(attrLocal, attrValue);
                }
                if (attrs == null) {
                    attrs = new ArrayList();
                }
                attrs.add(attribute);
            }
        }
        events[0] = nsMap == null ? Collections.EMPTY_LIST : nsMap.values();
        events[1] = attrs == null ? Collections.EMPTY_LIST : attrs;
    }

    private void parseQName(String qName, String[] results) {
        String prefix, local;
        int idx = qName.indexOf(':');
        if (idx >= 0) {
            prefix = qName.substring(0, idx);
            local = qName.substring(idx + 1);
        } else {
            prefix = "";
            local = qName;
        }
        results[0] = prefix;
        results[1] = local;
    }

    private Namespace createNamespace(String prefix, String uri) {
        if (prefix == null || prefix.length() == 0) {
            return eventFactory.createNamespace(uri);
        } else {
            return eventFactory.createNamespace(prefix, uri);
        }
    }

    private static final class SAXLocation implements Location {
        private int lineNumber;
        private int columnNumber;
        private String publicId;
        private String systemId;

        private SAXLocation(Locator locator) {
            lineNumber = locator.getLineNumber();
            columnNumber = locator.getColumnNumber();
            publicId = locator.getPublicId();
            systemId = locator.getSystemId();
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public int getColumnNumber() {
            return columnNumber;
        }

        public int getCharacterOffset() {
            return -1;
        }

        public String getPublicId() {
            return publicId;
        }

        public String getSystemId() {
            return systemId;
        }
    }

}
