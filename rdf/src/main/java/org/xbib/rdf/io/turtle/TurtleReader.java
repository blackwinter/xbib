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
package org.xbib.rdf.io.turtle;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Stack;

import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.Triplifier;
import org.xbib.rdf.simple.SimpleFactory;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.IdentifiableNode;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.rdf.simple.SimpleTriple;
import org.xbib.xml.namespace.XmlNamespaceContext;

/**
 * Turtle - Terse RDF Triple Parser
 *
 * @see <a href="http://www.w3.org/TeamSubmission/turtle/">Turtle - Terse RDF
 * Triple Language</a>
 *
 */
public class TurtleReader<S extends Identifier, P extends Property, O extends Node>
        implements Triplifier<S, P, O> {

    private final Logger logger = LoggerFactory.getLogger(TurtleReader.class.getName());

    private final SimpleFactory<S,P,O> simpleFactory = SimpleFactory.getInstance();
    /**
     * The base IRI
     */
    private IRI baseIRI;
    /**
     * The push back reader for reading input streams of turtle statements.
     */
    private PushbackReader reader;
    /**
     * The parsed subject
     */
    private S subject;
    /**
     * The parsed predicate
     */
    private P predicate;
    /**
     * The parsed object
     */
    private O object;
    /**
     * The last subject parsed, for sending record events. A collection of
     * triples with same subject in sequence is assumed a record.
     */
    private S lastsubject;
    /**
     * String builder for parsing
     */
    private StringBuilder sb;
    /**
     * Indicate if end of stream is reached
     */
    private boolean eof;
    /**
     * Stack for getResource statements
     */
    private Stack<Triple> triples;
    /**
     * Counter for parsed triples
     */
    private long tripleCounter;
    /**
     * The namespace context
     */
    private XmlNamespaceContext context = XmlNamespaceContext.getDefaultInstance();
    /**
     * An optional triple listener
     */
    private TripleListener<S, P, O> listener;
    private boolean strict = false;

    public TurtleReader(IRI baseIRI) {
        this.baseIRI = baseIRI;
    }

    public TurtleReader context(XmlNamespaceContext context) {
        this.context = context;
        return this;
    }

    @Override
    public TurtleReader setTripleListener(TripleListener<S, P, O> listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Parse turtle input stream. Character encoding must be UTF-8. Turtle is a
     * subset of N3, and N3 uses UTF-8.
     *
     * @see <a href="www.w3.org/DesignIssues/Notation3.html">N3</a>
     *
     * @param in the turtle input stream
     * @throws IOException
     */
    @Override
    public TurtleReader parse(InputStream in) throws IOException {
        return parse(new InputStreamReader(in, "UTF-8"));
    }

    /**
     * Read statements and parse them. A valid base URI must be given in the
     * properties.
     *
     * @param reader the reader
     * @throws IOException if stream can not be parsed
     */
    @Override
    public TurtleReader parse(Reader reader) throws IOException {
        this.reader = new PushbackReader(reader, 2);
        this.sb = new StringBuilder();
        this.eof = false;
        this.triples = new Stack();
        if (listener != null) {
            listener.begin();
        }
        try {
            while (!eof) {
                char ch = skipWhitespace();
                if (eof) {
                    break;
                }
                if (ch == '@') {
                    parseDirective();
                } else {
                    parseTriple();
                    tripleCounter++;
                }
            }
        } finally {
            this.reader.close();
            if (listener != null) {
                listener.end();
            }
        }
        return this;
    }

    public long getTripleCounter() {
        return tripleCounter;
    }

    /**
     * Parse a directive.
     *
     * The prefix directive binds a prefix to a namespace URI. It indicates that
     * a qualified name (qname) with that prefix will thereafter be a shorthand
     * for a URI consisting of the concatenation of the namespace identifier and
     * the bit of the qname to the right of the (only allowed) colon.
     *
     * The namespace prefix may be empty, in which case the qname starts with a
     * colon. This is known as the default namespace. The empty prefix "" is by
     * default , bound to "#" -- the local namespace of the file. The parser
     * behaves as though there were a @prefix : <#>. just before the file. This
     * means that <#foo> can be written :foo.
     *
     * The base directive sets the base URI to be used for the parsing of
     * relative URIs. It takes, itself, a relative URI, so it can be used to
     * change the base URI relative to the previous one.
     *
     * @throws IOException
     */
    private void parseDirective() throws IOException {
        String directive;
        sb.setLength(0);
        boolean b;
        do {
            char ch = read();
            b = !isWhitespace(ch);
            if (b) {
                sb.append(ch);
            }
        } while (b);
        directive = sb.toString();
        skipWhitespace();
        sb.setLength(0);
        if ("@prefix".equalsIgnoreCase(directive)) {
            char ch = read();
            while (ch != ':') {
                sb.append(ch);
                ch = read();
            }
            String prefix = sb.toString();
            reader.read();
            skipWhitespace();
            IRI nsURI = parseURI();
            if ("".equals(prefix)) {
                this.baseIRI = nsURI;
            }
        } else if ("@base".equalsIgnoreCase(directive)) {
            this.baseIRI = parseURI();
        } else {
            throw new IOException(baseIRI + ": unknown directive: " + directive);
        }
        skipWhitespace();
        validate(reader.read(), '.');
    }

    private void parseTriple() throws IOException {
        subject = null;
        predicate = null;
        object = null;
        parseSubject();
        skipWhitespace();
        parsePredicateObjectList();
        skipWhitespace();
        validate(reader.read(), '.');
    }

    private void parsePredicateObjectList() throws IOException {
        predicate = parsePredicate();
        skipWhitespace();
        parseObjectList();
        char ch = skipWhitespace();
        while (ch == ';') {
            reader.read();
            ch = skipWhitespace();
            if (ch == '.' || ch == ']') {
                break;
            }
            predicate = parsePredicate();
            skipWhitespace();
            parseObjectList();
            ch = skipWhitespace();
        }
    }

    private void parseObjectList() throws IOException {
        parseObject();
        char ch = skipWhitespace();
        while (ch == ',') {
            reader.read();
            skipWhitespace();
            parseObject();
            ch = skipWhitespace();
        }
    }

    private void parseSubject() throws IOException {
        char ch = peek();
        if (ch == '(') {
            subject = (S) parseCollection();
        } else if (ch == '[') {
            subject = (S) parseBlankNode();
        } else {
            O value = parseValue();
            if (value instanceof Resource) {
                subject = (S) value;
            } else {
                throw new IOException(baseIRI + ": illegal subject value: '" + value + "' (" + value.getClass() + ")");
            }
        }
    }

    private P parsePredicate() throws IOException {
        char ch = read();
        if (ch == 'a') {
            char ch2 = read();
            if (isWhitespace(ch2)) {
                return simpleFactory.asPredicate("rdf:type");
            }
            reader.unread(ch2);
        }
        reader.unread(ch);
        O obj = parseValue();
        if (obj instanceof Resource) {
            return simpleFactory.asPredicate(obj.toString());
        } else {
            throw new IOException(baseIRI + ": illegal predicate value: " + obj);
        }
    }

    /**
     * Parse triple object
     *
     * @throws IOException
     */
    private void parseObject() throws IOException {
        char ch = peek();
        if (ch == '(') {
            object = (O) parseCollection();
        } else if (ch == '[') {
            object = (O) parseBlankNode();
        } else {
            object = parseValue();
        }
        Triple stmt = new SimpleTriple(subject, predicate, object);
        if (subject instanceof Identifier) {
            // Push triples with blank node subjects on stack.
            // The idea for having ordered getResource properties is:
            // All getResource property triples should be serialized
            // after the getResource parent triple.
            triples.add(0, stmt);
        } else {
            // Send record events. A record is grouped by a sequence of same non-blank subjects
            if (lastsubject == null) {
                if (listener != null) {
                    listener.newIdentifier(subject.id());
                }
                lastsubject = subject;
            } else if (!subject.equals(lastsubject)) {
                if (listener != null) {
                    listener.newIdentifier(subject.id());
                }
                lastsubject = subject;
            }
            if (listener != null) {
                listener.triple(stmt);
            }
            while (!triples.isEmpty()) {
                Triple<S, P, O> s = triples.pop();
                if (listener != null) {
                    listener.triple(s);
                }
            }
        }
    }

    private O parseValue() throws IOException {
        char ch = peek();
        if (ch == '<') {
            return (O) new SimpleResource().id(parseURI());
        } else if (ch == ':' || isPrefixStartChar(ch)) {
            return parseQNameOrBoolean();
        } else if (ch == '_') {
            return (O) parseNodeID();
        } else if (ch == '(') {
            return (O) parseCollection();
        } else if (ch == '"') {
            return (O) parseQuotedLiteral();
        } else if (Character.isDigit(ch) || ch == '.' || ch == '+' || ch == '-') {
            return (O) parseNumber();
        } else if ((int) ch == 65535) {
            throw new EOFException();
        } else {
            throw new IOException(baseIRI
                    + ": unable to parse value, unknown character: code = " + (int) ch
                    + " character = '" + ch + "'");
        }
    }

    /**
     * Parse IRI
     *
     * @return an IRI
     * @throws IOException
     */
    private IRI parseURI() throws IOException {
        char ch = read();
        validate(ch, '<');
        sb.setLength(0);
        ch = read();
        boolean ended;
        do {
            while (ch != '>') {
                sb.append(ch);
                if (ch == '\\') {
                    ch = read();
                    sb.append(ch);
                }
                ch = read();
            }
            // '>' not escaped?
            ch = read();
            ended = (ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r');
            if (!ended) {
                logger.warn("{} unescaped ''>'' in URI: {}", subject, sb);
            }
        } while (!ended);
        String decoded = decode(sb.toString(), "UTF-8");
        IRI u = IRI.create(decoded);
        u = baseIRI.resolve(u);
        return u;
    }

    /**
     * Parse qualified name
     *
     * @return qualified name URI
     * @throws IOException
     */
    private O parseQNameOrBoolean() throws IOException {
        char ch = read();
        if (ch != ':' && !isPrefixStartChar(ch)) {
            throw new IOException(baseIRI + ": expected colon or letter, not: '" + ch + "'");
        }
        sb.setLength(0);
        String ns;
        if (ch == ':') {
            ns = context.getNamespaceURI("");
        } else {
            sb.append(ch);
            ch = read();
            while (Character.isLetter(ch) || Character.isDigit(ch) || ch == '-' || ch == '_') {
                sb.append(ch);
                ch = read();
            }
            if (ch != ':') {
                String value = sb.toString();
                if (value.equals("true") || value.equals("false")) {
                    return (O) simpleFactory.newLiteral(value).type(IRI.create("xsd:boolean"));
                }
            }
            validate(ch, ':');
            ns = context.getNamespaceURI(sb.toString());
            if (ns == null) {
                throw new IOException(baseIRI + ": namespace not found: " + sb.toString());
            }
        }
        sb.setLength(0);
        ch = read();
        if (Character.isLetter(ch) || ch == '_') {
            sb.append(ch);
            ch = read();
            while (Character.isLetter(ch) || Character.isDigit(ch) || ch == '-' || ch == '_') {
                sb.append(ch);
                ch = read();
            }
        }
        reader.unread(ch);
        // namespace is already resolved
        return (O) new SimpleResource().id(IRI.create(ns + sb));
    }

    /**
     * Parse blank node, with or without a node ID
     *
     * @return
     * @throws IOException
     */
    private Identifier parseBlankNode() throws IOException {
        char ch = peek();
        if (ch == '_') {
            return parseNodeID();
        } else if (ch == '[') {
            reader.read();
            Identifier bnode = new IdentifiableNode();
            ch = read();
            if (ch != ']') {
                S oldsubject = subject;
                P oldpredicate = predicate;
                subject = (S) bnode;
                skipWhitespace();
                parsePredicateObjectList();
                skipWhitespace();
                validate(reader.read(), ']');
                subject = oldsubject;
                predicate = oldpredicate;
            }
            return bnode;
        } else {
            throw new IOException(baseIRI + ":expected character: '[' or '_'");
        }
    }

    /**
     * Parse a collection
     *
     * @return the collection as a getResource
     * @throws IOException
     */
    private Resource<S, P, O> parseCollection() throws IOException {
        validate(reader.read(), '(');
        char ch = skipWhitespace();
        if (ch == ')') {
            reader.read();
            SimpleResource<S, P, O> r = new SimpleResource();
            r.id(IRI.create("rdf:nil"));
            return r;
        } else {
            SimpleResource<S,P,O> first = new SimpleResource();
            S oldsubject = subject;
            P oldpredicate = predicate;
            subject = (S)first.id();
            predicate = simpleFactory.asPredicate("rdf:first");
            parseObject();
            ch = skipWhitespace();
            Identifier blanknode = new IdentifiableNode().id(first.id());
            while (ch != ')') {
                Identifier value = new IdentifiableNode();
                if (listener != null) {
                    listener.triple(new SimpleTriple(blanknode, simpleFactory.asPredicate("rdf:rest"), value));
                }
                subject = (S) value;
                blanknode = value;
                parseObject();
                ch = skipWhitespace();
            }
            reader.read();
            if (listener != null) {
                listener.triple(new SimpleTriple(blanknode,
                        simpleFactory.asPredicate("rdf:rest"),
                        "rdf:null"));
            }
            subject = oldsubject;
            predicate = oldpredicate;
            return first;
        }
    }

    /**
     * Parse node ID
     *
     * @return
     * @throws IOException
     */
    private Identifier parseNodeID() throws IOException {
        validate(reader.read(), '_');
        validate(reader.read(), ':');
        char ch = read();
        sb.setLength(0);
        if (Character.isLetter(ch) || ch == '_') {
            sb.append(ch);
            ch = read();
            while (Character.isLetter(ch) || Character.isDigit(ch) || ch == '-' || ch == '_') {
                sb.append(ch);
                ch = read();
            }
        }
        reader.unread(ch);
        String nodeID = sb.toString();
        Identifier bnode = bnodes.get(nodeID);
        if (bnode != null) {
            return bnode;
        }
        bnode = new IdentifiableNode().id(nodeID);
        bnodes.put(nodeID, bnode);
        return bnode;
    }
    private final HashMap<String, Identifier> bnodes = new HashMap<>();

    /**
     * Parse a literal
     *
     * @return the literal
     * @throws IOException
     */
    private Literal<O> parseQuotedLiteral() throws IOException {
        String value = parseQuotedString();
        char ch = peek();
        if (ch == '@') {
            reader.read();
            sb.setLength(0);
            ch = read();
            if (!Character.isLowerCase(ch)) {
                throw new IOException(baseIRI + ": lower case character expected: " + ch);
            }
            sb.append(ch);
            ch = read();
            while (Character.isLowerCase(ch) || Character.isDigit(ch) || ch == '-') {
                sb.append(ch);
                ch = read();
            }
            reader.unread(ch);
            return new SimpleLiteral(value).language(sb.toString());
        } else if (ch == '^') {
            reader.read();
            validate(reader.read(), '^');
            return new SimpleLiteral(value).type(parseURI());
        } else {
            return new SimpleLiteral(value);
        }
    }

    /**
     * Parses a quoted string, which is either a "normal string" or a """long
     * string""".
     */
    private String parseQuotedString() throws IOException {
        String result;
        validate(reader.read(), '\"');
        char c2 = read();
        char c3 = read();
        if (c2 == '"' && c3 == '"') {
            result = parseLongString();
        } else {
            reader.unread(c3);
            reader.unread(c2);
            result = parseString();
        }
        return decodeTurtleString(result);
    }

    /**
     * Parses a "normal string". This method assumes that the first double quote
     * has already been parsed.
     */
    private String parseString() throws IOException {
        sb.setLength(0);
        while (true) {
            char ch = read();
            if (ch == '"') {
                break;
            }
            sb.append(ch);
            if (ch == '\\') {
                ch = read();
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Parses a """long string""". This method assumes that the first three
     * double quotes have already been parsed.
     */
    protected String parseLongString() throws IOException {
        sb.setLength(0);
        int doubleQuoteCount = 0;
        char ch;
        while (doubleQuoteCount < 3) {
            ch = read();
            if (ch == '"') {
                doubleQuoteCount++;
            } else {
                doubleQuoteCount = 0;
            }
            sb.append(ch);
            if (ch == '\\') {
                // This escapes the next character, which might be a '"'
                ch = read();
                sb.append(ch);
            }
        }

        return sb.substring(0, sb.length() - 3);
    }

    private Literal<O> parseNumber() throws IOException {
        sb.setLength(0);
        IRI datatype = IRI.create("xsd:integer");
        char ch = read();
        if (ch == '+' || ch == '-') {
            sb.append(ch);
            ch = read();
        }
        while (Character.isDigit(ch)) {
            sb.append(ch);
            ch = read();
        }
        if (ch == '.' || ch == 'e' || ch == 'E') {
            datatype = IRI.create("xsd:decimal");
            if (ch == '.') {
                sb.append(ch);
                ch = read();
                while (Character.isDigit(ch)) {
                    sb.append(ch);
                    ch = read();
                }
                if (sb.length() == 1) {
                    throw new IOException(" incomplete decimal: " + sb);
                }
            } else {
                if (sb.length() == 0) {
                    throw new IOException("in complete fraction: " + sb);
                }
            }
            if (ch == 'e' || ch == 'E') {
                datatype = IRI.create("xsd:double");
                sb.append(ch);
                ch = read();
                if (ch == '+' || ch == '-') {
                    sb.append(ch);
                    ch = read();
                }
                if (!Character.isDigit(ch)) {
                    throw new IOException("exponent value missing: " + sb);
                }
                sb.append(ch);
                ch = read();
                while (Character.isDigit(ch)) {
                    sb.append(ch);
                    ch = read();
                }
            }
        }
        reader.unread(ch);
        return new SimpleLiteral(sb.toString()).type(datatype);
    }

    private char skipWhitespace() throws IOException {
        int ch = reader.read();
        while (isWhitespace((char) ch) || ch == '#') {
            if (ch == '#') {
                skipLine();
            }
            ch = reader.read();
        }
        if (ch == -1) {
            eof = true;
        }
        reader.unread(ch);
        return (char) ch;
    }

    private void skipLine() throws IOException {
        int ch = reader.read();
        while (ch != 0xd && ch != 0xa && ch != -1) {
            ch = reader.read();
        }
        if (ch == 0xd) {
            ch = reader.read();
            if (ch != 0xa) {
                reader.unread(ch);
            }
        }
        if (ch == -1) {
            eof = true;
        }
    }

    private char peek() throws IOException {
        int ch = reader.read();
        if (ch == -1) {
            eof = true;
        }
        reader.unread(ch);
        return (char) ch;
    }

    private char read() throws IOException {
        int ch = reader.read();
        if (ch == -1) {
            throw new EOFException();
        }
        return (char) ch;
    }

    private void validate(int ch, char v) throws IOException {
        if ((char) ch != v) {
            String message = (subject != null ? subject : "") + " unexpected character: '" + (char) ch + "' expected: '" + v + "'";
            if (strict) {
                throw new IOException(message);
            } else {
                logger.info(message);
            }
        }
    }

    private boolean isWhitespace(char ch) {
        return ch == 0x20 || ch == 0x9 || ch == 0xA || ch == 0xD;
    }

    private boolean isPrefixStartChar(char ch) {
        return Character.isLetter(ch) || ch >= 0x00C0 && ch <= 0x00D6
                || ch >= 0x00D8 && ch <= 0x00F6 || ch >= 0x00F8 && ch <= 0x02FF
                || ch >= 0x0370 && ch <= 0x037D || ch >= 0x037F && ch <= 0x1FFF
                || ch >= 0x200C && ch <= 0x200D || ch >= 0x2070 && ch <= 0x218F
                || ch >= 0x2C00 && ch <= 0x2FEF || ch >= 0x3001 && ch <= 0xD7FF
                || ch >= 0xF900 && ch <= 0xFDCF || ch >= 0xFDF0 && ch <= 0xFFFD
                || ch >= 0x10000 && ch <= 0xEFFFF;
    }

    /**
     * Decodes an encoded Turtle string. Any \-escape sequences are substituted
     * with their decoded sb.
     *
     * @param s An encoded Turtle string.
     * @return The unencoded string.
     * @exception IllegalArgumentException If the supplied string is not a
     * correctly encoded Turtle string.
     *
     */
    private String decodeTurtleString(String s) {
        int pos = s.indexOf('\\');
        if (pos == -1) {
            return s;
        }
        int i = 0;
        int len = s.length();
        sb.setLength(0);
        while (pos != -1) {
            sb.append(s.substring(i, pos));
            if (pos + 1 >= len) {
                if (strict) {
                    throw new IllegalArgumentException("unescaped backslash in: " + s);
                } else {
                    logger.warn("unescaped backslash in: " + s);
                    break;
                }
            }
            char ch = s.charAt(pos + 1);
            if (ch == 't') {
                sb.append('\t');
                i = pos + 2;
            } else if (ch == 'r') {
                sb.append('\r');
                i = pos + 2;
            } else if (ch == 'n') {
                sb.append('\n');
                i = pos + 2;
            } else if (ch == '"') {
                sb.append('"');
                i = pos + 2;
            } else if (ch == '>') {
                sb.append('>');
                i = pos + 2;
            } else if (ch == '\\') {
                sb.append('\\');
                i = pos + 2;
            } else if (ch == 'u') {
                if (pos + 5 >= len) {
                    throw new IllegalArgumentException("incomplete Unicode escape sequence in: " + s);
                }
                String xx = s.substring(pos + 2, pos + 6);
                try {
                    ch = (char) Integer.parseInt(xx, 16);
                    sb.append(ch);
                    i = pos + 6;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("illegal Unicode escape sequence '\\u" + xx + "' in: " + s);
                }
            } else if (ch == 'U') {
                if (pos + 9 >= len) {
                    throw new IllegalArgumentException("incomplete Unicode escape sequence in: " + s);
                }
                String xx = s.substring(pos + 2, pos + 10);
                try {
                    ch = (char) Integer.parseInt(xx, 16);
                    sb.append(ch);
                    i = pos + 10;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("illegal Unicode escape sequence '\\U" + xx + "' in: " + s);
                }
            } else {
                if (strict) {
                    throw new IllegalArgumentException("unescaped backslash in: " + s);
                } else {
                    logger.warn("unescaped backslash in: " + s);
                    sb.append('\\');
                    i = pos + 2;
                }
            }
            pos = s.indexOf('\\', i);
        }
        sb.append(s.substring(i));
        return sb.toString();
    }
    
    
    public static String decode(String s, String encoding) {
        StringBuilder sb = new StringBuilder();
        boolean fragment = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '+':
                    sb.append(' ');
                    break;
                case '#':
                    sb.append(ch);
                    fragment = true;
                    break;
                case '%':
                    if (!fragment) {
                        // fast hex decode
                        sb.append((char) ((Character.digit(s.charAt(++i), 16) << 4)
                                | Character.digit(s.charAt(++i), 16)));
                    } else {
                        sb.append(ch);
                    }
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        try {
            return new String(sb.toString().getBytes("ISO-8859-1"), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new Error("encoding " + encoding + " not supported");
        }
    }
    
}
