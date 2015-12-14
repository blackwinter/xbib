package org.xbib.io.http.client.ws;

/**
 * A {@link WebSocketListener} for text message
 */
public interface WebSocketTextListener extends WebSocketListener {

    /**
     * Invoked when WebSocket text message are received.
     *
     * @param message a {@link String} message
     */
    void onMessage(String message);
}
