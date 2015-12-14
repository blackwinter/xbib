package org.xbib.io.http.client.ws;

/**
 * A WebSocket's Pong Listener
 */
public interface WebSocketPongListener extends WebSocketListener {

    /**
     * Invoked when a pong message is received
     *
     * @param message a byte array
     */
    void onPong(byte[] message);
}
