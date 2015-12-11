
package org.xbib.cluster;

import org.xbib.cluster.service.joiner.JoinerService;

import java.time.Duration;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


class DelayedJoinerService implements JoinerService {
    private final Duration duration;
    private final ArrayBlockingQueue<Member> members;
    private final ScheduledExecutorService executor;


    public DelayedJoinerService(List<Member> addedMembers, Duration duration) {
        members = new ArrayBlockingQueue<>(addedMembers.size());
        members.addAll(addedMembers);
        executor = Executors.newSingleThreadScheduledExecutor();
        this.duration = duration;
    }

    @Override
    public void onStart(ClusterMembership membership) {
        executor.schedule(new TimerTask() {
            @Override
            public void run() {
                Member poll = members.poll();
                if (poll != null) {
                    membership.addMember(poll);
                    executor.schedule(this, duration.toMillis(), TimeUnit.MILLISECONDS);
                }
            }
        }, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void onClose() {
    }
}
