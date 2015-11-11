
package org.xbib.io.archive.zip;

import org.xbib.io.AbstractConnectionFactory;

import java.io.IOException;
import java.net.URI;

/**
 * Zip connection factory
 */
public final class ZipConnectionFactory extends AbstractConnectionFactory<ZipSession, ZipConnection> {

    @Override
    public String getName() {
        return "zip";
    }

    /**
     * Get connection
     *
     * @param uri the connection URI
     * @return a new connection
     * @throws java.io.IOException if connection can not be established
     */
    @Override
    public ZipConnection getConnection(URI uri) throws IOException {
        ZipConnection connection = new ZipConnection();
        connection.setURI(uri);
        return connection;
    }

}
