package org.xbib.io.redis;

import io.netty.channel.ChannelHandler;

import java.util.concurrent.Future;

/**
 * Channel initializer to set up the transport before a Redis connection can be used. This is part of the internal API.
 * This class is part of the internal API.
 */
public interface RedisChannelInitializer extends ChannelHandler {

    /**
     * @return future to synchronize channel initialization. Returns a new future for every reconnect.
     */
    Future<Boolean> channelInitialized();
}
