
package org.xbib.io.archive.jar;

import org.xbib.io.Connection;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * Jar connection
 */
public class JarConnection implements Connection<JarSession> {

    private List<JarSession> sessions = new LinkedList();

    private URI uri;

    protected JarConnection() {
    }

    @Override
    public JarConnection setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public JarSession createSession() throws IOException {
        JarSession session = new JarSession();
        session.setURI(uri);
        sessions.add(session);
        return session;
    }

    @Override
    public void close() throws IOException {
        for (JarSession session : sessions) {
            session.close();
        }
    }
}
