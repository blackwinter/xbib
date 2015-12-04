package org.xbib.cluster;

import org.xbib.cluster.service.Service;

@FunctionalInterface
public interface Request<T extends Service, R> {

    void run(T service, OperationContext<R> ctx);
}
