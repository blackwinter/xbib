package org.xbib.analyzer.marc.zdb.hol;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;

import java.util.Map;

public class Ignore extends MARCEntity {

    public Ignore(Map<String, Object> params) {
        super(params);
    }

    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker, FieldList fields) {
        return true; // ignore!
    }
}
