package org.xbib.io.http.client.filter;

/**
 * A Filter interface that gets invoked before making the processing of the response bytes. {@link ResponseFilter} are
 * invoked
 * before the actual response's status code get processed. That means authorization, proxy authentication and redirects
 * processing hasn't occurred when {@link ResponseFilter} gets invoked.
 */
public interface ResponseFilter {

    /**
     * An {@link org.asynchttpclient.AsyncHttpClient} will invoke {@link ResponseFilter#filter} and will use the
     * returned {@link org.asynchttpclient.filter.FilterContext#replayRequest()} and {@link
     * org.asynchttpclient.filter.FilterContext#getAsyncHandler()} to decide if the response
     * processing can continue. If {@link org.asynchttpclient.filter.FilterContext#replayRequest()} return true, a new
     * request will be made
     * using {@link org.asynchttpclient.filter.FilterContext#getRequest()} and the current response processing will be
     * ignored.
     *
     * @param ctx a {@link org.asynchttpclient.filter.FilterContext}
     * @param <T> the handler result type
     * @return {@link org.asynchttpclient.filter.FilterContext}. The {@link org.asynchttpclient.filter.FilterContext}
     * instance may not the same as the original one.
     * @throws FilterException to interrupt the filter processing.
     */
    <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException;
}
