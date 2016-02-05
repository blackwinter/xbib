package org.xbib.io.http.client.netty.timeout;

import io.netty.channel.Channel;
import io.netty.util.TimerTask;
import org.xbib.io.http.client.netty.NettyResponseFuture;
import org.xbib.io.http.client.netty.request.NettyRequestSender;

import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TimeoutTimerTask implements TimerTask {

    protected final AtomicBoolean done = new AtomicBoolean();
    protected final NettyRequestSender requestSender;
    protected final TimeoutsHolder timeoutsHolder;
    protected final String remoteAddress;
    protected volatile NettyResponseFuture<?> nettyResponseFuture;

    public TimeoutTimerTask(NettyResponseFuture<?> nettyResponseFuture, NettyRequestSender requestSender, TimeoutsHolder timeoutsHolder) {
        this.nettyResponseFuture = nettyResponseFuture;
        this.requestSender = requestSender;
        this.timeoutsHolder = timeoutsHolder;
        // saving remote address as the channel might be removed from the future when an exception occurs
        Channel channel = nettyResponseFuture.channel();
        SocketAddress sa = channel == null ? null : channel.remoteAddress();
        remoteAddress = sa == null ? "not-connected" : sa.toString();
    }

    protected void expire(String message, long time) {
        requestSender.abort(nettyResponseFuture.channel(), nettyResponseFuture, new TimeoutException(message));
    }

    /**
     * When the timeout is cancelled, it could still be referenced for quite some time in the Timer.
     * Holding a reference to the future might mean holding a reference to the channel, and heavy objects such as
     * SslEngines
     */
    public void clean() {
        if (done.compareAndSet(false, true)) {
            nettyResponseFuture = null;
        }
    }
}
