package org.xbib.openurl.internal;

import org.xbib.openurl.Patent;
import org.xbib.openurl.entities.Referent;
import org.xbib.openurl.entities.Referrer;
import org.xbib.openurl.entities.ReferringEntity;
import org.xbib.openurl.entities.Requester;
import org.xbib.openurl.entities.Resolver;
import org.xbib.openurl.entities.ServiceType;

import java.util.List;

/**
 * Implementation of a Patent
 */
public class InternalPatent extends InternalContextObject implements Patent {

    public InternalPatent(Referent referent,
                          List<ReferringEntity> referringEntities,
                          List<Requester> requesters,
                          List<ServiceType> serviceTypes,
                          List<Resolver> resolvers,
                          List<Referrer> referrers,
                          String identifier,
                          String version,
                          String timestamp) {
        super(referent, referringEntities, requesters, serviceTypes, resolvers,
                referrers, identifier, version, timestamp);
    }
}
