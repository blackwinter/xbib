package org.xbib.openurl.internal;

import org.w3c.dom.Document;
import org.xbib.openurl.ContextObject;
import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequest;
import org.xbib.openurl.OpenURLRequestProcessor;
import org.xbib.openurl.OpenURLResponse;
import org.xbib.openurl.Service;
import org.xbib.openurl.descriptors.ByReferenceMetadata;
import org.xbib.openurl.descriptors.ByValueMetadataKev;
import org.xbib.openurl.descriptors.ByValueMetadataXml;
import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.descriptors.Identifier;
import org.xbib.openurl.entities.Referent;
import org.xbib.openurl.entities.Referrer;
import org.xbib.openurl.entities.ReferringEntity;
import org.xbib.openurl.entities.Requester;
import org.xbib.openurl.entities.Resolver;
import org.xbib.openurl.entities.ServiceType;
import org.xbib.openurl.internal.descriptors.ByReferenceMetadataImpl;
import org.xbib.openurl.internal.descriptors.ByValueMetadataKevImpl;
import org.xbib.openurl.internal.descriptors.ByValueMetadataXmlImpl;
import org.xbib.openurl.internal.entities.ReferentImpl;
import org.xbib.openurl.internal.entities.ReferrerImpl;
import org.xbib.openurl.internal.entities.ReferringEntityImpl;
import org.xbib.openurl.internal.entities.RequesterImpl;
import org.xbib.openurl.internal.entities.ResolverImpl;
import org.xbib.openurl.internal.entities.ServiceTypeImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * For the sake of simplicity, this resolver assumes that multiple
 * ServiceType Identifiers should be processed in sequence until one of
 * them returns a Response instead of null. For example, the ContextObject
 * could specify two services: 1) The desired service 2) A failover service
 */
public class InternalOpenURLRequestProcessor implements OpenURLRequestProcessor {

    private final static URI PROCESSOR_URI = URI.create("info:openurl/processor/xbib-1.0");
    private OpenURLRequest openURLRequest;

    public URI getProcessorID() {
        return PROCESSOR_URI;
    }

    public OpenURLResponse resolve(OpenURLRequest openURLRequest)
            throws OpenURLException {
        this.openURLRequest = openURLRequest;
        OpenURLResponse response = null;

        // Try each ContentObject until first service responds
        ContextObject[] contextObjects = openURLRequest.getContextObjects();
        for (ContextObject contextObject : contextObjects) {
            List<ServiceType> serviceTypes = contextObject.getServiceTypes();
            for (ServiceType serviceType : serviceTypes) {
                for (Service service : (Iterable<Service>) serviceType.getServices()) {
                    try {
                        response = service.resolve(serviceType, contextObject, openURLRequest, this);
                        if (response != null) {
                            return response;
                        }
                    } catch (Exception e) {
                        throw new OpenURLException(e.getMessage(), e);
                    }
                }
            }
        }
        throw new OpenURLException("resolving not successful for " + openURLRequest);
    }

    /**
     * After resolving, write service output to an output stream.
     *
     * @param out
     * @throws IOException
     */
    public void write(OutputStream out) throws IOException {
        if (openURLRequest == null) {
            throw new IOException("OpenURL request is null");
        }
        ContextObject[] contextObjects = openURLRequest.getContextObjects();
        for (ContextObject contextObject : contextObjects) {
            for (int j = 0; j < contextObject.getServiceTypes().size(); j++) {
                Collection<Service> services = contextObject.getServiceTypes().get(j).getServices();
                for (Service service : services) {
                    boolean written = false;
                    try {
                        service.write(out);
                    } catch (Exception e) {
                        throw new IOException(e.getMessage());
                    }
                    if (written) {
                        return;
                    }
                }
            }
        }
    }

    public ContextObject createContextObject(Referent referent,
                                             List<ReferringEntity> referringEntities,
                                             List<Requester> requesters,
                                             List<ServiceType> serviceTypes,
                                             List<Resolver> resolvers,
                                             List<Referrer> referrers,
                                             String id,
                                             String version,
                                             String timestamp) {
        return new InternalContextObject(
                referent,
                referringEntities,
                requesters,
                serviceTypes,
                resolvers,
                referrers,
                id,
                version,
                timestamp);
    }

    public Referent createReferent(List<Descriptor> descriptors) {
        return new ReferentImpl(descriptors);
    }

    public ServiceType createServiceType(Map<Identifier, Service> serviceMap) {
        return new ServiceTypeImpl(serviceMap);
    }

    public Requester createRequester(List<Descriptor> descriptors) {
        return new RequesterImpl(descriptors);
    }

    public ReferringEntity createReferringEntity(List<Descriptor> descriptors) {
        return new ReferringEntityImpl(descriptors);
    }

    public Referrer createReferrer(List<Descriptor> descriptors) {
        return new ReferrerImpl(descriptors);
    }

    public Resolver createResolver(List<Descriptor> descriptors) {
        return new ResolverImpl(descriptors);
    }

    public ByReferenceMetadata createByReferenceMetadata(URI ref_fmt, URL ref) {
        return new ByReferenceMetadataImpl(ref_fmt, ref);
    }

    public ByValueMetadataKev createByValueMetadataKEV(URI val_fmt, String prefix, Map map) {
        return new ByValueMetadataKevImpl(val_fmt, prefix, map);
    }

    public ByValueMetadataXml createByValueMetadataXML(URI val_fmt, Document document) {
        return new ByValueMetadataXmlImpl(val_fmt, document);
    }

    public OpenURLRequest createOpenURLRequest(ContextObject contextObject) {
        return new InternalOpenURLRequest(new ContextObject[]{contextObject});
    }
}
