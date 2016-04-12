package org.xbib.openurl;

import org.xbib.openurl.entities.Referent;
import org.xbib.openurl.entities.Referrer;
import org.xbib.openurl.entities.ReferringEntity;
import org.xbib.openurl.entities.Requester;
import org.xbib.openurl.entities.Resolver;
import org.xbib.openurl.entities.ServiceType;

import java.util.List;

/**
 * The ContextObject data structure captures relevant information
 * for the delivery of context-sensitive services pertaining to a
 * referenced resource.
 *
 * A ContextObject is a data structure that binds together descriptions of:
 * <ul>
 * <li>A Referent: A resource that is referenced on a network and about
 * which the ContextObject is created </li>
 * <li>A ReferringEntity: The resource that references the Referent </li>
 * <li>A Requester: The resource that requests services pertaining to
 * the Referent </li>
 * <li>A ServiceType: The resource that defines the type of service
 * (pertaining to the Referent) that is requested </li>
 * <li>A Resolver: The resource at which a service request pertaining
 * to the Referent is targeted </li>
 * <li>A Referrer: The resource that generates the ContextObject </li>
 * </ul>
 *
 * The ContextObject is created to enable the delivery of services
 * pertaining to the Referent, which is at the core of the ContextObject.
 * The descriptions of the ReferringEntity, the Requester, the
 * ServiceType, the Resolver, and the Referrer express the Context in
 * which the Referent is referenced and in which the request for services
 * pertaining to the Referent takes place.
 */
public interface ContextObject {

    /**
     * Context Object version
     */
    String VERSION = "Z39.88-2004";

    /**
     * Get the Context Object identifier. Note that every Context Object
     * must contain one and only one optional identifier.
     *
     * @return the identifier for this Context Object or null
     * if this ContextObject does not have an identifier
     */
    String getIdentifier();

    /**
     * Get the Context Object version. Note that every ContextObject
     * must contain one and only one optional version.
     *
     * @return the version for this ContextObject.
     * It is the fixed value 'Z39.88-2004' or null if
     * there is no version for this ContextObject.
     */
    String getVersion();

    /**
     * Get the UTC timestamp of the creation date of this Context Object.
     * Note that every ContextObject must contain one and only one
     * optional UTC timestamp.
     *
     * @return the UTC timestamp for this Context Object or null if
     * there is no UTC timestamp for this Context Object.
     */
    String getTimestamp();

    /**
     * Get the Referent. Note that every ContextObject
     * must contain one and only one Referent.
     *
     * @return the referent for this ContextObject
     */
    Referent getReferent();

    /**
     * Get a sequence of ReferringEntities.
     *
     * @return the ReferringEntities contained in this ContextObject
     */
    List<ReferringEntity> getReferringEntities();

    /**
     * Get a sequence of Requesters.
     *
     * @return the Requesters contained in this ContextObject
     */
    List<Requester> getRequesters();

    /**
     * Get a sequence of ServiceTypes.
     *
     * @return the ServiceTypes contained in this ContextObject
     */
    List<ServiceType> getServiceTypes();

    /**
     * Get a sequence of Resolvers.
     *
     * @return the Resolvers contained in this ContextObject
     */
    List<Resolver> getResolvers();

    /**
     * Get a sequence of Referrers.
     *
     * @return the Referrers contained in this ContextObject
     */
    List<Referrer> getReferrers();
}
