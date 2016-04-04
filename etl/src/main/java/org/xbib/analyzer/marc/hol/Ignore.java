package org.xbib.analyzer.marc.hol;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;

public class Ignore extends MARCEntity {
    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker, FieldList fields) {
        return true;
    }
}
