package org.xbib.openurl.internal.serializers;

import org.xbib.openurl.ContextObject;
import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.Serializer;
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
import org.xbib.openurl.internal.URIUtil;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serialize ContextObjects into an KEV format conforming to
 * ANSI/NISO Z39.88-2004, Part 2 "The KEV Context Object Format".
 */
public class KEVSerializer implements Serializer {

    private final static URIUtil util = new URIUtil();
    private LinkedHashMap<String, String[]> map = new LinkedHashMap();

    @Override
    public void serializeContextObjects(ContextObject[] contexts, Writer writer) throws OpenURLException {
        for (ContextObject ctx : contexts) {
            if (ctx.getIdentifier() != null) {
                map.put("identifier", new String[]{ctx.getIdentifier()});
            }
            if (ctx.getVersion() != null) {
                map.put("version", new String[]{ctx.getVersion()});
            }
            if (ctx.getTimestamp() != null) {
                map.put("timestamp", new String[]{ctx.getTimestamp()});
            }
            if (ctx.getReferent() != null) {
                serializeEntity("rft", ctx.getReferent());
            }
            if (ctx.getReferringEntities() != null) {
                for (ReferringEntity re : ctx.getReferringEntities()) {
                    serializeEntity("rfe", re);
                }
            }
            if (ctx.getRequesters() != null) {
                for (Requester r : ctx.getRequesters()) {
                    serializeEntity("req", r);
                }
            }
            if (ctx.getResolvers() != null) {
                for (Resolver r : ctx.getResolvers()) {
                    serializeEntity("res", r);
                }
            }
            if (ctx.getServiceTypes() != null) {
                for (ServiceType<Identifier> st : ctx.getServiceTypes()) {
                    for (Identifier id : st.getDescriptors()) {
                        serializeDescriptor("svc", id);
                    }
                }
            }
            if (ctx.getReferrers() != null) {
                for (Referrer r : ctx.getReferrers()) {
                    serializeEntity("ref", r);
                }
            }
        }
        try {
            writer.write(util.renderQueryString(map, "UTF-8"));
        } catch (IOException e) {
            throw new OpenURLException(e);
        }
    }

    private void serializeEntity(String prefix, Entity<Descriptor> entity) {
        Collection<Descriptor> descs = entity.getDescriptors();
        if (descs == null || descs.isEmpty()) {
            return;
        }
        for (Descriptor desc : descs) {
            serializeDescriptor(prefix, desc);
        }
    }

    private void serializeDescriptor(String prefix, Descriptor desc) {
        if (desc instanceof Identifier) {
            Identifier id = (Identifier) desc;
            map.put(prefix + "_id", new String[]{id.getURI().toString()});
        } else if (desc instanceof ByValueMetadataKev) {
            ByValueMetadataKev kev = (ByValueMetadataKev) desc;
            Map<String, String[]> fieldMap = kev.getFieldMap();
            if (fieldMap != null && fieldMap.size() > 0) {
                String format = kev.getValFmt().toString();
                int pos = format.lastIndexOf(':');
                String formatName = pos > 0 ? format.substring(pos + 1) : format;
                for (Map.Entry<String, String[]> me : fieldMap.entrySet()) {
                    map.put(me.getKey(), me.getValue());
                }
            }
        } // @todo - KEV serializing XML By-Value-Metadata
        else if (desc instanceof ByValueMetadataXml) {
            ByValueMetadataXml xml = (ByValueMetadataXml) desc;
        } else if (desc instanceof ByReferenceMetadata) {
            ByReferenceMetadata ref = (ByReferenceMetadata) desc;
            URL location = ref.getRef();
            URI format = ref.getRefFmt();
            if (location != null && format != null) {
                map.put(prefix + "_ref_fmt", new String[]{format.toString()});
                map.put(prefix + "_ref", new String[]{location.toString()});
            }
        } else if (desc instanceof PrivateData) {
            PrivateData data = (PrivateData) desc;
            if (data != null) {
                map.put(prefix + "_dat", new String[]{data.toString()});
            }
        }
    }

}