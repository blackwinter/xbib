package org.xbib.cluster.service.ringmap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.xbib.cluster.Cluster;
import org.xbib.cluster.ClusterBuilder;
import org.xbib.cluster.service.ServiceListBuilder;
import org.xbib.cluster.service.crdt.counter.GCounterService;
import com.google.common.collect.ImmutableList;
import org.xbib.cluster.Member;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConsoleRingMapTest {

    private final static Logger logger = LogManager.getLogger(ConsoleRingMapTest.class);

    @Test
    public void testMap() {
        try {
            ImmutableList<ServiceListBuilder.Constructor> services = new ServiceListBuilder()
                    .add("map", bus -> new RingMap<String, Long>(bus, GCounterService::merge, 2))
                    .build();

            Cluster cluster0 = new ClusterBuilder()
                    .serverAddress("127.0.0.1", 6001)
                    .services(services)
                    .start();
            logger.info("cluster 0 started");

            Cluster cluster1 = new ClusterBuilder()
                    .serverAddress("127.0.0.1", 6002)
                    .services(services)
                    .start();
            logger.info("cluster 1 started");

            RingMap ringMap0 = cluster0.getService("map");
            RingMap ringMap1 = cluster1.getService("map");

            ArrayList<Cluster> instances = new ArrayList<>(2);
            instances.add(cluster0);
            instances.add(cluster1);

            Executors.newScheduledThreadPool(1)
                    .scheduleAtFixedRate(() -> printMapStats(instances), 1, 1, TimeUnit.SECONDS);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            CountDownLatch countDownLatch = new CountDownLatch(2);

            executorService.execute(() -> {
                for (int i = 0; i < 500000; i++) {
                    ringMap0.put("deneme" + i, 4);
                }
                countDownLatch.countDown();
            });
            executorService.execute(() -> {
                for (int i = 0; i < 500000; i++) {
                    ringMap1.put("deneme" + i, 4);
                }
                countDownLatch.countDown();
            });

            countDownLatch.await();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    void printMapStats(Collection<Cluster> list) {
        System.out.printf("| Server            | Range          | Map Size |%n");
        System.out.format("+-------------------+----------------+----------+%n");
        for (Cluster cluster : list) {
            RingMap service = cluster.getService("map");
            double totalRingRange = service.getRing().getTotalRingRange(cluster.getLocalMember());
            System.out.format("| %-17s | %-14f | %-8d |%n", cluster.getLocalMember().getAddress(), totalRingRange, service.getLocalSize());
        }
        System.out.format("+-------------------+----------------+----------+%n");

        RingMap map = list.iterator().next().getService("map");
        Map<ConsistentHashRing.TokenRange, List<Member>> buckets = map.getRing().getBuckets();
        buckets.forEach((token, members) -> {
            double percentage = (Math.abs(token.end-token.start)/2)/(Long.MAX_VALUE/100.0);
            int i1 = ((Double) percentage).intValue()-1;
            System.out.print(token.id);
            for (int i2 = 0; i2 < i1; i2++)
                System.out.print("-");
        });
        System.out.println();
        System.out.println();
    }
}
