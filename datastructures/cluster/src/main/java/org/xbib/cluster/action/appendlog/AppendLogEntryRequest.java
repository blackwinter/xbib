package org.xbib.cluster.action.appendlog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.Cluster;
import org.xbib.cluster.Member;
import org.xbib.cluster.OperationContext;
import org.xbib.cluster.Request;
import org.xbib.cluster.service.InternalService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class AppendLogEntryRequest<R> implements Request<InternalService, Boolean> {
    private final static Logger logger = LogManager.getLogger(AppendLogEntryRequest.class);

    private final Request request;
    private final int serviceId;

    public AppendLogEntryRequest(Request request, int serviceId) {
        this.request = request;
        this.serviceId = serviceId;
    }

    @Override
    public void run(InternalService service, OperationContext<Boolean> ctx) {
//        synchronized (service.cluster) {
        Set<Member> counter = new HashSet<>();
        Set<Member> members = service.getCluster().getMembers();
        AtomicInteger size = new AtomicInteger(members.size());
        long index = service.getCluster().getLastCommitIndex().get();
        ArrayList<CompletableFuture> reqs = new ArrayList<>(members.size());
        for (Member member : members) {
            CompletableFuture<R> f = new CompletableFuture<>();
            service.getCluster().tryAskUntilDoneInternal(member, new UncommittedLogRequest(index, request), 5, 0, f);
            reqs.add(f.whenComplete((result, ex) -> {
                int lastSize;
                if (ex != null) {
                    service.getCluster().removeMemberAsMaster(member, true);
                    lastSize = size.decrementAndGet();
                } else {
                    counter.add(member);
                    lastSize = counter.size();
                }

                if (lastSize == members.size()) {
                    commit(service.getCluster(), index);
                    ctx.reply(true);
                }
            }));
        }
        reqs.forEach(f -> f.join());

//        }
    }

    public void commit(Cluster cluster, long index) {
        for (Member member : cluster.getMembers()) {
            CompletableFuture<Object> f0 = new CompletableFuture<>();
            cluster.tryAskUntilDoneInternal(member, new CommitLogRequest(index, serviceId), 5, 0, f0);
            f0.whenComplete((result0, ex0) -> {
                if (ex0 != null && ex0 instanceof TimeoutException) {
                    cluster.removeMemberAsMaster(member, true);
                }
            });
        }
    }

    public static class UncommittedLogRequest implements Request<InternalService, Boolean> {
        long index;
        Request request;

        public UncommittedLogRequest(long index, Request request) {
            this.index = index;
            this.request = request;
        }

        @Override
        public void run(InternalService service, OperationContext<Boolean> ctx) {
            service.getCluster().pendingConsensusMessages().put(index, request);
            ctx.reply(true);
        }
    }

    public static class CommitLogRequest implements Request<InternalService, Boolean> {
        private final int serviceId;
        long index;

        public CommitLogRequest(long index, int serviceId) {
            this.index = index;
            this.serviceId = serviceId;
        }

        @Override
        public void run(InternalService service, OperationContext<Boolean> ctx) {
            Cluster cluster = service.getCluster();
            cluster.pendingConsensusMessages().get(index).run(cluster.getServices().get(serviceId), ctx);
        }
    }
}
