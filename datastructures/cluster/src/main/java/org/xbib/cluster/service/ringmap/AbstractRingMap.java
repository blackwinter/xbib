package org.xbib.cluster.service.ringmap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.Cluster;
import org.xbib.cluster.action.changering.ChangeRingRequest;
import org.xbib.cluster.serialize.KryoSerializable;
import org.xbib.cluster.Member;
import org.xbib.cluster.MembershipListener;
import org.xbib.cluster.MigrationListener;
import org.xbib.cluster.service.ServiceContext;
import org.xbib.cluster.service.PausableService;
import org.xbib.cluster.OperationContext;
import org.xbib.cluster.Request;
import org.xbib.cluster.util.FutureUtil.MultipleFutureListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractRingMap<C extends AbstractRingMap, M extends Map, K, V> extends PausableService<C>
        implements MembershipListener {

    private final static Logger logger = LogManager.getLogger(AbstractRingMap.class);

    private static Random random = new Random();
    protected final Member localMember;
    private final MapMergePolicy<V> mergePolicy;
    private final int replicationFactor;
    private final Supplier<M> mapSupplier;
    private final int bucketCount;
    protected M[] map;
    protected int[] bucketIds;
    private Map<ConsistentHashRing.TokenRange, Map<K, V>> dataWaitingForMigration = new HashMap<>();
    private ConsistentHashRing ring;
    private LinkedList<MigrationListener> migrationListeners = new LinkedList<>();

    public AbstractRingMap(ServiceContext<C> serviceContext, Supplier<M> mapSupplier, MapMergePolicy<V> mergePolicy, int bucketCount, int replicationFactor) {
        super(serviceContext);
        this.mergePolicy = mergePolicy;
        this.replicationFactor = replicationFactor;
        this.mapSupplier = mapSupplier;
        this.bucketCount = bucketCount;

        Cluster cluster = getContext().getCluster();
        cluster.addMembershipListener(this);
        localMember = cluster.getLocalMember();

        // if we're the master node and initializing the service, then it's a new service.
        if (cluster.getMaster().equals(localMember)) {
            ConsistentHashRing newRing = new ConsistentHashRing(cluster.getMembers(), bucketCount, replicationFactor);
            ring = newRing;
            bucketIds = createBucketForRing(newRing);
            map = createEmptyMap(ring);
        } else {
            CompletableFuture<ConsistentHashRing> ringFuture = getContext().ask(cluster.getMaster(), (service, ctx0) -> ctx0.reply(service.getRing()));
            ConsistentHashRing ring = ringFuture.join();
            setRing(ring);
        }
    }

    protected int[] createBucketForRing(ConsistentHashRing ring) {
        return ring.getBuckets().entrySet().stream()
                .filter(entry -> entry.getValue().contains(localMember))
                .mapToInt(entry -> entry.getKey().id).sorted().toArray();
    }

    public int getBucketCount() {
        return bucketCount;
    }

    public Map<ConsistentHashRing.TokenRange, Map<K, V>> dataWaitingForMigration() {
        return dataWaitingForMigration;
    }

    protected M[] createEmptyMap(ConsistentHashRing ring) {
        Member localMember = getContext().getCluster().getLocalMember();
        int count = (int) ring.getBuckets().entrySet().stream()
                .filter(x -> x.getValue().contains(localMember)).count();
        Class clazz;
        M sample = null;
        if (map != null) {
            clazz = map[0].getClass();
        } else {
            sample = mapSupplier.get();
            clazz = sample.getClass();
        }
        // hacky way to create generic array
        M[] arr = (M[]) Array.newInstance(clazz, count);
        int i = 0;
        if (sample != null) {
            i = 1;
            arr[0] = sample;
        }
        for (; i < count; ++i) {
            arr[i] = mapSupplier.get();
        }

        return arr;
    }

    public void logOwnedBuckets() {
        StringBuilder str = new StringBuilder();
        str.append("ownedBuckets[").append(map.length).append("]: ");
        int i = 0;
        for (Map m : map) {
            str.append("[").append(m.size()).append("]");
            i += m.size();
        }
        str.append(" = ").append(i);
        logger.debug(str.toString());
    }

    @Override
    public synchronized void memberAdded(Member member) {
        if (ring.getMembers().contains(member)) {
            // it means we joined this cluster already
            // via requesting ring from an existing node.
            return;
        }

        logger.debug("Adding member {} to existing cluster of {} nodes.", member, getContext().getCluster().getMembers().size());

        ConsistentHashRing newRing = ring.addNode(member);
        changeRing(newRing).join();
    }

//    @Override
//    public void clusterChanged() {
//        ConsistentHashRing remoteRing = getContext()
//                .ask(getContext().getCluster().getMaster(), (service, ctx1) -> ctx1.reply(service.getRing()), ConsistentHashRing.class)
//                .join();
//
//        ConsistentHashRing newRing;
//        if (remoteRing.getMembers().contains(localMember)) {
//            ring = remoteRing.removeNode(localMember);
//            newRing = remoteRing;
//        } else {
//            ring = remoteRing;
//            newRing = remoteRing.addNode(localMember);
//        }
//        // we don't care about the old entries because the old ring doesn't have this local member so all operations will be remote.
//        // the old entries will be added to the cluster when the new ring is set.
//        changeRing(newRing).thenAccept(x -> {
//                    // maybe we can parallelize this operation in order to make it fast
////                        Arrays.stream(oldBuckets).forEach(map -> map.forEach(this::put));
//                    Set<Member> members = ring.getMembers();
//                    LOGGER.info("Joined a cluster which has {} members {}.", members.size(), members);
//                }
//        ).join();
//
//    }

    private synchronized CompletableFuture<Void> setRing(ConsistentHashRing newRing) {
        M[] newMap = createEmptyMap(newRing);
        int[] newBucketIds = createBucketForRing(newRing);

        ArrayList<CompletableFuture> migrations = new ArrayList<>();
        for (Map.Entry<ConsistentHashRing.TokenRange, List<Member>> entry : newRing.getBuckets().entrySet()) {
            ConsistentHashRing.TokenRange range = entry.getKey();
            List<Member> members = entry.getValue();

            if (members.contains(localMember)) {
                List<Member> bucketMembers = entry.getValue();
                Member ownerMember = bucketMembers.get(members.indexOf(localMember) % bucketMembers.size());

                logger.debug("asking entries [{}, {}] from {}", range.start, range.end, ownerMember);

                CompletableFuture<Map<K, V>> ask = getContext().ask(ownerMember, new ChangeRingRequest(range.start, range.end));
                CompletableFuture<Void> f = ask
                        .thenAccept(data -> {
                            newMap[Arrays.binarySearch(newBucketIds, range.id)].putAll(data);
                            if (!ownerMember.equals(localMember)) {
                                logger.debug("{} elements in token[{} - {}] moved from {} to {}", data.size(), range.start, range.end, ownerMember, localMember);
                            }
                        });
                migrations.add(f);
            }
        }

        if (migrations.size() > 0) {
            migrationListeners.forEach(l -> getContext().eventLoop().execute(() -> l.migrationStart(localMember)));
        }

        // resume when all migrations completed
        return CompletableFuture.allOf(migrations.toArray(new CompletableFuture[migrations.size()]))
                .thenRun(() -> {
                    logger.debug("{} migration completed.  New ring has {} buckets in member {}",
                            migrations.size(), newRing.getBuckets().size(), localMember);
                    synchronized (getContext()) {
                        bucketIds = newBucketIds;
                        map = newMap;
                        ring = newRing;
                    }
                    migrationListeners.forEach(l -> getContext().eventLoop().execute(() -> l.migrationEnd(localMember)));
                    logOwnedBuckets();
                });
    }

    private CompletableFuture<Void> changeRing(ConsistentHashRing newRing) {
        ConsistentHashRing oldRing = ring;
        M[] newMap = createEmptyMap(newRing);
        int[] newBucketIds = createBucketForRing(newRing);

        ArrayList<CompletableFuture> migrations = new ArrayList<>();
        for (Map.Entry<ConsistentHashRing.TokenRange, List<Member>> entry : newRing.getBuckets().entrySet()) {
            ConsistentHashRing.TokenRange range = entry.getKey();
            List<Member> members = entry.getValue();

            int start = oldRing.findBucketIdFromToken(range.start);
            int end = oldRing.findBucketIdFromToken(range.end - 1);
            if (end - start < 0) {
                end = end + oldRing.getBucketCount();
            }

            if (members.contains(localMember)) {
                long cursor = range.start;

                for (int bucketId = start; bucketId <= end; bucketId++) {
                    bucketId %= oldRing.getBucketCount();

                    ConsistentHashRing.Bucket oldBucket = oldRing.getBucket(bucketId);
                    List<Member> oldBucketMembers = oldBucket.members;

                    Member ownerMember;
                    if (oldBucketMembers.contains(localMember)) {
                        ownerMember = localMember;
                    } else {
                        int index = members.indexOf(localMember) % oldBucketMembers.size();
                        ownerMember = oldBucketMembers.get(index);
                    }

                    long queryStartToken = cursor;

                    long nextBucketToken = oldRing.getBucket(bucketId + 1).token;
                    long queryEndToken = (range.end - cursor > nextBucketToken - cursor) ? nextBucketToken - 1 : range.end - 1;
                    cursor = queryEndToken;

                    boolean isLocalOp = ownerMember.equals(localMember);
                    if (!isLocalOp) {
                        logger.debug("asking entries [{}, {}] from {}", queryStartToken, ownerMember);
                    } else {
                        logger.trace("asking entries [{}, {}] from {}", queryStartToken, ownerMember);
                    }

                    CompletableFuture<Map<K, V>> ask = getContext().ask(ownerMember, new ChangeRingRequest(queryStartToken, queryEndToken, oldRing));
                    CompletableFuture<Void> f = ask
                            .thenAccept(data -> {
                                int startBucket = newRing.findBucketIdFromToken(queryStartToken);
                                int nextBucket = newRing.findBucketIdFromToken(queryEndToken);
                                if (startBucket == nextBucket) {
                                    newMap[Arrays.binarySearch(newBucketIds, startBucket)].putAll(data);
                                } else {
                                    data.forEach((key, value) -> {
                                        int i = Arrays.binarySearch(newBucketIds, newRing.findBucketIdFromToken(ConsistentHashRing.hash(key)));
                                        if (i >= 0) {
                                            Map<K, V> partition = newMap[i];
                                            partition.put(key, value);
                                        }
                                    });
                                }

                                if (!ownerMember.equals(localMember)) {
                                    logger.debug("{} elements in token[{} - {}] moved from {} to {}", data.size(), queryStartToken, queryEndToken, ownerMember, localMember);
                                }
                            });
                    migrations.add(f);
                }

            } else {
                for (int bucketId = start; bucketId <= end; bucketId++) {
                    // we don't remove the old entries because
                    // the new member will request the entries and remove them via migration request,
                    // so it allows us to avoid the requirement for consensus between nodes when changing ring.
                    dataWaitingForMigration.put(range, getBucket(bucketId));
                }
            }
        }

        if (migrations.size() > 0) {
            migrationListeners.forEach(l -> getContext().eventLoop().execute(() -> l.migrationStart(localMember)));
        }

        // resume when all migrations completed
        return CompletableFuture.allOf(migrations.toArray(new CompletableFuture[migrations.size()]))
                .thenRun(() -> {
                    logger.debug("{} migration completed.  New ring has {} buckets in member {}",
                            migrations.size(), newRing.getBuckets().size(), localMember);
                    synchronized (getContext()) {
                        bucketIds = newBucketIds;
                        System.out.println(newMap);
                        map = newMap;
                        ring = newRing;
                    }
                    migrationListeners.forEach(l -> getContext().eventLoop().execute(() -> l.migrationEnd(localMember)));
                    logOwnedBuckets();
                });
    }

    public Map<K, V> getBucket(int bucketId) {
        return map[getPartitionId(bucketId)];
    }

    protected int getPartitionId(int bucketId) {
        int i = Arrays.binarySearch(bucketIds, bucketId);
        if (i < 0) {
            throw new IllegalArgumentException("bucket is not owned by this member");
        }
        return i;
    }

    @Override
    public synchronized void memberRemoved(Member member) {
        if (isPaused()) {
            addQueueIfPaused(() -> memberRemoved(member));
        } else {
            changeRing(ring.removeNode(member));
        }
    }

    @Override
    public void clusterMerged(Set<Member> newMembers) {

    }

    public void onClose() {
        Arrays.stream(map).forEach(Map::clear);
    }

    public CompletableFuture<Void> putAll(Map<K, V> fromMap) {
        Map<Member, List<Map.Entry>> m = new HashMap<>();
        for (Map.Entry entry : fromMap.entrySet()) {
            for (Member member : ring.findBucketFromToken(ConsistentHashRing.hash(entry.getKey())).members) {
                m.getOrDefault(member, new ArrayList<>()).add(entry);
            }
        }
        m.forEach((key, value) -> getContext().send(key, new PutAllRequest(value)));
        CompletableFuture[] completableFutures = fromMap.entrySet().stream()
                .map(entry -> put(entry.getKey(), entry.getValue()))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(completableFutures);
    }

    public CompletableFuture<Void> put(K key, V val) {
        int bucketId = ring.findBucketIdFromToken(ConsistentHashRing.hash(key));
        ConsistentHashRing.Bucket bucket = ring.getBucket(bucketId);
        MultipleFutureListener listener = new MultipleFutureListener((bucket.members.size() / 2) + 1);
        for (Member next : bucket.members) {
            if (next.equals(localMember)) {
                putLocal(key, val);
                listener.increment();
            } else {
                listener.listen(getContext().ask(next, new PutMapOperation(key, val)));
            }
        }
        return listener.get();
    }

    public CompletableFuture<V> get(K key) {
        int bucketId = ring.findBucketId(key);
        ConsistentHashRing.Bucket bucket = ring.getBucket(bucketId);

        ArrayList<Member> members = bucket.members;
        if (members.contains(localMember)) {
            return CompletableFuture.completedFuture(getBucket(bucketId).get(key));
        }

        return getContext().ask(members.get(random.nextInt(members.size())),
                new GetRequest(this, key));
    }

    public CompletableFuture<V> syncAndGet(String key) {
        int bucketId = ring.findBucketId(key);
        ConsistentHashRing.Bucket bucket = ring.getBucket(bucketId);

        AtomicReference<V> merged = new AtomicReference<>();
        CompletableFuture<Void>[] res = new CompletableFuture[bucket.members.size()];

        for (int i = 0; i < bucket.members.size(); i++) {
            CompletableFuture<V> ask = getContext().ask(bucket.members.get(i), new GetRequest(this, key));
            res[i] = ask.thenAccept(x -> {
                V v = merged.get();
                if (v == null) {
                    merged.set(x);
                } else {
                    merged.set(mergePolicy.merge(v, x));
                }

            });
        }

        return CompletableFuture.allOf(res).thenApply(x -> merged.get());
    }

    public int getLocalSize() {
        return Arrays.stream(map).collect(Collectors.summingInt(value -> value.size()));
    }

    public CompletableFuture<Map<Member, Integer>> size() {
        Request<C, Integer> req = (service, ctx) -> ctx.reply(service.getLocalSize());
        Map<Member, CompletableFuture<Integer>> resultMap = getContext().askAllMembers(req);

        Map<Member, Integer> m = new ConcurrentHashMap<>(getContext().getCluster().getMembers().size());
        m.put(localMember, getLocalSize());
        CompletableFuture<Map<Member, Integer>> future = new CompletableFuture<>();

        resultMap.forEach((key, f) -> f.thenAccept(x -> {
            m.put(key, x);
            resultMap.remove(key);
            if (resultMap.size() == 0) {
                future.complete(m);
            }
        }));

        return future;
    }

    public ConsistentHashRing getRing() {
        return ring;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public void listenMigrations(MigrationListener migrationListener) {
        migrationListeners.add(migrationListener);
    }

    public <R> CompletableFuture<R> execute(K key, BiFunction<K, Modifiable<V>, R> execute) {
        int bucketId = ring.findBucketId(key);
        ConsistentHashRing.Bucket bucket = ring.getBucket(bucketId);

        ArrayList<Member> members = bucket.members;

        return getContext().ask(members.get(0), (service, ctx) -> {
            Map<K, V> partition = service.getBucket(service.getRing().findBucketId(key));
            Modifiable<V> vModifiable = new Modifiable<>(partition.get(key));
            R apply = execute.apply(key, vModifiable);
            if (vModifiable.changed()) {
                partition.put(key, vModifiable.value());
            }
            ctx.reply(apply);
        });
    }

    protected void putLocal(K key, V value) {
        Map<K, V> partition = getBucket(ring.findBucketId(key));
        if (partition == null) {
            logger.error("Discarded put request for key {} because node doesn't own that token.", key);
        } else {
            partition.put(key, value);
        }
    }

    @KryoSerializable(id = 10)
    public static class PutMapOperation implements Request<AbstractRingMap, Void> {
        Object key;
        Object value;

        public PutMapOperation(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public void run(AbstractRingMap service, OperationContext ctx) {
            service.putLocal(key, value);
            ctx.reply(null);
        }
    }
}
