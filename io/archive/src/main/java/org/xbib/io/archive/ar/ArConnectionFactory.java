
package org.xbib.io.archive.ar;

import org.xbib.io.AbstractConnectionFactory;

import java.io.IOException;
import java.net.URI;

public final class ArConnectionFactory extends AbstractConnectionFactory<ArSession, ArConnection> {

    public final static String SUFFIX = "ar";

    @Override
    public String getName() {
        return SUFFIX;
    }

    /**
     * Get connection
     *
     * @param uri the connection URI
     * @return a new connection
     * @throws java.io.IOException if connection can not be established
     */
    @Override
    public ArConnection getConnection(URI uri) throws IOException {
        ArConnection connection = new ArConnection();
        connection.setURI(uri);
        return connection;
    }
}
