package org.xbib.io.http.client.handler;

import org.xbib.io.http.client.AsyncHandler;

public final class AsyncHandlerExtensionsUtils {

    private AsyncHandlerExtensionsUtils() {
    }

    public static AsyncHandlerExtensions toAsyncHandlerExtensions(AsyncHandler<?> asyncHandler) {
        return asyncHandler instanceof AsyncHandlerExtensions ? (AsyncHandlerExtensions) asyncHandler : null;
    }
}
