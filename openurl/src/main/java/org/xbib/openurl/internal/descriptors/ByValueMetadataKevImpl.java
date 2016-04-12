package org.xbib.openurl.internal.descriptors;

import org.xbib.openurl.descriptors.ByValueMetadataKev;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This descriptor specifies properties of an Entity
 * by the combination of: (1) a URI reference to a Metadata
 * Format and (2) a particular instance of metadata about the
 * Entity, expressed according to the indicated Metadata
 * Format.
 */
public class ByValueMetadataKevImpl implements ByValueMetadataKev {

    private final static String[] fieldKeys = new String[]{
            "atitle",
            "title",
            "jtitle",
            "stitle",
            "date",
            "chron",
            "ssn",
            "quarter",
            "volume",
            "part",
            "issue",
            "spage",
            "epage",
            "pages",
            "artnum",
            "issn",
            "eissn",
            "isbn",
            "coden",
            "sici",
            "genre",
            "aulast",
            "aufirst",
            "auinit",
            "auinit1",
            "auinitm"
    };
    private URI val_fmt;
    private LinkedHashMap<String, String[]> fieldMap = new LinkedHashMap<>();

    /**
     * Constructs a By-Value Metadata descriptor
     *
     * @param val_fmt A URI reference to a Metadata Format.
     * @param prefix  The KEV key prefix to be extracted from map
     * @param map     will be extracted according to the specified prefix.
     */
    public ByValueMetadataKevImpl(URI val_fmt, String prefix, Map<String, String[]> map) {
        this.val_fmt = val_fmt;
        Set<String> set = map.keySet();
        for (String fieldKey : fieldKeys) {
            String k = prefix + "." + fieldKey;
            if (set.contains(k)) {
                fieldMap.put(k, map.get(k));
            }
        }
    }

    public ByValueMetadataKevImpl(Map<String, String[]> map)
            throws URISyntaxException {
        Set<String> set = map.keySet();
        for (String fieldKey : fieldKeys) {
            String k = "rft." + fieldKey;
            if (set.contains(k)) {
                fieldMap.put(k, map.get(k));
                // special handling
                if ("genre".equals(fieldKey)) {
                    this.val_fmt = URI.create("info:ofi/fmt:kev:mtx:" + map.get("genre")[0]);
                } else if ("title".equals(fieldKey) && "info:ofi/fmt:kev:mtx:journal".equals(this.val_fmt.toString())) {
                    fieldMap.put("rft.jtitle", map.get(fieldKey));
                }
            }
        }
    }

    public URI getValFmt() {
        return val_fmt;
    }

    public LinkedHashMap<String, String[]> getFieldMap() {
        return fieldMap;
    }
}
