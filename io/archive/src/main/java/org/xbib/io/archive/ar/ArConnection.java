
package org.xbib.io.archive.ar;

import org.xbib.io.Connection;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * Ar connection
 */
public class ArConnection implements Connection<ArSession> {

    private List<ArSession> sessions = new LinkedList();

    private URI uri;

    protected ArConnection() {
    }

    @Override
    public ArConnection setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public ArSession createSession() throws IOException {
        ArSession session = new ArSession();
        session.setURI(uri);
        sessions.add(session);
        return session;
    }

    @Override
    public void close() throws IOException {
        for (ArSession session : sessions) {
            session.close();
        }
    }
}
