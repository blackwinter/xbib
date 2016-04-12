package org.xbib.openurl.internal.parsers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xbib.openurl.Book;
import org.xbib.openurl.ContextObject;
import org.xbib.openurl.Dissertation;
import org.xbib.openurl.Format;
import org.xbib.openurl.Journal;
import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequestProcessor;
import org.xbib.openurl.Patent;
import org.xbib.openurl.Service;
import org.xbib.openurl.config.OpenURLConfig;
import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.descriptors.Identifier;
import org.xbib.openurl.entities.Referent;
import org.xbib.openurl.entities.Referrer;
import org.xbib.openurl.entities.ReferringEntity;
import org.xbib.openurl.entities.Requester;
import org.xbib.openurl.entities.Resolver;
import org.xbib.openurl.entities.ServiceType;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Parse OpenURLs in XML Context Object Format
 */
public class XMLParser {

    private final static Logger logger = LogManager.getLogger(XMLParser.class);

    private final static HashMap<URI, Schema> schemas = new HashMap<>();

    static {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemas.put(Format.FORMAT_XML_CONTEXT_URI,
                    schemaFactory.newSchema(XMLParser.class.getResource("/org/xbib/openurl/info-ofi-fmt-xml-xsd-ctx.xsd")));
            schemas.put(Format.FORMAT_XML_BOOK_URI,
                    schemaFactory.newSchema(XMLParser.class.getResource("/org/xbib/openurl/info-ofi-fmt-xml-xsd-book.xsd")));
            schemas.put(Format.FORMAT_XML_DISSERTATION_URI,
                    schemaFactory.newSchema(XMLParser.class.getResource("/org/xbib/openurl/info-ofi-fmt-xml-xsd-dissertation.xsd")));
            schemas.put(Format.FORMAT_XML_JOURNAL_URI,
                    schemaFactory.newSchema(XMLParser.class.getResource("/org/xbib/openurl/info-ofi-fmt-xml-xsd-journal.xsd")));
            schemas.put(Format.FORMAT_XML_PATENT_URI,
                    schemaFactory.newSchema(XMLParser.class.getResource("/org/xbib/openurl/info-ofi-fmt-xml-xsd-patent.xsd")));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private final DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
    private OpenURLConfig config;

    public XMLParser(OpenURLConfig config) {
        this.config = config;
    }

    public ContextObject parseContextObject(URI format, InputStream in) throws OpenURLException {
        Document document = parse(format, in);
        // info URI mapping to objects
        if (Format.FORMAT_XML_CONTEXT_URI.equals(format)) {
            return createContextObject(document);
        }
        if (Format.FORMAT_XML_BOOK_URI.equals(format)) {
            return createBook(document);
        }
        if (Format.FORMAT_XML_JOURNAL_URI.equals(format)) {
            return createJournal(document);
        }
        if (Format.FORMAT_XML_DISSERTATION_URI.equals(format)) {
            return createDissertation(document);
        }
        if (Format.FORMAT_XML_PATENT_URI.equals(format)) {
            return createPatent(document);
        }
        throw new OpenURLException("unknown context object schema: " + format);
    }

    public Document parse(URI format, InputStream in) throws OpenURLException {
        try {
            Schema schema = schemas.get(format);
            if (schema == null) {
                throw new OpenURLException("no schema found for " + format);
            }
            documentFactory.setNamespaceAware(true);
            documentFactory.setSchema(schema);
            DocumentBuilder builder = documentFactory.newDocumentBuilder();
            Document document = builder.parse(in);
            return document;
        } catch (Exception ex) {
            throw new OpenURLException(ex);
        }
    }

    public ContextObject createContextObject(URI format, InputStream in) throws OpenURLException {
        return createContextObject(parse(format, in));
    }

    public ContextObject createContextObject(Document document) throws OpenURLException {
        OpenURLRequestProcessor processor = config.getProcessor();
        String identifier = null;
        String version = null;
        String timestamp = null;
        List<Descriptor> referentDescriptors = new ArrayList<>();
        List<Descriptor> requesterDescriptors = new ArrayList<>();
        List<Descriptor> referringEntityDescriptors = new ArrayList<>();
        List<Descriptor> referrerDescriptors = new ArrayList<>();
        List<Descriptor> resolverDescriptors = new ArrayList<>();
        List<Descriptor> serviceTypeDescriptors = new ArrayList<>();
        Map<Identifier, Service> serviceMap = new HashMap<>();
        Iterator<Node> it = null;
        try {
            Referent referent = processor.createReferent(referentDescriptors);
            Requester requester = processor.createRequester(requesterDescriptors);
            ReferringEntity referringEntity = processor.createReferringEntity(referringEntityDescriptors);
            Referrer referrer = processor.createReferrer(referrerDescriptors);
            Resolver resolver = processor.createResolver(resolverDescriptors);
            ServiceType serviceType = processor.createServiceType(serviceMap);
            ContextObject contextobject = processor.createContextObject(referent,
                    Collections.singletonList(referringEntity),
                    Collections.singletonList(requester),
                    Collections.singletonList(serviceType),
                    Collections.singletonList(resolver),
                    Collections.singletonList(referrer),
                    identifier,
                    version,
                    timestamp);
            return contextobject;
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
    }

    public Book createBook(Document document) throws OpenURLException {
        return null;
    }

    public Journal createJournal(Document document) throws OpenURLException {
        return null;
    }

    public Dissertation createDissertation(Document document) throws OpenURLException {
        return null;
    }

    public Patent createPatent(Document document) throws OpenURLException {
        return null;
    }
}