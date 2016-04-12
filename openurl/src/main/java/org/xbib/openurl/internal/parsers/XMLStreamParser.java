package org.xbib.openurl.internal.parsers;

import org.xbib.openurl.ContextObject;
import org.xbib.openurl.Format;
import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequestProcessor;
import org.xbib.openurl.Service;
import org.xbib.openurl.config.OpenURLConfig;
import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.descriptors.Identifier;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

public class XMLStreamParser {

    private final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private OpenURLConfig config;

    public XMLStreamParser(OpenURLConfig config) throws Exception {
        this.config = config;
        URI[] formats = new URI[]{
                Format.FORMAT_XML_CONTEXT_URI,
                Format.FORMAT_XML_BOOK_URI,
                Format.FORMAT_XML_DISSERTATION_URI,
                Format.FORMAT_XML_JOURNAL_URI,
                Format.FORMAT_XML_PATENT_URI
        };
        for (URI format : formats) {
            String res = "/org/xbib/openurl/" + format.toString().replace(':', '-').replace('/', '-') + ".xsd";
            //schemas.put(format, schemaFactory.createSchema(XMLStreamParser.class.getResource(res)));
        }
    }

    public ContextObject createContextObject(URI format, InputStream in) throws OpenURLException {
        return createContextObject(format, new InputStreamReader(in));
    }

    public ContextObject createContextObject(URI format, Reader reader) throws OpenURLException {
        XMLStreamReader xmlr = null;
        try {
            //XMLValidationSchema schema = schemas.get(format);
            xmlr = inputFactory.createXMLStreamReader(reader);
            //Stax2ReaderAdapter.wrapIfNecessary(xmlr).validateAgainst(schema);
        } catch (Exception e) {
            OpenURLException ex = new OpenURLException(e.getMessage(), e);
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        }
        // info URI mapping to objects
        if (Format.FORMAT_XML_CONTEXT_URI.equals(format)) {
            return createContextObject(xmlr);
        }
        if (Format.FORMAT_XML_BOOK_URI.equals(format)) {
            return createBook(xmlr);
        }
        if (Format.FORMAT_XML_JOURNAL_URI.equals(format)) {
            return createJournal(xmlr);
        }
        if (Format.FORMAT_XML_DISSERTATION_URI.equals(format)) {
            return createDissertation(xmlr);
        }
        if (Format.FORMAT_XML_PATENT_URI.equals(format)) {
            return createPatent(xmlr);
        }
        throw new OpenURLException("unknown context object schema: " + format);
    }

    private ContextObject createContextObject(XMLStreamReader xmlr) throws OpenURLException {
        OpenURLRequestProcessor processor = config.getProcessor();
        String identifier = null;
        String version = null;
        String timestamp = null;
        ArrayList<Descriptor> referentDescriptors = new ArrayList<Descriptor>();
        ArrayList<Descriptor> requesterDescriptors = new ArrayList<Descriptor>();
        ArrayList<Descriptor> referringEntityDescriptors = new ArrayList<Descriptor>();
        ArrayList<Descriptor> referrerDescriptors = new ArrayList<Descriptor>();
        ArrayList<Descriptor> resolverDescriptors = new ArrayList<Descriptor>();
        ArrayList<Descriptor> serviceTypeDescriptors = new ArrayList<Descriptor>();
        HashMap<Identifier, Service> serviceMap = new HashMap<Identifier, Service>();
        try {
            while (xmlr.hasNext()) {
                xmlr.next();
                //logger.log(Level.INFO, "event type = " + xmlr.next());
            }
        } catch (XMLStreamException e) {
            OpenURLException ex = new OpenURLException(e.getMessage(), e);
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        }

        return null;
    }

    public ContextObject createBook(XMLStreamReader xmlr) throws OpenURLException {
        OpenURLRequestProcessor processor = config.getProcessor();
        return null;
    }

    public ContextObject createJournal(XMLStreamReader xmlr) throws OpenURLException {
        OpenURLRequestProcessor processor = config.getProcessor();
        return null;
    }

    public ContextObject createDissertation(XMLStreamReader xmlr) throws OpenURLException {
        OpenURLRequestProcessor processor = config.getProcessor();
        return null;
    }

    public ContextObject createPatent(XMLStreamReader xmlr) throws OpenURLException {
        OpenURLRequestProcessor processor = config.getProcessor();
        return null;
    }
}
