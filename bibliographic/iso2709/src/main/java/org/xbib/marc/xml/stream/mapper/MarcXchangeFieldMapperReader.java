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
package org.xbib.marc.xml.stream.mapper;

import org.xbib.marc.Field;
import org.xbib.marc.FieldReader;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.event.FieldEvent;
import org.xbib.marc.event.RecordEvent;
import org.xbib.marc.event.EventListener;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.marc.xml.mapper.MarcXchangeFieldMapper;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * The MarcXchange mapping event consumer reads StaX events, maps the MarcXchange fields to
 * other fields, and fires MarcXchange events
 */
public class MarcXchangeFieldMapperReader extends MarcXchangeFieldMapper
    implements FieldReader, XMLEventConsumer, MarcXchangeConstants {

    private final Reader reader;

    private Stack<Field> stack = new Stack<Field>();

    private Map<String,MarcXchangeListener> listeners = new HashMap<String,MarcXchangeListener>();

    private MarcXchangeListener listener;

    private Map<String, StringTransformer> transformers = new HashMap<String, StringTransformer>();

    private EventListener<FieldEvent> fieldEventListener;

    private EventListener<RecordEvent> recordEventListener;

    private StringBuilder content = new StringBuilder();

    private String format = MARC21;

    private String type = BIBLIOGRAPHIC;

    private String recordIdentifier;

    protected boolean inData;

    protected boolean inLeader;

    protected boolean inControl;

    private boolean ignoreNamespace = false;

    private boolean transform = false;

    private Set<String> validNamespaces = new HashSet<String>() {{
        add(MARCXCHANGE_V1_NS_URI);
        add(MARCXCHANGE_V2_NS_URI);
        add(MARC21_NS_URI);
    }};

    public MarcXchangeFieldMapperReader(InputStream in) throws IOException {
        this(new InputStreamReader(in, "UTF-8"));
    }

    public MarcXchangeFieldMapperReader(Reader reader) {
        this.reader = reader;
    }


    public MarcXchangeFieldMapperReader setMarcXchangeListener(String type, MarcXchangeListener listener) {
        this.listeners.put(type, listener);
        return this;
    }

    public MarcXchangeFieldMapperReader setMarcXchangeListener(MarcXchangeListener listener) {
        this.listeners.put(BIBLIOGRAPHIC, listener);
        return this;
    }

    public MarcXchangeFieldMapperReader setFormat(String format) {
        this.format = format;
        return this;
    }

    public MarcXchangeFieldMapperReader setType(String type) {
        this.type = type;
        return this;
    }

    public MarcXchangeFieldMapperReader setIgnoreNamespace(boolean ignore) {
        this.ignoreNamespace = ignore;
        return this;
    }

    public MarcXchangeFieldMapperReader setTransform(boolean transform) {
        this.transform = transform;
        return this;
    }

    public MarcXchangeFieldMapperReader addNamespace(String uri) {
        this.validNamespaces.add(uri);
        return this;
    }

    public MarcXchangeFieldMapperReader setFieldEventListener(EventListener<FieldEvent> fieldEventListener) {
        this.fieldEventListener = fieldEventListener;
        return this;
    }

    public MarcXchangeFieldMapperReader setRecordEventListener(EventListener<RecordEvent> recordEventListener) {
        this.recordEventListener = recordEventListener;
        return this;
    }

    public MarcXchangeFieldMapperReader setTransformer(String fieldKey, StringTransformer transformer) {
        this.transformers.put(fieldKey, transformer);
        return this;
    }

    @Override
    public MarcXchangeFieldMapperReader addFieldMap(String fieldMapName, Map<String,Object> fieldMap) {
        super.addFieldMap(fieldMapName, fieldMap);
        return this;
    }

    public void parse() throws IOException {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader xmlEventReader = inputFactory.createXMLEventReader(reader);
            while (xmlEventReader.hasNext()) {
                add(xmlEventReader.nextEvent());
            }
            xmlEventReader.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private void transform(Field field) {
        StringTransformer transformer = transformers.get(field.toKey());
        if (transformer == null) {
            transformer = transformers.get("_default");
        }
        if (transformer != null) {
            String old = field.data();
            field.data(transformer.transform(field.data()));
            if (!old.equals(field.data())) {
                if (fieldEventListener != null) {
                    fieldEventListener.receive(FieldEvent.DATA_TRANSFORMED.setRecordIdentifier(recordIdentifier).setField(field));
                }
            }
        }
    }

    @Override
    public void beginCollection() {
        if (listener != null) {
            listener.beginCollection();
        }
    }

    @Override
    public void endCollection() {
        if (listener != null) {
            listener.endCollection();
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        this.listener = listeners.get(type != null ? type : BIBLIOGRAPHIC);
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
        if (RECORD_NUMBER_FIELD.equals(field.tag())) {
            this.recordIdentifier = field.data();
            if (fieldEventListener != null) {
                fieldEventListener.receive(FieldEvent.RECORD_NUMBER.setRecordIdentifier(recordIdentifier));
            }
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
    public void add(XMLEvent event) throws XMLStreamException {
        if (event.isStartElement()) {
            StartElement element = (StartElement) event;
            String uri = element.getName().getNamespaceURI();
            if (!isNamespace(uri)) {
                return;
            }
            String localName = element.getName().getLocalPart();
            Iterator<?> it = element.getAttributes();
            String format = null;
            String type = null;
            String tag = null;
            String code = null;
            StringBuilder sb = new StringBuilder();
            sb.setLength(10);
            int min = 10;
            int max = 0;
            while (it.hasNext()) {
                Attribute attr = (Attribute) it.next();
                QName attributeName = attr.getName();
                String name = attributeName.getLocalPart();
                if (TAG.equals(name)) {
                    tag = attr.getValue();
                } else if (CODE.equals(name)) {
                    code = attr.getValue();
                } else if (name.startsWith(IND)) {
                    int pos = Integer.parseInt(name.substring(3));
                    if (pos >= 0 && pos < 10) {
                        char ind = attr.getValue().charAt(0);
                        if (ind == '-') {
                            ind = ' '; // replace illegal '-' symbols
                        }
                        sb.setCharAt(pos-1, ind);
                        if (pos < min) {
                            min = pos;
                        }
                        if (pos > max) {
                            max = pos;
                        }
                    }
                } else if (FORMAT.equals(name)) {
                    format = attr.getValue();
                } else if (TYPE.equals(name)) {
                    type = attr.getValue();
                }
            }
            if (format == null) {
                format = this.format;
            }
            if (type == null) {
                type = this.type;
            }
            content.setLength(0);
            switch (localName) {
                case COLLECTION: {
                    beginCollection();
                    break;
                }
                case RECORD: {
                    setFormat(format);
                    setType(type);
                    if (recordEventListener != null) {
                        recordEventListener.receive(RecordEvent.START);
                    }
                    break;
                }
                case LEADER: {
                    inLeader = true;
                    break;
                }
                case CONTROLFIELD: {
                    Field field = new Field().tag(tag);
                    stack.push(field);
                    if (field.isControlField()) {
                        inControl = true;
                    } else {
                        inData = true;
                    }
                    break;
                }
                case DATAFIELD: {
                    Field field = new Field().tag(tag).indicator(sb.substring(min-1, max));
                    stack.push(field);
                    if (field.isControlField()) {
                        inControl = true;
                    } else {
                        inData = true;
                    }
                    break;
                }
                case SUBFIELD: {
                    if (!inControl) {
                        Field f = stack.peek();
                        Field subfield = new Field(f.tag(), f.indicator(), code);
                        stack.push(subfield);
                        inData = true;
                    }
                    break;
                }
            }
        } else if (event.isEndElement()) {
            EndElement element = (EndElement) event;
            String uri = element.getName().getNamespaceURI();
            if (!isNamespace(uri)) {
                return;
            }
            String localName = element.getName().getLocalPart();
            switch (localName) {
                case COLLECTION: {
                    endCollection();
                    break;
                }
                case RECORD: {
                    flushRecord(getFormat(), getType());
                    break;
                }
                case LEADER: {
                    setRecordLabel(content.toString());
                    inLeader = false;
                    break;
                }
                case CONTROLFIELD: {
                    Field f = stack.pop();
                    if (f.isControlField()) {
                        addControlField(new Field(f).subfieldId(null).data(content.toString()));
                        inControl = false;
                    } else {
                        // conversion from control- to datafield
                        Field data = new Field().tag(f.tag()).subfieldId("a").data(content.toString());
                        if (transform) {
                            transform(data);
                        }
                        addField(data);
                        addField(Field.EMPTY_FIELD); // this means "end of field"
                        flushField();
                        inData = false;
                    }
                    break;
                }
                case DATAFIELD: {
                    Field f = stack.pop();
                    if (f.isControlField()) {
                        addControlField(new Field(f).subfieldId(null));
                        inControl = false;
                    } else {
                        addField(f.subfieldId(null).data(null));
                        flushField();
                        inData = false;
                    }
                    break;
                }
                case SUBFIELD: {
                    if (inControl) {
                        // repair, move data to controlfield
                        stack.peek().data(content.toString());
                        break;
                    } else {
                        Field f = stack.pop().data(content.toString());
                        if (transform) {
                            transform(f);
                        }
                        addField(f);
                        inData = false;
                        break;
                    }
                }
            }
            content.setLength(0);
        } else if (event.isCharacters()) {
            Characters c = (Characters) event;
            if (!c.isIgnorableWhiteSpace()) {
                if (inData || inControl || inLeader) {
                    content.append(c.getData());
                }
            }
        } else if (event.isStartDocument()) {
            stack.clear();
        }
    }

    public String getFormat() {
        return format;
    }

    public String getType() {
        return type;
    }

    private boolean isNamespace(String uri) {
        return !ignoreNamespace && validNamespaces.contains(uri);
    }

}
