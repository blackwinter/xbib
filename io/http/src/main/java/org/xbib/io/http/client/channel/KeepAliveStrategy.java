package org.xbib.io.http.client.channel;

import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import org.xbib.io.http.client.Request;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Values.CLOSE;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;

public interface KeepAliveStrategy {

    /**
     * Determines whether the connection should be kept alive after this HTTP message exchange.
     *
     * @param ahcRequest    the Request, as built by AHC
     * @param nettyRequest  the HTTP request sent to Netty
     * @param nettyResponse the HTTP response received from Netty
     * @return true if the connection should be kept alive, false if it should be closed.
     */
    boolean keepAlive(Request ahcRequest, HttpRequest nettyRequest, HttpResponse nettyResponse);

    /**
     * Connection strategy implementing standard HTTP 1.0/1.1 behaviour.
     */
    enum DefaultKeepAliveStrategy implements KeepAliveStrategy {

        INSTANCE;

        /**
         * Implemented in accordance with RFC 7230 section 6.1
         * https://tools.ietf.org/html/rfc7230#section-6.1
         */
        @Override
        public boolean keepAlive(Request ahcRequest, HttpRequest request, HttpResponse response) {

            String responseConnectionHeader = connectionHeader(response);

            if (CLOSE.equalsIgnoreCase(responseConnectionHeader)) {
                return false;
            } else {
                String requestConnectionHeader = connectionHeader(request);

                if (request.getProtocolVersion() == HttpVersion.HTTP_1_0) {
                    // only use keep-alive if both parties agreed upon it
                    return KEEP_ALIVE.equalsIgnoreCase(requestConnectionHeader) && KEEP_ALIVE.equalsIgnoreCase(responseConnectionHeader);

                } else {
                    // 1.1+, keep-alive is default behavior
                    return !CLOSE.equalsIgnoreCase(requestConnectionHeader);
                }
            }
        }

        private String connectionHeader(HttpMessage message) {
            return message.headers().get(CONNECTION);
        }
    }
}
