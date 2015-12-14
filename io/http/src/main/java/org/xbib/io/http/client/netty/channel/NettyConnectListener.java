package org.xbib.io.http.client.netty.channel;

import io.netty.channel.Channel;
import io.netty.handler.ssl.SslHandler;
import org.xbib.io.http.client.Request;
import org.xbib.io.http.client.handler.AsyncHandlerExtensions;
import org.xbib.io.http.client.netty.NettyResponseFuture;
import org.xbib.io.http.client.netty.SimpleChannelFutureListener;
import org.xbib.io.http.client.netty.SimpleGenericFutureListener;
import org.xbib.io.http.client.netty.future.StackTraceInspector;
import org.xbib.io.http.client.netty.request.NettyRequestSender;
import org.xbib.io.http.client.uri.Uri;

import java.net.ConnectException;

import static org.xbib.io.http.client.handler.AsyncHandlerExtensionsUtils.toAsyncHandlerExtensions;
import static org.xbib.io.http.client.util.HttpUtils.getBaseUrl;

/**
 * Non Blocking connect.
 */
public final class NettyConnectListener<T> extends SimpleChannelFutureListener {

    private final NettyRequestSender requestSender;
    private final NettyResponseFuture<T> future;
    private final ChannelManager channelManager;
    private final boolean channelPreempted;
    private final Object partitionKey;

    public NettyConnectListener(NettyResponseFuture<T> future,//
                                NettyRequestSender requestSender,//
                                ChannelManager channelManager,//
                                boolean channelPreempted,//
                                Object partitionKey) {
        this.future = future;
        this.requestSender = requestSender;
        this.channelManager = channelManager;
        this.channelPreempted = channelPreempted;
        this.partitionKey = partitionKey;
    }

    private void abortChannelPreemption() {
        if (channelPreempted) {
            channelManager.abortChannelPreemption(partitionKey);
        }
    }

    private void writeRequest(Channel channel) {

        Channels.setAttribute(channel, future);

        if (future.isDone()) {
            abortChannelPreemption();
            return;
        }

        channelManager.registerOpenChannel(channel, partitionKey);
        future.attachChannel(channel, false);
        requestSender.writeRequest(future, channel);
    }

    @Override
    public void onSuccess(Channel channel) throws Exception {

        Request request = future.getTargetRequest();
        Uri uri = request.getUri();

        // in case of proxy tunneling, we'll add the SslHandler later, after the CONNECT request
        if (future.getProxyServer() == null && uri.isSecured()) {
            SslHandler sslHandler = channelManager.addSslHandler(channel.pipeline(), uri, request.getVirtualHost());

            final AsyncHandlerExtensions asyncHandlerExtensions = toAsyncHandlerExtensions(future.getAsyncHandler());

            if (asyncHandlerExtensions != null) {
                asyncHandlerExtensions.onTlsHandshakeAttempt();
            }

            sslHandler.handshakeFuture().addListener(new SimpleGenericFutureListener<Channel>() {

                @Override
                protected void onSuccess(Channel value) throws Exception {
                    if (asyncHandlerExtensions != null) {
                        asyncHandlerExtensions.onTlsHandshakeSuccess();
                    }
                    writeRequest(channel);
                }

                @Override
                protected void onFailure(Throwable cause) throws Exception {
                    if (asyncHandlerExtensions != null) {
                        asyncHandlerExtensions.onTlsHandshakeFailure(cause);
                    }
                    NettyConnectListener.this.onFailure(channel, cause);
                }
            });

        } else {
            writeRequest(channel);
        }
    }

    @Override
    public void onFailure(Channel channel, Throwable cause) throws Exception {

        abortChannelPreemption();

        boolean canRetry = future.canRetry();
        if (canRetry//
                && cause != null // FIXME when can we have a null cause?
                && (future.getChannelState() != ChannelState.NEW || StackTraceInspector.recoverOnNettyDisconnectException(cause))) {

            if (requestSender.retry(future)) {
                return;
            }
        }

        boolean printCause = cause != null && cause.getMessage() != null;
        String printedCause = printCause ? cause.getMessage() : getBaseUrl(future.getUri());
        ConnectException e = new ConnectException(printedCause);
        if (cause != null) {
            e.initCause(cause);
        }
        future.abort(e);
    }
}
