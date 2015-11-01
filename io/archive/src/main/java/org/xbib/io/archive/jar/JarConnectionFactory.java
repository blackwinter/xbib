
package org.xbib.io.archive.jar;

import org.xbib.io.AbstractConnectionFactory;

import java.io.IOException;
import java.net.URI;

/**
 * Jar connection factory
 */
public final class JarConnectionFactory extends AbstractConnectionFactory<JarSession, JarConnection> {

    @Override
    public String getName() {
        return "jar";
    }

    /**
     * Get connection
     *
     * @param uri the connection URI
     * @return a new connection
     * @throws java.io.IOException if connection can not be established
     */
    @Override
    public JarConnection getConnection(URI uri) throws IOException {
        JarConnection connection = new JarConnection();
        connection.setURI(uri);
        return connection;
    }
}
