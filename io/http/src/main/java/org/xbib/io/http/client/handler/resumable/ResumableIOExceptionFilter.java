package org.xbib.io.http.client.handler.resumable;

import org.xbib.io.http.client.Request;
import org.xbib.io.http.client.filter.FilterContext;
import org.xbib.io.http.client.filter.FilterException;
import org.xbib.io.http.client.filter.IOExceptionFilter;

/**
 * Simple {@link IOExceptionFilter} that replay the current {@link Request} using a {@link ResumableAsyncHandler}
 */
public class ResumableIOExceptionFilter implements IOExceptionFilter {
    public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {
        if (ctx.getIOException() != null && ctx.getAsyncHandler() instanceof ResumableAsyncHandler) {

            Request request = ResumableAsyncHandler.class.cast(ctx.getAsyncHandler()).adjustRequestRange(ctx.getRequest());

            return new FilterContext.FilterContextBuilder<>(ctx).request(request).replayRequest(true).build();
        }
        return ctx;
    }
}
