package org.xbib.io.http.client.handler.resumable;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A listener class that can be used to digest the bytes from an {@link ResumableAsyncHandler}
 */
public interface ResumableListener {

    /**
     * Invoked when some bytes are available to digest.
     *
     * @param byteBuffer the current bytes
     * @throws IOException exception while writing the byteBuffer
     */
    void onBytesReceived(ByteBuffer byteBuffer) throws IOException;

    /**
     * Invoked when all the bytes has been sucessfully transferred.
     */
    void onAllBytesReceived();

    /**
     * Return the length of previously downloaded bytes.
     *
     * @return the length of previously downloaded bytes
     */
    long length();
}
