package org.xbib.cluster;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.action.addservice.AddServiceRequest;
import org.xbib.cluster.action.appendlog.AppendLogEntryRequest;
import org.xbib.cluster.operation.heartbeat.HeartbeatOperation;
import org.xbib.cluster.service.InternalService;
import org.xbib.cluster.service.Service;
import org.xbib.cluster.service.ServiceConstructor;
import org.xbib.cluster.service.ServiceContext;
import org.xbib.cluster.service.ServiceListBuilder;
import org.xbib.cluster.network.Packet;
import org.xbib.cluster.service.joiner.JoinerService;
import org.xbib.cluster.transport.Transport;
import org.xbib.cluster.transport.TransportConstructor;
import org.xbib.cluster.transport.ThrowableNioEventLoopGroup;
import org.xbib.cluster.util.Tuple;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


public class Cluster {
    private final static Logger logger = LogManager.getLogger(Cluster.class);
    private final ThrowableNioEventLoopGroup eventLoop = new ThrowableNioEventLoopGroup("event-executor", (t1, e) ->
            logger.error("error while executing operation", e));
    private final ThrowableNioEventLoopGroup requestExecutor = new ThrowableNioEventLoopGroup("request-executor", (t1, e) ->
            logger.error("error while executing request", e));
    final protected List<Service> services;
    private final ServiceContext<InternalService> internalBus;
    final private Map<String, Service> serviceNameMap;
    private final ConcurrentHashMap<Member, MemberChannel> clusterConnection = new ConcurrentHashMap<>();
    final private AtomicInteger messageSequence = new AtomicInteger();
    final private Member localMember;
    private final JoinerService joinerService;
    private final Transport transport;
    final private List<MembershipListener> membershipListeners = Collections.synchronizedList(new ArrayList<>());
    final private Map<Member, Long> heartbeatMap = new ConcurrentHashMap<>();
    final private long clusterStartTime;
    private long lastContactedTimeMaster;
    private Member master;
    private Set<Member> members;
    private AtomicInteger currentTerm = new AtomicInteger();
    private ScheduledFuture<?> heartbeatTask;
    //private ConcurrentMap<InetSocketAddress, Integer> pendingUserVotes = CacheBuilder.newBuilder().expireAfterWrite(100, TimeUnit.SECONDS).<InetSocketAddress, Integer>build().asMap();
    private MemberState memberState;
    private Map<Long, Request> pendingConsensusMessages = new ConcurrentHashMap<>();
    private AtomicLong lastCommitIndex = new AtomicLong();

    public Cluster(Collection<Member> members, ImmutableList<ServiceListBuilder.Constructor> services, TransportConstructor transportConstructor, InetSocketAddress serverAddress, JoinerService joinerService, boolean mustJoinCluster, boolean client) {
        clusterStartTime = System.currentTimeMillis();
        this.members = new HashSet<>(members);
        this.services = new ArrayList<>(services.size() + 16);
        InternalService internalService = new InternalService(new ServiceContext<>(this, 0, "internal"), this);
        this.services.add(internalService);
        localMember = new Member(serverAddress.getHostName(), serverAddress.getPort(), client);
        this.transport = transportConstructor.newInstance(requestExecutor, this.services, localMember);
        master = localMember;
        if (mustJoinCluster) {
            joinCluster();
        }
        internalBus = internalService.getContext();
        IntStream.range(0, services.size())
                .mapToObj(idx -> {
                    ServiceListBuilder.Constructor c = services.get(idx);
                    ServiceContext bus = new ServiceContext(this, idx + 1, c.name);
                    return c.constructor.newInstance(bus);
                }).forEach(this.services::add);

        serviceNameMap = IntStream.range(0, services.size())
                .mapToObj(idx -> new Tuple<>(services.get(idx).name, this.services.get(idx + 1)))
                .collect(Collectors.toConcurrentMap(Tuple::a, Tuple::b));

        scheduleClusteringTask();
        transport.initialize();
        this.joinerService = joinerService;
        if (joinerService != null) {
            joinerService.onStart(new ClusterMembership() {
                @Override
                public void addMember(Member member) {
                    addMemberInternal(member);
                }

                @Override
                public void removeMember(Member member) {
                    throw new UnsupportedOperationException("not implemented");
                }
            });
        }
        members.stream().forEach(this::getConnection);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
//                try {
//                    server.waitForClose();
//                } catch (InterruptedException e) {
                eventLoop.shutdownGracefully();
                requestExecutor.shutdownGracefully();
                transport.close();
//                }
            }
        });

    }

    public Map<String, Service> serviceNameMap() {
        return serviceNameMap;
    }

    public Map<Member, MemberChannel> clusterConnection() {
        return clusterConnection;
    }

    public ServiceContext<InternalService> getServiceContext() {
        return internalBus;
    }

    public ThrowableNioEventLoopGroup eventLoop() {
        return eventLoop;
    }

    private void joinCluster() {
        CompletableFuture<Boolean> latch = new CompletableFuture<>();
        AtomicInteger count = new AtomicInteger();
        eventLoop.scheduleAtFixedRate(() -> {
            if (getMembers().size() > 0) {
                latch.complete(true);
                // this is a trick that stops this task. the exception will be swallowed.
                throw new RuntimeException("found cluster");
            }
            if (count.incrementAndGet() >= 20) {
                latch.complete(false);
            }
        }, 0, 1, TimeUnit.SECONDS);
        memberState = MemberState.FOLLOWER;
        if (!latch.join()) {
            throw new IllegalStateException("Could not found a cluster. You may set mustJoinCluster.set(false) for creating new cluster.");
        }
    }

    private void addMemberInternal(Member member) {
        if (!members.contains(member) && !member.equals(localMember)) {
            logger.info("Discovered new member {}", member);
            // we may create the connection before executing this method.
            if (!clusterConnection.containsKey(member)) {
                MemberChannel channel;
                try {
                    channel = transport.connect(member);
                } catch (InterruptedException e) {
                    logger.error("Couldn't connect new server", e);
                    return;
                }
                clusterConnection.put(member, channel);
            }
            members.add(member);
            if (isMaster()) {
                heartbeatMap.put(member, System.currentTimeMillis());
            }
            if (!member.isClient()) {
                membershipListeners.forEach(x -> eventLoop.execute(() -> x.memberAdded(member)));
            }
        }
    }

    /*private synchronized void addMembersInternal(Set<Member> newMembers) {
        if (!members.containsAll(newMembers)) {
            logger.info("Discovered another cluster of {} members", members.size());

            for (Member member : newMembers) {
                if (member.equals(localMember)) {
                    continue;
                }
                // we may create the connection before executing this method.
                if (!clusterConnection.containsKey(member)) {
                    MemberChannel channel;
                    try {
                        channel = transport.connect(member);
                    } catch (InterruptedException e) {
                        logger.error("Couldn't connect new server", e);
                        return;
                    }
                    clusterConnection.put(member, channel);
                }

                members.add(member);
                if (isMaster()) {
                    heartbeatMap.put(member, System.currentTimeMillis());
                }
            }
            membershipListeners.forEach(x -> eventLoop.execute(() -> x.clusterMerged(newMembers)));
        }
    }*/

    public Transport getTransport() {
        return transport;
    }

    public void setLastContactedTimeMaster(long lastContactedTimeMaster) {
        this.lastContactedTimeMaster = lastContactedTimeMaster;
    }

    public long lastContactedTimeMaster() {
        return lastContactedTimeMaster;
    }

    private void scheduleClusteringTask() {
        heartbeatTask = eventLoop.scheduleAtFixedRate(() -> {
            long time = System.currentTimeMillis();

            if (isMaster()) {
//                heartbeatMap.forEach((member, lastResponse) -> {
//                    if (time - lastResponse > 20000) {
//                        removeMemberAsMaster(member, true);
//                    }
//                });
                members.forEach(member -> internalBus.send(member, new HeartbeatOperation(localMember)));
            } else {
                if (time - lastContactedTimeMaster > 500) {
                    eventLoop.schedule(() -> {
                        if (time - lastContactedTimeMaster > 500) {
                            memberState = MemberState.CANDIDATE;
                            voteElection();
                        }
                    }, 150 + new Random().nextInt(150), TimeUnit.MILLISECONDS);
                } else {
                    Member localMember = getLocalMember();
                    internalBus.send(master, (masterCluster, ctx) ->
                            masterCluster.getCluster().heartbeatMap.put(localMember, System.currentTimeMillis()));
                }
            }

        }, 200, 200, TimeUnit.MILLISECONDS);
    }

    public long startTime() {
        return clusterStartTime;
    }

    public void voteElection() {
        Collection<Member> clusterMembers = getMembers();

        Map<Member, Boolean> map = new ConcurrentHashMap<>();
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        int cursor = currentTerm.incrementAndGet();
        Map<Member, CompletableFuture<Boolean>> m = internalBus.askAllMembers((service, ctx) -> {
            ctx.reply(service.getCluster().currentTerm.incrementAndGet() == cursor - 1);
        });

        m.forEach((member, resultFuture) ->
                resultFuture.thenAccept(result -> {
            map.put(member, result);
            Map<Boolean, Long> stream = map.entrySet().stream()
                    .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.counting()));
            if (stream.getOrDefault(true, 0L) > clusterMembers.size() / 2) {
                future.complete(true);
            } else if (stream.getOrDefault(false, 0L) > clusterMembers.size() / 2) {
                future.complete(false);
            }

        }));

        if (future.join()) {
            memberState = MemberState.MASTER;
            Member localMember = this.localMember;
            internalBus.sendAllMembers((service, ctx) -> service.getCluster().changeMaster(localMember));
        } else {
            memberState = MemberState.FOLLOWER;
        }
    }

    public MemberState memberState() {
        return memberState;
    }

    private synchronized void changeMaster(Member masterMember) {
        master = masterMember;
        memberState = masterMember.equals(localMember) ? MemberState.MASTER : MemberState.FOLLOWER;
    }

    public synchronized void removeMemberAsMaster(Member member, boolean replicate) {
        if (!isMaster()) {
            throw new IllegalStateException();
        }

        heartbeatMap.remove(member);
        members.remove(member);

//        if(replicate) {
//        internalBus.sendAllMembers((cluster, ctx) -> {
//            cluster.clusterConnection.remove(member);
//            Cluster.LOGGER.info("Member removed {}", member);
//            cluster.membershipListeners.forEach(l -> Throwables.propagate(() -> l.memberRemoved(member)));
//        }, true);
//        }
    }

    public void addMembershipListener(MembershipListener listener) {
        membershipListeners.add(listener);
    }

    public Set<Member> getMembers() {
        return ImmutableSet.copyOf(Iterables.concat(members, () -> Iterators.forArray(localMember)));
    }

    public <T extends Service> T getService(String serviceName) {
        checkNotNull(serviceName, "null is not allowed for service name");
        return (T) serviceNameMap.get(serviceName);
    }

    public <T extends Service> T getService(String serviceName, Class<T> clazz) {
        return getService(serviceName);
    }

    public <T extends Service> T createOrGetService(String name, ServiceConstructor<T> ser) {
        checkNotNull(ser, "null is not allowed for service constructor");
        Service existingService = serviceNameMap.get(name);
        if (existingService != null) {
            return (T) existingService;
        }
        int maxSize = Short.MAX_VALUE * 2;
        checkState(services.size() < maxSize, "Maximum number of allowed services is %s", maxSize);
        String finalName = name == null ? UUID.randomUUID().toString() : name;
        Boolean result = internalBus.replicateSafely(new AddServiceRequest(finalName, name, ser)).join();
        if (!result) {
            throw new IllegalArgumentException("there is already another service with same name");
        }
        Service service = serviceNameMap.get(finalName);
        if (service == null) {
            throw new IllegalStateException("service couldn't created");
        }
        return (T) service;
    }

    /**
     * It uses Raft log replication protocol for consensus.
     * Most of the requests that are planned to execute don't need consensus,
     * so unlike Raft implementation which waits the quorum it waits all nodes to execute the request.
     * Because there's no way to find out consistency issues without consensus methods like this one.
     * In Raft algorithm, since each log replication request uses consensus algorithm, it's easy to recover
     * from inconsistent states.
     * Since consensus is expensive compared to fire-and-forget fashion, use this method when you really need.
     *
     * @param request
     */
    public CompletableFuture<Boolean> replicateSafelyInternal(Request<?, Boolean> request, int serviceId) {
        AppendLogEntryRequest requestFromMaster = new AppendLogEntryRequest(request, serviceId);
        return askInternal(getMaster(), requestFromMaster, 0);
    }

    public Map<Long, Request> pendingConsensusMessages() {
        return pendingConsensusMessages;
    }

    public <T extends Service> T createService(ServiceConstructor<T> ser) {
        return createOrGetService(null, ser);
    }

    public boolean destroyService(String serviceName) {
        checkNotNull(serviceName, "null is not allowed for service name");
        Service service = serviceNameMap.remove(serviceName);
        if (service == null) {
            return false;
        }
        service.onClose();
        int serviceId = services.indexOf(service);
        // we do not shift the array because if the indexes change, we have to ensure consensus among nodes.
        services.set(serviceId, null);
        return true;
    }

    public Member getLocalMember() {
        return localMember;
    }

    private void send(Member server, Object bytes, int service) {
        sendInternal(server, bytes, service);
    }

    public void sendAllMembersInternal(Object bytes, boolean includeThisMember, int service) {
        logger.info(this + "internal -> " + members.size());
        members.stream().filter(member -> !member.equals(localMember) && !member.isClient())
                .forEach(member -> sendInternal(clusterConnection.get(member), bytes, service));
        if (includeThisMember) {
            if (localMember.isClient()) {
                throw new IllegalArgumentException();
            }
            Service s = services.get(service);
            LocalOperationContext ctx = new LocalOperationContext(null, service, localMember);
            s.handle(requestExecutor, ctx, bytes);
        }
    }

    public <R> Map<Member, CompletableFuture<R>> askAllMembersInternal(Object bytes, boolean includeThisMember, int service) {
        Map<Member, CompletableFuture<R>> map = new ConcurrentHashMap<>();
        clusterConnection.forEach((member, conn) -> {
            if (!member.equals(localMember)) {
                map.put(member, askInternal(conn, bytes, service));
            }
        });
        if (includeThisMember) {
            CompletableFuture<R> f = new CompletableFuture<>();
            Service s = services.get(service);
            LocalOperationContext ctx = new LocalOperationContext(f, service, localMember);
            s.handle(requestExecutor, ctx, bytes);
            map.put(localMember, f);
        }
        return map;
    }

    public void close() throws InterruptedException {
        for (MemberChannel entry : clusterConnection.values()) {
            entry.close();
        }
        transport.close();
        heartbeatTask.cancel(true);
        services.forEach(s -> s.onClose());
        joinerService.onStart(new ClusterMembership() {
            @Override
            public void addMember(Member member) {
                addMemberInternal(member);
            }
            @Override
            public void removeMember(Member member) {
                throw new UnsupportedOperationException("not implemented");
            }
        });
    }

    public void sendInternal(MemberChannel channel, Object obj, int service) {
        Packet message = new Packet(obj, service);
        channel.ask(message);
    }

    public void sendInternal(Member member, Object obj, int service) {
        if (member.equals(localMember)) {
            LocalOperationContext ctx1 = new LocalOperationContext(null, service, localMember);
            services.get(service).handle(requestExecutor, ctx1, obj);
        } else {
            Packet message = new Packet(obj, service);
            getConnection(member).send(message);
        }
    }

    public void sendInternal(Member member, Request request, int service) {
        if (member.equals(localMember)) {
            LocalOperationContext ctx1 = new LocalOperationContext(null, service, localMember);
            services.get(service).handle(requestExecutor, ctx1, request);
        } else {
            Packet message = new Packet(request, service);
            getConnection(member).ask(message);
        }
    }

    public <R> void tryAskUntilDoneInternal(Member member, Request req, int numberOfTimes, int service, CompletableFuture future) {
        CompletableFuture<R> ask = askInternal(member, req, service);
        ask.whenComplete((val, ex) -> {
            if (ex != null) {
                if (ex instanceof TimeoutException) {
                    if (numberOfTimes == 0) {
                        future.completeExceptionally(new TimeoutException());
                    } else {
                        tryAskUntilDoneInternal(member, req, numberOfTimes, service, future);
                    }
                } else {
                    future.completeExceptionally(ex);
                }
            } else {
                future.complete(val);
            }
        });
    }

    public <R> CompletableFuture<R> askInternal(MemberChannel channel, Object obj, int service) {
        int andIncrement = messageSequence.getAndIncrement();
        Packet message = new Packet(andIncrement, obj, service);
        return channel.ask(message);
    }

    public <R> CompletableFuture<R> askInternal(Member member, Object obj, int service) {
        if (member.equals(localMember)) {
            CompletableFuture<R> future = new CompletableFuture<>();
            LocalOperationContext ctx1 = new LocalOperationContext(future, service, localMember);
            services.get(service).handle(requestExecutor, ctx1, obj);
            return future;
        } else {
            return askInternal(getConnection(member), obj, service);
        }
    }

    public <R> CompletableFuture<R> askInternal(Member member, Request request, int service) {
        if (member.equals(localMember)) {
            CompletableFuture<R> future = new CompletableFuture<>();
            LocalOperationContext ctx1 = new LocalOperationContext(future, service, localMember);
            services.get(service).handle(requestExecutor, ctx1, request);
            return future;
        } else {
            return askInternal(getConnection(member), request, service);
        }
    }

    private MemberChannel getConnection(Member member) {
        MemberChannel channel = clusterConnection.get(member);
        if (channel == null) {
            if (!members.contains(member)) {
                throw new IllegalArgumentException("member doesn't exist in the cluster");
            }
            MemberChannel created;
            try {
                created = transport.connect(member);
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
            synchronized (this) {
                clusterConnection.put(member, created);
            }
            return created;
        }
        return channel;
    }

    public boolean isMaster() {
        return localMember.equals(master);
    }

    public Member getMaster() {
        return master;
    }

    public List<Service> getServices() {
        return Collections.unmodifiableList(services);
    }

    public AtomicLong getLastCommitIndex() {
        return lastCommitIndex;
    }

//    protected synchronized void changeCluster(Set<Member> newClusterMembers, Member masterMember, boolean isNew) {
//        try {
//            pause();
//            clusterConnection.clear();
//            master = masterMember;
//            members = newClusterMembers;
//            messageHandlers.cleanUp();
//            LOGGER.info("Joined a cluster of {} nodes.", members.size());
//            if (!isNew)
//                membershipListeners.forEach(x -> eventLoop.execute(() -> x.clusterChanged()));
//        } finally {
//            resume();
//        }
//    }

}
