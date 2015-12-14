package org.xbib.io.http.client.ws;

import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.HttpResponseHeaders;
import org.xbib.io.http.client.HttpResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An {@link AsyncHandler} which is able to execute WebSocket upgrade. Use the Builder for configuring WebSocket
 * options.
 */
public class WebSocketUpgradeHandler implements UpgradeHandler<WebSocket>, AsyncHandler<WebSocket> {

    private final List<WebSocketListener> listeners;
    private final AtomicBoolean ok = new AtomicBoolean(false);
    private WebSocket webSocket;
    private boolean onSuccessCalled;
    private int status;

    public WebSocketUpgradeHandler(List<WebSocketListener> listeners) {
        this.listeners = listeners;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onThrowable(Throwable t) {
        onFailure(t);
    }

    public boolean touchSuccess() {
        boolean prev = onSuccessCalled;
        onSuccessCalled = true;
        return prev;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        return State.CONTINUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
        status = responseStatus.getStatusCode();
        if (responseStatus.getStatusCode() == 101) {
            return State.UPGRADE;
        } else {
            return State.ABORT;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final State onHeadersReceived(HttpResponseHeaders headers) throws Exception {
        return State.CONTINUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final WebSocket onCompleted() throws Exception {

        if (status != 101) {
            IllegalStateException e = new IllegalStateException("Invalid Status Code " + status);
            for (WebSocketListener listener : listeners) {
                listener.onError(e);
            }
            throw e;
        }
        return webSocket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onSuccess(WebSocket webSocket) {
        this.webSocket = webSocket;
        for (WebSocketListener listener : listeners) {
            webSocket.addWebSocketListener(listener);
            listener.onOpen(webSocket);
        }
        ok.set(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onFailure(Throwable t) {
        for (WebSocketListener listener : listeners) {
            if (!ok.get() && webSocket != null) {
                webSocket.addWebSocketListener(listener);
            }
            listener.onError(t);
        }
    }

    public final void onClose(WebSocket webSocket, int status, String reasonPhrase) {
        // Connect failure
        if (this.webSocket == null) {
            this.webSocket = webSocket;
        }

        for (WebSocketListener listener : listeners) {
            if (webSocket != null) {
                webSocket.addWebSocketListener(listener);
            }
            listener.onClose(webSocket);
            if (listener instanceof WebSocketCloseCodeReasonListener) {
                WebSocketCloseCodeReasonListener.class.cast(listener).onClose(webSocket, status, reasonPhrase);
            }
        }
    }

    /**
     * Build a {@link WebSocketUpgradeHandler}
     */
    public final static class Builder {

        private List<WebSocketListener> listeners = new ArrayList<>();

        /**
         * Add a {@link WebSocketListener} that will be added to the {@link WebSocket}
         *
         * @param listener a {@link WebSocketListener}
         * @return this
         */
        public Builder addWebSocketListener(WebSocketListener listener) {
            listeners.add(listener);
            return this;
        }

        /**
         * Remove a {@link WebSocketListener}
         *
         * @param listener a {@link WebSocketListener}
         * @return this
         */
        public Builder removeWebSocketListener(WebSocketListener listener) {
            listeners.remove(listener);
            return this;
        }

        /**
         * Build a {@link WebSocketUpgradeHandler}
         *
         * @return a {@link WebSocketUpgradeHandler}
         */
        public WebSocketUpgradeHandler build() {
            return new WebSocketUpgradeHandler(listeners);
        }
    }
}
