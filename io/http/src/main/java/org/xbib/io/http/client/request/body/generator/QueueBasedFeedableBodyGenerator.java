package org.xbib.io.http.client.request.body.generator;

import org.xbib.io.http.client.request.body.Body;

import java.nio.ByteBuffer;
import java.util.Queue;

public abstract class QueueBasedFeedableBodyGenerator<T extends Queue<BodyChunk>> implements FeedableBodyGenerator {

    protected final T queue;
    private FeedListener listener;

    public QueueBasedFeedableBodyGenerator(T queue) {
        this.queue = queue;
    }

    @Override
    public Body createBody() {
        return new PushBody(queue);
    }

    protected abstract boolean offer(BodyChunk chunk) throws Exception;

    @Override
    public boolean feed(final ByteBuffer buffer, final boolean isLast) throws Exception {
        boolean offered = offer(new BodyChunk(buffer, isLast));
        if (offered && listener != null) {
            listener.onContentAdded();
        }
        return offered;
    }

    @Override
    public void setListener(FeedListener listener) {
        this.listener = listener;
    }
}
