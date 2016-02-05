package org.xbib.io.http.client.channel;

import io.netty.channel.Channel;

public interface ChannelPool {

    /**
     * Add a channel to the pool
     *
     * @param channel      an I/O channel
     * @param partitionKey a key used to retrieve the cached channel
     * @return true if added.
     */
    boolean offer(Channel channel, Object partitionKey);

    /**
     * Remove the channel associated with the uri.
     *
     * @param partitionKey the partition used when invoking offer
     * @return the channel associated with the uri
     */
    Channel poll(Object partitionKey);

    /**
     * Remove all channels from the cache. A channel might have been associated
     * with several uri.
     *
     * @param channel a channel
     * @return the true if the channel has been removed
     */
    boolean removeAll(Channel channel);

    /**
     * Return true if a channel can be cached. A implementation can decide based
     * on some rules to allow caching Calling this method is equivalent of
     * checking the returned value of {@link ChannelPool#offer(Channel, Object)}
     *
     * @return true if a channel can be cached.
     */
    boolean isOpen();

    /**
     * Destroy all channels that has been cached by this instance.
     */
    void destroy();

    /**
     * Flush a partition
     *
     * @param partitionKey the partition
     */
    void flushPartition(Object partitionKey);

    /**
     * Flush partitions based on a selector
     *
     * @param selector the selector
     */
    void flushPartitions(ChannelPoolPartitionSelector selector);
}
