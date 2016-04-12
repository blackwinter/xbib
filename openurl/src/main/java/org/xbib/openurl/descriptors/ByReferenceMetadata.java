package org.xbib.openurl.descriptors;

import java.net.URI;
import java.net.URL;

/**
 * This class represents the By-Reference Metadata Descriptor described
 * in section 5.2.2 of the
 * <a href="http://www.openurl.info/registry/docs/pdf/z39_88_2004.pdf">OpenURL 1.0 spec</a>.
 */
public interface ByReferenceMetadata extends Descriptor {

    /**
     * Get an identifier for the type of key/value pairs in the fieldMap
     *
     * @return a URI indicating the metadata format
     * represented in the fieldMap.
     */
    URI getRefFmt();

    /**
     * @return a URL pointing to the location of the metadata
     */
    URL getRef();
}
