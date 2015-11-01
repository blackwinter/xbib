
package org.xbib.io.archive.tar;

import org.xbib.io.Connection;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * Tar connection
 */
public class TarConnection implements Connection<TarSession> {

    private List<TarSession> sessions = new LinkedList();

    private URI uri;

    protected TarConnection() {
    }

    @Override
    public TarConnection setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public TarSession createSession() throws IOException {
        TarSession session = new TarSession();
        session.setURI(uri);
        sessions.add(session);
        return session;
    }

    @Override
    public void close() throws IOException {
        for (TarSession session : sessions) {
            session.close();
        }
    }
}
