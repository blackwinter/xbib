package org.xbib.openurl.descriptors;

import java.net.URI;

/**
 * An Identifier is a descriptor that can be defined by an URI.
 */
public interface Identifier extends Descriptor {

    /**
     * Return URI of this identifier.
     *
     * @return the URI of this identifier
     */
    URI getURI();

}
