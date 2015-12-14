package org.xbib.io.http.client;

/**
 * Simple {@link AsyncHandler} of type {@link Response}
 */
public class AsyncCompletionHandlerBase extends AsyncCompletionHandler<Response> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Response onCompleted(Response response) throws Exception {
        return response;
    }
}
