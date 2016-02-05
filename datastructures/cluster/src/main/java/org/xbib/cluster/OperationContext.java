package org.xbib.cluster;

public interface OperationContext<R> {
    void reply(R obj);

    Member getSender();

    int serviceId();
}
