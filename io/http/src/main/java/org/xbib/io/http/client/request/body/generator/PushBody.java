package org.xbib.io.http.client.request.body.generator;

import io.netty.buffer.ByteBuf;
import org.xbib.io.http.client.request.body.Body;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;

public final class PushBody implements Body {

    private final Queue<BodyChunk> queue;
    private BodyState state = BodyState.CONTINUE;

    public PushBody(Queue<BodyChunk> queue) {
        this.queue = queue;
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public BodyState transferTo(final ByteBuf target) throws IOException {
        switch (state) {
            case CONTINUE:
                return readNextChunk(target);
            case STOP:
                return BodyState.STOP;
            default:
                throw new IllegalStateException("Illegal process state.");
        }
    }

    private BodyState readNextChunk(ByteBuf target) throws IOException {
        BodyState res = BodyState.SUSPEND;
        while (target.isWritable() && state != BodyState.STOP) {
            BodyChunk nextChunk = queue.peek();
            if (nextChunk == null) {
                // Nothing in the queue. suspend stream if nothing was read. (reads == 0)
                return res;
            } else if (!nextChunk.buffer.hasRemaining() && !nextChunk.last) {
                // skip empty buffers
                queue.remove();
            } else {
                res = BodyState.CONTINUE;
                readChunk(target, nextChunk);
            }
        }
        return res;
    }

    private void readChunk(ByteBuf target, BodyChunk part) {
        move(target, part.buffer);

        if (!part.buffer.hasRemaining()) {
            if (part.last) {
                state = BodyState.STOP;
            }
            queue.remove();
        }
    }

    private void move(ByteBuf target, ByteBuffer source) {
        int size = Math.min(target.writableBytes(), source.remaining());
        if (size > 0) {
            ByteBuffer slice = source.slice();
            slice.limit(size);
            target.writeBytes(slice);
            source.position(source.position() + size);
        }
    }

    @Override
    public void close() {
    }
}
