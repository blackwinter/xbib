package org.xbib.io.http.client.ws;

/**
 * A {@link WebSocketListener} for bytes
 */
public interface WebSocketByteListener extends WebSocketListener {

    /**
     * Invoked when bytes are available.
     *
     * @param message a byte array.
     */
    void onMessage(byte[] message);
}
