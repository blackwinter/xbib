package org.xbib.io.http.client.ws;

import java.io.Closeable;
import java.net.SocketAddress;

/**
 * A Websocket client
 */
public interface WebSocket extends Closeable {

    /**
     * Get remote address client initiated request to.
     *
     * @return remote address client initiated request to, may be {@code null}
     * if asynchronous provider is unable to provide the remote address
     */
    SocketAddress getRemoteAddress();

    /**
     * Get local address client initiated request from.
     *
     * @return local address client initiated request from, may be {@code null}
     * if asynchronous provider is unable to provide the local address
     */
    SocketAddress getLocalAddress();

    /**
     * Send a byte message.
     *
     * @param message a byte message
     * @return this
     */
    WebSocket sendMessage(byte[] message);

    /**
     * Allows streaming of multiple binary fragments.
     *
     * @param fragment binary fragment.
     * @param last     flag indicating whether or not this is the last fragment.
     * @return this
     */
    WebSocket stream(byte[] fragment, boolean last);

    /**
     * Allows streaming of multiple binary fragments.
     *
     * @param fragment binary fragment.
     * @param offset   starting offset.
     * @param len      length.
     * @param last     flag indicating whether or not this is the last fragment.
     * @return this
     */
    WebSocket stream(byte[] fragment, int offset, int len, boolean last);

    /**
     * Send a text message
     *
     * @param message a text message
     * @return this
     */
    WebSocket sendMessage(String message);

    /**
     * Allows streaming of multiple text fragments.
     *
     * @param fragment text fragment.
     * @param last     flag indicating whether or not this is the last fragment.
     * @return this
     */
    WebSocket stream(String fragment, boolean last);

    /**
     * Send a <code>ping</code> with an optional payload
     * (limited to 125 bytes or less).
     *
     * @param payload the ping payload.
     * @return this
     */
    WebSocket sendPing(byte[] payload);

    /**
     * Send a <code>ping</code> with an optional payload
     * (limited to 125 bytes or less).
     *
     * @param payload the pong payload.
     * @return this
     */
    WebSocket sendPong(byte[] payload);

    /**
     * Add a {@link WebSocketListener}
     *
     * @param l a {@link WebSocketListener}
     * @return this
     */
    WebSocket addWebSocketListener(WebSocketListener l);

    /**
     * Remove a {@link WebSocketListener}
     *
     * @param l a {@link WebSocketListener}
     * @return this
     */
    WebSocket removeWebSocketListener(WebSocketListener l);

    /**
     * @return <code>true</code> if the WebSocket is open/connected.
     */
    boolean isOpen();
}
