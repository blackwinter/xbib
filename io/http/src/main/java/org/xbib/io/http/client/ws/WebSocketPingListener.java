package org.xbib.io.http.client.ws;

/**
 * A WebSocket's Ping Listener
 */
public interface WebSocketPingListener extends WebSocketListener {

    /**
     * Invoked when a ping message is received
     *
     * @param message a byte array
     */
    void onPing(byte[] message);
}
