package org.xbib.analyzer.marc.hol;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;

import java.io.IOException;
import java.util.Map;

public class RecordIdentifier extends MARCEntity {

    public RecordIdentifier(Map<String, Object> params) {
        super(params);
    }

    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker, FieldList fields) throws IOException {
        String value = fields.getLast().data();
        String predicate = getClass().getSimpleName();
        if (getParams().containsKey("_predicate")) {
            predicate = (String) getParams().get("_predicate");
        }
        worker.state().setRecordIdentifier(value);
        worker.state().getResource().add(predicate, value);
        return false;
    }

}
