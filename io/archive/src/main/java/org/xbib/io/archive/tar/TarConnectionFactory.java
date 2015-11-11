
package org.xbib.io.archive.tar;

import org.xbib.io.AbstractConnectionFactory;

import java.io.IOException;
import java.net.URI;

/**
 * Tar connection factory
 */
public final class TarConnectionFactory extends AbstractConnectionFactory<TarSession, TarConnection> {

    @Override
    public String getName() {
        return "tarold";
    }

    /**
     * Get connection
     *
     * @param uri the connection URI
     * @return a new connection
     * @throws java.io.IOException if connection can not be established
     */
    @Override
    public TarConnection getConnection(URI uri) throws IOException {
        TarConnection connection = new TarConnection();
        connection.setURI(uri);
        return connection;
    }

}
