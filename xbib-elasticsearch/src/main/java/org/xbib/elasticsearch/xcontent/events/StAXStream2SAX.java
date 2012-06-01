package org.xbib.elasticsearch.xcontent.events;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class StAXStream2SAX {

    private final SAXEventBuffer buffer;

    public StAXStream2SAX(SAXEventBuffer buffer) {
        this.buffer = buffer;
    }

    public void bridgeEvent(XMLStreamReader reader) throws XMLStreamException {
        switch (reader.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                handleStartElement(reader);
                break;
            case XMLStreamConstants.END_ELEMENT:
                handleEndElement(reader);
                break;
            case XMLStreamConstants.CHARACTERS:
                handleCharacters(reader);
                break;
        }
    }

    private void handleCharacters(XMLStreamReader reader) throws XMLStreamException {
        char[] buf = new char[reader.getTextLength()];
        int len, start = 0;

        do {
            len = reader.getTextCharacters(start, buf, 0, buf.length);
            start += len;
            try {
                buffer.characters(buf, 0, len);
            } catch (SAXException e) {
                throw new XMLStreamException(e);
            }
        } while (len == buf.length);
    }

    private void handleEndElement(XMLStreamReader reader) throws XMLStreamException {
        try {
            QName name = reader.getName();
            String prefix = name.getPrefix();
            String rawname;

            if (prefix == null || prefix.length() == 0) {
                rawname = name.getLocalPart();
            } else {
                rawname = prefix + ':' + name.getLocalPart();
            }

            buffer.endElement(
                    name.getNamespaceURI(),
                    name.getLocalPart(),
                    rawname);

            for (int i = reader.getNamespaceCount() - 1; i >= 0; i--) {
                prefix = reader.getNamespacePrefix(i);
                if (prefix == null) {
                    prefix = "";
                }

                buffer.endPrefixMapping(prefix);
            }
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleStartElement(XMLStreamReader reader) throws XMLStreamException {
        try {
            for (int i = 0; i < reader.getNamespaceCount(); i++) {
                String uri = reader.getNamespaceURI(i);
                if (uri == null) {
                    uri = "";
                }

                String prefix = reader.getNamespacePrefix(i);
                if (prefix == null) {
                    prefix = "";
                }

                buffer.startPrefixMapping(
                        prefix,
                        uri);
            }

            QName name = reader.getName();
            String prefix = name.getPrefix();
            String rawname;

            if (prefix == null || prefix.length() == 0) {
                rawname = name.getLocalPart();
            } else {
                rawname = prefix + ':' + name.getLocalPart();
            }

            Attributes attrs = getAttributes(reader);
            buffer.startElement(
                    name.getNamespaceURI(),
                    name.getLocalPart(),
                    rawname,
                    attrs);

        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private Attributes getAttributes(XMLStreamReader reader) {
        AttributesImpl attrs = new AttributesImpl();

        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            String uri = reader.getNamespaceURI(i);
            if (uri == null) {
                uri = "";
            }

            String prefix = reader.getNamespacePrefix(i);
            if (prefix == null) {
                prefix = "";
            }

            String name = "xmlns";
            if (prefix.length() == 0) {
                prefix = name;
            } else {
                name += ':' + prefix;
            }

            attrs.addAttribute("http://www.w3.org/2000/xmlns/", prefix, name, "CDATA", uri);
        }

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String uri = reader.getAttributeNamespace(i);
            if (uri == null) {
                uri = "";
            }

            String localName = reader.getAttributeLocalName(i);
            String prefix = reader.getAttributePrefix(i);
            String name;

            if (prefix == null || prefix.length() == 0) {
                name = localName;
            } else {
                name = prefix + ':' + localName;
            }

            String type = reader.getAttributeType(i);
            String value = reader.getAttributeValue(i);

            attrs.addAttribute(uri, localName, name, type, value);
        }

        return attrs;
    }
}
