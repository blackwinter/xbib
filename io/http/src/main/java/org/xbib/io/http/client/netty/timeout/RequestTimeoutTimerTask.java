package org.xbib.io.http.client.netty.timeout;

import io.netty.util.Timeout;
import org.xbib.io.http.client.netty.NettyResponseFuture;
import org.xbib.io.http.client.netty.request.NettyRequestSender;

import static org.xbib.io.http.client.util.DateUtils.millisTime;

public class RequestTimeoutTimerTask extends TimeoutTimerTask {

    private final long requestTimeout;

    public RequestTimeoutTimerTask(//
                                   NettyResponseFuture<?> nettyResponseFuture,//
                                   NettyRequestSender requestSender,//
                                   TimeoutsHolder timeoutsHolder,//
                                   long requestTimeout) {
        super(nettyResponseFuture, requestSender, timeoutsHolder);
        this.requestTimeout = requestTimeout;
    }

    public void run(Timeout timeout) throws Exception {

        if (done.getAndSet(true) || requestSender.isClosed()) {
            return;
        }

        // in any case, cancel possible readTimeout sibling
        timeoutsHolder.cancel();

        if (nettyResponseFuture.isDone()) {
            return;
        }

        String message = "Request timed out to " + remoteAddress + " of " + requestTimeout + " ms";
        long age = millisTime() - nettyResponseFuture.getStart();
        expire(message, age);
    }
}
