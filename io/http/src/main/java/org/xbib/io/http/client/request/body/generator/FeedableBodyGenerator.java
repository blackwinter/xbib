package org.xbib.io.http.client.request.body.generator;

import java.nio.ByteBuffer;

/**
 * {@link BodyGenerator} which may return just part of the payload at the time handler is requesting it.
 * If it happens, client becomes responsible for providing the rest of the chunks.
 */
public interface FeedableBodyGenerator extends BodyGenerator {

    boolean feed(ByteBuffer buffer, boolean isLast) throws Exception;

    void setListener(FeedListener listener);
}
