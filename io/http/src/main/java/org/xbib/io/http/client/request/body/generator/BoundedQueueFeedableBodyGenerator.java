package org.xbib.io.http.client.request.body.generator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class BoundedQueueFeedableBodyGenerator extends QueueBasedFeedableBodyGenerator<BlockingQueue<BodyChunk>> {

    public BoundedQueueFeedableBodyGenerator(int capacity) {
        super(new ArrayBlockingQueue<>(capacity, true));
    }

    @Override
    protected boolean offer(BodyChunk chunk) throws InterruptedException {
        return queue.offer(chunk);
    }
}
