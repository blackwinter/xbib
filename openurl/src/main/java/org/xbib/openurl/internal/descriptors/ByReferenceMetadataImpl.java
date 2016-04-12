package org.xbib.openurl.internal.descriptors;

import org.xbib.openurl.descriptors.ByReferenceMetadata;

import java.net.URI;
import java.net.URL;

/**
 * A Descriptor that details properties of an Entity
 * by the combination of: (1) a URI reference to a Metadata
 * Format and (2) the network location of a particular instance
 * of metadata about the Entity, the metadata being
 * expressed according to the indicated Metadata Format.
 */
public class ByReferenceMetadataImpl implements ByReferenceMetadata {
    private final URI ref_fmt;
    private final URL ref;

    /**
     * Constructs a By-Reference Metadata descriptor
     *
     * @param ref_fmt A URI reference to a <em>Metadata Format</em>.
     * @param ref     The network location of a particular instance of
     *                metadata about the <em>Entity</em>, the metadata being
     *                expressed according to the indicated <em>Metadata Format</em>.
     */
    public ByReferenceMetadataImpl(URI ref_fmt, URL ref) {
        this.ref_fmt = ref_fmt;
        this.ref = ref;
    }

    public URI getRefFmt() {
        return ref_fmt;
    }

    public URL getRef() {
        return ref;
    }
}
