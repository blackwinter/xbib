package org.xbib.io.http.client.request.body.generator;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class UnboundedQueueFeedableBodyGenerator extends QueueBasedFeedableBodyGenerator<ConcurrentLinkedQueue<BodyChunk>> {

    public UnboundedQueueFeedableBodyGenerator() {
        super(new ConcurrentLinkedQueue<>());
    }

    @Override
    protected boolean offer(BodyChunk chunk) throws Exception {
        return queue.offer(chunk);
    }
}
