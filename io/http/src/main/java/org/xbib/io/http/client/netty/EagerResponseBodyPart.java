package org.xbib.io.http.client.netty;

import io.netty.buffer.ByteBuf;
import org.xbib.io.http.client.HttpResponseBodyPart;

import java.nio.ByteBuffer;

import static org.xbib.io.http.client.util.ByteBufUtils.byteBuf2Bytes;

/**
 * A callback class used when an HTTP response body is received.
 * Bytes are eagerly fetched from the ByteBuf
 */
public class EagerResponseBodyPart extends HttpResponseBodyPart {

    private final byte[] bytes;

    public EagerResponseBodyPart(ByteBuf buf, boolean last) {
        super(last);
        bytes = byteBuf2Bytes(buf);
    }

    /**
     * Return the response body's part bytes received.
     *
     * @return the response body's part bytes received.
     */
    @Override
    public byte[] getBodyPartBytes() {
        return bytes;
    }

    @Override
    public int length() {
        return bytes.length;
    }

    @Override
    public ByteBuffer getBodyByteBuffer() {
        return ByteBuffer.wrap(bytes);
    }
}
