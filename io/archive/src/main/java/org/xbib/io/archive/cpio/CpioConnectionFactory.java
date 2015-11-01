
package org.xbib.io.archive.cpio;

import org.xbib.io.AbstractConnectionFactory;

import java.io.IOException;
import java.net.URI;

/**
 * Cpio connection factory
 */
public final class CpioConnectionFactory extends AbstractConnectionFactory<CpioSession, CpioConnection> {

    @Override
    public String getName() {
        return "cpio";
    }

    /**
     * Get connection
     *
     * @param uri the connection URI
     * @return a new connection
     * @throws java.io.IOException if connection can not be established
     */
    @Override
    public CpioConnection getConnection(URI uri) throws IOException {
        CpioConnection connection = new CpioConnection();
        connection.setURI(uri);
        return connection;
    }

}
