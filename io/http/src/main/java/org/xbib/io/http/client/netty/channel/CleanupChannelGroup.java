package org.xbib.io.http.client.netty.channel;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Extension of {@link DefaultChannelGroup} that's used mainly as a cleanup container, where {@link #close()} is only
 * supposed to be called once.
 */
public class CleanupChannelGroup extends DefaultChannelGroup {

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public CleanupChannelGroup() {
        super(GlobalEventExecutor.INSTANCE);
    }

    public CleanupChannelGroup(String name) {
        super(name, GlobalEventExecutor.INSTANCE);
    }

    @Override
    public ChannelGroupFuture close() {
        this.lock.writeLock().lock();
        try {
            if (!this.closed.getAndSet(true)) {
                // First time close() is called.
                return super.close();
            } else {
                // FIXME DefaultChannelGroupFuture is package protected
                // Collection<ChannelFuture> futures = new ArrayList<>();
                // logger.debug("CleanupChannelGroup already closed");
                // return new DefaultChannelGroupFuture(ChannelGroup.class.cast(this), futures,
                // GlobalEventExecutor.INSTANCE);
                throw new UnsupportedOperationException("CleanupChannelGroup already closed");
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public boolean add(Channel channel) {
        // Synchronization must occur to avoid add() and close() overlap (thus potentially leaving one channel open).
        // This could also be done by synchronizing the method itself but using a read lock here (rather than a
        // synchronized() block) allows multiple concurrent calls to add().
        this.lock.readLock().lock();
        try {
            if (this.closed.get()) {
                // Immediately close channel, as close() was already called.
                Channels.silentlyCloseChannel(channel);
                return false;
            }

            return super.add(channel);
        } finally {
            this.lock.readLock().unlock();
        }
    }
}
