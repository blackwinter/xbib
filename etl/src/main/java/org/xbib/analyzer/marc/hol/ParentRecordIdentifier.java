package org.xbib.analyzer.marc.hol;

import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;

import java.io.IOException;

public class ParentRecordIdentifier extends org.xbib.analyzer.marc.bib.Identifier {

    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker,
                          FieldList fields) throws IOException {
        String predicate = getClass().getSimpleName();
        if (getParams().containsKey("_predicate")) {
            predicate = (String) getParams().get("_predicate");
        }
        worker.state().getResource().add(predicate, fields.getLast().data());
        return false;
    }
}
