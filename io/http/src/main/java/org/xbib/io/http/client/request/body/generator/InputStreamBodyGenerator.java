package org.xbib.io.http.client.request.body.generator;

import io.netty.buffer.ByteBuf;
import org.xbib.io.http.client.request.body.Body;

import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link BodyGenerator} which use an {@link InputStream} for reading bytes, without having to read the entire stream
 * in memory.
 * NOTE: The {@link InputStream} must support the {@link InputStream#mark} and {@link InputStream#reset()} operation. If
 * not, mechanisms like authentication, redirect, or
 * resumable download will not works.
 */
public final class InputStreamBodyGenerator implements BodyGenerator {

    private final InputStream inputStream;

    public InputStreamBodyGenerator(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Body createBody() {
        return new InputStreamBody(inputStream);
    }

    private class InputStreamBody implements Body {

        private final InputStream inputStream;
        private byte[] chunk;

        private InputStreamBody(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public long getContentLength() {
            return -1L;
        }

        public BodyState transferTo(ByteBuf target) throws IOException {

            // To be safe.
            chunk = new byte[target.writableBytes() - 10];

            int read = -1;
            boolean write = false;
            try {
                read = inputStream.read(chunk);
            } catch (IOException ex) {
                //
            }

            if (read > 0) {
                target.writeBytes(chunk, 0, read);
                write = true;
            }
            return write ? BodyState.CONTINUE : BodyState.STOP;
        }

        public void close() throws IOException {
            inputStream.close();
        }
    }
}

