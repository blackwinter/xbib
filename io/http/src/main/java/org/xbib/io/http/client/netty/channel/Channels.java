package org.xbib.io.http.client.netty.channel;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.xbib.io.http.client.netty.DiscardEvent;

public class Channels {

    private static final AttributeKey<Object> DEFAULT_ATTRIBUTE = AttributeKey.valueOf("default");

    public static Object getAttribute(Channel channel) {
        Attribute<Object> attr = channel.attr(DEFAULT_ATTRIBUTE);
        return attr != null ? attr.get() : null;
    }

    public static void setAttribute(Channel channel, Object o) {
        channel.attr(DEFAULT_ATTRIBUTE).set(o);
    }

    public static void setDiscard(Channel channel) {
        setAttribute(channel, DiscardEvent.INSTANCE);
    }

    public static boolean isChannelValid(Channel channel) {
        return channel != null && channel.isActive();
    }

    public static void silentlyCloseChannel(Channel channel) {
        try {
            if (channel != null && channel.isActive()) {
                channel.close();
            }
        } catch (Throwable t) {
            //
        }
    }
}
