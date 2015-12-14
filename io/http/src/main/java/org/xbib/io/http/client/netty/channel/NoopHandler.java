package org.xbib.io.http.client.netty.channel;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;

/**
 * A noop handler that just serves as a pinned reference for adding and removing handlers in the pipeline
 */
@Sharable
public class NoopHandler extends ChannelHandlerAdapter {
}
