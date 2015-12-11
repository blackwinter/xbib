package org.xbib.cluster.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.service.Service;
import org.xbib.cluster.transport.ThrowableNioEventLoopGroup;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

public class TCPServerHandler {
    private final static Logger logger = LogManager.getLogger(TCPServerHandler.class);
    private final Channel server;

    public TCPServerHandler(EventLoopGroup bossGroup, EventLoopGroup workerGroup, ThrowableNioEventLoopGroup eventExecutor, List<Service> services, InetSocketAddress serverAddress) throws InterruptedException {
        ChannelFuture bind = new ServerBootstrap()
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.AUTO_READ, false)
                .option(ChannelOption.SO_BACKLOG, 100)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
                        p.addLast("packetDecoder", new PacketDecoder());
                        p.addLast("frameEncoder", new LengthFieldPrepender(Integer.BYTES));
                        p.addLast("packetEncoder", new PacketEncoder());
                        p.addLast(new ServerChannelAdapter(services, eventExecutor));
                    }
                }).bind(serverAddress);

        server = bind.sync()
                .addListener(future -> {
                    if (!future.isSuccess()) {
                        logger.error("Failed to bind {}", bind.channel().localAddress());
                    }
                }).awaitUninterruptibly().channel();
    }

    public ChannelFuture waitForClose() throws InterruptedException {
        return server.closeFuture().sync();
    }

    public SocketAddress localAddress() {
        return server.localAddress();
    }

    public void setAutoRead(boolean b) {
        server.config().setAutoRead(b);
    }

    public void close() throws InterruptedException {
        server.close().sync();
    }
}
