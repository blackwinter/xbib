package org.xbib.io.http.client.ws;

/**
 * Default WebSocketListener implementation.  Most methods are no-ops.  This
 * allows for quick override customization without clutter of methods that the
 * developer isn't interested in dealing with.
 */
public class DefaultWebSocketListener implements WebSocketByteListener, WebSocketTextListener, WebSocketPingListener, WebSocketPongListener {

    protected WebSocket webSocket;

    // -------------------------------------- Methods from WebSocketByteListener

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(byte[] message) {
    }

    // -------------------------------------- Methods from WebSocketPingListener

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPing(byte[] message) {
    }

    // -------------------------------------- Methods from WebSocketPongListener

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPong(byte[] message) {
    }

    // -------------------------------------- Methods from WebSocketTextListener

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(String message) {
    }

    // ------------------------------------------ Methods from WebSocketListener

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(WebSocket websocket) {
        this.webSocket = websocket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose(WebSocket websocket) {
        this.webSocket = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(Throwable t) {
    }
}
