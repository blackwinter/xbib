package org.xbib.cluster.network;

import com.esotericsoftware.kryo.Kryo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.xbib.cluster.serialize.kryo.KryoFactory;


public class PacketEncoder extends MessageToByteEncoder<Packet> {
    private final Kryo kryo;

    public PacketEncoder() {
        this.kryo = KryoFactory.getKryoInstance();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
        out.writeInt(msg.sequence);
        out.writeShort(msg.service);
        kryo.writeClassAndObject(new ByteBufOutput(out), msg.data);
    }
}