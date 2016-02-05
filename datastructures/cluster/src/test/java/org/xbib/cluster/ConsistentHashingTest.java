package org.xbib.cluster;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Ignore;
import org.junit.Test;
import org.xbib.cluster.service.ringmap.ConsistentHashRing;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ConsistentHashingTest {
    /*
     We need an extensive testing framework in order to efficiently test ConsistentHashRing
     - testing for immutability
     - testing for balancing mechanism
     - testing for edge cases
       - add same node twice
       - remove same node twice
     */
    @Test
    public void testAdd() {

        Member member = new Member("127.0.0.1", 0);
        ConsistentHashRing ring = new ConsistentHashRing(Lists.newArrayList(member), 8, 2);


        List<Member> members = Lists.newArrayList(member);

        for (int i = 1; i <= 100; i++) {
            Map<ConsistentHashRing.TokenRange, List<Member>> buckets = ring.getBuckets();

            assertEquals(buckets.size(), 8 * members.size());
            buckets.forEach((token, memberList) -> {
                if (members.size() <= 2) {
                    assertEquals(members.size(), memberList.size());
                    for (Member m : members) {
                        assertTrue(memberList.contains(m));
                    }
                } else {
                    assertEquals(memberList.size(), 2);
                    assertEquals(new HashSet<>(memberList).size(), 2);
                }
            });

            Member member1 = new Member("127.0.0.1", i);
            ring = ring.addNode(member1);
            members.add(member1);
        }

//        final ConsistentHashRing finalRing = ring;
//        ring.getMembers().forEach(y -> {
//            long count = finalRing.getBuckets().entrySet().stream().filter(x -> x.getValue().contains(y)).count();
//            System.out.println(count + " " + y);
//        });
    }

    @Ignore
    @Test
    public void testMemberContainsFixedBuckets() {

        Member member = new Member("127.0.0.1", 0);
        ConsistentHashRing ring = new ConsistentHashRing(Lists.newArrayList(member), 8, 2);
        List<Member> members = Lists.newArrayList(member);

        for (int i = 1; i < 100; i++) {
            Map<ConsistentHashRing.TokenRange, List<Member>> buckets = ring.getBuckets();
            ring.getMembers().forEach(m -> {
                long count = buckets.entrySet().stream().filter(x -> x.getValue().contains(m)).count();
                assertEquals(Math.min(8 * 2, buckets.size()), count);
            });

            Member member1 = new Member("127.0.0.1", i);
            ring = ring.addNode(member1);
            members.add(member1);

        }
    }

//    @Test
    public void testRemove() {

        Member member0 = new Member("127.0.0.1", 0);
        Member member1 = new Member("127.0.0.1", 1);
        ConsistentHashRing ring = new ConsistentHashRing(Lists.newArrayList(member0, member1), 8, 2);


        ConsistentHashRing removedRing = ring.removeNode(member0);
        Map<ConsistentHashRing.TokenRange, List<Member>> buckets = removedRing.getBuckets();

        assertEquals(buckets.size(), 8);
        buckets.forEach((range, members) -> {
            assertEquals(members.size(), 1);
            assertTrue(members.contains(member1));
        });

    }

//    @Test
    public void testRemoveMultiple() {

        Member member0 = new Member("127.0.0.1", 0);
        Member member1 = new Member("127.0.0.1", 1);
        Member member2 = new Member("127.0.0.1", 2);
        ConsistentHashRing ring = new ConsistentHashRing(Lists.newArrayList(member0, member1, member2), 8, 2);


        ConsistentHashRing removedRing = ring.removeNode(member2);
        Map<ConsistentHashRing.TokenRange, List<Member>> buckets = removedRing.getBuckets();

        assertEquals(buckets.size(), 16);
        buckets.forEach((range, members) -> {
            assertEquals(members.size(), 2);
            assertTrue(members.contains(member1));
            assertTrue(members.contains(member0));
        });

    }

//    @Test
    public void testBalanced() {

        Member member0 = new Member("127.0.0.1", 0);
        Member member1 = new Member("127.0.0.1", 1);
        Member member2 = new Member("127.0.0.1", 2);
        Member member3 = new Member("127.0.0.1", 3);
        Member member4 = new Member("127.0.0.1", 4);
        Member member5 = new Member("127.0.0.1", 5);
        ConsistentHashRing ring = new ConsistentHashRing(Lists.newArrayList(member0, member1), 8, 2);

        ConsistentHashRing newRing = ring.addNode(member2).addNode(member3).addNode(member4).addNode(member5);

        newRing.getMembers().forEach(x -> System.out.println(x.getAddress().getPort() + " " + newRing.getTotalRingRange(x)));

        newRing.getBuckets().forEach((range, members) -> {
            long percentage = (range.gap() / 100) / (Long.MAX_VALUE / 5000);
            System.out.println("%" + percentage + " " + members.stream().map(x -> Integer.toString(x.getAddress().getPort())).collect(Collectors.joining(", ")));
        });

    }

    public void printRing(ConsistentHashRing ring) {
        ring.getBuckets()
                .forEach((token, members) -> System.out.println(token + " ->" + members));
    }



    @Test()
    public void testEquals() {
        ConsistentHashRing ring1 = new ConsistentHashRing(Sets.newHashSet(new Member("127.0.0.1", 0)), 8, 2);
        ConsistentHashRing ring2 = new ConsistentHashRing(Sets.newHashSet(new Member("127.0.0.1", 0)), 8, 2);

        assertEquals(ring1, ring2);

        for (int i = 1; i < 100; i++) {
            Member member = new Member("127.0.0.1", i);
            ring1 = ring1.addNode(member);
            ring2 = ring2.addNode(member);
            assertEquals(ring1, ring2);
        }

    }
}
