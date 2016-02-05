package org.xbib.cluster.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.serialize.kryo.KryoFactory;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    final static Logger logger = LogManager.getLogger(PacketDecoder.class);
    private final Kryo kryo;

    public PacketDecoder() {
        this.kryo = KryoFactory.getKryoInstance();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        try {
            if (!buffer.isReadable()) {
                return;
            }
            int packetNum = buffer.readInt();
            int serviceId = buffer.readUnsignedShort();
            Object o;
            try {
                o = kryo.readClassAndObject(new ByteBufInput(buffer));
            } catch (KryoException e) {
                logger.warn("Couldn't deserialize object", e);
                return;
            }
            Packet e = new Packet(packetNum, o, serviceId);
            out.add(e);
        } catch (Exception e) {
            logger.error("error while handling package", e);
        }
    }
}
