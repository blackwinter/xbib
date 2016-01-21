package org.xbib.json.xml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.xbib.json.events.CharactersEvent;
import org.xbib.json.events.EndDocumentEvent;
import org.xbib.json.events.EndElementEvent;
import org.xbib.json.events.JsonReaderXmlEvent;
import org.xbib.json.events.StartDocumentEvent;
import org.xbib.json.events.StartElementEvent;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class JsonStaxReader implements XMLStreamReader {

    private static class ProcessingInfo {

        QName name;
        boolean isArray;
        boolean isFirstElement;

        ProcessingInfo(QName name, boolean isArray, boolean isFirstElement) {
            this.name = name;
            this.isArray = isArray;
            this.isFirstElement = isFirstElement;
        }
    }

    private final JsonParser parser;

    private final Queue<JsonReaderXmlEvent> eventQueue = new LinkedList<JsonReaderXmlEvent>();

    private final Stack<ProcessingInfo> processingStack = new Stack<ProcessingInfo>();

    private final JsonNamespaceContext namespaceContext = new JsonNamespaceContext();

    private final Map<String, QName> qNamesOfExpElems = new HashMap<String, QName>();

    private final Map<String, QName> qNamesOfExpAttrs = new HashMap<String, QName>();

    public JsonStaxReader(JsonParser parser) throws XMLStreamException {
        this.parser = parser;
        try {
            readNext();
        } catch (IOException ex) {
            throw new XMLStreamException(ex);
        }
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void readNext() throws IOException {
        readNext(false);
    }

    static final Collection<JsonToken> valueTokens = new HashSet<JsonToken>() {

        {
            add(JsonToken.VALUE_FALSE);
            add(JsonToken.VALUE_TRUE);
            add(JsonToken.VALUE_NULL);
            add(JsonToken.VALUE_STRING);
            add(JsonToken.VALUE_NUMBER_FLOAT);
            add(JsonToken.VALUE_NUMBER_INT);
        }
    };

    private QName getQNameForTagLocName(final String localName) {
        return getQNameForLocName(localName, qNamesOfExpElems);
    }

    private QName getQNameForLocName(final String localName, final Map<String, QName> qNamesMap) {
        final QName result = qNamesMap.get(localName);
        if (result != null) {
            return result;
        } else {
            return new QName(localName);
        }
    }

    private void readNext(boolean lookingForAttributes) throws IOException {
        if (!lookingForAttributes) {
            eventQueue.poll();
        }
        while (eventQueue.isEmpty() || lookingForAttributes) {
            JsonToken jtok;
            while (true) {
                parser.nextToken();
                jtok = parser.getCurrentToken();
                final ProcessingInfo pi = processingStack.peek();
                switch (jtok) {
                    case FIELD_NAME:
                        // start tag
                        String currentName = parser.getCurrentName();
                        if (currentName.startsWith("@")) {
                            currentName = currentName.substring(1);
                        }
                        boolean currentIsAttribute = false;
                        if (lookingForAttributes && currentIsAttribute) {
                            parser.nextToken();
                            if (valueTokens.contains(parser.getCurrentToken())) {
                                eventQueue.peek().addAttribute(getQNameForLocName(currentName, qNamesOfExpAttrs), parser.getText());
                            } else {
                                throw new IOException("Not an attribute, expected primitive value! CurrentNme=" + currentName);
                            }
                        } else { // non attribute
                            if (!("$".equals(currentName))) {
                                final QName currentQName = getQNameForTagLocName(currentName);
                                eventQueue.add(new StartElementEvent(currentQName, new StaxLocation(parser.getCurrentLocation())));
                                processingStack.add(new ProcessingInfo(currentQName, false, true));
                                return;
                            } else {
                                parser.nextToken();
                                if (valueTokens.contains(parser.getCurrentToken())) {
                                    eventQueue.add(new CharactersEvent(parser.getText(), new StaxLocation(parser.getCurrentLocation())));
                                    return;
                                } else {
                                    throw new IOException("Not a xml value, expected primitive value!");
                                }
                            }
                        }
                        break;
                    case START_OBJECT:
                        if (pi == null) {
                            eventQueue.add(new StartDocumentEvent(new StaxLocation(0, 0, 0)));
                            return;
                        }
                        if (pi.isArray && !pi.isFirstElement) {
                            eventQueue.add(new StartElementEvent(pi.name, new StaxLocation(parser.getCurrentLocation())));
                            return;
                        } else {
                            pi.isFirstElement = false;
                        }
                        break;
                    case END_OBJECT:
                        // end tag
                        eventQueue.add(new EndElementEvent(pi.name, new StaxLocation(parser.getCurrentLocation())));
                        if (!pi.isArray) {
                            processingStack.pop();
                        }
                        if (processingStack.isEmpty()) {
                            eventQueue.add(new EndDocumentEvent(new StaxLocation(parser.getCurrentLocation())));
                        }
                        return;
                    case VALUE_FALSE:
                    case VALUE_NULL:
                    case VALUE_NUMBER_FLOAT:
                    case VALUE_NUMBER_INT:
                    case VALUE_TRUE:
                    case VALUE_STRING:
                        if (!pi.isFirstElement) {
                            eventQueue.add(new StartElementEvent(pi.name, new StaxLocation(parser.getCurrentLocation())));
                        } else {
                            pi.isFirstElement = false;
                        }
                        if (jtok != JsonToken.VALUE_NULL) {
                            eventQueue.add(new CharactersEvent(parser.getText(), new StaxLocation(parser.getCurrentLocation())));
                        }
                        eventQueue.add(new EndElementEvent(pi.name, new StaxLocation(parser.getCurrentLocation())));
                        if (!pi.isArray) {
                            processingStack.pop();
                        }
                        if (processingStack.isEmpty()) {
                            eventQueue.add(new EndDocumentEvent(new StaxLocation(parser.getCurrentLocation())));
                        }
                        return;
                    case START_ARRAY:
                        processingStack.peek().isArray = true;
                        break;
                    case END_ARRAY:
                        processingStack.pop();
                        lookingForAttributes = false;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void require(int arg0, String arg1, String arg2) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getElementText() throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int next() throws XMLStreamException {
        try {
            readNext();
            return eventQueue.peek().getEventType();
        } catch (IOException ex) {
            throw new XMLStreamException(ex);
        }
    }

    @Override
    public int nextTag() throws XMLStreamException {
        int eventType = next();
        while ((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip whitespace
                || (eventType == XMLStreamConstants.CDATA && isWhiteSpace()) // skip whitespace
                || eventType == XMLStreamConstants.SPACE || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION || eventType == XMLStreamConstants.COMMENT) {
            eventType = next();
        }
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("expected start or end tag", getLocation());
        }
        return eventType;
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getNamespaceURI(String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStartElement() {
        return eventQueue.peek().isStartElement();
    }

    @Override
    public boolean isEndElement() {
        return eventQueue.peek().isEndElement();
    }

    @Override
    public boolean isCharacters() {
        return eventQueue.peek().isCharacters();
    }

    @Override
    public boolean isWhiteSpace() {
        return false;
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        return eventQueue.peek().getAttributeValue(namespaceURI, localName);
    }

    @Override
    public int getAttributeCount() {
        return eventQueue.peek().getAttributeCount();
    }

    @Override
    public QName getAttributeName(
            int index) {
        return eventQueue.peek().getAttributeName(index);
    }

    @Override
    public String getAttributeNamespace(
            int index) {
        return eventQueue.peek().getAttributeNamespace(index);
    }

    @Override
    public String getAttributeLocalName(
            int index) {
        return eventQueue.peek().getAttributeLocalName(index);
    }

    @Override
    public String getAttributePrefix(
            int index) {
        return eventQueue.peek().getAttributePrefix(index);
    }

    @Override
    public String getAttributeType(
            int index) {
        return eventQueue.peek().getAttributeType(index);
    }

    @Override
    public String getAttributeValue(
            int index) {
        return eventQueue.peek().getAttributeValue(index);
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        return eventQueue.peek().isAttributeSpecified(index);
    }

    @Override
    public int getNamespaceCount() {
        return this.namespaceContext.getNamespaceCount();
    }

    @Override
    public String getNamespacePrefix(int idx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getNamespaceURI(int idx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }

    @Override
    public int getEventType() {
        return eventQueue.peek().getEventType();
    }

    @Override
    public String getText() {
        return eventQueue.peek().getText();
    }

    @Override
    public char[] getTextCharacters() {
        return eventQueue.peek().getTextCharacters();
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        return eventQueue.peek().getTextCharacters(sourceStart, target, targetStart, length);
    }

    @Override
    public int getTextStart() {
        return eventQueue.peek().getTextStart();
    }

    @Override
    public int getTextLength() {
        return eventQueue.peek().getTextLength();
    }

    @Override
    public String getEncoding() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasText() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Location getLocation() {
        return eventQueue.peek().getLocation();
    }

    @Override
    public QName getName() {
        return eventQueue.peek().getName();
    }

    @Override
    public String getLocalName() {
        return eventQueue.peek().getLocalName();
    }

    @Override
    public boolean hasName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getNamespaceURI() {
        return eventQueue.peek().getName().getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        return eventQueue.peek().getPrefix();
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStandalone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean standaloneSet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getCharacterEncodingScheme() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPITarget() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPIData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}