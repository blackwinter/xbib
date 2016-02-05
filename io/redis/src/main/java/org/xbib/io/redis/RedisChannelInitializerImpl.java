package org.xbib.io.redis;

import io.netty.channel.ChannelDuplexHandler;

/**
 * Channel initializer to set up the transport before a Redis connection can be used.
 * This class is part of the internal API.
 */
public abstract class RedisChannelInitializerImpl extends ChannelDuplexHandler implements RedisChannelInitializer {
}
