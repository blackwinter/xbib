package org.xbib.io.http.client.request.body.generator;

import io.netty.buffer.ByteBuf;
import org.xbib.io.http.client.request.body.Body;

import java.io.IOException;

/**
 * A {@link BodyGenerator} backed by a byte array.
 */
public final class ByteArrayBodyGenerator implements BodyGenerator {

    private final byte[] bytes;

    public ByteArrayBodyGenerator(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Body createBody() {
        return new ByteBody();
    }

    protected final class ByteBody implements Body {
        private boolean eof = false;
        private int lastPosition = 0;

        public long getContentLength() {
            return bytes.length;
        }

        public BodyState transferTo(ByteBuf target) throws IOException {

            if (eof) {
                return BodyState.STOP;
            }

            final int remaining = bytes.length - lastPosition;
            final int initialTargetWritableBytes = target.writableBytes();
            if (remaining <= initialTargetWritableBytes) {
                target.writeBytes(bytes, lastPosition, remaining);
                eof = true;
            } else {
                target.writeBytes(bytes, lastPosition, initialTargetWritableBytes);
                lastPosition += initialTargetWritableBytes;
            }
            return BodyState.CONTINUE;
        }

        public void close() throws IOException {
            lastPosition = 0;
            eof = false;
        }
    }
}
