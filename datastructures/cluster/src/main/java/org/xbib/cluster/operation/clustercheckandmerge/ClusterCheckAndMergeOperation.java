package org.xbib.cluster.operation.clustercheckandmerge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.Cluster;
import org.xbib.cluster.Member;
import org.xbib.cluster.MemberChannel;
import org.xbib.cluster.OperationContext;
import org.xbib.cluster.Request;
import org.xbib.cluster.operation.Operation;
import org.xbib.cluster.service.InternalService;
import org.xbib.cluster.util.FutureUtil;
import org.xbib.cluster.util.Tuple;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.google.common.base.Preconditions.checkState;

public class ClusterCheckAndMergeOperation implements Operation<InternalService> {
    final static Logger logger = LogManager.getLogger(ClusterCheckAndMergeOperation.class);

    @Override
    public void run(InternalService service, OperationContext<Void> ctx) {
        Cluster cluster = service.getCluster();

        Set<Member> clusterMembers = cluster.getMembers();
        Member masterNode = cluster.getMaster();
        checkState(masterNode.equals(cluster.getLocalMember()), "only master node must execute ClusterCheckAndMergeOperation.");

        Member sender = ctx.getSender();
        if (clusterMembers.contains(sender)) {
            return;
        }

        logger.trace("got cluster check and merge request from a server who is not in this cluster");

        MemberChannel channel;
        try {
            channel = cluster.getTransport().connect(ctx.getSender());
        } catch (InterruptedException e) {
            logger.trace("a server send me join request from udp but i can't connect him. ignoring");
            return;
        }
        logger.trace("connected to the new node, now getting cluster information");
        cluster.clusterConnection().put(ctx.getSender(), channel);
        final Tuple<Set<Member>, Long> clusterStatus;
        try {
            clusterStatus = cluster.getServiceContext()
                    .tryAskUntilDone(ctx.getSender(), new GetInformationFromDiscoveredCluster(), 5, Tuple.class)
                    .join();
            logger.trace("got answer from discovered cluster: " + clusterStatus.a().size()
                    + "members, last commit index: " + clusterStatus.b());
        } catch (CompletionException e) {
            cluster.clusterConnection().remove(ctx.getSender());
            return;
        }
        Set<Member> otherMembers = new HashSet<>(clusterStatus.a());
        Set<Member> members = cluster.getMembers();
        otherMembers.removeAll(members);

        int otherSize = otherMembers.size();
        int mySize = cluster.getMembers().size();

        long myCommitIndex = cluster.getLastCommitIndex().get();
        Long otherCommitIndex = clusterStatus.b();

        if (otherSize > mySize || (otherSize == mySize && myCommitIndex < otherCommitIndex)) {
            logger.trace("they will eventually add me");
            return;
        }
        logger.trace("they must join me, my cluster is bigger than theirs");
        for (Member otherMember : otherMembers) {
            MemberChannel memberChannel;
            try {
                memberChannel = cluster.getTransport().connect(otherMember);
            } catch (InterruptedException e) {
                otherMembers.remove(otherMember);
                continue;
            }
            cluster.clusterConnection().put(otherMember, memberChannel);
        }
        FutureUtil.MultipleFutureListener f = new FutureUtil.MultipleFutureListener(otherMembers.size());
        logger.trace("asking new members to join our party.");
        for (Member otherMember : otherMembers) {
            CompletableFuture<Void> ask = cluster.getServiceContext()
                    .tryAskUntilDone(otherMember, new JoinThisClusterRequest(cluster.getMembers(), masterNode), 5);
            ask.whenComplete((result, ex) -> {
                if (ex != null) {
                    otherMembers.remove(otherMember);
                    cluster.clusterConnection().remove(otherMember);
                    logger.trace(otherMember + " was a member of the new cluster but since I (master) couldn't connect it," +
                            " it will be ignored.");
                } else {
                    logger.trace(otherMember + " successfully connected to master");
                }
                f.increment();
            });
        }
        logger.trace("waiting responses from new members");
        f.get().join();
        logger.trace(otherMembers.size() + " members is added to the cluster");
        logger.trace("replicating information about new members to cluster members");
        cluster.getServiceContext().replicateSafely(new MembersJoinedRequest(otherMembers)).join();
        logger.trace("all members in other cluster successfully added into this cluster");
    }

    public static class JoinThisClusterRequest implements Request<InternalService, Void> {
        final Set<Member> members;
        final Member masterNode;

        public JoinThisClusterRequest(Set<Member> members, Member masterNode) {
            this.members = members;
            this.masterNode = masterNode;
        }

        @Override
        public void run(InternalService service, OperationContext<Void> ctx) {
            logger.trace("someone wants me in his cluster, i will join her party.");
//            service.cluster.changeCluster(members, masterNode, false);
            ctx.reply(null);
        }
    }

    public static class MembersJoinedRequest implements Request<InternalService, Boolean> {
        final Set<Member> members;

        public MembersJoinedRequest(Set<Member> members) {
            this.members = members;
        }

        @Override
        public void run(InternalService service, OperationContext<Boolean> ctx) {
            logger.trace("there are new members in the cluster. welcoming them.");
//            if(members.size() > 1)
//                service.cluster.getTransport().aad(members);
//            else
//            if(members.size() == 1)
//                service.cluster.addMemberInternal(members.iterator().next());

            ctx.reply(null);
        }
    }

    public static class GetInformationFromDiscoveredCluster implements Request<InternalService, Tuple> {

        @Override
        public void run(InternalService service, OperationContext<Tuple> ctx) {
            Cluster cluster1 = service.getCluster();
            Tuple<Set, Long> obj = new Tuple<>(cluster1.getMembers(), cluster1.getLastCommitIndex().get());
            logger.trace("i was asked my cluster status. here it is: " +
                    obj.a().size() + "members, last commit index: " + obj.b());
            ctx.reply(obj);
        }
    }

}
