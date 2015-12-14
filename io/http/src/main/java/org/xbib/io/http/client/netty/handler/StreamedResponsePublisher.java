package org.xbib.io.http.client.netty.handler;

import com.typesafe.netty.HandlerPublisher;
import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.netty.NettyResponseFuture;
import org.xbib.io.http.client.netty.channel.ChannelManager;

public class StreamedResponsePublisher extends HandlerPublisher<HttpResponseBodyPart> {

    private final ChannelManager channelManager;
    private final NettyResponseFuture<?> future;
    private final Channel channel;

    public StreamedResponsePublisher(EventExecutor executor, ChannelManager channelManager, NettyResponseFuture<?> future, Channel channel) {
        super(executor, HttpResponseBodyPart.class);
        this.channelManager = channelManager;
        this.future = future;
        this.channel = channel;
    }

    @Override
    protected void cancelled() {

        // The subscriber cancelled early, we need to drain the remaining elements from the stream
        channelManager.drainChannelAndOffer(channel, future);
        channel.pipeline().remove(StreamedResponsePublisher.class);

        try {
            future.done();
        } catch (Exception t) {
            // Never propagate exception once we know we are done.
        }
    }

    NettyResponseFuture<?> future() {
        return future;
    }
}
