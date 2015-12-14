package org.xbib.io.http.client.request.body.multipart.part;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.xbib.io.http.client.request.body.multipart.ByteArrayPart;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

public class ByteArrayMultipartPart extends MultipartPart<ByteArrayPart> {

    // lazy
    private ByteBuf contentBuffer;

    public ByteArrayMultipartPart(ByteArrayPart part, byte[] boundary) {
        super(part, boundary);
        contentBuffer = Unpooled.wrappedBuffer(part.getBytes());
    }

    @Override
    protected long getContentLength() {
        return part.getBytes().length;
    }

    @Override
    protected long transferContentTo(ByteBuf target) throws IOException {
        return transfer(lazyLoadContentBuffer(), target, MultipartState.POST_CONTENT);
    }

    @Override
    protected long transferContentTo(WritableByteChannel target) throws IOException {
        return transfer(lazyLoadContentBuffer(), target, MultipartState.POST_CONTENT);
    }

    private ByteBuf lazyLoadContentBuffer() {
        if (contentBuffer == null) {
            contentBuffer = Unpooled.wrappedBuffer(part.getBytes());
        }
        return contentBuffer;
    }

    @Override
    public void close() {
        super.close();
        contentBuffer.release();
    }
}
