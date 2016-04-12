package org.xbib.openurl.internal.parsers;

import org.xbib.openurl.ContextObject;
import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequestProcessor;
import org.xbib.openurl.Service;
import org.xbib.openurl.config.OpenURLConfig;
import org.xbib.openurl.descriptors.ByReferenceMetadata;
import org.xbib.openurl.descriptors.ByValueMetadataKev;
import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.descriptors.Identifier;
import org.xbib.openurl.entities.Referent;
import org.xbib.openurl.entities.Referrer;
import org.xbib.openurl.entities.ReferringEntity;
import org.xbib.openurl.entities.Requester;
import org.xbib.openurl.entities.Resolver;
import org.xbib.openurl.entities.ServiceType;
import org.xbib.openurl.internal.URIUtil;
import org.xbib.openurl.internal.descriptors.ByReferenceMetadataImpl;
import org.xbib.openurl.internal.descriptors.ByValueMetadataKevImpl;
import org.xbib.openurl.internal.descriptors.IdentifierImpl;
import org.xbib.openurl.internal.descriptors.PrivateDataImpl;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KEVParser {

    private OpenURLConfig config;

    /**
     * Construct KEV parser
     *
     * @param config
     */
    public KEVParser(OpenURLConfig config) {
        this.config = config;
    }

    public ContextObject createContextObject(URI format, String value) throws OpenURLException {
        try {
            Map<String, String[]> m = URIUtil.parseQueryString(value);
            return parseContextObject(m);
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
    }

    public ContextObject parseContextObject(Map<String, String[]> req) throws OpenURLException {
        try {
            if (req == null) {
                return null;
            }
            OpenURLRequestProcessor processor = config.getProcessor();
            List<Descriptor> referentDescriptors = new ArrayList<>();
            List<Descriptor> requesterDescriptors = new ArrayList<>();
            List<Descriptor> referringEntityDescriptors = new ArrayList<>();
            List<Descriptor> referrerDescriptors = new ArrayList<>();
            List<Descriptor> resolverDescriptors = new ArrayList<>();
            List<Descriptor> serviceTypeDescriptors = new ArrayList<>();
            Map<Identifier, Service> serviceMap = new HashMap<>();
            String identifier = null;
            String version = null;
            String timestamp = null;
            for (Map.Entry<String, String[]> entry : req.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                if ("rft_id".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        referentDescriptors.add(new IdentifierImpl(URI.create(values[i])));
                    }
                } else if ("rft_dat".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        referentDescriptors.add(new PrivateDataImpl(values[i]));
                    }
                } else if ("rft_val_fmt".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        ByValueMetadataKevImpl bvm =
                                new ByValueMetadataKevImpl(URI.create(values[i]),
                                        "rft", req);
                        referentDescriptors.add(bvm);
                    }
                } else if ("rft_ref_fmt".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        String[] rft_refs = req.get("rft_ref");
                        for (int j = 0; j < rft_refs.length; j++) {
                            ByReferenceMetadata brm =
                                    new ByReferenceMetadataImpl(URI.create(values[i]), new URL(rft_refs[j]));
                            referentDescriptors.add(brm);
                        }
                    }
                } else if ("req_id".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        requesterDescriptors.add(new IdentifierImpl(URI.create(values[i])));
                    }
                } else if ("req_dat".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        requesterDescriptors.add(new PrivateDataImpl(values[i]));
                    }
                } else if ("req_val_fmt".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        ByValueMetadataKev bvm =
                                new ByValueMetadataKevImpl(URI.create(values[i]), "req", req);
                        requesterDescriptors.add(bvm);
                    }
                } else if ("req_ref_fmt".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        String[] req_refs = req.get("req_ref");
                        for (int j = 0; j < req_refs.length; j++) {
                            ByReferenceMetadata brm =
                                    new ByReferenceMetadataImpl(URI.create(values[i]), new URL(req_refs[j]));
                            requesterDescriptors.add(brm);
                        }
                    }
                } else if ("rfe_id".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        referringEntityDescriptors.add(new IdentifierImpl(URI.create(values[i])));
                    }
                } else if ("rfe_dat".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        referringEntityDescriptors.add(new PrivateDataImpl(values[i]));
                    }
                } else if ("rfe_val_fmt".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        ByValueMetadataKev bvm =
                                new ByValueMetadataKevImpl(URI.create(values[i]), "rfe", req);
                        referringEntityDescriptors.add(bvm);
                    }
                } else if ("rfe_ref_fmt".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        String[] rfe_refs = req.get("rfe_ref");
                        for (int j = 0; j < rfe_refs.length; j++) {
                            ByReferenceMetadata brm =
                                    new ByReferenceMetadataImpl(URI.create(values[i]), new URL(rfe_refs[j]));
                            referringEntityDescriptors.add(brm);
                        }
                    }
                } else if ("rfr_id".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        referrerDescriptors.add(new IdentifierImpl(URI.create(values[i])));
                    }
                } else if ("rfr_dat".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        referrerDescriptors.add(new PrivateDataImpl(values[i]));
                    }
                } else if ("rfr_val_fmt".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        ByValueMetadataKev bvm =
                                new ByValueMetadataKevImpl(URI.create(values[i]), "rfr", req);
                        referrerDescriptors.add(bvm);
                    }
                } else if ("rfr_ref_fmt".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        String[] rfr_refs = req.get("rfr_ref");
                        for (int j = 0; j < rfr_refs.length; ++j) {
                            ByReferenceMetadata brm =
                                    new ByReferenceMetadataImpl(URI.create(values[i]), new URL(rfr_refs[j]));
                            referrerDescriptors.add(brm);
                        }
                    }
                } else if ("res_id".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        resolverDescriptors.add(new IdentifierImpl(URI.create(values[i])));
                    }
                } else if ("res_dat".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        resolverDescriptors.add(new PrivateDataImpl(values[i]));
                    }
                } else if ("res_val_fmt".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        ByValueMetadataKev bvm =
                                new ByValueMetadataKevImpl(URI.create(values[i]), "res", req);
                        resolverDescriptors.add(bvm);
                    }
                } else if ("res_ref_fmt".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        String[] res_refs = req.get("res_ref");
                        for (int j = 0; j < res_refs.length; ++j) {
                            ByReferenceMetadata brm =
                                    new ByReferenceMetadataImpl(URI.create(values[i]), new URL(res_refs[j]));
                            resolverDescriptors.add(brm);
                        }
                    }
                } else if ("svc_id".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        Identifier serviceID = new IdentifierImpl(URI.create(values[i]));
                        serviceTypeDescriptors.add(serviceID);
                        try {
                            Service service = config.getService(serviceID);
                            if (service != null) {
                                serviceMap.put(serviceID, service);
                            }
                        } catch (Exception e) {
                            throw new OpenURLException("no service implementation found for ID " + serviceID, e);
                        }
                    }
                } else if ("svc_dat".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        serviceTypeDescriptors.add(new PrivateDataImpl(values[i]));
                    }
                } else if ("svc_val_fmt".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        ByValueMetadataKev bvm =
                                new ByValueMetadataKevImpl(URI.create(values[i]), "svc", req);
                        serviceTypeDescriptors.add(bvm);
                    }
                } else if ("svc_ref_fmt".equals(key)) {
                    for (int i = 0; i < values.length; i++) {
                        String[] svc_refs = req.get("svc_ref");
                        for (int j = 0; j < svc_refs.length; j++) {
                            ByReferenceMetadata brm =
                                    new ByReferenceMetadataImpl(URI.create(values[i]), new URL(svc_refs[j]));
                            serviceTypeDescriptors.add(brm);
                        }
                    }
                }
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
                    processor.createServiceType(serviceMap);
            return processor.createContextObject(
                    referent,
                    Collections.singletonList(referringEntity),
                    Collections.singletonList(requester),
                    Collections.singletonList(serviceType),
                    Collections.singletonList(resolver),
                    Collections.singletonList(referrer),
                    identifier,
                    version,
                    timestamp);
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
    }
}
