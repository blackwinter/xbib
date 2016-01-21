package org.xbib.rdf.io.source;

import org.xbib.rdf.io.sink.XmlSink;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

final class XmlSource extends AbstractSource<XmlSink> {

    private XMLReader xmlReader = null;

    XmlSource(XmlSink sink) {
        super(sink);
    }

    @Override
    public void process(Reader reader, String mimeType, String baseUri) throws IOException {
        try {
            initXmlReader();
        } catch (SAXException e) {
            throw new IOException("can not instantinate XMLReader", e);
        }
        try {
            sink.setBaseUri(baseUri);
            xmlReader.parse(new InputSource(reader));
        } catch (SAXException e) {
            try {
                sink.endDocument();
            } catch (SAXException e2) {
                // do nothing
            }
            throw new IOException(e);
        }
    }

    @Override
    public void process(InputStream inputStream, String mimeType, String baseUri) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"))) {
            process(reader, mimeType, baseUri);
        }
    }

    private void initXmlReader() throws SAXException {
        if (xmlReader == null) {
            xmlReader = getDefaultXmlReader();
        }
        xmlReader.setContentHandler(sink);
        xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", sink);
    }

    public void setXmlReader(XMLReader xmlReader) throws SAXException {
        if (xmlReader == null) {
            this.xmlReader = getDefaultXmlReader();
        } else {
            this.xmlReader = xmlReader;
        }
    }

    public static XMLReader getDefaultXmlReader() throws SAXException {
        XMLReader result = XMLReaderFactory.createXMLReader();
        result.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return result;
    }
}
