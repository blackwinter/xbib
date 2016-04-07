package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.Map;

public class RecordIdentifier extends MARCEntity {

    private String prefix = "";

    public RecordIdentifier(Map<String,Object> params) {
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
        String v = prefix + value.trim();
        worker.state().setRecordIdentifier(v);
        try {
            worker.state().getResource().newResource("xbib").add("uid", v);
        } catch (IOException e) {
            // ignore
        }
        return v;
    }
}
