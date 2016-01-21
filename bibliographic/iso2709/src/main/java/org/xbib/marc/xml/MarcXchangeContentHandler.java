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
package org.xbib.marc.xml;

import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.event.EventListener;
import org.xbib.marc.event.FieldEvent;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.transformer.StringTransformer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * The Sax-ContentHandler-based MarcXchange handler can handle SaX event input
 * and fires events to a MarcXchange listener
 */
public class MarcXchangeContentHandler
        implements EntityResolver, DTDHandler, ContentHandler, ErrorHandler, MarcXchangeConstants, MarcXchangeListener {

    private Stack<Field> stack = new Stack<Field>();

    private Map<String,MarcXchangeListener> listeners = new HashMap<String,MarcXchangeListener>();

    private MarcXchangeListener listener;

    private Map<String, StringTransformer> transformers = new HashMap<String, StringTransformer>();

    private EventListener<FieldEvent> fieldEventListener;

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

    public MarcXchangeContentHandler addListener(String type, MarcXchangeListener listener) {
        this.listeners.put(type, listener);
        return this;
    }

    public MarcXchangeContentHandler setMarcXchangeListener(MarcXchangeListener listener) {
        this.listeners.put(BIBLIOGRAPHIC, listener);
        return this;
    }

    public MarcXchangeContentHandler setFormat(String format) {
        this.format = format;
        return this;
    }

    public MarcXchangeContentHandler setType(String type) {
        this.type = type;
        return this;
    }

    public MarcXchangeContentHandler setIgnoreNamespace(boolean ignore) {
        this.ignoreNamespace = ignore;
        return this;
    }

    public MarcXchangeContentHandler setTransform(boolean transform) {
        this.transform = transform;
        return this;
    }

    public MarcXchangeContentHandler addNamespace(String uri) {
        this.validNamespaces.add(uri);
        return this;
    }

    public MarcXchangeContentHandler setFieldEventListener(EventListener<FieldEvent> fieldEventListener) {
        this.fieldEventListener = fieldEventListener;
        return this;
    }

    public MarcXchangeContentHandler setTransformer(String fieldKey, StringTransformer transformer) {
        this.transformers.put(fieldKey, transformer);
        return this;
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
    public void leader(String label) {
        if (listener != null) {
            listener.leader(label);
        }
    }

    @Override
    public void beginControlField(Field designator) {
        if (listener != null) {
            listener.beginControlField(designator);
        }
    }

    @Override
    public void beginDataField(Field designator) {
        if (listener != null) {
            listener.beginDataField(designator);
        }
    }

    @Override
    public void beginSubField(Field designator) {
        if (listener != null) {
            listener.beginSubField(designator);
        }
    }

    @Override
    public void endRecord() {
        if (listener != null) {
            listener.endRecord();
        }
    }

    @Override
    public void endControlField(Field designator) {
        if (listener != null) {
            if (transform) {
                transform(designator);
            }
            listener.endControlField(designator);
        }
        if (RECORD_NUMBER_FIELD.equals(designator.tag())) {
            this.recordIdentifier = designator.data();
            if (fieldEventListener != null) {
                fieldEventListener.receive(FieldEvent.RECORD_NUMBER.setRecordIdentifier(recordIdentifier));
            }
        }
    }

    @Override
    public void endDataField(Field designator) {
        if (listener != null) {
            if (transform) {
                transform(designator);
            }
            listener.endDataField(designator);
        }
    }

    @Override
    public void endSubField(Field designator) {
        if (listener != null) {
            if (transform) {
                transform(designator);
            }
            listener.endSubField(designator);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // not used yet
    }

    @Override
    public void startDocument() throws SAXException {
        stack.clear();
        content.setLength(0);
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // ignore all mappings
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // ignore all mappings
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (!isNamespace(uri)) {
            return;
        }
        content.setLength(0);
        switch (localName) {
            case COLLECTION: {
                beginCollection();
                break;
            }
            case RECORD: {
                String format = null;
                String type = null;
                for (int i = 0; i < atts.getLength(); i++) {
                    if (FORMAT.equals(atts.getLocalName(i))) {
                        format = atts.getValue(i);
                    } else if (TYPE.equals(atts.getLocalName(i))) {
                        type = atts.getValue(i);
                    }
                }
                if (format == null) {
                    format = this.format;
                }
                if (type == null) {
                    type = this.type;
                }
                beginRecord(format, type);
                break;
            }
            case LEADER: {
                inLeader = true;
                break;
            }
            case CONTROLFIELD: // fall-through
            case DATAFIELD: {
                String tag = null;
                StringBuilder sb = new StringBuilder();
                sb.setLength(atts.getLength());
                int min = atts.getLength();
                int max = 0;
                for (int i = 0; i < atts.getLength(); i++) {
                    String name = atts.getLocalName(i);
                    if (TAG.equals(name)) {
                        tag = atts.getValue(i);
                    }
                    if (name.startsWith(IND)) {
                        int pos = Integer.parseInt(name.substring(3));
                        if (pos >= 0 && pos < atts.getLength()) {
                            char ind = atts.getValue(i).charAt(0);
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
                    }
                }
                Field field = new Field().tag(tag);
                if (max > 0) {
                    field.indicator(sb.substring(min-1, max));
                }
                stack.push(field);
                if (field.isControlField()) {
                    beginControlField(field);
                    inControl = true;
                } else {
                    beginDataField(field);
                    inData = true;
                }
                break;
            }
            case SUBFIELD: {
                if (!inControl) {
                    Field subfield = new Field(stack.peek());
                    for (int i = 0; i < atts.getLength(); i++) {
                        if (CODE.equals(atts.getLocalName(i))) {
                            subfield.subfieldId(atts.getValue(i));
                        }
                    }
                    stack.push(subfield);
                    beginSubField(subfield);
                    inData = true;
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!isNamespace(uri)) {
            return;
        }
        switch (localName) {
            case COLLECTION: {
                endCollection();
                break;
            }
            case RECORD: {
                endRecord();
                break;
            }
            case LEADER: {
                // create fixed leader if broken
                RecordLabel recordLabel = new RecordLabel(content.toString().toCharArray());
                leader(recordLabel.getRecordLabel());
                inLeader = false;
                break;
            }
            case CONTROLFIELD: {
                Field field = stack.pop();
                if (field.isControlField()) {
                    Field f = new Field(field).indicator(null).data(content.toString());
                    endControlField(f);
                    inControl = false;
                } else {
                    endDataField(new Field(field).subfieldId("a").data(content.toString()));
                    inData = false;
                }
                break;
            }
            case DATAFIELD: {
                Field field = stack.pop();
                if (field.isControlField()) {
                    endControlField(field);
                    inControl = false;
                } else {
                    endDataField(field.subfieldId(null).data(null));
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
                    endSubField(f);
                    inData = false;
                    break;
                }
            }
            default:
                break;
        }
        content.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inData || inControl || inLeader) {
            content.append(new String(ch, start, length));
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
    }

    @Override
    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return null;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        //logger.warn(exception.getMessage(), exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        //logger.error(exception.getMessage(), exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        //logger.error(exception.getMessage(), exception);
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
