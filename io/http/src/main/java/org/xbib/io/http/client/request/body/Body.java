package org.xbib.io.http.client.request.body;

import io.netty.buffer.ByteBuf;

import java.io.Closeable;
import java.io.IOException;

/**
 * A request body.
 */
public interface Body extends Closeable {

    /**
     * Gets the length of the body.
     *
     * @return The length of the body in bytes, or negative if unknown.
     */
    long getContentLength();

    /**
     * Reads the next chunk of bytes from the body.
     *
     * @param target The buffer to store the chunk in, must not be {@code null}.
     * @return The non-negative number of bytes actually read or {@code -1} if the body has been read completely.
     * @throws IOException If the chunk could not be read.
     */
    BodyState transferTo(ByteBuf target) throws IOException;

    enum BodyState {

        /**
         * There's something to read
         */
        CONTINUE,

        /**
         * There's nothing to read and input has to suspend
         */
        SUSPEND,

        /**
         * There's nothing to read and input has to stop
         */
        STOP;
    }
}
