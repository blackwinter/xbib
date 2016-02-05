package org.xbib.cluster.service.ringmap;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.hash.PrimitiveSink;
import org.xbib.cluster.Member;
import org.xbib.cluster.util.Tuple;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ConsistentHashRing {
    private static final HashFunction hashFunction = Hashing.murmur3_128();
    private static final Funnel keyFunnel = new Funnel<SinkSerializable>() {
        @Override
        public void funnel(SinkSerializable from, PrimitiveSink into) {
            from.writeTo(into);
        }
    };
    private final int bucketPerNode;
    private final Bucket[] buckets;
    private final int replicationFactor;

    public ConsistentHashRing(Collection<Member> members, int bucketPerNode, int replicationFactor) {
        this.bucketPerNode = bucketPerNode;
        this.replicationFactor = replicationFactor;
        // todo: find a way to construct buckets without adding elements one by one.
        Bucket[] list = null;
        for (Member member : members.stream().sorted(this::compareMembers).collect(Collectors.toList())) {
            list = findBucketListForNewNode(member, list);
        }
        buckets = list;
    }

    protected ConsistentHashRing(Bucket[] buckets, int bucketPerNode, int replicationFactor) {
        this.bucketPerNode = bucketPerNode;
        this.buckets = buckets;
        this.replicationFactor = replicationFactor;
    }

    public static boolean isTokenBetween(long hash, long start, long end) {
        if (start <= end) {
            return hash >= start && hash <= end;
        }
        // we're in the start point of ring
        else {
            return hash > start || hash < end;
        }
    }

    public static Hasher newHasher() {
        return hashFunction.newHasher();
    }

    public static long hash(String hash) {
        return hashFunction.hashString(hash, Charset.forName("UTF-8")).asLong();
    }

    public static long hash(Object hash) {
        // TODO: man, it's ugly. we should find a nice way to fix this problem. maybe strategy pattern?

        if (hash instanceof String) {
            return hash((String) hash);
        }
        if (hash instanceof Long) {
            return hash((long) hash);
        }
        if (hash instanceof Integer) {
            return hash((int) hash);
        }
        if (hash instanceof SinkSerializable) {
            return hashFunction.hashObject(hash, keyFunnel).asLong();
        }
        throw new IllegalArgumentException("map key should be one of [String, Long, Integer or com.google.common.hash.Funnel]");
    }

    public static long hash(byte[] hash) {
        return hashFunction.hashBytes(hash).asLong();
    }

    public static long hash(long hash) {
        return hashFunction.hashLong(hash).asLong();
    }

    public static long hash(int hash) {
        return hashFunction.hashInt(hash).asLong();
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        getBuckets().forEach((range, members) -> {
            str.append("[").append(range.start).append("-").append(range.end).append(", ").append(members.size()).append(" members]");
        });
        return str.toString();
    }

    private int compareMembers(Member o1, Member o2) {
        String o1Str = o1.getAddress().getHostString() + o1.getAddress().getPort();
        String o2Str = o2.getAddress().getHostString() + o2.getAddress().getPort();
        return o1Str.compareTo(o2Str);
    }

    public Map<TokenRange, List<Member>> getBuckets() {
        return getBuckets(buckets);
    }

    private Map<TokenRange, List<Member>> getBuckets(Bucket[] buckets) {
        return IntStream.range(0, buckets.length)
                .mapToObj(i -> {
                    Bucket bucket = buckets[i];
                    TokenRange tokenRange = new TokenRange(i, bucket.token, getBucketFromRing(buckets, i + 1).token);
                    return new Tuple<>(tokenRange, bucket.members);
                }).collect(Collectors.toMap(Tuple::a, Tuple::b));
    }

    public Bucket getBucket(int i) {
        int length = buckets.length;
        if (i >= length) {
            i = i % length;
        }
        return buckets[i >= 0 ? i : i + length];
    }

    public int getBucketCount() {
        return buckets.length;
    }

    public int getMemberCount() {
        return buckets.length / bucketPerNode;
    }

    public TokenRange getBucketRange(int i) {
        return new TokenRange(i, buckets[i].token, getBucketFromRing(buckets, i + 1).token);
    }

    public int findBucketIdFromToken(long l) {
        int low = 0;
        int high = buckets.length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;

            if (buckets[mid].token < l) {
                low = mid + 1;
            } else if (buckets[mid].token > l) {
                high = mid - 1;
            } else {
                return mid;
            }
        }

        return high;
    }

    public Bucket findBucketFromToken(long l) {
        return buckets[findBucketIdFromToken(l)];
    }

    private Bucket getBucketFromRing(Bucket[] buckets, int i) {
        return buckets[(i % buckets.length) + (i < 0 ? buckets.length : 0)];
    }

    public double getTotalRingRange(Member member) {
        double total = 0;
        for (int i = 0; i < buckets.length; i++) {
            Bucket current = buckets[i];
            if (current.members.contains(member)) {
                if (i == buckets.length - 1) {
                    total += (((Long.MAX_VALUE - current.token) + (buckets[0].token - Long.MIN_VALUE)) / 2) / (Long.MAX_VALUE / 100.0);
                } else {
                    total += Math.abs((buckets[i + 1].token - current.token) / 2) / (Long.MAX_VALUE / 100.0);
                }

            }
        }
        return total;
    }

    private Bucket[] findBucketListForNewNode(Member member, Bucket[] buckets) {
        if (buckets == null) {
            long token = Long.MAX_VALUE / (bucketPerNode / 2);

            return IntStream.range(0, bucketPerNode).mapToObj(i -> {
                long t = Long.MIN_VALUE + (token * i);
                return new Bucket(Sets.newHashSet(member), t);
            }).toArray(Bucket[]::new);
        }
        Bucket[] newBucketList = Arrays.copyOf(buckets, buckets.length + bucketPerNode);
        // find the members who owns less data than other to use them as replica of new buckets
        Map<Member, Long> result = new HashMap<>();
        getBuckets(buckets)
                .entrySet().stream()
                .filter(x -> !x.getValue().contains(member))
                .forEach(x -> x.getValue().forEach(z -> result.merge(z, x.getKey().gap() / 2, Long::sum)));
        Set<Map.Entry<Member, Long>> memberSet = result.entrySet();
        // find the larger gaps to and divide them in order to create new buckets
        TokenRange[] tokens = getBuckets(buckets).entrySet().stream()
                .sorted((o1, o2) -> {
                    int compare = Long.compare(o2.getKey().gap(), o1.getKey().gap());
                    if (compare == 0) {
                        // we compare the nodes that own this bucket and choose the bucket
                        // that has members that owns minimum range on the ring.
                        long sum1 = o1.getValue().stream().mapToLong(result::get).sum();
                        long sum2 = o2.getValue().stream().mapToLong(result::get).sum();
                        compare = Long.compare(sum2, sum1);
                        if (compare == 0) {
                            // it's pointless but if such condition occurs we need
                            // a way that all nodes agree.
                            return Long.compare(o1.getKey().start, o2.getKey().start);
                        }
                    }
                    return compare;
                }).limit(8).map(Map.Entry::getKey).toArray(TokenRange[]::new);

        for (int idx = 0; idx < tokens.length; idx++) {
            TokenRange current = tokens[idx];
            long gap = current.gap() / 2;

            HashSet<Member> members = new HashSet<>();
            members.add(member);

            IntStream.range(0, replicationFactor - 1).forEach(i -> {
                Map.Entry<Member, Long> m = memberSet.stream()
                        .sorted((x, y) -> Long.compare(x.getValue(), y.getValue())).findFirst().get();
                members.add(m.getKey());
                m.setValue(m.getValue() + gap);
            });

            Bucket element = new Bucket(members, current.start + gap);
            newBucketList[idx + buckets.length] = element;
        }
        Arrays.sort(newBucketList, (o1, o2) -> Long.compare(o1.token, o2.token));

        if (buckets.length / bucketPerNode < replicationFactor) {
            for (int i = 0; i < newBucketList.length; i++) {
                Bucket oldBucket = newBucketList[i];

                if (oldBucket.members.size() < replicationFactor) {
                    HashSet<Member> members = new HashSet<>(oldBucket.members);
                    members.add(member);
                    newBucketList[i] = new Bucket(members, newBucketList[i].token);
                }
            }
        }

        return newBucketList;
    }

    public ConsistentHashRing addNode(Member member) {
        if (getMembers().contains(member)) {
            return new ConsistentHashRing(buckets, bucketPerNode, replicationFactor);
        }

        Bucket[] buckets = findBucketListForNewNode(member, this.buckets);
        return new ConsistentHashRing(buckets, bucketPerNode, replicationFactor);
    }

    public ConsistentHashRing removeNode(Member member) {
        if (!getMembers().contains(member)) {
            return new ConsistentHashRing(buckets, bucketPerNode, replicationFactor);
        }

        if (getMemberCount() == 1) {
            throw new IllegalStateException("ring must contain at least one member");
        }

        List<Bucket> result = Lists.newArrayList(this.buckets);
        int newMemberSize = (buckets.length / bucketPerNode) - 1;

        // remove smallest buckets which is replicated by member
        getBuckets().entrySet().stream()
                .filter(x -> x.getValue().contains(member))
                .sorted((x, y) -> Long.compare(x.getKey().gap(), y.getKey().gap()))
                .limit(bucketPerNode)
                .map(x -> x.getKey().id)
                .sorted((x, y) -> Integer.compare(y, x)) // we need reverse order, otherwise the indexes change
                .forEach(i -> {
                    // cause IntStream doesn't have a method sorted(Comparator)
                    // and you know, it sucks.
                    result.remove((int) i);
                });

        // replace this member to another in other buckets which is replicated by this member
        Stream<Bucket> resultArr = result.stream()
                .map(bucket -> {
                    if (!bucket.members.contains(member)) {
                        return bucket;
                    }
                    HashSet arrayList = new HashSet(bucket.members);
                    arrayList.remove(member);
                    if (newMemberSize >= replicationFactor) {
                        Map<Member, Long> memberTokenRange = new HashMap<>();
                        getBuckets().forEach((val, members) ->
                                members.forEach(m -> memberTokenRange.merge(m, val.gap(), Long::sum)));
                        Optional<Map.Entry<Member, Long>> first = memberTokenRange.entrySet().stream()
                                .filter(x -> !x.getKey().equals(member))
                                .sorted((o1, o2) -> Long.compare(o1.getValue(), o2.getValue()))
                                .findFirst();
                        first.ifPresent(entry -> arrayList.add(entry.getKey()));
                    }
                    return new Bucket(arrayList, bucket.token);
                });
        Bucket[] buckets1 = resultArr.toArray(Bucket[]::new);
        return new ConsistentHashRing(buckets1, bucketPerNode, replicationFactor);
    }

    public int findBucketId(Object key) {
        return findBucketIdFromToken(hash(key));
    }

    public Bucket findBucket(Object key) {
        return buckets[findBucketIdFromToken(hash(key))];
    }

    public Set<Member> getMembers() {
        HashSet<Member> members = new HashSet<>();

        for (Bucket bucket : buckets) {
            members.addAll(bucket.members);
        }

        return members;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConsistentHashRing)) {
            return false;
        }
        ConsistentHashRing that = (ConsistentHashRing) o;
        return bucketPerNode == that.bucketPerNode && replicationFactor == that.replicationFactor && Arrays.equals(buckets, that.buckets);
    }

    @Override
    public int hashCode() {
        int result = bucketPerNode;
        result = 31 * result + Arrays.hashCode(buckets);
        result = 31 * result + replicationFactor;
        return result;
    }

    public static class Bucket {
        public long token;

        public ArrayList<Member> members;

        public Bucket(Set<Member> members, long token) {
            this.members = new ArrayList<>(members);
            this.token = token;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Bucket)) {
                return false;
            }
            Bucket bucket = (Bucket) o;
            return token == bucket.token && members.equals(bucket.members);
        }

        @Override
        public int hashCode() {
            int result = (int) (token ^ (token >>> 32));
            result = 31 * result + members.hashCode();
            return result;
        }
    }

    public static class TokenRange {
        public final long start;
        public final long end;
        public final int id;

        private TokenRange(int id, long start, long end) {
            this.id = id;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "TokenRange{" + "start=" + start + ", end=" + end + ", bucketId=" + id + '}';
        }

        public long gap() {
            return Math.abs(end - start);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TokenRange)) {
                return false;
            }
            TokenRange that = (TokenRange) o;
            return end == that.end && start == that.start;
        }

        @Override
        public int hashCode() {
            int result = (int) (start ^ (start >>> 32));
            result = 31 * result + (int) (end ^ (end >>> 32));
            return result;
        }
    }
}
