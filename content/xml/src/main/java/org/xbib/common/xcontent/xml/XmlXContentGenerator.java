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
package org.xbib.common.xcontent.xml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import org.xbib.common.xcontent.XContent;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.common.xcontent.XContentString;
import org.xbib.common.xcontent.XContentGenerator;
import org.xbib.common.xcontent.XContentHelper;
import org.xbib.common.xcontent.XContentParser;
import org.xbib.io.BytesReference;
import org.xbib.xml.ISO9075;
import org.xbib.xml.XMLUtil;
import org.xbib.xml.namespace.XmlNamespaceContext;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Content generator for XML formatted content
 */
public class XmlXContentGenerator implements XContentGenerator {

    protected final ToXmlGenerator generator;

    private XmlXParams params ;

    private boolean writeLineFeedAtEnd;

    private boolean rootUsed = false;

    public XmlXContentGenerator(ToXmlGenerator generator) {
        this.params = XmlXParams.getDefaultParams();
        this.generator = generator;
        generator.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
    }

    public XmlXContentGenerator setParams(XmlXParams params) {
        this.params = params;
        try {
            generator.getStaxWriter().setPrefix(getParams().getRoot().getPrefix(), getParams().getRoot().getNamespaceURI());
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return this;
    }

    public XmlXParams getParams() {
        return params;
    }

    public XmlNamespaceContext getNamespaceContext() {
        return params.getNamespaceContext();
    }

    @Override
    public XContent content() {
        return XmlXContent.xmlXContent();
    }

    @Override
    public void usePrettyPrint() {
        generator.useDefaultPrettyPrinter();
    }

    @Override
    public void usePrintLineFeedAtEnd() {
        writeLineFeedAtEnd = true;
    }

    @Override
    public void writeStartArray() throws IOException {
        generator.writeStartArray();
    }

    @Override
    public void writeEndArray() throws IOException {
        generator.writeEndArray();
    }

    @Override
    public void writeStartObject() throws IOException {
        if (!rootUsed) {
            generator.setNextName(getParams().getRoot());
        }
        generator.writeStartObject();
        if (!rootUsed) {
            try {
                for (String prefix : getNamespaceContext().getNamespaces().keySet()) {
                    String uri = getNamespaceContext().getNamespaceURI(prefix);
                    if (uri == null || uri.isEmpty()) {
                        continue;
                    }
                    generator.getStaxWriter().writeNamespace(prefix, uri);
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
            rootUsed = true;
        }
    }

    @Override
    public void writeEndObject() throws IOException {
        generator.writeEndObject();
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        writeFieldNameWithNamespace(name);
    }

    @Override
    public void writeFieldName(XContentString name) throws IOException {
        writeFieldNameWithNamespace(name);
    }

    @Override
    public void writeString(String text) throws IOException {
        generator.writeString(XMLUtil.sanitizeXml10(text));
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException {
        generator.writeString(XMLUtil.sanitizeXml10(text, offset, len));
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        generator.writeUTF8String(text, offset, length);
    }

    @Override
    public void writeBinary(byte[] data, int offset, int len) throws IOException {
        // write base64
        generator.writeBinary(data, offset, len);
    }

    @Override
    public void writeBinary(byte[] data) throws IOException {
        generator.writeBinary(data);
    }

    @Override
    public void writeNumber(int v) throws IOException {
        generator.writeNumber(v);
    }

    @Override
    public void writeNumber(long v) throws IOException {
        generator.writeNumber(v);
    }

    @Override
    public void writeNumber(double d) throws IOException {
        generator.writeNumber(d);
    }

    @Override
    public void writeNumber(float f) throws IOException {
        generator.writeNumber(f);
    }

    @Override
    public void writeNumber(BigInteger bi) throws IOException {
        generator.writeNumber(bi);
    }

    @Override
    public void writeNumber(BigDecimal bd) throws IOException {
        generator.writeNumber(bd);
    }

    @Override
    public void writeBoolean(boolean state) throws IOException {
        generator.writeBoolean(state);
    }

    @Override
    public void writeNull() throws IOException {
        generator.writeNull();
    }

    @Override
    public void writeStringField(String fieldName, String value) throws IOException {
        generator.writeStringField(fieldName, value);
    }

    @Override
    public void writeStringField(XContentString fieldName, String value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeString(value);
    }

    @Override
    public void writeBooleanField(String fieldName, boolean value) throws IOException {
        generator.writeBooleanField(fieldName, value);
    }

    @Override
    public void writeBooleanField(XContentString fieldName, boolean value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeBoolean(value);
    }

    @Override
    public void writeNullField(String fieldName) throws IOException {
        generator.writeNullField(fieldName);
    }

    @Override
    public void writeNumberField(String fieldName, int value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, int value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    @Override
    public void writeNumberField(String fieldName, long value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, long value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    @Override
    public void writeNumberField(String fieldName, double value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, double value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    @Override
    public void writeNumberField(String fieldName, float value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, float value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    @Override
    public void writeNumberField(String fieldName, BigInteger value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, BigInteger value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    @Override
    public void writeNumberField(String fieldName, BigDecimal value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, BigDecimal value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    @Override
    public void writeBinaryField(String fieldName, byte[] data) throws IOException {
        generator.writeBinaryField(fieldName, data);
    }

    @Override
    public void writeBinaryField(XContentString fieldName, byte[] data) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeBinary(data);
    }

    @Override
    public void writeArrayFieldStart(String fieldName) throws IOException {
        generator.writeArrayFieldStart(fieldName);
    }

    @Override
    public void writeArrayFieldStart(XContentString fieldName) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeStartArray();
    }

    @Override
    public void writeObjectFieldStart(String fieldName) throws IOException {
        generator.writeObjectFieldStart(fieldName);
    }

    @Override
    public void writeObjectFieldStart(XContentString fieldName) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeStartObject();
    }

    @Override
    public void writeRawField(String fieldName, InputStream content, OutputStream bos) throws IOException {
        writeFieldNameWithNamespace(fieldName);
        try (JsonParser parser = params.getXmlFactory().createParser(content)) {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        }
    }

    @Override
    public void writeRawField(String fieldName, byte[] content, OutputStream bos) throws IOException {
        writeFieldNameWithNamespace(fieldName);
        try (JsonParser parser = params.getXmlFactory().createParser(content)) {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        }
    }

    @Override
    public void writeRawField(String fieldName, BytesReference content, OutputStream bos) throws IOException {
        writeFieldNameWithNamespace(fieldName);
        JsonParser parser;
        if (content.hasArray()) {
            parser = params.getXmlFactory().createParser(content.array(), content.arrayOffset(), content.length());
        } else {
            parser = params.getXmlFactory().createParser(content.streamInput());
        }
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    @Override
    public void writeRawField(String fieldName, byte[] content, int offset, int length, OutputStream bos) throws IOException {
        writeFieldNameWithNamespace(fieldName);
        try (JsonParser parser = params.getXmlFactory().createParser(content, offset, length)) {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        }
    }

    @Override
    public void writeValue(XContentBuilder builder) throws IOException {
        generator.writeRawValue(builder.string());
    }

    @Override
    public void copy(XContentBuilder builder, OutputStream bos) throws IOException {
        flush();
        builder.bytes().writeTo(bos);
    }

    @Override
    public void copyCurrentStructure(XContentParser parser) throws IOException {
        if (parser.currentToken() == null) {
            parser.nextToken();
        }
        if (parser instanceof XmlXContentParser) {
            generator.copyCurrentStructure(((XmlXContentParser) parser).parser);
        } else {
            XContentHelper.copyCurrentStructure(this, parser);
        }
    }

    @Override
    public void flush() throws IOException {
        generator.flush();
    }

    @Override
    public void close() throws IOException {
        if (generator.isClosed()) {
            return;
        }
        if (writeLineFeedAtEnd) {
            flush();
            generator.writeRaw(LF);
        }
        generator.close();
    }

    private static final SerializedString LF = new SerializedString("\n");

    private void writeFieldNameWithNamespace(String name) throws IOException {
        QName qname = toQName(params.getNamespaceContext(), name);
        try {
            generator.getStaxWriter().setPrefix(qname.getPrefix(), qname.getNamespaceURI());
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        generator.setNextName(qname);
        generator.writeFieldName(qname.getLocalPart());
    }

    private void writeFieldNameWithNamespace(XContentString name) throws IOException {
        writeFieldNameWithNamespace(name.getValue());
    }

    private QName toQName(NamespaceContext context, String name) {
        if (name.startsWith("_")) {
            name = name.substring(1);
        } else if (name.startsWith("@")) {
            name = name.substring(1);
        }
        name = ISO9075.encode(name);
        int pos = name.indexOf(':');
        String nsPrefix = "";
        String nsURI = context.getNamespaceURI("");
        if (pos > 0) {
            nsPrefix = name.substring(0, pos);
            nsURI = context.getNamespaceURI(nsPrefix);
            if (nsURI == null) {
                if (params.isFatalNamespaceErrors()) {
                    throw new IllegalArgumentException("unknown namespace prefix: " + nsPrefix);
                } else {
                    nsURI = "xbib:namespace/" + nsPrefix;
                }
            }
            name = name.substring(pos + 1);
        }
        return new QName(nsURI, name, nsPrefix);
    }

}
