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
package org.xbib.marc.xml.mapper;

import org.xbib.marc.FieldReader;
import org.xbib.marc.MarcXchangeListener;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

/**
 * This Sax-ContentHandler-based MarcXchangeReader reads MarcXML or MarcXchange, maps MarcXchange field to other fields,
 * and fires events to a MarcXchange listener
 */
public class MarcXchangeFieldMapperReader extends MarcXchangeFieldMapperContentHandler implements FieldReader {

    private final Reader reader;

    private ContentHandler contentHandler;

    public MarcXchangeFieldMapperReader(InputStream in) throws IOException {
        this(new InputStreamReader(in, "UTF-8"));
    }

    public MarcXchangeFieldMapperReader(Reader reader) throws IOException {
        this.reader = reader;
    }

    public void parse() throws IOException {
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
    public MarcXchangeFieldMapperReader addFieldMap(String fieldMapName, Map<String,Object> fieldMap) {
        super.addFieldMap(fieldMapName, fieldMap);
        return this;
    }

    @Override
    public MarcXchangeFieldMapperReader setMarcXchangeListener(MarcXchangeListener listener) {
        super.setMarcXchangeListener(listener);
        return this;
    }

    public MarcXchangeFieldMapperReader addNamespace(String uri) {
        super.addNamespace(uri);
        return this;
    }

    /**
     * Override content handler
     * @param handler the new content handler
     * @return this reader
     */
    public MarcXchangeFieldMapperReader setHandler(ContentHandler handler) {
        this.contentHandler = handler;
        return this;
    }
}
