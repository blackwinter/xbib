package org.xbib.io.http.client.handler;

import io.netty.handler.codec.http.HttpHeaders;

/**
 * A simple interface an application can implements in order to received byte transfer information.
 */
public interface TransferListener {

    /**
     * Invoked when the request bytes are starting to get send.
     *
     * @param headers the headers
     */
    void onRequestHeadersSent(HttpHeaders headers);

    /**
     * Invoked when the response bytes are starting to get received.
     *
     * @param headers the headers
     */
    void onResponseHeadersReceived(HttpHeaders headers);

    /**
     * Invoked every time response's chunk are received.
     *
     * @param bytes a {@link byte[]}
     */
    void onBytesReceived(byte[] bytes);

    /**
     * Invoked every time request's chunk are sent.
     *
     * @param amount  The amount of bytes to transfer
     * @param current The amount of bytes transferred
     * @param total   The total number of bytes transferred
     */
    void onBytesSent(long amount, long current, long total);

    /**
     * Invoked when the response bytes are been fully received.
     */
    void onRequestResponseCompleted();

    /**
     * Invoked when there is an unexpected issue.
     *
     * @param t a {@link Throwable}
     */
    void onThrowable(Throwable t);
}

