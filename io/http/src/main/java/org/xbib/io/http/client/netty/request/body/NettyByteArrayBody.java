package org.xbib.io.http.client.netty.request.body;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class NettyByteArrayBody extends NettyDirectBody {

    private final byte[] bytes;
    private final String contentType;

    public NettyByteArrayBody(byte[] bytes) {
        this(bytes, null);
    }

    public NettyByteArrayBody(byte[] bytes, String contentType) {
        this.bytes = bytes;
        this.contentType = contentType;
    }

    @Override
    public long getContentLength() {
        return bytes.length;
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
