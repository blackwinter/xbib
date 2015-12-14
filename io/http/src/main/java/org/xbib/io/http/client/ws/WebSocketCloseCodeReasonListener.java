package org.xbib.io.http.client.ws;

/**
 * Extend the normal close listener with one that support the WebSocket's code and reason.
 *
 * @see "http://tools.ietf.org/html/rfc6455#section-5.5.1"
 */
public interface WebSocketCloseCodeReasonListener {

    /**
     * Invoked when the {@link WebSocket} is close.
     *
     * @param websocket the WebSocket
     * @param code      the status code
     * @param reason    the reason message
     */
    void onClose(WebSocket websocket, int code, String reason);
}
