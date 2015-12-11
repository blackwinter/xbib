package org.xbib.cluster.action.addservice;

import org.xbib.cluster.Cluster;
import org.xbib.cluster.service.InternalService;
import org.xbib.cluster.OperationContext;
import org.xbib.cluster.Request;
import org.xbib.cluster.service.ServiceContext;
import org.xbib.cluster.service.Service;
import org.xbib.cluster.service.ServiceConstructor;

public class AddServiceRequest implements Request<InternalService, Boolean> {
    String finalName;
    String name;
    ServiceConstructor constructor;

    public AddServiceRequest(String finalName, String name, ServiceConstructor constructor) {
        this.finalName = finalName;
        this.name = name;
        this.constructor = constructor;
    }

    @Override
    public void run(InternalService service, OperationContext<Boolean> ctx) {
        Cluster cluster = service.getCluster();
        if (cluster.getService(finalName) != null) {
            ctx.reply(false);
        }
        Service s = constructor.newInstance(new ServiceContext(service.getCluster(), cluster.getServices().size(), name));
        // service variable is not thread-safe
        synchronized (cluster.getServices()) {
            cluster.getServices().add(s);
        }
        cluster.serviceNameMap().put(finalName, s);
        ctx.reply(true);
    }
}
