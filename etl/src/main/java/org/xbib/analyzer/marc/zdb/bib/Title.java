package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.rdf.Resource;

import java.util.Map;

public class Title extends MARCEntity {

    public Title(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String data(MARCEntityQueue.MARCWorker worker,
                       String predicate, Resource resource, String property, String value) {
        // let's make "sorting" marker characters visible again
        // 0098 = START OF STRING, 009c = END OF STRING
        // --> 00ac = negation sign
        return value.replace('\u0098', '\u00ac').replace('\u009c', '\u00ac');
    }
}
