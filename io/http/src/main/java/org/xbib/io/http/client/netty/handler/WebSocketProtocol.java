package org.xbib.io.http.client.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.HttpResponseHeaders;
import org.xbib.io.http.client.HttpResponseStatus;
import org.xbib.io.http.client.Realm;
import org.xbib.io.http.client.Request;
import org.xbib.io.http.client.netty.Callback;
import org.xbib.io.http.client.netty.NettyResponseFuture;
import org.xbib.io.http.client.netty.NettyResponseStatus;
import org.xbib.io.http.client.netty.channel.ChannelManager;
import org.xbib.io.http.client.netty.channel.Channels;
import org.xbib.io.http.client.netty.request.NettyRequestSender;
import org.xbib.io.http.client.netty.ws.NettyWebSocket;
import org.xbib.io.http.client.ws.WebSocketUpgradeHandler;

import java.io.IOException;
import java.util.Locale;

import static io.netty.handler.codec.http.HttpResponseStatus.SWITCHING_PROTOCOLS;
import static org.xbib.io.http.client.ws.WebSocketUtils.getAcceptKey;

public final class WebSocketProtocol extends Protocol {

    private final static Logger logger = LogManager.getLogger(WebSocketProtocol.class.getName());

    public WebSocketProtocol(ChannelManager channelManager,//
                             AsyncHttpClientConfig config,//
                             NettyRequestSender requestSender) {
        super(channelManager, config, requestSender);
    }

    // We don't need to synchronize as replacing the "ws-decoder" will
    // process using the same thread.
    private void invokeOnSucces(Channel channel, WebSocketUpgradeHandler h) {
        if (!h.touchSuccess()) {
            try {
                h.onSuccess(new NettyWebSocket(channel, config));
            } catch (Exception ex) {
                logger.warn("onSuccess unexpected exception", ex);
            }
        }
    }

    @Override
    public void handle(Channel channel, NettyResponseFuture<?> future, Object e) throws Exception {

        if (e instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) e;
            if (logger.isDebugEnabled()) {
                HttpRequest httpRequest = future.getNettyRequest().getHttpRequest();
                logger.debug("\n\nRequest {}\n\nResponse {}\n", httpRequest, response);
            }

            WebSocketUpgradeHandler handler = WebSocketUpgradeHandler.class.cast(future.getAsyncHandler());
            HttpResponseStatus status = new NettyResponseStatus(future.getUri(), config, response, channel);
            HttpResponseHeaders responseHeaders = new HttpResponseHeaders(response.headers());

            Request request = future.getCurrentRequest();
            Realm realm = request.getRealm() != null ? request.getRealm() : config.getRealm();

            if (exitAfterProcessingFilters(channel, future, handler, status, responseHeaders)) {
                return;
            }

            if (REDIRECT_STATUSES.contains(status.getStatusCode()) && exitAfterHandlingRedirect(channel, future, response, request, response.getStatus().code(), realm)) {
                return;
            }

            Channels.setAttribute(channel, new UpgradeCallback(future, channel, response, handler, status, responseHeaders));

        } else if (e instanceof WebSocketFrame) {

            final WebSocketFrame frame = (WebSocketFrame) e;
            WebSocketUpgradeHandler handler = WebSocketUpgradeHandler.class.cast(future.getAsyncHandler());
            NettyWebSocket webSocket = NettyWebSocket.class.cast(handler.onCompleted());
            invokeOnSucces(channel, handler);

            if (webSocket != null) {
                if (frame instanceof CloseWebSocketFrame) {
                    Channels.setDiscard(channel);
                    CloseWebSocketFrame closeFrame = CloseWebSocketFrame.class.cast(frame);
                    webSocket.onClose(closeFrame.statusCode(), closeFrame.reasonText());
                } else {
                    ByteBuf buf = frame.content();
                    if (buf != null && buf.readableBytes() > 0) {
                        HttpResponseBodyPart part = config.getResponseBodyPartFactory().newResponseBodyPart(buf, frame.isFinalFragment());
                        handler.onBodyPartReceived(part);

                        if (frame instanceof BinaryWebSocketFrame) {
                            webSocket.onBinaryFragment(part);
                        } else if (frame instanceof TextWebSocketFrame) {
                            webSocket.onTextFragment(part);
                        } else if (frame instanceof PingWebSocketFrame) {
                            webSocket.onPing(part);
                        } else if (frame instanceof PongWebSocketFrame) {
                            webSocket.onPong(part);
                        }
                    }
                }
            } else {
                logger.debug("UpgradeHandler returned a null NettyWebSocket ");
            }
        } else {
            logger.error("Invalid message {}", e);
        }
    }

    @Override
    public void onError(NettyResponseFuture<?> future, Throwable e) {
        logger.warn("onError {}", e);

        try {
            WebSocketUpgradeHandler h = (WebSocketUpgradeHandler) future.getAsyncHandler();

            NettyWebSocket webSocket = NettyWebSocket.class.cast(h.onCompleted());
            if (webSocket != null) {
                webSocket.onError(e.getCause());
                webSocket.close();
            }
        } catch (Throwable t) {
            logger.error("onError", t);
        }
    }

    @Override
    public void onClose(NettyResponseFuture<?> future) {
        logger.trace("onClose");

        try {
            WebSocketUpgradeHandler h = (WebSocketUpgradeHandler) future.getAsyncHandler();
            NettyWebSocket webSocket = NettyWebSocket.class.cast(h.onCompleted());

            logger.trace("Connection was closed abnormally (that is, with no close frame being sent).");
            if (webSocket != null) {
                webSocket.close(1006, "Connection was closed abnormally (that is, with no close frame being sent).");
            }
        } catch (Throwable t) {
            logger.error("onError", t);
        }
    }

    private class UpgradeCallback extends Callback {

        private final Channel channel;
        private final HttpResponse response;
        private final WebSocketUpgradeHandler handler;
        private final HttpResponseStatus status;
        private final HttpResponseHeaders responseHeaders;

        public UpgradeCallback(NettyResponseFuture<?> future, Channel channel, HttpResponse response, WebSocketUpgradeHandler handler, HttpResponseStatus status, HttpResponseHeaders responseHeaders) {
            super(future);
            this.channel = channel;
            this.response = response;
            this.handler = handler;
            this.status = status;
            this.responseHeaders = responseHeaders;
        }

        @Override
        public void call() throws Exception {

            boolean validStatus = response.getStatus().equals(SWITCHING_PROTOCOLS);
            boolean validUpgrade = response.headers().get(HttpHeaders.Names.UPGRADE) != null;
            String connection = response.headers().get(HttpHeaders.Names.CONNECTION);
            if (connection == null) {
                connection = response.headers().get(HttpHeaders.Names.CONNECTION.toLowerCase(Locale.ENGLISH));
            }
            boolean validConnection = HttpHeaders.Values.UPGRADE.equalsIgnoreCase(connection);
            boolean statusReceived = handler.onStatusReceived(status) == AsyncHandler.State.UPGRADE;

            if (!statusReceived) {
                try {
                    handler.onCompleted();
                } finally {
                    future.done();
                }
                return;
            }

            final boolean headerOK = handler.onHeadersReceived(responseHeaders) == AsyncHandler.State.CONTINUE;
            if (!headerOK || !validStatus || !validUpgrade || !validConnection) {
                requestSender.abort(channel, future, new IOException("Invalid handshake response"));
                return;
            }

            String accept = response.headers().get(HttpHeaders.Names.SEC_WEBSOCKET_ACCEPT);
            String key = getAcceptKey(future.getNettyRequest().getHttpRequest().headers().get(HttpHeaders.Names.SEC_WEBSOCKET_KEY));
            if (accept == null || !accept.equals(key)) {
                requestSender.abort(channel, future, new IOException(String.format("Invalid challenge. Actual: %s. Expected: %s", accept, key)));
            }

            channelManager.upgradePipelineForWebSockets(channel.pipeline());

            invokeOnSucces(channel, handler);
            future.done();
            // set back the future so the protocol gets notified of frames
            Channels.setAttribute(channel, future);
        }

    }
}
