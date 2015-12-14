package org.xbib.io.http.client.request.body;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

/**
 * A request body which supports random access to its contents.
 */
public interface RandomAccessBody extends Body {

    /**
     * Transfers the specified chunk of bytes from this body to the specified channel.
     *
     * @param target The destination channel to transfer the body chunk to, must not be {@code null}.
     * @return The non-negative number of bytes actually transferred.
     * @throws IOException If the body chunk could not be transferred.
     */
    long transferTo(WritableByteChannel target) throws IOException;
}
