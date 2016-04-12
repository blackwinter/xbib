package org.xbib.openurl;

import java.io.Writer;

/**
 * Interface for Serialization of OpenURL objects.
 */
public interface Serializer {
    String FMT_XML_URI_PREFIX = "info:ofi/fmt:xml:xsd:";
    String[] authorTypeKeys = new String[]{"au", "aucorp"};
    String[] detailedAuthorTypeKeys = new String[]{"aulast", "aufirst", "auinit", "auinit1", "auinitm", "ausuffix"};

    /**
     * Serialize context objects.
     *
     * @param contexts
     * @param writer
     * @throws OpenURLException
     */
    void serializeContextObjects(ContextObject[] contexts, Writer writer) throws OpenURLException;

}
