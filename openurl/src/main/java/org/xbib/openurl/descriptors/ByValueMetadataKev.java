package org.xbib.openurl.descriptors;

import java.net.URI;
import java.util.Map;

/**
 * This class represents the By-Value Metadata Descriptor described
 * in section 5.2.3 of the
 * <a href="http://www.openurl.info/registry/docs/pdf/z39_88_2004.pdf">OpenURL 1.0 spec</a>.
 */
public interface ByValueMetadataKev extends Descriptor {

    /**
     * Get an identifier for the type of key/value pairs in the fieldMap
     *
     * @return a URI indicating the metadata format represented in the fieldMap.
     */
    URI getValFmt();

    /**
     * Get the metadata elements
     *
     * @return an ordered map of key/value metadata elements
     */
    Map<String, String[]> getFieldMap();
}
