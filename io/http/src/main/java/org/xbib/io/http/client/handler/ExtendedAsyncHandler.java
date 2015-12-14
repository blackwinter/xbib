package org.xbib.io.http.client.handler;

import io.netty.channel.Channel;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.netty.request.NettyRequest;

import java.net.InetSocketAddress;
import java.util.List;

public abstract class ExtendedAsyncHandler<T> implements AsyncHandler<T>, AsyncHandlerExtensions {

    @Override
    public void onHostnameResolutionAttempt(String name) {
    }

    @Override
    public void onHostnameResolutionSuccess(String name, List<InetSocketAddress> addresses) {
    }

    @Override
    public void onHostnameResolutionFailure(String name, Throwable cause) {
    }

    @Override
    public void onTcpConnectAttempt(InetSocketAddress address) {
    }

    @Override
    public void onTcpConnectSuccess(InetSocketAddress remoteAddress, Channel connection) {
    }

    @Override
    public void onTcpConnectFailure(InetSocketAddress remoteAddress, Throwable cause) {
    }

    @Override
    public void onTlsHandshakeAttempt() {
    }

    @Override
    public void onTlsHandshakeSuccess() {
    }

    @Override
    public void onTlsHandshakeFailure(Throwable cause) {
    }

    @Override
    public void onConnectionPoolAttempt() {
    }

    @Override
    public void onConnectionPooled(Channel connection) {
    }

    @Override
    public void onConnectionOffer(Channel connection) {
    }

    @Override
    public void onRequestSend(NettyRequest request) {
    }

    @Override
    public void onRetry() {
    }
}
