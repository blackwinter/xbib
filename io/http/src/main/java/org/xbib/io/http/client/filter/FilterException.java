package org.xbib.io.http.client.filter;

/**
 * An exception that can be thrown by an {@link org.asynchttpclient.AsyncHandler} to interrupt invocation of
 * the {@link org.asynchttpclient.filter.RequestFilter} and {@link ResponseFilter}. It also interrupt the request and
 * response processing.
 */
@SuppressWarnings("serial")
public class FilterException extends Exception {

    public FilterException(final String message) {
        super(message);
    }

    public FilterException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
