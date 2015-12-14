package org.xbib.io.http.client.request.body.generator;

import java.nio.ByteBuffer;

public final class BodyChunk {
    public final boolean last;
    public final ByteBuffer buffer;

    public BodyChunk(final ByteBuffer buffer, final boolean last) {
        this.buffer = buffer;
        this.last = last;
    }
}
