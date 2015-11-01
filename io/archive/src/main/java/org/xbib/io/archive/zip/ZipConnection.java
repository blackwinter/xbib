package org.xbib.io.archive.zip;

import org.xbib.io.Connection;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class ZipConnection implements Connection<ZipSession> {

    private List<ZipSession> sessions = new LinkedList();

    private URI uri;

    protected ZipConnection() {
    }

    @Override
    public ZipConnection setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public ZipSession createSession() throws IOException {
        ZipSession session = new ZipSession();
        session.setURI(uri);
        sessions.add(session);
        return session;
    }

    @Override
    public void close() throws IOException {
        for (ZipSession session : sessions) {
            session.close();
        }
    }
}
