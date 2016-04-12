package org.xbib.openurl.internal;

import org.xbib.openurl.ContextObject;
import org.xbib.openurl.entities.Referent;
import org.xbib.openurl.entities.Referrer;
import org.xbib.openurl.entities.ReferringEntity;
import org.xbib.openurl.entities.Requester;
import org.xbib.openurl.entities.Resolver;
import org.xbib.openurl.entities.ServiceType;

import java.util.List;

/**
 * Implementation of a ContextObject
 */
public class InternalContextObject implements ContextObject {

    private Referent referent;
    private List<ReferringEntity> referringEntities;
    private List<Requester> requesters;
    private List<ServiceType> serviceTypes;
    private List<Resolver> resolvers;
    private List<Referrer> referrers;
    private String identifier;
    private String version;
    private String timestamp;

    public InternalContextObject(Referent referent,
                                 List<ReferringEntity> referringEntities,
                                 List<Requester> requesters,
                                 List<ServiceType> serviceTypes,
                                 List<Resolver> resolvers,
                                 List<Referrer> referrers,
                                 String identifier,
                                 String version,
                                 String timestamp) {
        this.referent = referent;
        this.referringEntities = referringEntities;
        this.requesters = requesters;
        this.serviceTypes = serviceTypes;
        this.resolvers = resolvers;
        this.referrers = referrers;
        this.identifier = identifier;
        this.version = version;
        this.timestamp = timestamp;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getVersion() {
        return version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Referent getReferent() {
        return referent;
    }

    public List<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public List<ReferringEntity> getReferringEntities() {
        return referringEntities;
    }

    public List<Requester> getRequesters() {
        return requesters;
    }

    public List<Resolver> getResolvers() {
        return resolvers;
    }

    public List<Referrer> getReferrers() {
        return referrers;
    }
}
