package org.xbib.io.http.client.netty.request;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.handler.AsyncHandlerExtensions;
import org.xbib.io.http.client.netty.SimpleChannelFutureListener;
import org.xbib.io.http.client.netty.channel.NettyConnectListener;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import static org.xbib.io.http.client.handler.AsyncHandlerExtensionsUtils.toAsyncHandlerExtensions;

public class NettyChannelConnector {

    private final AsyncHandlerExtensions asyncHandlerExtensions;
    private final InetSocketAddress localAddress;
    private final List<InetSocketAddress> remoteAddresses;
    private volatile int i = 0;

    public NettyChannelConnector(InetAddress localAddress, List<InetSocketAddress> remoteAddresses, AsyncHandler<?> asyncHandler) {
        this.localAddress = localAddress != null ? new InetSocketAddress(localAddress, 0) : null;
        this.remoteAddresses = remoteAddresses;
        this.asyncHandlerExtensions = toAsyncHandlerExtensions(asyncHandler);
    }

    private boolean pickNextRemoteAddress() {
        i++;
        return i < remoteAddresses.size();
    }

    public void connect(final Bootstrap bootstrap, final NettyConnectListener<?> connectListener) {
        final InetSocketAddress remoteAddress = remoteAddresses.get(i);

        if (asyncHandlerExtensions != null) {
            asyncHandlerExtensions.onTcpConnectAttempt(remoteAddress);
        }

        final ChannelFuture future = localAddress != null ? bootstrap.connect(remoteAddress, localAddress) : bootstrap.connect(remoteAddress);

        future.addListener(new SimpleChannelFutureListener() {

            @Override
            public void onSuccess(Channel channel) throws Exception {
                if (asyncHandlerExtensions != null) {
                    asyncHandlerExtensions.onTcpConnectSuccess(remoteAddress, future.channel());
                }

                connectListener.onSuccess(channel);
            }

            @Override
            public void onFailure(Channel channel, Throwable t) throws Exception {
                if (asyncHandlerExtensions != null) {
                    asyncHandlerExtensions.onTcpConnectFailure(remoteAddress, t);
                }
                boolean retry = pickNextRemoteAddress();
                if (retry) {
                    NettyChannelConnector.this.connect(bootstrap, connectListener);
                } else {
                    connectListener.onFailure(channel, t);
                }
            }
        });
    }
}
