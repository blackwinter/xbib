package org.xbib.openurl.entities;

import org.xbib.openurl.Service;
import org.xbib.openurl.descriptors.Identifier;

import java.util.Collection;

/**
 * ServiceType is the entity that defines the service type
 * you want (full text, ILL, etc.)
 */
public interface ServiceType<I extends Identifier> extends Entity<I> {

    /**
     * Add a service to this service type.
     *
     * @param serviceID the ID for this service
     * @param service
     */
    void addService(I serviceID, Service service);

    /**
     * Get service for a service ID
     *
     * @return service for ID or null if service does not exist
     */
    Service getService(I serviceID);

    /**
     * Get services.
     *
     * @return all services
     */
    Collection<Service> getServices();
}
