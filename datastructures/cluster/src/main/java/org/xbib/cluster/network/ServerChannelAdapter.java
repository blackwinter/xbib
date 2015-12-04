package org.xbib.cluster.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.service.Service;
import org.xbib.cluster.RemoteOperationContext;
import org.xbib.cluster.Request;
import org.xbib.cluster.transport.ThrowableNioEventLoopGroup;

import java.util.List;

public class ServerChannelAdapter extends ChannelInboundHandlerAdapter {
    final static Logger logger = LogManager.getLogger(PacketDecoder.class);

    List<Service> services;

    ThrowableNioEventLoopGroup eventExecutors;

    public ServerChannelAdapter(List<Service> services, ThrowableNioEventLoopGroup executor) {
        this.services = services;
        eventExecutors = executor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.trace("server {} got message {}", ctx.channel().localAddress(), msg);
        Packet read = (Packet) msg;
        Object o = read.getData();
        RemoteOperationContext ctx1 = new RemoteOperationContext(ctx, read.service, read.sequence);
        Service service = services.get(read.service);
        if (o instanceof Request) {
            service.handle(eventExecutors, ctx1, (Request) o);
        } else {
            service.handle(eventExecutors, ctx1, o);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Dropped unsupported package.", cause);
        ctx.close();
    }
}