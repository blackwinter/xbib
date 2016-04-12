package org.xbib.openurl.descriptors;

import org.w3c.dom.Document;

import java.net.URI;

/**
 * This class represents the By-Value Metadata Descriptor described
 * in section 5.2.3 of the
 * <a href="http://www.openurl.info/registry/docs/pdf/z39_88_2004.pdf">OpenURL 1.0 spec</a>.
 */
public interface ByValueMetadataXml extends Descriptor {

    /**
     * Get an identifier
     *
     * @return a URI indicating the metadata format
     * represented in the fieldMap.
     */
    URI getValFmt();

    /**
     * Get the metadata elements
     *
     * @return the XML document
     */
    Document getDocument();
}
