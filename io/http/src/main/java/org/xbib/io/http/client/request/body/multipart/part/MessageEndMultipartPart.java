package org.xbib.io.http.client.request.body.multipart.part;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.xbib.io.http.client.request.body.multipart.FileLikePart;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

import static org.xbib.io.http.client.request.body.multipart.Part.CRLF_BYTES;
import static org.xbib.io.http.client.request.body.multipart.Part.EXTRA_BYTES;

public class MessageEndMultipartPart extends MultipartPart<FileLikePart> {

    // lazy
    private ByteBuf contentBuffer;

    public MessageEndMultipartPart(byte[] boundary) {
        super(null, boundary);
        state = MultipartState.PRE_CONTENT;
    }

    @Override
    public long transferTo(ByteBuf target) throws IOException {
        return transfer(lazyLoadContentBuffer(), target, MultipartState.DONE);
    }

    @Override
    public long transferTo(WritableByteChannel target) throws IOException {
        slowTarget = false;
        return transfer(lazyLoadContentBuffer(), target, MultipartState.DONE);
    }

    private ByteBuf lazyLoadContentBuffer() {
        if (contentBuffer == null) {
            contentBuffer = ByteBufAllocator.DEFAULT.buffer((int) getContentLength());
            contentBuffer.writeBytes(EXTRA_BYTES).writeBytes(boundary).writeBytes(EXTRA_BYTES).writeBytes(CRLF_BYTES);
        }
        return contentBuffer;
    }

    @Override
    protected int computePreContentLength() {
        return 0;
    }

    @Override
    protected ByteBuf computePreContentBytes(int preContentLength) {
        return Unpooled.EMPTY_BUFFER;
    }

    @Override
    protected int computePostContentLength() {
        return 0;
    }

    @Override
    protected ByteBuf computePostContentBytes(int postContentLength) {
        return Unpooled.EMPTY_BUFFER;
    }

    @Override
    protected long getContentLength() {
        return EXTRA_BYTES.length + boundary.length + EXTRA_BYTES.length + CRLF_BYTES.length;
    }

    @Override
    protected long transferContentTo(ByteBuf target) throws IOException {
        throw new UnsupportedOperationException("Not supposed to be called");
    }

    @Override
    protected long transferContentTo(WritableByteChannel target) throws IOException {
        throw new UnsupportedOperationException("Not supposed to be called");
    }

    @Override
    public void close() {
        super.close();
        if (contentBuffer != null) {
            contentBuffer.release();
        }
    }
}
