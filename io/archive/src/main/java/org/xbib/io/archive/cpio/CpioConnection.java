
package org.xbib.io.archive.cpio;

import org.xbib.io.Connection;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * Cpio connection
 */
public class CpioConnection implements Connection<CpioSession> {

    private List<CpioSession> sessions = new LinkedList();

    private URI uri;

    protected CpioConnection() {
    }

    @Override
    public CpioConnection setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public CpioSession createSession() throws IOException {
        CpioSession session = new CpioSession();
        session.setURI(uri);
        sessions.add(session);
        return session;
    }

    @Override
    public void close() throws IOException {
        for (CpioSession session : sessions) {
            session.close();
        }
    }
}
