package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;

import java.io.IOException;
import java.util.Map;

public class PhysicalDescriptionCode extends MARCEntity {

    public PhysicalDescriptionCode(Map<String, Object> params) {
        super(params);
    }

    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker, FieldList fields) throws IOException {
        Map<String,Object> codes = (Map<String,Object>) getParams().get("codes");
        if (codes != null) {
            // position 0 is the selector
            codes = (Map<String,Object>)codes.get("0");
        }
        if (codes == null) {
            return false;
        }
        for (Field field: fields) {
            if (field == null) {
                continue;
            }
            String data = field.data();
            if (data == null) {
                continue;
            }
            Map<String,Object> m = (Map<String,Object>)codes.get(data.substring(0,1));
            if (m == null) {
                continue;
            }
            // transform all codes except position 0
            String predicate = (String)m.get("_predicate");
            for (int i = 1; i < data.length(); i++) {
                String ch = data.substring(i,i+1);
                Map<String,Object> q = (Map<String,Object>)m.get(Integer.toString(i));
                if (q != null) {
                    String code = (String)q.get(ch);
                    worker.state().getResource().add(predicate, code);
                }
            }
        }
        return false;
    }
    
}
