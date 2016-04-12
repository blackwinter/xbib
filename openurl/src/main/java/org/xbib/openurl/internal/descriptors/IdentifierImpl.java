package org.xbib.openurl.internal.descriptors;

import org.xbib.openurl.descriptors.Identifier;

import java.net.URI;

public class IdentifierImpl implements Identifier {

    private URI uri;

    public IdentifierImpl(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public String toString() {
        return uri != null ? uri.toString() : null;
    }

    @Override
    public boolean equals(Object o) {
        return (uri != null && o instanceof Identifier) && uri.equals(((Identifier) o).getURI());
    }

    @Override
    public int hashCode() {
        return uri != null ? uri.hashCode() : super.hashCode();
    }

}
