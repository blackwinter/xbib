package org.xbib.openurl.internal.serializers;

import org.xbib.openurl.ContextObject;
import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.Serializer;
import org.xbib.openurl.config.OpenURLConfig;
import org.xbib.openurl.descriptors.ByReferenceMetadata;
import org.xbib.openurl.descriptors.ByValueMetadataKev;
import org.xbib.openurl.descriptors.ByValueMetadataXml;
import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.descriptors.Identifier;
import org.xbib.openurl.descriptors.PrivateData;
import org.xbib.openurl.entities.Entity;
import org.xbib.openurl.entities.Referrer;
import org.xbib.openurl.entities.ReferringEntity;
import org.xbib.openurl.entities.Requester;
import org.xbib.openurl.entities.Resolver;
import org.xbib.openurl.entities.ServiceType;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serialize ContextObjects into an XML format conforming to
 * ANSI/NISO Z39.88-2004, Part 3 "The XML Context Object Format".
 */
public class XMLSerializer implements Serializer {

    private final static XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private final static Map<String, Namespace> namespaces = new HashMap();

    static {
        for (String prefix : new String[]{"ctx", "journal", "book", "patent", "sch_svc", "dissertation"}) {
            namespaces.put(prefix, eventFactory.createNamespace(prefix, FMT_XML_URI_PREFIX + prefix));
        }
    }

    public XMLSerializer(OpenURLConfig config) {
    }

    /**
     * Serialize whole XML document into a writer.
     *
     * @param contexts
     * @param writer
     * @throws OpenURLException
     */
    public void serializeContextObjects(ContextObject[] contexts, Writer writer) throws OpenURLException {
        try {
            XMLEventWriter eventWriter = XMLOutputFactory.newInstance().createXMLEventWriter(writer);
            eventWriter.add(eventFactory.createStartDocument("UTF-8", "1.0"));
            serializeContextObjects(contexts, eventWriter);
            eventWriter.add(eventFactory.createEndDocument());
        } catch (XMLStreamException e) {
            throw new OpenURLException(e);
        }
    }

    public void serializeContextObjectReferent(ContextObject ctx, Writer writer) throws OpenURLException {
        try {
            XMLEventWriter eventWriter = XMLOutputFactory.newInstance().createXMLEventWriter(writer);
            eventWriter.add(eventFactory.createStartDocument("UTF-8", "1.0"));
            if (ctx.getReferent() != null) {
                Collection<Descriptor> descs = ctx.getReferent().getDescriptors();
                for (Descriptor desc : descs) {
                    if (desc instanceof ByValueMetadataKev) {
                        ByValueMetadataKev rft = (ByValueMetadataKev) desc;
                        serializeReferent(eventWriter, rft.getValFmt().toString(), rft.getFieldMap());
                    }
                }
            }
            eventWriter.add(eventFactory.createEndDocument());
        } catch (XMLStreamException e) {
            throw new OpenURLException(e);
        }
    }

    public void serializeContextObjects(ContextObject[] contexts, XMLEventWriter writer) throws XMLStreamException {
        writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", "context-objects",
                null, Collections.singletonList(namespaces.get("ctx")).iterator()));
        for (ContextObject ctx : contexts) {
            writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", "context-object"));
            if (ctx.getIdentifier() != null) {
                writer.add(eventFactory.createAttribute("identifier", ctx.getIdentifier()));
            }
            if (ctx.getVersion() != null) {
                writer.add(eventFactory.createAttribute("version", ctx.getVersion()));
            }
            if (ctx.getTimestamp() != null) {
                writer.add(eventFactory.createAttribute("timestamp", ctx.getTimestamp()));
            }
            if (ctx.getReferent() != null) {
                serializeEntity(writer, "referent", ctx.getReferent());
            }
            if (ctx.getReferringEntities() != null) {
                for (ReferringEntity re : ctx.getReferringEntities()) {
                    serializeEntity(writer, "referring-entity", re);
                }
            }
            if (ctx.getRequesters() != null) {
                for (Requester r : ctx.getRequesters()) {
                    serializeEntity(writer, "requester", r);
                }
            }
            if (ctx.getResolvers() != null) {
                for (Resolver r : ctx.getResolvers()) {
                    serializeEntity(writer, "resolver", r);
                }
            }
            if (ctx.getServiceTypes() != null) {
                for (ServiceType st : ctx.getServiceTypes()) {
                    writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", "service-type"));
                    Iterator<Descriptor> it = st.getDescriptors().iterator();
                    while (it.hasNext()) {
                        serializeDescriptor(writer, it.next());
                    }
                    writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", "service-type"));
                }
            }
            if (ctx.getReferrers() != null) {
                for (Referrer r : ctx.getReferrers()) {
                    serializeEntity(writer, "referrer", r);
                }
            }
            writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", "context-object"));
        }
        writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", "context-objects"));
    }

    protected void serializeEntity(XMLEventWriter writer, String elementname, Entity<Descriptor> entity) throws XMLStreamException {
        Collection<Descriptor> descs = entity.getDescriptors();
        if (descs == null || descs.isEmpty()) {
            return;
        }
        writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", elementname));
        for (Descriptor desc : descs) {
            serializeDescriptor(writer, desc);
        }
        writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", elementname));
    }

    protected void serializeDescriptor(XMLEventWriter writer, Descriptor desc) throws XMLStreamException {

        if (desc instanceof Identifier) {
            writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", "identifier"));
            writer.add(eventFactory.createCharacters(desc.toString()));
            writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", "identifier"));
        } else if (desc instanceof ByValueMetadataKev) {
            ByValueMetadataKev kev = (ByValueMetadataKev) desc;
            writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", "metadata-by-val"));
            Map<String, String[]> map = kev.getFieldMap();
            if (map != null && map.size() > 0) {
                writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", "format"));
                writer.add(eventFactory.createCharacters(kev.getValFmt().toString()));
                writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", "format"));
                writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", "metadata"));
                serializeReferent(writer, kev.getValFmt().toString(), map);
                writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", "metadata"));
            }
            writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", "metadata-by-val"));
        } else if (desc instanceof ByValueMetadataXml) {
            ByValueMetadataXml xml = (ByValueMetadataXml) desc;
        } else if (desc instanceof ByReferenceMetadata) {
            ByReferenceMetadata ref = (ByReferenceMetadata) desc;
            URL location = ref.getRef();
            URI format = ref.getRefFmt();
            writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", "metadata-by-ref"));
            if (location != null && format != null) {
                writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", "metadata"));
                writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", "format"));
                writer.add(eventFactory.createCharacters(format.toString()));
                writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", "format"));
                writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", "location"));
                writer.add(eventFactory.createCharacters(location.toString()));
                writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", "location"));
                writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", "metadata"));
            }
            writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", "metadata-by-ref"));
        } else if (desc instanceof PrivateData) {
            writer.add(eventFactory.createStartElement("ctx", FMT_XML_URI_PREFIX + "ctx", "private-data"));
            writer.add(eventFactory.createCharacters(desc.toString()));
            writer.add(eventFactory.createEndElement("ctx", FMT_XML_URI_PREFIX + "ctx", "private-data"));
        }
    }

    protected void serializeReferent(XMLEventWriter writer, String format, Map<String, String[]> map) throws XMLStreamException {
        int pos = format.lastIndexOf(':');
        String formatName = pos > 0 ? format.substring(pos + 1) : format;
        Attribute schemaLocation = eventFactory.createAttribute("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                "schemaLocation", format + " http://www.openurl.info/registry/docs/" + FMT_XML_URI_PREFIX + formatName);
        writer.add(eventFactory.createStartElement("rft", FMT_XML_URI_PREFIX + formatName, formatName,
                Collections.singletonList(schemaLocation).iterator(),
                Arrays.asList(eventFactory.createNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI),
                        eventFactory.createNamespace("rft", FMT_XML_URI_PREFIX + formatName)).iterator()));
        map = serializeAuthors(writer, "rft", formatName, map);
        for (Map.Entry<String, String[]> me : map.entrySet()) {
            for (String v : me.getValue()) {
                String[] s = me.getKey().split("\\.");
                if (s.length != 2) {
                    s = new String[]{"rft", me.getKey()};
                }
                writer.add(eventFactory.createStartElement(s[0], FMT_XML_URI_PREFIX + formatName, s[1]));
                writer.add(eventFactory.createCharacters(v));
                writer.add(eventFactory.createEndElement(s[0], FMT_XML_URI_PREFIX + formatName, s[1]));
            }
        }
        writer.add(eventFactory.createEndElement("rft", FMT_XML_URI_PREFIX + formatName, formatName));
    }

    protected Map<String, String[]> serializeAuthors(XMLEventWriter writer, String prefix, String formatName, Map<String, String[]> map) throws XMLStreamException {
        LinkedHashMap<String, String[]> authorType = new LinkedHashMap();
        LinkedHashMap<String, String[]> detailedAuthorType = new LinkedHashMap();
        for (String s : authorTypeKeys) {
            if (map.containsKey(s)) {
                authorType.put(s, map.get(s));
            }
            String k = prefix + "." + s;
            if (map.containsKey(k)) {
                authorType.put(s, map.get(k));
            }
        }
        int max = 0;
        for (String s : detailedAuthorTypeKeys) {
            if (map.containsKey(s)) {
                max = map.get(s).length > max ? map.get(s).length : max;
                detailedAuthorType.put(s, map.get(s));
            }
            String k = prefix + "." + s;
            if (map.containsKey(k)) {
                max = map.get(k).length > max ? map.get(k).length : max;
                detailedAuthorType.put(s, map.get(k));
            }
        }
        if (!authorType.isEmpty() || !detailedAuthorType.isEmpty()) {
            writer.add(eventFactory.createStartElement(prefix, FMT_XML_URI_PREFIX + formatName, "authors"));
            for (String s : authorType.keySet()) {
                for (String v : authorType.get(s)) {
                    writer.add(eventFactory.createStartElement(prefix, FMT_XML_URI_PREFIX + formatName, s));
                    writer.add(eventFactory.createCharacters(v));
                    writer.add(eventFactory.createEndElement(prefix, FMT_XML_URI_PREFIX + formatName, s));
                }
            }
            for (int i = 0; i < max; i++) {
                writer.add(eventFactory.createStartElement(prefix, FMT_XML_URI_PREFIX + formatName, "author"));
                for (String s : detailedAuthorType.keySet()) {
                    String v = detailedAuthorType.get(s)[i];
                    writer.add(eventFactory.createStartElement(prefix, FMT_XML_URI_PREFIX + formatName, s));
                    writer.add(eventFactory.createCharacters(v));
                    writer.add(eventFactory.createEndElement(prefix, FMT_XML_URI_PREFIX + formatName, s));
                }
                writer.add(eventFactory.createEndElement(prefix, FMT_XML_URI_PREFIX + formatName, "author"));
            }
            writer.add(eventFactory.createEndElement(prefix, FMT_XML_URI_PREFIX + formatName, "authors"));
        }
        for (String s : authorTypeKeys) {
            map.remove(s);
            map.remove(prefix + "." + s);
        }
        for (String s : detailedAuthorTypeKeys) {
            map.remove(s);
            map.remove(prefix + "." + s);
        }
        return map;
    }
}
