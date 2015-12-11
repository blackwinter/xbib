package org.xbib.cluster.network.multicast;

import org.xbib.cluster.Member;
import org.xbib.cluster.OperationContext;

public class MulticastOperationContext implements OperationContext {
    private final int serviceId;
    Member sender;

    public MulticastOperationContext(Member sender, int serviceId) {
        this.sender = sender;
        this.serviceId = serviceId;
    }

    @Override
    public void reply(Object obj) {
        throw new IllegalAccessError("reply is not supported for multicast operations");
    }

    @Override
    public Member getSender() {
        return sender;
    }

    @Override
    public int serviceId() {
        return serviceId;
    }
}
