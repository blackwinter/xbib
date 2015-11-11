package org.xbib.util.concurrent;

import java.net.URI;

public class URIWorkerRequest implements WorkerRequest<URI> {

    private URI uri;

    @Override
    public URI get() {
        return uri;
    }

    @Override
    public URIWorkerRequest set(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}