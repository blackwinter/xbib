package org.xbib.io.http.client.channel;

import io.netty.channel.Channel;

public enum NoopChannelPool implements ChannelPool {

    INSTANCE;

    @Override
    public boolean offer(Channel channel, Object partitionKey) {
        return false;
    }

    @Override
    public Channel poll(Object partitionKey) {
        return null;
    }

    @Override
    public boolean removeAll(Channel channel) {
        return false;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void flushPartition(Object partitionKey) {
    }

    @Override
    public void flushPartitions(ChannelPoolPartitionSelector selector) {
    }
}
