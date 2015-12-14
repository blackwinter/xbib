package org.xbib.io.http.client.netty.request.body;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;

public class NettyCompositeByteArrayBody extends NettyDirectBody {

    private final byte[][] bytes;
    private final String contentType;
    private final long contentLength;

    public NettyCompositeByteArrayBody(List<byte[]> bytes) {
        this(bytes, null);
    }

    public NettyCompositeByteArrayBody(List<byte[]> bytes, String contentType) {
        this.bytes = new byte[bytes.size()][];
        bytes.toArray(this.bytes);
        this.contentType = contentType;
        long l = 0;
        for (byte[] b : bytes) {
            l += b.length;
        }
        contentLength = l;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ByteBuf byteBuf() {
        return Unpooled.wrappedBuffer(bytes);
    }
}
