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
package org.xbib.sru;

import org.xbib.marc.Field;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.xml.MarcXchangeSaxAdapter;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.text.Normalizer.Form;

/**
 * SRU filter reader
 */
public class SRUFilterReader extends Iso2709Reader implements MarcXchangeListener {

    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    private final String nsURI = MarcXchangeConstants.MARCXCHANGE_V2_NS_URI;

    private final String nsPrefix = MarcXchangeConstants.MARCXCHANGE_NS_PREFIX;

    private final SearchRetrieveResponse response;

    private final String encoding;

    private int recordPosition;

    public SRUFilterReader(SearchRetrieveResponse response, Reader reader, String encoding) {
        super(reader);
        this.response = response;
        this.recordPosition = 1;
        this.encoding = encoding;
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        MarcXchangeSaxAdapter adapter = new MarcXchangeSaxAdapter()
                .setInputSource(input)
                .setContentHandler(getContentHandler())
                .setMarcXchangeListener(this)
                .setSchema((String) getProperty(SCHEMA))
                .setFormat((String) getProperty(FORMAT))
                .setType((String) getProperty(TYPE));
        adapter.parseCollection(adapter.fieldStream());
    }

    @Override
    public void beginCollection() {

    }

    @Override
    public void endCollection() {

    }

    @Override
    public void beginRecord(String format, String type) {
        response.beginRecord();
        response.recordSchema(nsPrefix);
        String recordPacking = "xml";
        response.recordPacking(recordPacking);
        response.recordPosition(recordPosition);
        try {
            response.add(eventFactory.createStartDocument());
            // emit additional parameter values for federating
            response.add(eventFactory.createProcessingInstruction("format", format));
            response.add(eventFactory.createProcessingInstruction("type", type));
            response.add(eventFactory.createProcessingInstruction("id", Integer.toString(recordPosition)));
            // SRU
            response.add(eventFactory.createProcessingInstruction("recordSchema", nsPrefix));
            response.add(eventFactory.createProcessingInstruction("recordPacking", recordPacking));
            // no recordIdentifier
            response.add(eventFactory.createProcessingInstruction("recordPosition", Integer.toString(recordPosition)));
        } catch (XMLStreamException e) {
            // ignore?
        }
        recordPosition++;
    }

    @Override
    public void endRecord() {
        response.endRecord();
        try {
            response.add(eventFactory.createEndDocument());
        } catch (XMLStreamException e) {
            // ignore?
        }
    }

    @Override
    public void leader(String label) {
        try {
            response.add(eventFactory.createStartElement(nsPrefix, nsURI, "leader"));
            response.add(eventFactory.createCharacters(label));
            response.add(eventFactory.createEndElement(nsPrefix, nsURI, "leader"));
        } catch (XMLStreamException e) {
            // ignore?
        }
    }

    @Override
    public void beginControlField(Field designator) {
        try {
            response.add(eventFactory.createStartElement(nsPrefix, nsURI, "controlfield"));
            if (designator != null && designator.tag() != null) {
                response.add(eventFactory.createAttribute("tag", designator.tag()));
            }
        } catch (XMLStreamException e) {
            // ignore?
        }
    }

    @Override
    public void endControlField(Field designator) {
        try {
            if (designator != null && designator.data() != null) {
                String s = decode(designator.data());
                response.add(eventFactory.createCharacters(s));
            }
            response.add(eventFactory.createEndElement(nsPrefix, nsURI, "controlfield"));
        } catch (XMLStreamException e) {
            // ignore?
        }
    }

    @Override
    public void beginDataField(Field designator) {
        try {
            response.add(eventFactory.createStartElement(nsPrefix, nsURI, "datafield"));
            if (designator != null && designator.tag() != null) {
                response.add(eventFactory.createAttribute("tag", designator.tag()));
                if (designator.indicator() != null) {
                    for (int i = 0; i < designator.indicator().length(); i++) {
                        response.add(eventFactory.createAttribute("ind" + (i + 1),
                                designator.indicator().substring(i, i + 1)));
                    }
                }
            }
        } catch (XMLStreamException e) {
            // ignore?
        }
    }

    @Override
    public void endDataField(Field designator) {
        try {
            if (designator != null && designator.data() != null) {
                response.add(eventFactory.createCharacters(decode(designator.data())));
            }
            response.add(eventFactory.createEndElement(nsPrefix, nsURI, "datafield"));
        } catch (XMLStreamException e) {
            // ignore?
        }
    }

    @Override
    public void beginSubField(Field designator) {
        try {
            response.add(eventFactory.createStartElement(nsPrefix, nsURI, "subfield"));
            if (designator != null && designator.subfieldId() != null) {
                response.add(eventFactory.createAttribute("code", designator.subfieldId()));
            }
        } catch (XMLStreamException e) {
            // ignore?
        }
    }

    @Override
    public void endSubField(Field designator) {
        try {
            if (designator != null && designator.data() != null) {
                response.add(eventFactory.createCharacters(decode(designator.data())));
            }
            response.add(eventFactory.createEndElement(nsPrefix, nsURI, "subfield"));
        } catch (XMLStreamException e) {
            // ignore?
        }
    }

    private String decode(String value) {
        String s = value;
        try {
            // read from octet stream (ISO-8859-1 = 8 bit) and map to encoding, then normalize 
            s = Normalizer.normalize(new String(s.getBytes("ISO-8859-1"), encoding), Form.NFKC);
            return s;
        } catch (UnsupportedEncodingException ex) {
            return s;
        }
    }

}
