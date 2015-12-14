package org.xbib.io.http.client.netty.request.body;

import io.netty.channel.Channel;
import org.xbib.io.http.client.netty.NettyResponseFuture;

import java.io.IOException;

public interface NettyBody {

    long getContentLength();

    String getContentType();

    void write(Channel channel, NettyResponseFuture<?> future) throws IOException;
}
