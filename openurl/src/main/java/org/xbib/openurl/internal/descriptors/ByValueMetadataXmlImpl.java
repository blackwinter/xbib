package org.xbib.openurl.internal.descriptors;

import org.w3c.dom.Document;
import org.xbib.openurl.descriptors.ByValueMetadataXml;

import java.net.URI;

/**
 * A <em>Descriptor</em> that specifies properties of an <em>Entity</em>
 * by the combination of: (1) a URI reference to a <em>Metadata
 * Format</em> and (2) a particular instance of metadata about the
 * <em>Entity</em>, expressed according to the indicated <em>Metadata
 * Format</em>.
 */
public class ByValueMetadataXmlImpl implements ByValueMetadataXml {

    private URI val_fmt;
    private Document document;

    /**
     * Constructs a By-Value Metadata descriptor
     *
     * @param val_fmt  A URI reference to a <em>Metadata Format</em>.
     *                 will be extracted according to the specified prefix.
     * @param document XML representing an OpenURL request
     */
    public ByValueMetadataXmlImpl(URI val_fmt, Document document) {
        this.val_fmt = val_fmt;
        this.document = document;
    }

    public URI getValFmt() {
        return val_fmt;
    }

    public Document getDocument() {
        return document;
    }
}
