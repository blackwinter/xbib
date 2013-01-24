package org.xbib.rdf.jsonld;

import java.util.HashMap;
import java.util.Map;

public class JSONLDProcessingError extends Exception {

    public enum Error {

        SYNTAX_ERROR, PARSE_ERROR, RDF_ERROR, CONTEXT_URL_ERROR, INVALID_URL, COMPACT_ERROR
    }
    String message;
    Map details;

    public JSONLDProcessingError(String string, Map<String, Object> details) {
        message = string;
        details = details;
    }

    public JSONLDProcessingError(String string) {
        message = string;
        details = new HashMap();
    }

    public JSONLDProcessingError setDetail(String string, Object val) {
        details.put(string, val);
       // System.out.println("ERROR DETAIL: " + string + ": " + val.toString());
        return this;
    }

    public JSONLDProcessingError setType(Error error) {
        return this;
    }
;
}
