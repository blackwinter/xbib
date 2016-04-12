package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;

import java.util.Map;

public class Error extends MARCEntity {

    public Error(Map<String, Object> params) {
        super(params);
    }

    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker, FieldList fields) {
        return true;
    }
}
