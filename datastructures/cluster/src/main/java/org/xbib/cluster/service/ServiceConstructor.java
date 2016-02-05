package org.xbib.cluster.service;

@FunctionalInterface
public interface ServiceConstructor<T extends Service> {
    T newInstance(ServiceContext bus);
}
