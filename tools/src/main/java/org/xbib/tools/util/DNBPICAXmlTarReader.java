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
package org.xbib.tools.util;

import org.xbib.io.Connection;
import org.xbib.io.ConnectionService;
import org.xbib.io.Packet;
import org.xbib.io.Session;
import org.xbib.io.archive.tar.TarSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.dialects.pica.DNBPICAConstants;
import org.xbib.metric.MeterMetric;
import org.xbib.pipeline.AbstractPipeline;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineException;
import org.xbib.pipeline.element.LongPipelineElement;
import org.xbib.util.Strings;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;


public class DNBPICAXmlTarReader<P extends Packet> extends AbstractPipeline<LongPipelineElement, PipelineException>
        implements DNBPICAConstants, MarcXchangeListener {

    private final Logger logger = LoggerFactory.getLogger(DNBPICAXmlTarReader.class.getName());

    private final ConnectionService<TarSession> service = ConnectionService.getInstance();

    private final XMLInputFactory factory = XMLInputFactory.newInstance();

    private final LongPipelineElement counter = new LongPipelineElement().set(new AtomicLong(0L));

    private URI uri;

    private Iterator<Long> iterator;

    private Connection<TarSession> connection;

    private TarSession session;

    private P packet;

    private boolean prepared;

    private String clob;

    private StringBuilder sb = new StringBuilder();

    private MarcXchangeListener listener;

    public DNBPICAXmlTarReader() {
    }

    public DNBPICAXmlTarReader setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    public DNBPICAXmlTarReader setIterator(Iterator<Long> iterator) {
        this.iterator = iterator;
        return this;
    }

    public DNBPICAXmlTarReader setListener(MarcXchangeListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void beginCollection() {

    }

    @Override
    public void endCollection() {

    }

    @Override
    public void beginRecord(String format, String type) {
        if (listener != null) {
            listener.beginRecord(format, type);
        }
    }

    @Override
    public void endRecord() {
        if (listener != null) {
            listener.endRecord();
        }
    }

    @Override
    public void leader(String label) {
        if (listener != null) {
            listener.leader(label);
        }
    }

    @Override
    public void beginControlField(Field field) {
        if (listener != null) {
            listener.beginControlField(field);
        }
    }

    @Override
    public void endControlField(Field field) {
        if (listener != null) {
            listener.endControlField(field);
        }
    }

    @Override
    public void beginDataField(Field field) {
        if (listener != null) {
            listener.beginDataField(field);
        }
    }

    @Override
    public void endDataField(Field field) {
        if (listener != null) {
            listener.endDataField(field);
        }
    }

    @Override
    public void beginSubField(Field field) {
        if (listener != null) {
            listener.beginSubField(field);
        }
    }

    @Override
    public void endSubField(Field field) {
        if (listener != null) {
            listener.endSubField(field);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return prepareRead();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public LongPipelineElement next() {
        return nextRead();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void newRequest(Pipeline<MeterMetric, LongPipelineElement> pipeline, LongPipelineElement request) {
        try {
            try (StringReader sr = new StringReader(clob)) {
                XMLEventReader xmlReader = factory.createXMLEventReader(sr);
                Stack<Field> stack = new Stack();
                while (xmlReader.hasNext()) {
                    processEvent(stack, xmlReader.peek());
                    xmlReader.nextEvent();
                }
            }
        } catch (XMLStreamException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void error(Pipeline<MeterMetric, LongPipelineElement> pipeline, LongPipelineElement request, PipelineException error) {
        logger.error(error.getMessage(), error);

    }

    @Override
    public void close() throws IOException {
        if (session != null) {
            session.close();
            logger.info("session closed");
        }
        if (connection != null) {
            connection.close();
            logger.info("connection closed");
        }
    }

    private boolean prepareRead() throws IOException {
        try {
            if (prepared) {
                return true;
            }
            if (session == null) {
                createSession();
            }
            this.packet = (P) read(session);
            this.prepared = packet != null;
            if (prepared) {
                nextNumber();
                clob = packet.toString();
            }
            return prepared;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }

    private LongPipelineElement nextRead() {
        if (clob == null || clob.length() == 0) {
            // special case, message length 0 means deletion
            return null;
        }
        prepared = false;
        return counter;
    }

    private void processEvent(Stack<Field> stack, XMLEvent event) {
        if (event.isStartElement()) {
            StartElement element = (StartElement) event;
            String localName = element.getName().getLocalPart();
            Iterator<?> it = element.getAttributes();
            String format = null;
            String type = null;
            String tag = null;
            String indicator = null;
            String subfield = null;
            Field field = null;
            while (it.hasNext()) {
                Attribute attr = (Attribute) it.next();
                QName attributeName = attr.getName();
                String attributeLocalName = attributeName.getLocalPart();
                String attributeValue = attr.getValue();
                switch (attributeLocalName) {
                    case ID:
                        if (attributeValue.length() > 3) {
                            tag = attributeValue.substring(0, 3);
                            indicator = attributeValue.substring(3);
                        } else if (attributeValue.length() == 1) {
                            subfield = attributeValue;
                        }
                        break;
                }
            }
            switch (localName) {
                case RECORD: {
                    // get format and type
                    format = "Pica";
                    type = " XML";
                    beginRecord(format, type);
                    break;
                }
                case TAG: {
                    field = new Field().tag(tag).indicator(indicator);
                    stack.push(field);
                    beginDataField(field);
                    break;
                }
                case SUBF: {
                    Field f = stack.peek();
                    field = new Field().tag(f.tag()).indicator(f.indicator());
                    field.subfieldId(subfield); // reset sub field ID
                    field.data(null); // reset data
                    stack.push(field);
                    beginSubField(field);
                    break;
                }
                case GLOBAL: {
                    // ignore
                    break;
                }
                default: {
                    logger.error("unknown element {}", localName);
                    throw new IllegalArgumentException("unknown begin element: " + uri + " " + localName);
                }
            }
        } else if (event.isCharacters()) {
            Characters c = (Characters) event;
            if (!c.isIgnorableWhiteSpace()) {
                sb.append(c.getData());
            }
        } else if (event.isEndElement()) {
            EndElement element = (EndElement) event;
            String localName = element.getName().getLocalPart();
            switch (localName) {
                case SUBF:
                    stack.peek().data(sb.toString());
                    endSubField(stack.pop());
                    break;
                case TAG:
                    // can't have data
                    endDataField(stack.pop());
                    break;
                case RECORD:
                    //if (inRecord) {
                    endRecord();
                    //inRecord = false;
                    //}
                    break;
                case GLOBAL: {
                    // ignore
                    break;
                }
                default: {
                    logger.error("unknown element {}", localName);
                    // stop processing, this is fatal
                    throw new IllegalArgumentException("unknown end element: " + uri + " " + localName);
                }
            }
            sb.setLength(0);
        }
    }

    private void createSession() throws IOException {
        this.connection = service
                .getConnectionFactory(uri)
                .getConnection(uri);
        this.session = connection.createSession();
        session.open(Session.Mode.READ);
        if (!session.isOpen()) {
            throw new IOException("session could not be opened");
        }
    }

    /**
     * Like files on ancient magnetic tape. Move forward to the packet we want, compare the number
     * until the desired one.
     *
     * @return number
     * @throws IOException
     */
    private String nextNumber() throws IOException {
        String name = packet.name();
        int pos = name == null ? -1 : name.lastIndexOf('/');
        String numberStr = pos >= 0 ? name.substring(pos + 1) : null;
        while ((pos < 0) || Strings.isNullOrEmpty(numberStr)) {
            logger.warn("skipping packet {}, number does not match", name);
            // next message
            packet = (P) read(session);
            name = packet.name();
            pos = name == null ? -1 : name.lastIndexOf('/');
            numberStr = pos >= 0 ? name.substring(pos + 1) : null;
        }
        return numberStr;
    }

    /**
     * Read packet, optionally check if iterator gives enough numbers
     * (assuming iterator counts from 1)
     *
     * @param session the session
     * @return packet
     * @throws java.io.IOException
     */
    private Packet read(Session session) throws IOException {
        if (iterator != null) {
            if (iterator.hasNext()) {
                iterator.next();
                return session.read();
            } else {
                throw new EOFException("end of iterator");
            }
        } else {
            return session.read();
        }
    }
}
