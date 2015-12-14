package org.xbib.io.http.client.netty.request.body;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.xbib.io.http.client.netty.NettyResponseFuture;

import java.io.IOException;

public abstract class NettyDirectBody implements NettyBody {

    public abstract ByteBuf byteBuf();

    @Override
    public void write(Channel channel, NettyResponseFuture<?> future) throws IOException {
        throw new UnsupportedOperationException("This kind of body is supposed to be writen directly");
    }
}
