package org.xbib.cluster;

import java.util.concurrent.CompletableFuture;

public class LocalOperationContext<R> implements OperationContext<R> {
    private final CompletableFuture<R> callback;
    private final Member member;
    private final int serviceId;

    public LocalOperationContext(CompletableFuture<R> callback, int serviceId, Member localMember) {
        this.callback = callback;
        this.serviceId = serviceId;
        this.member = localMember;
    }

    @Override
    public void reply(R obj) {
        callback.complete(obj);
    }

    @Override
    public Member getSender() {
        return member;
    }

    @Override
    public int serviceId() {
        return serviceId;
    }
}
