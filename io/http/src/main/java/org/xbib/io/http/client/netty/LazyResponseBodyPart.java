package org.xbib.io.http.client.netty;

import io.netty.buffer.ByteBuf;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.util.ByteBufUtils;

import java.nio.ByteBuffer;

/**
 * A callback class used when an HTTP response body is received.
 */
public class LazyResponseBodyPart extends HttpResponseBodyPart {

    private final ByteBuf buf;

    public LazyResponseBodyPart(ByteBuf buf, boolean last) {
        super(last);
        this.buf = buf;
    }

    public ByteBuf getBuf() {
        return buf;
    }

    @Override
    public int length() {
        return buf.readableBytes();
    }

    /**
     * Return the response body's part bytes received.
     *
     * @return the response body's part bytes received.
     */
    @Override
    public byte[] getBodyPartBytes() {
        return ByteBufUtils.byteBuf2Bytes(buf.duplicate());
    }

    @Override
    public ByteBuffer getBodyByteBuffer() {
        return buf.nioBuffer();
    }
}
