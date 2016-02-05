package org.xbib.cluster.service.crdt.set;

import org.xbib.cluster.service.ServiceContext;
import org.xbib.cluster.service.DistributedObjectServiceAdapter;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class GSetService<T> extends DistributedObjectServiceAdapter<GSetService<T>, Set<T>> {

    public GSetService(ServiceContext clusterContext, Supplier<Set<T>> value, int replicationFactor) {
        super(clusterContext, value.get(), replicationFactor);
    }

    public static <T> Set<T> merge(Set<T> val0, Set<T> val1) {
        if (val0.size() > val1.size()) {
            val0.addAll(val1);
            return val0;

        } else {
            val1.addAll(val0);
            return val1;
        }
    }

    public void add(T entry) {
        sendToReplicas((service, ctx) -> service.value.add(entry));
    }

    public int size() {
        return askReplicas((service, ctx) -> service.value.size(), Integer.class)
                .mapToInt(CompletableFuture::join).max().getAsInt();
    }

    public boolean contains(T entry) {
        return askReplicas((service, ctx) -> service.value.contains(entry), Boolean.class)
                .anyMatch(CompletableFuture::join);
    }

    @Override
    protected boolean mergeIn(Set<T> val) {
        int size = value.size();
        value.addAll(val);
        return value.size() == size;
    }
}
