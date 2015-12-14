package org.xbib.io.http.client.channel;

public interface ChannelPoolPartitionSelector {

    boolean select(Object partitionKey);
}
