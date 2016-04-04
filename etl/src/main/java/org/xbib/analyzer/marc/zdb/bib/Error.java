package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;

public class Error extends MARCEntity {

    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker, FieldList fields) {
        return true;
    }
}
