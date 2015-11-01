
package org.xbib.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * A connection factory
 */
public interface ConnectionFactory<S extends Session> {

    String getName();

    /**
     * Checks if this connection factory can handle this URI.
     *
     * @param uri the URI to check
     * @return true if the URI can be provided, otherwise false
     */
    boolean canOpen(URI uri);

    /**
     * Creates a new connection
     *
     * @param uri the URI for the connection
     * @return the connection
     * @throws java.io.IOException if the connection can not be established
     */
    Connection<S> getConnection(URI uri) throws IOException;

    /**
     * Create input stream from URI.
     * @param uri the URI
     * @return input stream or null
     * @throws IOException if creation of input streams fails or is not
     */
    InputStream open(URI uri) throws IOException;

}
