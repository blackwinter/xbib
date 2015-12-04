package org.xbib.cluster.transport;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.Member;
import org.xbib.cluster.MemberChannel;
import org.xbib.cluster.network.ClientChannelAdapter;
import org.xbib.cluster.network.TCPServerHandler;
import org.xbib.cluster.service.Service;
import org.xbib.cluster.network.Packet;
import org.xbib.cluster.network.PacketDecoder;
import org.xbib.cluster.network.PacketEncoder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class NettyTransport implements Transport {
    private final static Logger logger = LogManager.getLogger(NettyTransport.class);

    // IO thread for TCP and UDP connections
    final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    // Processor thread pool that de-serializing/serializing incoming/outgoing packets
    final EventLoopGroup workerGroup = new NioEventLoopGroup(4);

    private final Cache<Integer, CompletableFuture<Object>> messageHandlers = CacheBuilder.newBuilder()
            .expireAfterWrite(200, TimeUnit.SECONDS)
            .removalListener(this::removalListener).build();

    private final TCPServerHandler server;

    public NettyTransport(ThrowableNioEventLoopGroup requestExecutor, List<Service> services, Member localMember) {
        try {
            this.server = new TCPServerHandler(bossGroup, workerGroup, requestExecutor, services, localMember.getAddress());
        } catch (InterruptedException e) {
            throw new IllegalStateException("Failed to bind TCP " + localMember.getAddress());
        }
    }

    private void removalListener(RemovalNotification<Integer, CompletableFuture<Object>> notification) {
        if (!notification.getCause().equals(RemovalCause.EXPLICIT) && notification.getValue() != null) {
            notification.getValue().completeExceptionally(new TimeoutException());
        }
    }

    @Override
    public MemberChannel connect(Member member) throws InterruptedException {
        Bootstrap b = new Bootstrap();
        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
                        p.addLast("packetDecoder", new PacketDecoder());
                        p.addLast("frameEncoder", new LengthFieldPrepender(4));
                        p.addLast("packetEncoder", new PacketEncoder());
                        p.addLast("server", new ClientChannelAdapter(messageHandlers));
                    }
                });
        ChannelFuture f = b.connect(member.getAddress()).sync()
                .addListener(future -> {
                    if (!future.isSuccess()) {
                        logger.error("Failed to connect server {}", member.getAddress());
                    }
                }).sync();
        return new NettyChannel(f.channel());
    }

    @Override
    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        try {
            server.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() {
        server.setAutoRead(true);
    }


    public class NettyChannel implements MemberChannel {
        private final Channel channel;

        public NettyChannel(Channel channel) {
            this.channel = channel;
        }

        public CompletableFuture ask(Packet message) {
            CompletableFuture future = new CompletableFuture<>();
            messageHandlers.put(message.sequence, future);
            channel.writeAndFlush(message);
            return future;
        }

        @Override
        public void send(Packet message) {
            channel.writeAndFlush(message);
        }

        @Override
        public void close() throws InterruptedException {
            channel.close().sync();
        }
    }

}
