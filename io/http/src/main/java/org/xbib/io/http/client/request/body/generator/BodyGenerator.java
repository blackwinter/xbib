package org.xbib.io.http.client.request.body.generator;

import org.xbib.io.http.client.request.body.Body;

/**
 * Creates a request body.
 */
public interface BodyGenerator {

    /**
     * Creates a new instance of the request body to be read. While each invocation of this method is supposed to
     * create
     * a fresh instance of the body, the actual contents of all these body instances is the same. For example, the body
     * needs to be resend after an authentication challenge of a redirect.
     *
     * @return The request body, never {@code null}.
     */
    Body createBody();
}
