package org.xbib.analyzer.marc.hol;

import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;

import java.io.IOException;
import java.util.Map;

public class ParentRecordIdentifier extends org.xbib.analyzer.marc.bib.Identifier {

    public ParentRecordIdentifier(Map<String, Object> params) {
        super(params);
    }

    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker,
                          FieldList fields) throws IOException {
        String predicate = getClass().getSimpleName();
        if (getParams().containsKey("_predicate")) {
            predicate = (String) getParams().get("_predicate");
        }
        worker.getWorkerState().getResource().add(predicate, fields.getLast().data());
        return false;
    }
}
