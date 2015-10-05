package org.xbib.pipeline;

import java.net.URI;

public class URIPipelineRequest implements PipelineRequest<URI> {

    private URI uri;

    @Override
    public URI get() {
        return uri;
    }

    @Override
    public URIPipelineRequest set(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}