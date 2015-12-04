package org.xbib.cluster;

import org.xbib.cluster.service.ServiceListBuilder;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class SimpleTest {

    public static Stream<ClusterBuilder> createFixedFakeCluster(IntStream intStream, ImmutableList<ServiceListBuilder.Constructor> services) {
        List<NoNetworkTransport> buses = intStream
                .mapToObj(idx -> new NoNetworkTransport(new Member("", idx))).collect(Collectors.toList());

        buses.stream().forEach(bus -> buses.stream().filter(other -> !other.equals(bus)).forEach(bus::addMember));

        return buses.stream().map(bus -> new ClusterBuilder()
                .members(buses.stream().filter(b -> !bus.equals(b)).map(NoNetworkTransport::getLocalMember).collect(Collectors.toList()))
                .transport(bus::setContext).services(services).serverAddress(bus.getLocalMember().getAddress()));
    }

    public static Stream<ClusterBuilder> createFixedFakeCluster(int i, ImmutableList<ServiceListBuilder.Constructor> services) {
        return createFixedFakeCluster(IntStream.range(0, i), services);
    }

    public static void waitForDiscovery(Cluster cluster, int numberOfInstances) throws InterruptedException {
        int i = numberOfInstances - (cluster.getMembers().size() - 1);
        if (i <= 1)
            return;

        CountDownLatch countDownLatch = new CountDownLatch(i);

        cluster.addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(Member member) {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
    }

    public static void waitForNodeToLeave(Cluster cluster, int numberOfInstances) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(numberOfInstances);

        cluster.addMembershipListener(new MembershipListener() {
            @Override
            public void memberRemoved(Member member) {
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
    }
}
