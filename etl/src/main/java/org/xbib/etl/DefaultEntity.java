package org.xbib.etl;

import java.util.Collections;
import java.util.Map;

public class DefaultEntity implements Entity {

    private final Map<String,Object> params;

    @SuppressWarnings("unchecked")
    public DefaultEntity() {
        this.params = Collections.EMPTY_MAP;
    }

    public DefaultEntity(Map<String,Object> params) {
        this.params = params;
    }

    public Map<String,Object> getParams() {
        return params;
    }

}
