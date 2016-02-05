package org.xbib.graphics.vector.pdf;

import java.util.LinkedHashMap;
import java.util.Map;

public class PDFObject {
    public final int id;
    public final int version;
    public final Map<String, Object> dict;
    public final Payload payload;

    public PDFObject(int id, int version, Map<String, Object> dict, Payload payload) {
        this.dict = new LinkedHashMap<String, Object>();
        this.id = id;
        this.version = version;
        this.payload = payload;
        if (dict != null) {
            this.dict.putAll(dict);
        }
    }
}

