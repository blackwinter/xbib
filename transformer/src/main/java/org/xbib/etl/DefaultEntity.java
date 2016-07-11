package org.xbib.etl;

import org.xbib.marc.FieldList;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.Map;

public class DefaultEntity<W extends EntityQueue.EntityWorker> implements Entity {

    private final Map<String,Object> params;

    public DefaultEntity(Map<String,Object> params) {
        this.params = params;
    }

    public Map<String,Object> getParams() {
        return params;
    }

    /**
     * Process field list
     *
     * @param worker the worker
     * @param fields the fields
     * @return true if processing has been completed and should not continue at this point,
     * false if it should continue
     */
    public boolean fields(W worker, FieldList fields) throws IOException {
        return false;
    }

    /**
     * Transform value in context
     *
     * @param worker the worker
     * @param resourcePredicate the resource predicate
     * @param resource the resource
     * @param property the property
     * @param value the value
     * @return the transformed value
     * @throws IOException
     */
    public String data(W worker, String resourcePredicate, Resource resource, String property, String value) throws IOException {
        return value;
    }

}
