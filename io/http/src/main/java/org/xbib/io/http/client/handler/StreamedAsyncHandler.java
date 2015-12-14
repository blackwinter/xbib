package org.xbib.io.http.client.handler;

import org.reactivestreams.Publisher;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.HttpResponseBodyPart;

/**
 * AsyncHandler that uses reactive streams to handle the request.
 */
public interface StreamedAsyncHandler<T> extends AsyncHandler<T> {

    /**
     * Called when the body is received. May not be called if there's no body.
     *
     * @param publisher The publisher of response body parts.
     * @return Whether to continue or abort.
     */
    State onStream(Publisher<HttpResponseBodyPart> publisher);
}
