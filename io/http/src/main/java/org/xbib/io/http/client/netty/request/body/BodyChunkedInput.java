package org.xbib.io.http.client.netty.request.body;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;
import org.xbib.io.http.client.request.body.Body;

/**
 * Adapts a {@link Body} to Netty's {@link ChunkedInput}.
 */
public class BodyChunkedInput implements ChunkedInput<ByteBuf> {

    public static final int DEFAULT_CHUNK_SIZE = 8 * 1024;

    private final Body body;
    private final int chunkSize;
    private boolean endOfInput;

    public BodyChunkedInput(Body body) {
        this.body = body;
        long contentLength = body.getContentLength();
        if (contentLength <= 0) {
            chunkSize = DEFAULT_CHUNK_SIZE;
        } else {
            chunkSize = (int) Math.min(contentLength, (long) DEFAULT_CHUNK_SIZE);
        }
    }

    @Override
    public ByteBuf readChunk(ChannelHandlerContext ctx) throws Exception {
        if (endOfInput) {
            return null;
        }
        ByteBuf buffer = ctx.alloc().buffer(chunkSize);
        Body.BodyState state = body.transferTo(buffer);
        switch (state) {
            case STOP:
                endOfInput = true;
                return buffer;
            case SUSPEND:
                // this will suspend the stream in ChunkedWriteHandler
                return null;
            case CONTINUE:
                return buffer;
            default:
                throw new IllegalStateException("Unknown state: " + state);
        }
    }

    @Override
    public ByteBuf readChunk(ByteBufAllocator allocator) throws Exception {
        if (endOfInput) {
            return null;
        }
        ByteBuf buffer = allocator.buffer(chunkSize);
        Body.BodyState state = body.transferTo(buffer);
        switch (state) {
            case STOP:
                endOfInput = true;
                return buffer;
            case SUSPEND:
                // this will suspend the stream in ChunkedWriteHandler
                return null;
            case CONTINUE:
                return buffer;
            default:
                throw new IllegalStateException("Unknown state: " + state);
        }
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public long progress() {
        return 0;
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        return endOfInput;
    }

    @Override
    public void close() throws Exception {
        body.close();
    }
}
