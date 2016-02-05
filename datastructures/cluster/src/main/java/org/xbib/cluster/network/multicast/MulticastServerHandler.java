package org.xbib.cluster.network.multicast;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.Cluster;
import org.xbib.cluster.service.InternalService;
import org.xbib.cluster.Member;
import org.xbib.cluster.operation.Operation;
import org.xbib.cluster.serialize.kryo.Serializer;

import java.net.InetSocketAddress;
import java.net.NetworkInterface;

public class MulticastServerHandler {
    private final static Logger logger = LogManager.getLogger(MulticastServerHandler.class);
    static NetworkInterface multicastInterface = NetUtil.LOOPBACK_IF;
    private final Member localMember;
    InetSocketAddress address;
    Bootstrap handler;
    private NioDatagramChannel server;
    private boolean joinGroup;

    public MulticastServerHandler(Cluster cluster, InetSocketAddress address) throws InterruptedException {
        this.address = address;

        handler = new Bootstrap()
                .channelFactory(() -> new NioDatagramChannel(InternetProtocolFamily.IPv4))
                .localAddress(address)
                .group(new NioEventLoopGroup())
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.IP_MULTICAST_IF, multicastInterface)
                .option(ChannelOption.AUTO_READ, false)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    public void initChannel(NioDatagramChannel ch) throws Exception {
                        ch.pipeline().addLast(new MulticastChannelAdapter(cluster));
                    }
                });
        localMember = cluster.getLocalMember();
    }

    public MulticastServerHandler start() throws InterruptedException {
        server = (NioDatagramChannel) handler.bind(address.getPort()).sync().channel();
        server.joinGroup(address, multicastInterface).sync();
        // why netty doesn't have a get method for group memberships?
        joinGroup = true;
        return this;
    }

    public void setAutoRead(boolean bool) {
        server.config().setAutoRead(bool);
    }

    public void sendMulticast(Operation req) {
        ByteBuf buf = Unpooled.wrappedBuffer(Serializer.toByteBuf(new MulticastPacket(req, localMember)));
        server.writeAndFlush(new DatagramPacket(buf, address, localMember.getAddress()));
    }

    public void send(InetSocketAddress address, Operation<InternalService> req) {
        ByteBuf buf = Unpooled.wrappedBuffer(Serializer.toByteBuf(new MulticastPacket(req, localMember)));
        server.writeAndFlush(new DatagramPacket(buf, address, localMember.getAddress()));
    }

    public void close() throws InterruptedException {
        server.leaveGroup(address, NetUtil.LOOPBACK_IF).sync();
        server.close().sync();
    }

    public void setJoinGroup(boolean joinGroup) {
        try {
            if (this.joinGroup && !joinGroup) {
                server.leaveGroup(address, multicastInterface).sync();
            } else if (!this.joinGroup && joinGroup) {
                server.joinGroup(address, multicastInterface).sync();
            }
        } catch (InterruptedException e) {
            logger.error("couldn't change multicast server state.", e);
        }
        this.joinGroup = joinGroup;
    }

}
