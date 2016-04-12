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
package org.xbib.sru.searchretrieve;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.util.XMLEventConsumer;

import org.xbib.sru.SRUConstants;
import org.xbib.xml.XMLFilterReader;
import org.xbib.xml.namespace.XmlNamespaceContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SearchRetrieve filter reader
 */
public class SearchRetrieveFilterReader extends XMLFilterReader {

    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    private XmlNamespaceContext namespaceContext;

    private final SearchRetrieveRequest request;

    private String recordPacking;

    private String recordSchema;

    private int recordPosition;

    private String recordIdentifier;

    private String content;

    private List<XMLEventConsumer> consumers = new LinkedList<>();

    private boolean inRecordData;

    private List<XMLEventConsumer> extraConsumers = new LinkedList<>();

    private boolean inExtraRecordData;

    private boolean echo;

    private boolean isRecordIdentifier;

    public SearchRetrieveFilterReader(SearchRetrieveRequest request) {
        this(XmlNamespaceContext.newInstance(), request);
    }

    public SearchRetrieveFilterReader(XmlNamespaceContext namespaceContext, SearchRetrieveRequest request) {
        this.namespaceContext = namespaceContext;
        this.request = request;
    }

    public SearchRetrieveFilterReader addRecordDataConsumer(XMLEventConsumer consumer) {
        if (consumer != null) {
            this.consumers.add(consumer);
        }
        return this;
    }

    public SearchRetrieveFilterReader addExtraRecordDataConsumer(XMLEventConsumer extraConsumer) {
        if (extraConsumer != null) {
            this.extraConsumers.add(extraConsumer);
        }
        return this;
    }

    @Override
    public void startDocument() throws SAXException {
        echo = false;
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localname, String qname, Attributes atts)
            throws SAXException {
        if (SRUConstants.NS_URI.equals(uri)) {
            switch (localname) {
                case "record":
                    recordPacking = null;
                    recordSchema = null;
                    recordIdentifier = null;
                    recordPosition = 0;
                    for (SearchRetrieveListener listener : request.getListeners()) {
                        listener.beginRecord();
                    }

                    break;
                case "recordData":
                    try {
                        for (XMLEventConsumer consumer : consumers) {
                            inRecordData = true;
                            consumer.add(eventFactory.createStartDocument());
                            ListIterator<Namespace> namespaces = getNamespaces(namespaceContext);
                            while (namespaces.hasNext()) {
                                Namespace ns = namespaces.next();
                                consumer.add(eventFactory.createNamespace(ns.getPrefix(), ns.getNamespaceURI()));
                            }
                        }
                    } catch (XMLStreamException e) {
                        throw new SAXException(e);
                    }
                    break;
                case "extraRecordData":
                    try {
                        for (XMLEventConsumer extraConsumer : extraConsumers) {
                            inExtraRecordData = true;
                            extraConsumer.add(eventFactory.createStartDocument());
                            ListIterator<Namespace> namespaces = getNamespaces(namespaceContext);
                            while (namespaces.hasNext()) {
                                Namespace ns = namespaces.next();
                                extraConsumer.add(eventFactory.createNamespace(ns.getPrefix(), ns.getNamespaceURI()));
                            }
                        }
                    } catch (XMLStreamException e) {
                        throw new SAXException(e);
                    }
                    break;
                case "echoedSearchRetrieveRequest":
                    echo = true;

                    break;
            }
        } else {
            isRecordIdentifier = false;
            if (inRecordData) {
                try {
                    QName q = toQName(uri, qname);
                    for (XMLEventConsumer consumer : consumers) {
                        ListIterator<Attribute> attributes = getAttributes(atts);
                        while (attributes.hasNext()) {
                            Attribute a = attributes.next();
                            isRecordIdentifier = "controlfield".equals(q.getLocalPart())
                                    && "tag".equals(a.getName().getLocalPart())
                                    && "001".equals(a.getValue());
                        }
                        consumer.add(eventFactory.createStartElement(q, getAttributes(atts), getNamespaces(namespaceContext)));
                    }
                } catch (XMLStreamException e) {
                    throw new SAXException(e);
                }
            }
            if (inExtraRecordData) {
                try {
                    QName q = toQName(uri, qname);
                    for (XMLEventConsumer extraConsumer : extraConsumers) {
                        extraConsumer.add(eventFactory.createStartElement(q, getAttributes(atts), getNamespaces(namespaceContext)));
                    }
                } catch (XMLStreamException e) {
                    throw new SAXException(e);
                }
            }
        }
        super.startElement(uri, localname, qname, atts);
    }

    @Override
    public void endElement(String uri, String localname, String qname) throws SAXException {
        if (SRUConstants.NS_URI.equals(uri)) {
            if ("recordPacking".equals(localname)) {
                recordPacking = content;
            } else if ("recordSchema".equals(localname)) {
                recordSchema = content;
            } else if ("recordData".equals(localname)) {
                for (XMLEventConsumer consumer : consumers) {
                    try {
                        consumer.add(eventFactory.createEndDocument());
                    } catch (XMLStreamException e) {
                        throw new SAXException(e);
                    }
                }
                inRecordData = false;
            } else if ("extraRecordData".equals(localname)) {
                for (XMLEventConsumer extraConsumer : extraConsumers) {
                    try {
                        extraConsumer.add(eventFactory.createEndDocument());
                    } catch (XMLStreamException e) {
                        throw new SAXException(e);
                    }
                }
                inExtraRecordData = false;
            } else if ("recordPosition".equals(localname)) {
                recordPosition = Integer.parseInt(content);
            } else if ("recordIdentifier".equals(localname)) {
                recordIdentifier = content;
            } else if ("record".equals(localname)) {
                // send metadata about record after record
                for (SearchRetrieveListener listener : request.getListeners()) {
                    listener.recordSchema(recordSchema);
                    listener.recordPacking(recordPacking);
                    listener.recordIdentifier(recordIdentifier);
                    listener.recordPosition(recordPosition);
                    listener.endRecord();
                }
            } else if ("echoedSearchRetrieveRequest".equals(localname)) {
                echo = false;
            } else if ("version".equals(localname) && !echo) {
                for (SearchRetrieveListener listener : request.getListeners()) {
                    listener.version(content);
                }
            } else if ("numberOfRecords".equals(localname)) {
                int n = -1;
                try {
                    n = Integer.parseInt(content);
                } catch (NumberFormatException e) {
                    // drop                    
                }
                for (SearchRetrieveListener listener : request.getListeners()) {
                    listener.numberOfRecords(n);
                }
            }
        } else {
            if (inRecordData) {
                for (XMLEventConsumer consumer : consumers) {
                    QName q = toQName(uri, qname);
                    try {
                        consumer.add(eventFactory.createEndElement(q, getNamespaces(namespaceContext)));
                    } catch (XMLStreamException e) {
                        throw new SAXException(e);
                    }
                }
            }
            if (isRecordIdentifier && recordIdentifier == null) {
                recordIdentifier = content;
            }
            if (inExtraRecordData) {
                for (XMLEventConsumer extraConsumer : extraConsumers) {
                    QName q = toQName(uri, qname);
                    try {
                        extraConsumer.add(eventFactory.createEndElement(q, getNamespaces(namespaceContext)));
                    } catch (XMLStreamException e) {
                        throw new SAXException(e);
                    }
                }
            }
        }
        super.endElement(uri, localname, qname);
    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        this.content = new String(chars, start, length);
        for (XMLEventConsumer consumer : consumers) {
            if (inRecordData) {
                try {
                    consumer.add(eventFactory.createCharacters(content));
                } catch (XMLStreamException e) {
                    throw new SAXException(e);
                }
            }
        }
        for (XMLEventConsumer extraConsumer : extraConsumers) {
            if (inExtraRecordData) {
                try {
                    extraConsumer.add(eventFactory.createCharacters(content));
                } catch (XMLStreamException e) {
                    throw new SAXException(e);
                }
            }
        }
        super.characters(chars, start, length);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        namespaceContext.addNamespace(prefix, uri);
        super.startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // no, we do not remove namespaces. Or you will get in trouble in RDF
        //getNamespaceContext.removeNamespace(prefix);
        super.endPrefixMapping(prefix);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        super.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        for (XMLEventConsumer consumer : consumers) {
            if (inRecordData) {
                try {
                    consumer.add(eventFactory.createProcessingInstruction(target, data));
                } catch (XMLStreamException e) {
                    throw new SAXException(e);
                }
            }
        }
        for (XMLEventConsumer extraConsumer : extraConsumers) {
            if (inExtraRecordData) {
                try {
                    extraConsumer.add(eventFactory.createProcessingInstruction(target, data));
                } catch (XMLStreamException e) {
                    throw new SAXException(e);
                }
            }
        }
        super.processingInstruction(target, data);
    }

    private QName toQName(String namespaceUri, String qname) {
        int i = qname.indexOf(':');
        if (i == -1) {
            return new QName(namespaceUri, qname);
        } else {
            String prefix = qname.substring(0, i);
            String localPart = qname.substring(i + 1);
            return new QName(namespaceUri, localPart, prefix);
        }
    }

    private ListIterator<Attribute> getAttributes(Attributes attributes) {
        List<Attribute> list = new LinkedList<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            QName q = new QName(attributes.getURI(i), attributes.getLocalName(i));
            list.add(eventFactory.createAttribute(q, attributes.getValue(i)));
        }
        return list.listIterator();
    }

    private ListIterator<Namespace> getNamespaces(XmlNamespaceContext namespaceContext) {
        List<Namespace> namespaces = new LinkedList<>();
        String defaultNamespaceUri = namespaceContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
        if (defaultNamespaceUri != null && defaultNamespaceUri.length() > 0) {
            namespaces.add(eventFactory.createNamespace(defaultNamespaceUri));
        }
        for (Map.Entry<String,String> me : namespaceContext.getNamespaces().entrySet()) {
            namespaces.add(eventFactory.createNamespace(me.getKey(), me.getValue()));
        }
        return namespaces.listIterator();
    }
    
}