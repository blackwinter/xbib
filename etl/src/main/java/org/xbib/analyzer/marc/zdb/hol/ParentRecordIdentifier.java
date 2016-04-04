package org.xbib.analyzer.marc.zdb.hol;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.Map;

public class ParentRecordIdentifier extends MARCEntity {

    private String prefix = "";

    public ParentRecordIdentifier(Map<String,Object> params) {
        super(params);
        if (params.containsKey("_prefix")) {
            this.prefix = params.get("_prefix").toString();
        }
        if (params.containsKey("catalogid")) {
            this.prefix = "(" + params.get("catalogid").toString() + ")";
        }
    }

    @Override
    public String data(MARCEntityQueue.MARCWorker worker,
                       String predicate, Resource resource, String property, String value) throws IOException {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return prefix + value.trim();
    }
}
