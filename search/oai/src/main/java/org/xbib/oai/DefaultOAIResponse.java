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
package org.xbib.oai;

import org.xbib.io.http.netty.NettyHttpResponse;
import org.xbib.xml.XMLFilterReader;
import org.xbib.xml.transform.StylesheetTransformer;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

/**
 * Default OAI response
 *
 */
public class DefaultOAIResponse<R extends DefaultOAIResponse>
        extends NettyHttpResponse
        implements OAIResponse<R>, XMLEventConsumer {

    //private static final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    private OAIRequest request;

    private Reader reader;

    private StringBuilder sb;

    private String errorCode;

    //private StylesheetTransformer transformer;

    public DefaultOAIResponse(OAIRequest request) {
        this.request = request;
        this.sb = new StringBuilder();
        //this.transformer = new StylesheetTransformer("/xsl");
    }

    public OAIRequest getRequest() {
        return request;
    }

    @Override
    public R setReader(Reader reader) {
        this.reader = reader;
        return (R)this;
    }

    public void flush() throws IOException {
    }

    @Override
    public void add(XMLEvent xmle) throws XMLStreamException {
    }

    public void setError(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getError() {
        return errorCode;
    }

    public void setResponseDate(Date date) {
    }

    public void setExpire(long millis) {
    }

    @Override
    public R to(Writer writer) throws IOException {
        try {
            XMLFilterReader filter = new OAIResponseFilterReader();
            InputSource source = new InputSource(reader);
            StreamResult streamResult = new StreamResult(writer);
            StylesheetTransformer transformer = new StylesheetTransformer("/xsl");
            transformer.setSource(filter, source).setResult(streamResult).transform();
            transformer.close();
        } catch (TransformerException e) {
            throw new IOException(e.getMessage(), e);
        }
        return (R)this;
    }

    class OAIResponseFilterReader extends XMLFilterReader {

        OAIResponseFilterReader() {
            super();
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localname, String qname, Attributes atts) throws SAXException {
        }

        @Override
        public void endElement(String uri, String localname, String qname) throws SAXException {
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }
    }
}
