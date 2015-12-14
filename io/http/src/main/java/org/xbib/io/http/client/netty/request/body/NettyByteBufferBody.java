package org.xbib.io.http.client.netty.request.body;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

public class NettyByteBufferBody extends NettyDirectBody {

    private final ByteBuffer bb;
    private final String contentType;
    private final long length;

    public NettyByteBufferBody(ByteBuffer bb) {
        this(bb, null);
    }

    public NettyByteBufferBody(ByteBuffer bb, String contentType) {
        this.bb = bb;
        length = bb.remaining();
        bb.mark();
        this.contentType = contentType;
    }

    @Override
    public long getContentLength() {
        return length;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ByteBuf byteBuf() {
        // for retry
        bb.reset();
        return Unpooled.wrappedBuffer(bb);
    }
}
