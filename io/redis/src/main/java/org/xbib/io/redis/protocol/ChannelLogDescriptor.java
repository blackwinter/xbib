package org.xbib.io.redis.protocol;

import io.netty.channel.Channel;

/**
 */
class ChannelLogDescriptor {

    static String logDescriptor(Channel channel) {

        if (channel == null) {
            return "unknown";
        }

        StringBuilder buffer = new StringBuilder(64);

        if (channel.localAddress() != null) {
            buffer.append(channel.localAddress()).append(" -> ");
        }
        if (channel.remoteAddress() != null) {
            buffer.append(channel.remoteAddress());
        }

        if (!channel.isActive()) {
            buffer.append(" (inactive)");
        }

        return buffer.toString();
    }
}
