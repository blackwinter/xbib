package org.xbib.openurl.internal.transports;

import org.xbib.openurl.ContextObject;
import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequest;
import org.xbib.openurl.OpenURLRequestProcessor;
import org.xbib.openurl.Service;
import org.xbib.openurl.Transport;
import org.xbib.openurl.config.OpenURLConfig;
import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.descriptors.Identifier;
import org.xbib.openurl.entities.Referent;
import org.xbib.openurl.entities.Referrer;
import org.xbib.openurl.entities.ReferringEntity;
import org.xbib.openurl.entities.Requester;
import org.xbib.openurl.entities.Resolver;
import org.xbib.openurl.entities.ServiceType;
import org.xbib.openurl.internal.descriptors.ByValueMetadataKevImpl;
import org.xbib.openurl.internal.descriptors.IdentifierImpl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Legacy OpenURL transport
 */
public class LegacyOpenURLTransport implements Transport {

    private final static URI TRANSPORT_ID = URI.create("info:ofi/tsp:http:openurl-inline");

    private OpenURLConfig config;

    /**
     * Construct an HTTP OpenURL Inline Transport object
     *
     * @param config
     */
    public LegacyOpenURLTransport(OpenURLConfig config) {
        this.config = config;
    }

    @Override
    public URI getTransportID() {
        return TRANSPORT_ID;
    }

    @Override
    public OpenURLRequest toOpenURLRequest(Map<String, String[]> req, boolean isSecure)
            throws OpenURLException {
        try {
            OpenURLRequestProcessor processor = config.getProcessor();
            // Assume this is a 0.1 request

            List<Descriptor> referentDescriptors = new ArrayList();
            List<Descriptor> requesterDescriptors = new ArrayList();
            List<Descriptor> referringEntityDescriptors = new ArrayList();
            List<Descriptor> referrerDescriptors = new ArrayList();
            List<Descriptor> resolverDescriptors = new ArrayList();
            List<Descriptor> serviceTypeDescriptors = new ArrayList();
            Map<Identifier, Service> services = new HashMap();

            for (Map.Entry<String, String[]> entry : req.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();

                if ("sid".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        referrerDescriptors.add(new IdentifierImpl(URI.create("info:sid/" + values[i])));
                    }
                } else if ("id".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        referentDescriptors.add(new IdentifierImpl(URI.create("info:" + values[i])));
                    }
                } else {
                    ByValueMetadataKevImpl bvm =
                            new ByValueMetadataKevImpl(req);
                    referentDescriptors.add(bvm);
                }
            }

            if (serviceTypeDescriptors.isEmpty()) {
                Identifier serviceID = new IdentifierImpl(URI.create("info:localhost/svc_id/default"));
                serviceTypeDescriptors.add(serviceID);
                Service service = config.getService(serviceID);
                services.put(serviceID, service);
            }

            Referent referent =
                    processor.createReferent(referentDescriptors);
            Requester requester =
                    processor.createRequester(requesterDescriptors);
            ReferringEntity referringEntity =
                    processor.createReferringEntity(referringEntityDescriptors);
            Referrer referrer =
                    processor.createReferrer(referrerDescriptors);
            Resolver resolver =
                    processor.createResolver(resolverDescriptors);
            ServiceType serviceType =
                    processor.createServiceType(services);

            ContextObject contextObject = processor.createContextObject(
                    referent,
                    Collections.singletonList(referringEntity),
                    Collections.singletonList(requester),
                    Collections.singletonList(serviceType),
                    Collections.singletonList(resolver),
                    Collections.singletonList(referrer),
                    null,
                    null,
                    null);
            return processor.createOpenURLRequest(contextObject);
        } catch (Exception e) {
            throw new OpenURLException(e.getMessage(), e);
        }
    }

}
