package org.xbib.cluster.network.multicast;

import com.esotericsoftware.kryo.KryoException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.Cluster;
import org.xbib.cluster.Member;
import org.xbib.cluster.serialize.kryo.Serializer;

import java.net.ConnectException;

public class MulticastChannelAdapter extends ChannelInboundHandlerAdapter {

    private final static Logger logger = LogManager.getLogger(MulticastChannelAdapter.class);

    private Cluster cluster;

    public MulticastChannelAdapter(Cluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            DatagramPacket msg1 = (DatagramPacket) msg;
            ByteBuf content = msg1.content();
            Object o;
            try {
                o = Serializer.toObject(content, content.readShort());
            } catch (KryoException e) {
                logger.warn("Kryo couldn't deserialize packet", e);
                return;
            } catch (Exception e) {
                logger.warn("Couldn't deserialize packet", e);
                return;
            }
            if (o instanceof MulticastPacket) {
                MulticastPacket req = (MulticastPacket) o;
                Member sender = req.sender;
                if (sender == null || sender.equals(cluster.getLocalMember())) {
                    return;
                }
                req.data.run(cluster.getServices().get(0), new MulticastOperationContext(sender, 0));
            } else {
                logger.warn("multicast server in member {}, couldn't handle package: {}", cluster.getLocalMember(), msg);
            }
        } catch (Exception e) {
            logger.error("an error occurred while processing data in multicast server", e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ConnectException) {
            /* most likely the server is down but we don't do anything here
            since the heartbeat mechanism automatically removes the member from the cluster */
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
