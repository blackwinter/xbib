package org.xbib.io.http.client;

import java.nio.ByteBuffer;

/**
 * A callback class used when an HTTP response body is received.
 */
public abstract class HttpResponseBodyPart {

    private final boolean last;

    public HttpResponseBodyPart(boolean last) {
        this.last = last;
    }

    /**
     * @return length of this part in bytes
     */
    public abstract int length();

    /**
     * @return the response body's part bytes received.
     */
    public abstract byte[] getBodyPartBytes();

    /**
     * @return a {@link ByteBuffer} that wraps the actual bytes read from the response's chunk.
     * The {@link ByteBuffer}'s capacity is equal to the number of bytes available.
     */
    public abstract ByteBuffer getBodyByteBuffer();

    /**
     * @return true if this is the last part.
     */
    public boolean isLast() {
        return last;
    }
}
