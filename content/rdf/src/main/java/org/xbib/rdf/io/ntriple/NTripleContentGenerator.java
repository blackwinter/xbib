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
package org.xbib.rdf.io.ntriple;

import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.RdfContentGenerator;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * NTriple content generator
 */
public class NTripleContentGenerator
        implements RdfContentGenerator<NTripleContentParams>, Flushable {

    private final static char LF = '\n';

    private final Writer writer;

    private NTripleContentParams params = NTripleContentParams.DEFAULT_PARAMS;

    NTripleContentGenerator(OutputStream out) throws IOException {
        this(new OutputStreamWriter(out, "UTF-8"));
    }

    NTripleContentGenerator(Writer writer) throws IOException {
        this.writer = writer;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public NTripleContentGenerator receive(IRI iri) throws IOException {
        //String compact = params.getNamespaceContext().compact(iri);
        return this;
    }

    @Override
    public RdfContentGenerator setParams(NTripleContentParams rdfContentParams) {
        this.params = rdfContentParams;
        return this;
    }

    @Override
    public NTripleContentParams getParams() {
        return params;
    }

    @Override
    public NTripleContentGenerator startStream() {
        return this;
    }

    @Override
    public RdfContentGenerator setBaseUri(String baseUri) {
        startPrefixMapping("", baseUri);
        return this;
    }

    @Override
    public NTripleContentGenerator receive(Triple triple) throws IOException {
        writer.write(writeStatement(triple));
        return this;
    }

    @Override
    public NTripleContentGenerator endStream() {
        return this;
    }

    @Override
    public NTripleContentGenerator startPrefixMapping(String prefix, String uri) {
        params.getNamespaceContext().addNamespace(prefix, uri);
        return this;
    }

    @Override
    public NTripleContentGenerator endPrefixMapping(String prefix) {
        // we don't remove name spaces. It's troubling RDF serializations.
        //namespaceContext.removeNamespace(prefix);
        return this;
    }

    @Override
    public NTripleContentGenerator receive(Resource resource) throws IOException {
        resource.triples().stream().forEach(t -> {
            try {
                writer.write(writeStatement(t));
            } catch (IOException e) {
                //
            }
        });
        return this;
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    public String writeStatement(Triple stmt) throws IOException {
        Resource subj = stmt.subject();
        IRI pred = stmt.predicate();
        Node obj = stmt.object();
        return writeSubject(subj) + " " + writePredicate(pred) + " " + writeObject(obj) + " ." + LF;
    }

    public String writeSubject(Resource subject) {
        return subject.isEmbedded() ? subject.toString() : "<" + escape(subject.toString()) + ">";
    }

    public String writePredicate(IRI predicate) {
        return "<" + escape(predicate.toString()) + ">";
    }

    public String writeObject(Node object) {
        if (object instanceof Resource) {
            Resource subject = (Resource) object;
            return writeSubject(subject);
        } else if (object instanceof Literal) {
            Literal value = (Literal) object;
            String s = "\"" + escape(value.object().toString()) + "\"";
            String lang = value.language();
            IRI type = value.type();
            if (lang != null) {
                return s + "@" + lang;
            }
            if (type != null) {
                return s + "^^<" + escape(type.toString()) + ">";
            }
            return s;
        } else if (object instanceof IRI) {
            return  "<" + escape(object.toString()) + ">";
        }
        return "<class?>^^" + object.getClass();
    }

    private String escape(String buffer) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buffer.length(); i++) {
            char ch = buffer.charAt(i);
            switch (ch) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch >= 32 && ch <= 126) {
                        sb.append(ch);
                    } else {
                        sb.append("\\u");
                        String hexstr = Integer.toHexString(ch).toUpperCase();
                        int pad = 4 - hexstr.length();
                        for (; pad > 0; pad--) {
                            sb.append("0");
                        }
                        sb.append(hexstr);
                    }
            }
        }
        return sb.toString();
    }


    /**
     * Translate a literal according to given sort language (e.g. mechanical word order, sort area).
     * see http://www.w3.org/International/articles/language-tags/
     *
     * @param literal the literal
     * @return the process literal
     */
    private Literal translateLanguage(Literal literal, String langTag) {
        if (literal == null) {
            return null;
        }
        // we assume we have only one sort language. Search for '@' symbol.
        String value = literal.object().toString();
        // ignore if on position 0
        int pos = value.indexOf(" @");
        if (pos == 0) {
            literal.object(value.substring(1));
        } else if (pos > 0) {
            literal.object('\u0098' + value.substring(0, pos + 1) + '\u009c' + value.substring(pos + 2))
                    .language(langTag);
        }
        return literal;
    }

}
