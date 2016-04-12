package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.Map;

public class ClassificationNumber extends MARCEntity {

    private Map<String,Object> ddc;

    public ClassificationNumber(Map<String,Object> params) {
        super(params);
        ddc = (Map<String, Object>) getParams().get("ddc");
    }

    @Override
    public String data(MARCEntityQueue.MARCWorker worker,
                       String predicate, Resource resource, String property, String value) throws IOException {
        if (ddc != null && ddc.containsKey(value)) {
            resource.add(property + "Text", (String)ddc.get(value));
        }
        return value;
    }
}
