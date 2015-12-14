package org.xbib.io.http.client.netty.channel;

import io.netty.channel.Channel;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.channel.ChannelPool;
import org.xbib.io.http.client.channel.ChannelPoolPartitionSelector;
import org.xbib.io.http.client.netty.NettyResponseFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.xbib.io.http.client.util.DateUtils.millisTime;

/**
 * A simple implementation of
 * {@link ChannelPool} based on a
 * {@link ConcurrentHashMap}
 */
public final class DefaultChannelPool implements ChannelPool {

    private final ConcurrentHashMap<Object, ConcurrentLinkedQueue<IdleChannel>> partitions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ChannelCreation> channelId2Creation = new ConcurrentHashMap<>();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Timer nettyTimer;
    private final int maxConnectionTtl;
    private final boolean maxConnectionTtlDisabled;
    private final long maxIdleTime;
    private final boolean maxIdleTimeDisabled;
    private final long cleanerPeriod;

    public DefaultChannelPool(AsyncHttpClientConfig config, Timer hashedWheelTimer) {
        this(config.getPooledConnectionIdleTimeout(),//
                config.getConnectionTtl(),//
                hashedWheelTimer);
    }

    public DefaultChannelPool(long maxIdleTime,//
                              int maxConnectionTtl,//
                              Timer nettyTimer) {
        this.maxIdleTime = maxIdleTime;
        this.maxConnectionTtl = maxConnectionTtl;
        maxConnectionTtlDisabled = maxConnectionTtl <= 0;
        this.nettyTimer = nettyTimer;
        maxIdleTimeDisabled = maxIdleTime <= 0;

        cleanerPeriod = Math.min(maxConnectionTtlDisabled ? Long.MAX_VALUE : maxConnectionTtl, maxIdleTimeDisabled ? Long.MAX_VALUE : maxIdleTime);

        if (!maxConnectionTtlDisabled || !maxIdleTimeDisabled) {
            scheduleNewIdleChannelDetector(new IdleChannelDetector());
        }
    }

    private int channelId(Channel channel) {
        return channel.hashCode();
    }

    private void scheduleNewIdleChannelDetector(TimerTask task) {
        nettyTimer.newTimeout(task, cleanerPeriod, TimeUnit.MILLISECONDS);
    }

    private boolean isTtlExpired(Channel channel, long now) {
        if (maxConnectionTtlDisabled) {
            return false;
        }

        ChannelCreation creation = channelId2Creation.get(channelId(channel));
        return creation != null && now - creation.creationTime >= maxConnectionTtl;
    }

    private boolean isRemotelyClosed(Channel channel) {
        return !channel.isActive();
    }

    /**
     * {@inheritDoc}
     */
    public boolean offer(Channel channel, Object partitionKey) {
        if (isClosed.get()) {
            return false;
        }

        long now = millisTime();

        if (isTtlExpired(channel, now)) {
            return false;
        }

        boolean added = partitions.computeIfAbsent(partitionKey, pk -> new ConcurrentLinkedQueue<>()).add(new IdleChannel(channel, now));
        if (added) {
            channelId2Creation.putIfAbsent(channelId(channel), new ChannelCreation(now, partitionKey));
        }

        return added;
    }

    /**
     * {@inheritDoc}
     */
    public Channel poll(Object partitionKey) {

        IdleChannel idleChannel = null;
        ConcurrentLinkedQueue<IdleChannel> partition = partitions.get(partitionKey);
        if (partition != null) {
            while (idleChannel == null) {
                idleChannel = partition.poll();

                if (idleChannel == null)
                // pool is empty
                {
                    break;
                } else if (isRemotelyClosed(idleChannel.channel)) {
                    idleChannel = null;
                }
            }
        }
        return idleChannel != null ? idleChannel.channel : null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeAll(Channel channel) {
        ChannelCreation creation = channelId2Creation.remove(channelId(channel));
        return !isClosed.get() && creation != null && partitions.get(creation.partitionKey).remove(channel);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOpen() {
        return !isClosed.get();
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() {
        if (isClosed.getAndSet(true)) {
            return;
        }

        for (ConcurrentLinkedQueue<IdleChannel> partition : partitions.values()) {
            for (IdleChannel idleChannel : partition) {
                close(idleChannel.channel);
            }
        }

        partitions.clear();
        channelId2Creation.clear();
    }

    private void close(Channel channel) {
        // FIXME pity to have to do this here
        Channels.setDiscard(channel);
        channelId2Creation.remove(channelId(channel));
        Channels.silentlyCloseChannel(channel);
    }

    private void flushPartition(Object partitionKey, ConcurrentLinkedQueue<IdleChannel> partition) {
        if (partition != null) {
            partitions.remove(partitionKey);
            for (IdleChannel idleChannel : partition) {
                close(idleChannel.channel);
            }
        }
    }

    @Override
    public void flushPartition(Object partitionKey) {
        flushPartition(partitionKey, partitions.get(partitionKey));
    }

    @Override
    public void flushPartitions(ChannelPoolPartitionSelector selector) {

        for (Map.Entry<Object, ConcurrentLinkedQueue<IdleChannel>> partitionsEntry : partitions.entrySet()) {
            Object partitionKey = partitionsEntry.getKey();
            if (selector.select(partitionKey)) {
                flushPartition(partitionKey, partitionsEntry.getValue());
            }
        }
    }

    private static final class ChannelCreation {
        final long creationTime;
        final Object partitionKey;

        ChannelCreation(long creationTime, Object partitionKey) {
            this.creationTime = creationTime;
            this.partitionKey = partitionKey;
        }
    }

    private static final class IdleChannel {
        final Channel channel;
        final long start;

        IdleChannel(Channel channel, long start) {
            this.channel = channel;
            this.start = start;
        }

        @Override
        // only depends on channel
        public boolean equals(Object o) {
            return this == o || (o instanceof IdleChannel && channel.equals(IdleChannel.class.cast(o).channel));
        }

        @Override
        public int hashCode() {
            return channel.hashCode();
        }
    }

    private final class IdleChannelDetector implements TimerTask {

        private boolean isIdleTimeoutExpired(IdleChannel idleChannel, long now) {
            return !maxIdleTimeDisabled && now - idleChannel.start >= maxIdleTime;
        }

        private List<IdleChannel> expiredChannels(ConcurrentLinkedQueue<IdleChannel> partition, long now) {
            // lazy create
            List<IdleChannel> idleTimeoutChannels = null;
            for (IdleChannel idleChannel : partition) {
                if (isTtlExpired(idleChannel.channel, now) || isIdleTimeoutExpired(idleChannel, now) || isRemotelyClosed(idleChannel.channel)) {
                    if (idleTimeoutChannels == null) {
                        idleTimeoutChannels = new ArrayList<>();
                    }
                    idleTimeoutChannels.add(idleChannel);
                }
            }

            return idleTimeoutChannels != null ? idleTimeoutChannels : Collections.<IdleChannel>emptyList();
        }

        private boolean isChannelCloseable(Channel channel) {
            Object attribute = Channels.getAttribute(channel);
            if (attribute instanceof NettyResponseFuture) {
                NettyResponseFuture<?> future = (NettyResponseFuture<?>) attribute;
                if (!future.isDone()) {
                    return false;
                }
            }
            return true;
        }

        private List<IdleChannel> closeChannels(List<IdleChannel> candidates) {

            // lazy create, only if we have a non-closeable channel
            List<IdleChannel> closedChannels = null;
            for (int i = 0; i < candidates.size(); i++) {
                IdleChannel idleChannel = candidates.get(i);
                if (isChannelCloseable(idleChannel.channel)) {
                    close(idleChannel.channel);
                    if (closedChannels != null) {
                        closedChannels.add(idleChannel);
                    }

                } else if (closedChannels == null) {
                    // first non closeable to be skipped, copy all
                    // previously skipped closeable channels
                    closedChannels = new ArrayList<>(candidates.size());
                    for (int j = 0; j < i; j++) {
                        closedChannels.add(candidates.get(j));
                    }
                }
            }

            return closedChannels != null ? closedChannels : candidates;
        }

        public void run(Timeout timeout) throws Exception {

            if (isClosed.get()) {
                return;
            }

            try {
                long start = millisTime();
                int closedCount = 0;
                int totalCount = 0;

                for (ConcurrentLinkedQueue<IdleChannel> partition : partitions.values()) {

                    // store in intermediate unsynchronized lists to minimize
                    // the impact on the ConcurrentLinkedQueue
                    List<IdleChannel> closedChannels = closeChannels(expiredChannels(partition, start));

                    if (!closedChannels.isEmpty()) {
                        for (IdleChannel closedChannel : closedChannels) {
                            channelId2Creation.remove(channelId(closedChannel.channel));
                        }

                        partition.removeAll(closedChannels);
                        closedCount += closedChannels.size();
                    }
                }

                long duration = millisTime() - start;
            } catch (Throwable t) {
                // ignore
            }

            scheduleNewIdleChannelDetector(timeout.task());
        }
    }
}
