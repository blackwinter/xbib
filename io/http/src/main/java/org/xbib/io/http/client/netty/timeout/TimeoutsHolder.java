package org.xbib.io.http.client.netty.timeout;

import io.netty.util.Timeout;

import java.util.concurrent.atomic.AtomicBoolean;

public class TimeoutsHolder {

    private final AtomicBoolean cancelled = new AtomicBoolean();
    public volatile Timeout requestTimeout;
    public volatile Timeout readTimeout;

    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            if (requestTimeout != null) {
                requestTimeout.cancel();
                RequestTimeoutTimerTask.class.cast(requestTimeout.task()).clean();
                requestTimeout = null;
            }
            if (readTimeout != null) {
                readTimeout.cancel();
                ReadTimeoutTimerTask.class.cast(readTimeout.task()).clean();
                readTimeout = null;
            }
        }
    }
}
