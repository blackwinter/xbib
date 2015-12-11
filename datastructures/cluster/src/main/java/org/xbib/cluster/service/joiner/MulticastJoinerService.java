package org.xbib.cluster.service.joiner;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.Cluster;
import org.xbib.cluster.operation.clustercheckandmerge.ClusterCheckAndMergeOperation;
import org.xbib.cluster.ClusterMembership;
import org.xbib.cluster.network.multicast.MulticastServerHandler;
import org.xbib.cluster.Member;
import org.xbib.cluster.MemberState;
import org.xbib.cluster.service.ServiceContext;

import java.net.InetSocketAddress;
import java.util.Set;

public class MulticastJoinerService implements JoinerService {
    private final static Logger logger = LogManager.getLogger(Cluster.class);

    private final MulticastServerHandler multicastServer;
    private final Cluster cluster;
    private final Member localMember;
    //final private Map<Member, Long> heartbeatMap = new ConcurrentHashMap<>();
    //NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);
    //private AtomicInteger currentTerm;
    //private long lastContactedTimeMaster;
    private Member master;
    //private ScheduledFuture<?> heartbeatTask;
    //private ConcurrentMap<InetSocketAddress, Integer> pendingUserVotes = CacheBuilder.newBuilder().expireAfterWrite(100, TimeUnit.SECONDS).<InetSocketAddress, Integer>build().asMap();
    private MemberState memberState;

    //private Map<Long, Request> pendingConsensusMessages = new ConcurrentHashMap<>();
    //private AtomicLong lastCommitIndex = new AtomicLong();

    public MulticastJoinerService(ServiceContext ctx) {
        cluster = ctx.getCluster();
        localMember = cluster.getLocalMember();
        InetSocketAddress multicastAddress = new InetSocketAddress("224.0.67.67", 5001);
        try {
            multicastServer = new MulticastServerHandler(ctx.getCluster(), multicastAddress)
                    .start();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Failed to bind UDP " + multicastAddress);
        }
        logger.info("{} started, listening UDP multicast server {}", localMember, multicastAddress);
        multicastServer.setAutoRead(true);
    }

    private synchronized void changeMaster(Member masterMember) {
        master = masterMember;
        memberState = masterMember.equals(localMember) ? MemberState.MASTER : MemberState.FOLLOWER;
        multicastServer.setJoinGroup(memberState == MemberState.MASTER);
    }

    public MemberState memberState() {
        return memberState;
    }

    public synchronized void removeMemberAsMaster(Member member, boolean replicate) {
//        if (!isMaster())
//            throw new IllegalStateException();

//        heartbeatMap.remove(member);
//        members.remove(member);
//        if(replicate) {

//        internalBus.sendAllMembers((cluster, ctx) -> {
//            cluster.clusterConnection.remove(member);
//            Cluster.LOGGER.info("Member removed {}", member);
//            cluster.membershipListeners.forEach(l -> Throwables.propagate(() -> l.memberRemoved(member)));
//        }, true);
//        }
    }


//        workerGroup.scheduleWithFixedDelay(() -> {
//            ClusterCheckAndMergeOperation req = new ClusterCheckAndMergeOperation();
//            multicastServer.sendMulticast(req);
//        }, 0, 2000, TimeUnit.MILLISECONDS);


    public void joinCluster() {
        multicastServer.sendMulticast(new ClusterCheckAndMergeOperation());
    }

    protected synchronized void changeCluster(Set<Member> newClusterMembers, Member masterMember, boolean isNew) {
//        try {
//            pause();
//            clusterConnection.clear();
//            master = masterMember;
//            members = newClusterMembers;
//            messageHandlers.cleanUp();
//            LOGGER.info("Joined a cluster of {} nodes.", members.size());
//            multicastServer.setJoinGroup(masterMember.equals(localMember));
//            if (!isNew)
//                membershipListeners.forEach(x -> eventLoop.execute(() -> x.clusterChanged()));
//        } finally {
//            resume();
//        }
    }

    @Override
    public void onClose() {
        //heartbeatTask.cancel(true);
        try {
            multicastServer.close();
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void onStart(ClusterMembership membership) {

    }
}
