package org.xbib.pipeline;

import java.util.concurrent.atomic.AtomicLong;

public class LongPipelineRequest implements PipelineRequest<AtomicLong> {

    private AtomicLong n;

    @Override
    public AtomicLong get() {
        return n;
    }

    @Override
    public LongPipelineRequest set(AtomicLong n) {
        this.n = n;
        return this;
    }

    @Override
    public String toString() {
        return n.toString();
    }
}
