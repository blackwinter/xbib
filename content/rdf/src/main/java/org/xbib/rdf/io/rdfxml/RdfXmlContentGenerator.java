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
package org.xbib.rdf.io.rdfxml;

import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.RdfContentGenerator;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.memory.BlankMemoryResource;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.xml.XMLUtil;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * RDF/XML writer
 */
public class RdfXmlContentGenerator
        implements RdfContentGenerator<RdfXmlContentParams>, Flushable, RdfConstants {

    private final Writer writer;

    private boolean writingStarted;

    private boolean headerWritten;

    private Resource lastWrittenSubject;

    private Resource resource;

    private RdfXmlContentParams params = RdfXmlContentParams.DEFAULT_PARAMS;

    public RdfXmlContentGenerator(OutputStream out) throws IOException{
        this(new OutputStreamWriter(out, "UTF-8"));
    }

    public RdfXmlContentGenerator(Writer writer) throws IOException {
        this.writer = writer;
        this.resource = new BlankMemoryResource();
    }

    @Override
    public void close() throws IOException {
        // write last resource
        receive(resource);
    }

    @Override
    public RdfXmlContentGenerator receive(IRI iri) throws IOException {
        if (!iri.equals(resource.id())) {
            receive(resource);
            resource = new MemoryResource(iri);
        }
        return this;
    }

    @Override
    public RdfContentGenerator setParams(RdfXmlContentParams rdfContentParams) {
        this.params = rdfContentParams;
        return this;
    }

    @Override
    public RdfXmlContentParams getParams() {
        return params;
    }

    @Override
    public RdfXmlContentGenerator startStream() {
        return this;
    }

    @Override
    public RdfContentGenerator setBaseUri(String baseUri) {
        startPrefixMapping("", baseUri);
        return this;
    }

    @Override
    public RdfXmlContentGenerator receive(Triple triple) {
        resource.add(triple);
        return this;
    }

    @Override
    public RdfXmlContentGenerator endStream() {
        return this;
    }

    @Override
    public RdfXmlContentGenerator startPrefixMapping(String prefix, String uri) {
        params.getNamespaceContext().addNamespace(prefix, uri);
        return this;
    }

    @Override
    public RdfXmlContentGenerator endPrefixMapping(String prefix) {
        // we don't remove name spaces. It's troubling RDF serializations.
        return this;
    }

    @Override
    public RdfXmlContentGenerator receive(Resource resource) throws IOException {
        this.writingStarted = false;
        this.headerWritten = false;
        this.lastWrittenSubject = null;
        for (Map.Entry<String, String> entry : params.getNamespaceContext().getNamespaces().entrySet()) {
            handleNamespace(entry.getKey(), entry.getValue());
        }
        startRDF();
        writeHeader();
        resource.triples().stream().forEach(this::writeTriple);
        endRDF();
        return this;
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    private void startRDF() throws IOException {
        if (writingStarted) {
            throw new IOException("writing has already started");
        }
        writingStarted = true;
    }

    private void writeHeader() throws IOException {
        try {
            setNamespace("rdf", NS_URI, false);
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writeStartOfStartTag(NS_URI, "RDF");
            for (Map.Entry<String, String> entry : params.getNamespaceContext().getNamespaces().entrySet()) {
                String prefix = entry.getKey();
                String name = entry.getValue();
                writeNewLine();
                writer.write("\t");
                writer.write("xmlns");
                if (prefix.length() > 0) {
                    writer.write(':');
                    writer.write(prefix);
                }
                writer.write("=\"");
                writer.write(escapeDoubleQuotedAttValue(name));
                writer.write("\"");
            }
            writer.write(">");
            writeNewLine();
        } finally {
            headerWritten = true;
        }
    }

    private void endRDF() throws IOException {
        if (!writingStarted) {
            throw new IOException("Document writing has not yet started");
        }
        try {
            if (!headerWritten) {
                writeHeader();
            }
            flushPendingStatements();
            writeNewLine();
            writeEndTag(NS_URI, "RDF");
            writer.flush();
        } finally {
            writingStarted = false;
            headerWritten = false;
        }
    }

    private void handleNamespace(String prefix, String name) {
        setNamespace(prefix, name, false);
    }

    private void setNamespace(String prefix, String name, boolean fixedPrefix) {
        if (headerWritten) {
            return;
        }
        Map map = params.getNamespaceContext().getNamespaces();
        if (!map.containsKey(name)) {
            boolean isLegalPrefix = prefix.length() == 0 || XMLUtil.isNCName(prefix);
            if (!isLegalPrefix || map.containsValue(prefix)) {
                if (fixedPrefix) {
                    if (isLegalPrefix) {
                        throw new IllegalArgumentException("Prefix is already in use: " + prefix);
                    } else {
                        throw new IllegalArgumentException("Prefix is not a valid XML namespace prefix: " + prefix);
                    }
                }
                if (prefix.length() == 0 || !isLegalPrefix) {
                    prefix = "ns";
                }
                int number = 1;
                while (map.containsValue(prefix + number)) {
                    number++;
                }
                prefix += number;
            }
            params.getNamespaceContext().addNamespace(prefix, name);
        }
    }

    private RdfXmlContentGenerator writeTriple(Triple triple) {
        try {
            if (!writingStarted) {
                throw new IOException("document writing has not yet been started");
            }
            Resource subj = triple.subject();
            IRI pred = triple.predicate();
            Node obj = triple.object();
            String predString = pred.toString();
            int predSplitIdx = findURISplitIndex(predString);
            if (predSplitIdx == -1) {
                throw new IOException("unable to create XML namespace-qualified name for predicate: " + predString);
            }
            String predNamespace = predString.substring(0, predSplitIdx);
            String predLocalName = predString.substring(predSplitIdx);
            if (!headerWritten) {
                writeHeader();
            }
            if (!subj.equals(lastWrittenSubject)) {
                flushPendingStatements();
                writeNewLine();
                writeStartOfStartTag(NS_URI, "Description");
                if (subj.isEmbedded()) {
                    writeAttribute(NS_URI, "nodeID", subj.toString());
                } else {
                    writeAttribute(NS_URI, "about", subj.toString());
                }
                writer.write(">");
                writeNewLine();
                lastWrittenSubject = subj;
            }
            writer.write("\t");
            writeStartOfStartTag(predNamespace, predLocalName);
            if (obj instanceof Resource) {
                Resource objRes = (Resource) obj;
                if (objRes.isEmbedded()) {
                    writeAttribute(NS_URI, "nodeID", objRes.id().toString());
                } else {
                    writeAttribute(NS_URI, "resource", objRes.id().toString());
                }
                writer.write("/>");
            } else if (obj instanceof Literal) {
                Literal l = (Literal) obj;
                if (l.language() != null) {
                    writeAttribute("xml:lang", l.language());
                }
                boolean isXMLLiteral = false;
                IRI datatype = l.type();
                if (datatype != null) {
                    isXMLLiteral = datatype.equals(RDF_XMLLITERAL);
                    if (isXMLLiteral) {
                        writeAttribute(NS_URI, "parseType", "Literal");
                    } else {
                        writeAttribute(NS_URI, "datatype", datatype.toString());
                    }
                }
                writer.write(">");
                if (isXMLLiteral) {
                    writer.write(obj.toString());
                } else {
                    writer.write(escapeCharacterData(obj.toString()));
                }
                writeEndTag(predNamespace, predLocalName);
            }
            writeNewLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private void flushPendingStatements() throws IOException {
        if (lastWrittenSubject != null) {
            writeEndTag(NS_URI, "Description");
            writeNewLine();
            lastWrittenSubject = null;
        }
    }

    private void writeStartOfStartTag(String namespace, String localName)
            throws IOException {
        String prefix = params.getNamespaceContext().getPrefix(namespace);
        if (prefix == null) {
            writer.write("<");
            writer.write(localName);
            writer.write(" xmlns=\"");
            writer.write(escapeDoubleQuotedAttValue(namespace));
            writer.write("\"");
        } else if (prefix.length() == 0) {
            writer.write("<");
            writer.write(localName);
        } else {
            writer.write("<");
            writer.write(prefix);
            writer.write(":");
            writer.write(localName);
        }
    }

    private void writeAttribute(String attName, String value) throws IOException {
        writer.write(" ");
        writer.write(attName);
        writer.write("=\"");
        writer.write(escapeDoubleQuotedAttValue(value));
        writer.write("\"");
    }

    private void writeAttribute(String namespace, String attName, String value)
            throws IOException {
        String prefix = params.getNamespaceContext().getPrefix(namespace);
        if (prefix == null || prefix.length() == 0) {
            throw new IOException("No prefix has been declared for the namespace used in this attribute: " + namespace);
        }
        writer.write(" ");
        writer.write(prefix);
        writer.write(":");
        writer.write(attName);
        writer.write("=\"");
        writer.write(escapeDoubleQuotedAttValue(value));
        writer.write("\"");
    }

    private void writeEndTag(String namespace, String localName) throws IOException {
        String prefix = params.getNamespaceContext().getPrefix(namespace);
        if (prefix == null || prefix.length() == 0) {
            writer.write("</");
            writer.write(localName);
            writer.write(">");
        } else {
            writer.write("</");
            writer.write(prefix);
            writer.write(":");
            writer.write(localName);
            writer.write(">");
        }
    }

    private void writeNewLine() throws IOException {
        writer.write("\n");
    }

    private String escapeCharacterData(String text) {
        text = gsub("&", "&amp;", text);
        text = gsub("<", "&lt;", text);
        text = gsub(">", "&gt;", text);
        text = gsub("\r", "&#xD;", text);
        return text;
    }

    private String escapeDoubleQuotedAttValue(String value) {
        value = escapeAttValue(value);
        value = gsub("\"", "&quot;", value);
        return value;
    }

    private String escapeAttValue(String value) {
        value = gsub("&", "&amp;", value);
        value = gsub("<", "&lt;", value);
        value = gsub(">", "&gt;", value);
        value = gsub("\t", "&#x9;", value);
        value = gsub("\n", "&#xA;", value);
        value = gsub("\r", "&#xD;", value);
        return value;
    }

    private String gsub(String olds, String news, String text) {
        if (olds == null || olds.length() == 0) {
            return text;
        }
        if (text == null) {
            return null;
        }
        int oldsIndex = text.indexOf(olds);
        if (oldsIndex == -1) {
            return text;
        }
        StringBuilder buf = new StringBuilder(text.length());
        int prevIndex = 0;
        while (oldsIndex >= 0) {
            buf.append(text.substring(prevIndex, oldsIndex));
            buf.append(news);
            prevIndex = oldsIndex + olds.length();
            oldsIndex = text.indexOf(olds, prevIndex);
        }
        buf.append(text.substring(prevIndex));
        return buf.toString();
    }

    private int findURISplitIndex(String uri) {
        int uriLength = uri.length();
        int i = uriLength - 1;
        while (i >= 0) {
            char c = uri.charAt(i);
            if (c == '#' || c == '/' || !XMLUtil.isNCNameChar(c)) {
                break;
            }
            i--;
        }
        i++;
        while (i < uriLength) {
            char c = uri.charAt(i);
            if (c == '_' || XMLUtil.isLetter(c)) {
                break;
            }
            i++;
        }
        if (i == uriLength) {
            i = -1;
        }
        return i;
    }
}
