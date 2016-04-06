package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;

import java.io.IOException;
import java.util.Map;

public class AlternateGraphicRepresentation extends MARCEntity {

    public AlternateGraphicRepresentation(Map<String, Object> params) {
        super(params);
    }

    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker,
                          FieldList fields) throws IOException {
        // http://www.loc.gov/marc/bibliographic/ecbdcntf.html
        // find linkage: $6 [linking tag]-[occurrence number]/[script identification code]/[field orientation code]
        for (Field field : fields) {
            if ("6".equals(field.subfieldId())) {
                String tag = field.data().substring(0,3);
                field.tag(tag);
            }
        }
        return super.fields(worker, fields);
    }
}
