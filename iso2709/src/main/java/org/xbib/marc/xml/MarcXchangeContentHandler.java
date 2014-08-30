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

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.RecordLabel;
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
 * The MarcXchange ContentHandler can handle SaX event input and fires events to a MarcXchange listener
 */
public class MarcXchangeContentHandler
        implements EntityResolver, DTDHandler, ContentHandler, ErrorHandler, MarcXchangeConstants, MarcXchangeListener {

    private static final Logger logger = LoggerFactory.getLogger(MarcXchangeContentHandler.class.getName());

    private Stack<Field> stack = new Stack<Field>();

    private Map<String,MarcXchangeListener> listeners = new HashMap<String,MarcXchangeListener>();

    private MarcXchangeListener listener;

    private StringBuilder content = new StringBuilder();

    private String format = MARC21;

    private String type = BIBLIOGRAPHIC;

    protected boolean inData;

    protected boolean inLeader;

    protected boolean inControl;

    private boolean ignoreNamespace = false;

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

    public MarcXchangeContentHandler addNamespace(String uri) {
        this.validNamespaces.add(uri);
        return this;
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
            listener.endControlField(designator);
        }
    }

    @Override
    public void endDataField(Field designator) {
        if (listener != null) {
            listener.endDataField(designator);
        }
    }

    @Override
    public void endSubField(Field designator) {
        if (listener != null) {
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
        content.setLength(0);
        if (!isNamespace(uri)) {
            return;
        }
        switch (localName) {
            case COLLECTION: {
                beginCollection();
                break;
            }
            case RECORD: {
                String format = null;
                String type = null;
                for (int i = 0; i < atts.getLength(); i++) {
                    switch (atts.getLocalName(i)) {
                        case FORMAT:
                            format = atts.getValue(i);
                            break;
                        case TYPE:
                            type = atts.getValue(i);
                            break;
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
            case CONTROLFIELD: {
                String tag = "";
                for (int i = 0; i < atts.getLength(); i++) {
                    if (TAG.equals(atts.getLocalName(i))) {
                        tag = atts.getValue(i);
                    }
                }
                Field field = new Field().tag(tag);
                stack.push(field);
                beginControlField(field);
                inControl = true;
                break;
            }
            case DATAFIELD: {
                String tag = "";
                char[] indicators = new char[atts.getLength()];
                for (int i = 0; i < atts.getLength(); i++) {
                    indicators[i] = '\0';
                    String name = atts.getLocalName(i);
                    if (TAG.equals(name)) {
                        tag = atts.getValue(i);
                    }
                    if (name.startsWith(IND)) {
                        int pos = Integer.parseInt(name.substring(3));
                        if (pos >= 0 && pos < atts.getLength()) {
                            indicators[pos-1] = atts.getValue(i).charAt(0);
                        }
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (char indicator : indicators) {
                    if (indicator != '\0') {
                        sb.append(indicator);
                    }
                }
                Field field = new Field().tag(tag).indicator(sb.toString()).data(null);
                stack.push(field);
                beginDataField(field);
                inData = true;
                break;
            }
            case SUBFIELD: {
                if (inControl) {
                    // no subfields are allowed in controlfield
                    break;
                } else {
                    Field f = stack.peek();
                    Field subfield = new Field(f);
                    for (int i = 0; i < atts.getLength(); i++) {
                        if (CODE.equals(atts.getLocalName(i))) {
                            subfield.subfieldId(atts.getValue(i));
                        }
                    }
                    stack.push(subfield);
                    beginSubField(subfield);
                    inData = true;
                    break;
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!isNamespace(uri)) {
            content.setLength(0);
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
                endControlField(stack.pop().data(content.toString()));
                inControl = false;
                break;
            }
            case DATAFIELD: {
                endDataField(stack.pop().subfieldId(null).data(""));
                inData = false;
                break;
            }
            case SUBFIELD: {
                if (inControl) {
                    // repair, move data to controlfield
                    stack.peek().data(content.toString());
                    break;
                } else {
                    endSubField(stack.pop().data(content.toString()));
                    inData = false;
                    break;
                }
            }
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
        logger.warn(exception.getMessage(), exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        logger.error(exception.getMessage(), exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        logger.error(exception.getMessage(), exception);
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