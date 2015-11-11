package org.xbib.util.concurrent;

import java.util.concurrent.atomic.AtomicLong;

public class LongWorkerRequest implements WorkerRequest<AtomicLong> {

    private AtomicLong n;

    @Override
    public AtomicLong get() {
        return n;
    }

    @Override
    public LongWorkerRequest set(AtomicLong n) {
        this.n = n;
        return this;
    }

    @Override
    public String toString() {
        return n.toString();
    }
}
