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
package org.xbib.marc.dialects.pica;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.MarcXchangeListener;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * PICA XML parser
 */
public class DNBPICAXmlReader
        implements EntityResolver, DTDHandler, ContentHandler, ErrorHandler, DNBPICAConstants, MarcXchangeListener {

    private static final Logger logger = LoggerFactory.getLogger(DNBPICAXmlReader.class.getName());

    private ContentHandler contentHandler;

    private MarcXchangeListener listener;

    private FieldCollection fields = new FieldCollection();

    private StringBuilder content = new StringBuilder();

    public DNBPICAXmlReader setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
        return this;
    }

    public DNBPICAXmlReader setListener(MarcXchangeListener listener) {
        this.listener = listener;
        return this;
    }

    public void parse(InputStream in) throws IOException {
        parse(new InputStreamReader(in, "UTF-8"));
    }

    public void parse(Reader reader) throws IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            parser.getXMLReader().setContentHandler(contentHandler != null ? contentHandler : this);
            parser.getXMLReader().parse(new InputSource(reader));
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void leader(String label) {
        if (listener != null) {
            listener.leader(label);
        }
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
    public void beginControlField(Field field) {
        if (listener != null) {
            listener.beginControlField(field);
        }
    }

    @Override
    public void beginDataField(Field field) {
        if (listener != null) {
            listener.beginDataField(field);
        }
    }

    @Override
    public void beginSubField(Field field) {
        if (listener != null) {
            listener.beginSubField(field);
        }
    }

    @Override
    public void endRecord() {
        if (listener != null) {
            listener.endRecord();
        }
    }

    @Override
    public void endControlField(Field field) {
        if (listener != null) {
            listener.endControlField(field);
        }
    }

    @Override
    public void endDataField(Field field) {
        if (listener != null) {
            listener.endDataField(field);
        }
    }

    @Override
    public void endSubField(Field field) {
        if (listener != null) {
            listener.endSubField(field);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // not needed
    }

    @Override
    public void startDocument() throws SAXException {
        fields.clear();
        content.setLength(0);
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // ignore all mappings
        if (contentHandler != null) {
            contentHandler.startPrefixMapping(prefix, uri);
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // ignore all mappings
        if (contentHandler != null) {
            contentHandler.endPrefixMapping(prefix);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        content.setLength(0);
        if (!isNamespace(uri)) {
            return;
        }
        switch (localName) {
            case RECORD: {
                // get format and type
                String format = "Pica";
                String type = " XML";
                beginRecord(format, type);
                break;
            }
            case TAG: {
                String tag = "";
                String indicator = null;
                for (int i = 0; i < atts.getLength(); i++) {
                    String name = atts.getLocalName(i);
                    String value = atts.getValue(i);
                    if (ID.equals(name)) {
                        tag = value.substring(0, 3);
                        indicator = value.substring(3);
                    }
                }
                Field field = new Field().tag(tag).indicator(indicator);
                beginDataField(field);
                fields.add(field);
                break;
            }
            case SUBF: {
                // get tag and indicator from previous data field
                Field field = new Field(fields .getLast());
                field.subfieldId(null); // reset sub field ID
                field.data(null); // reset data
                for (int i = 0; i < atts.getLength(); i++) {
                    String name = atts.getLocalName(i);
                    String value = atts.getValue(i);
                    if (ID.equals(name)) {
                        field.subfieldId(value);
                    }
                }
                beginSubField(field);
                fields.add(field);
                break;
            }
            case GLOBAL : {
                // ignore
                break;
            }
            default : {
                throw new IllegalArgumentException("unknown begin element: " +
                        uri + " " + localName + " " + qName + " atts=" + atts.toString());
            }
        }
        if (contentHandler != null) {
            contentHandler.startElement(uri, localName, qName, atts);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!isNamespace(uri)) {
            content.setLength(0);
            return;
        }
        // ignore namespaces, just check local names
        switch (localName) {
            case RECORD: {
                endRecord();
                break;
            }
            case TAG: {
                Field field = fields.getFirst().subfieldId(null).data(content.toString());
                endDataField(field);
                fields.clear();
                break;
            }
            case SUBF: {
                Field field = fields.getLast().data(content.toString());
                endSubField(field);
                break;
            }
            case GLOBAL : {
                // ignore
                break;
            }
            default : {
                // stop processing, this is fatal
                throw new IllegalArgumentException("unknown end element: " + uri + " " + localName + " " + qName);
            }
        }
        content.setLength(0);
        if (contentHandler != null) {
            contentHandler.endElement(uri, localName, qName);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String s = new String(ch, start, length);
        content.append(s);
        if (contentHandler != null) {
            contentHandler.characters(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (contentHandler != null) {
            contentHandler.ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        if (contentHandler != null) {
            contentHandler.processingInstruction(target, data);
        }
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        if (contentHandler != null) {
            contentHandler.skippedEntity(name);
        }
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

    private boolean isNamespace(String uri) {
        return NS_URI.equals(uri);
    }

}