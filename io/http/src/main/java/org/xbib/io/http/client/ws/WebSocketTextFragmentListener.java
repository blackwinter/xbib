package org.xbib.io.http.client.ws;

import org.xbib.io.http.client.HttpResponseBodyPart;

/**
 * Invoked when WebSocket text fragments are received.
 */
public interface WebSocketTextFragmentListener extends WebSocketListener {

    /**
     * @param fragment a text fragment
     */
    void onFragment(HttpResponseBodyPart fragment);
}
