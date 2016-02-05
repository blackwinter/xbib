
package org.xbib.cluster.service;

import org.xbib.cluster.Cluster;

public class InternalService extends Service {
    private final Cluster cluster;
    private final ServiceContext<InternalService> ctx;

    public InternalService(ServiceContext<InternalService> ctx, Cluster cluster) {
        this.ctx = ctx;
        this.cluster = cluster;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public ServiceContext<InternalService> getContext() {
        return ctx;
    }

    @Override
    public void onClose() {

    }
}
