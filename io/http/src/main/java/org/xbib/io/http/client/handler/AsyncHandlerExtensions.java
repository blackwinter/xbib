package org.xbib.io.http.client.handler;

import io.netty.channel.Channel;
import org.xbib.io.http.client.netty.request.NettyRequest;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * This interface hosts new low level callback methods on {@link AsyncHandler}.
 */
public interface AsyncHandlerExtensions {

    /**
     * Notify the callback before hostname resolution
     *
     * @param name the name to be resolved
     */
    void onHostnameResolutionAttempt(String name);

    /**
     * Notify the callback after hostname resolution was successful.
     *
     * @param name      the name to be resolved
     * @param addresses the resolved addresses
     */
    void onHostnameResolutionSuccess(String name, List<InetSocketAddress> addresses);

    /**
     * Notify the callback after hostname resolution failed.
     *
     * @param name  the name to be resolved
     * @param cause the failure cause
     */
    void onHostnameResolutionFailure(String name, Throwable cause);

    /**
     * Notify the callback when trying to open a new connection.
     *
     * Might be called several times if the name was resolved to multiple addresses and we failed to connect to the
     * first(s) one(s).
     *
     * @param remoteAddress the address we try to connect to
     */
    void onTcpConnectAttempt(InetSocketAddress remoteAddress);

    /**
     * Notify the callback after a successful connect
     *
     * @param remoteAddress the address we try to connect to
     * @param connection    the connection
     */
    void onTcpConnectSuccess(InetSocketAddress remoteAddress, Channel connection);

    /**
     * Notify the callback after a failed connect.
     *
     * Might be called several times, or be followed by onTcpConnectSuccess when the name was resolved to multiple
     * addresses.
     *
     * @param remoteAddress the address we try to connect to
     * @param cause         the cause of the failure
     */
    void onTcpConnectFailure(InetSocketAddress remoteAddress, Throwable cause);

    /**
     * Notify the callback before TLS handshake
     */
    void onTlsHandshakeAttempt();

    /**
     * Notify the callback after the TLS was successful
     */
    void onTlsHandshakeSuccess();

    /**
     * Notify the callback after the TLS failed
     *
     * @param cause the cause of the failure
     */
    void onTlsHandshakeFailure(Throwable cause);

    /**
     * Notify the callback when trying to fetch a connection from the pool.
     */
    void onConnectionPoolAttempt();

    /**
     * Notify the callback when a new connection was successfully fetched from the pool.
     *
     * @param connection the connection
     */
    void onConnectionPooled(Channel connection);

    /**
     * Notify the callback when trying to offer a connection to the pool.
     *
     * @param connection the connection
     */
    void onConnectionOffer(Channel connection);

    /**
     * Notify the callback when a request is being written on the channel. If the original request causes multiple
     * requests to be sent, for example, because of authorization or
     * retry, it will be notified multiple times.
     *
     * @param request the real request object as passed to the provider
     */
    void onRequestSend(NettyRequest request);

    /**
     * Notify the callback every time a request is being retried.
     */
    void onRetry();
}
