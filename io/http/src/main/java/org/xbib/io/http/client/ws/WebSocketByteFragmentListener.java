package org.xbib.io.http.client.ws;

import org.xbib.io.http.client.HttpResponseBodyPart;

/**
 * Invoked when WebSocket binary fragments are received.
 */
public interface WebSocketByteFragmentListener extends WebSocketListener {

    /**
     * @param fragment a fragment
     */
    void onFragment(HttpResponseBodyPart fragment);
}
