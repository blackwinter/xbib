package org.xbib.io.http.client.filter;

/**
 * This filter is invoked when an {@link java.io.IOException} occurs during an http transaction.
 */
public interface IOExceptionFilter {

    /**
     * An {@link org.asynchttpclient.AsyncHttpClient} will invoke {@link IOExceptionFilter#filter} and will
     * use the returned {@link FilterContext} to replay the {@link Request} or abort the processing.
     *
     * @param ctx a {@link FilterContext}
     * @param <T> the handler result type
     * @return {@link org.asynchttpclient.filter.FilterContext}. The {@link FilterContext} instance may not the same as
     * the original one.
     * @throws FilterException to interrupt the filter processing.
     */
    <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException;
}
