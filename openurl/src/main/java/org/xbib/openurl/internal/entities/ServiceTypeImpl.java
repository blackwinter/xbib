package org.xbib.openurl.internal.entities;

import org.xbib.openurl.Service;
import org.xbib.openurl.descriptors.Identifier;
import org.xbib.openurl.entities.ServiceType;

import java.util.Collection;
import java.util.Map;

/**
 * The resource that defines the type of service pertaining
 * to the Referent that is requested; an Entity
 * of the <em>ContextObject</em>.
 */
public class ServiceTypeImpl<I extends Identifier> extends EntityImpl<I> implements ServiceType<I> {

    private Map<I, Service> services;

    /**
     * Construct a ServiceType.
     *
     * @param services a map of services for given identifiers
     */
    public ServiceTypeImpl(Map<I, Service> services) {
        super(services.keySet());
        this.services = services;
    }

    @Override
    public void addService(I serviceID, Service service) {
        services.put(serviceID, service);
    }

    @Override
    public Service getService(I serviceID) {
        return services.get(serviceID);
    }

    @Override
    public Collection<Service> getServices() {
        return services.values();
    }
}
