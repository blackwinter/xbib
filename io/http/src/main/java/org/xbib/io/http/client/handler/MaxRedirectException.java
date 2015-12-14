package org.xbib.io.http.client.handler;

/**
 * Thrown when the {@link DefaultAsyncHttpClientConfig#getMaxRedirects()} has been reached.
 */
public class MaxRedirectException extends Exception {
    private static final long serialVersionUID = 1L;

    public MaxRedirectException(String msg) {
        super(msg, null, true, false);
    }
}
