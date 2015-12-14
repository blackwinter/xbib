package org.xbib.io.http.client.ws;

/**
 * A generic {@link WebSocketListener} for WebSocket events. Use the appropriate listener for receiving message bytes.
 */
public interface WebSocketListener {

    /**
     * Invoked when the {@link WebSocket} is open.
     *
     * @param websocket the WebSocket
     */
    void onOpen(WebSocket websocket);

    /**
     * Invoked when the {@link WebSocket} is close.
     *
     * @param websocket the WebSocket
     */
    void onClose(WebSocket websocket);

    /**
     * Invoked when the {@link WebSocket} is open.
     *
     * @param t a {@link Throwable}
     */
    void onError(Throwable t);
}
