package org.xbib.analyzer.marc.hol;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;

import java.io.IOException;
import java.util.Map;

public class RecordLeader extends MARCEntity {

    private Map<String,Object> codes;

    private String predicate;

    public RecordLeader(Map<String,Object> params) {
        super(params);
        this.codes= (Map<String,Object>)params.get("codes");
        this.predicate = params.containsKey("_predicate") ?
                (String)params.get("_predicate") : "leader";
    }

    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker, FieldList fields) throws IOException {
        String value = fields.getLast().data();
        worker.state().setLabel(value);
        if (codes == null) {
            return false;
        }
        for (Map.Entry<String,Object> entry : codes.entrySet()) {
            String k = entry.getKey();
            int pos = Integer.parseInt(k);
            Map<String,String> v = (Map<String,String>)codes.get(k);
            String code = value.length() > pos ? value.substring(pos,pos+1) : "";
            if (v.containsKey(code)) {
                worker.state().getResource().add(predicate, v.get(code));
            }
        }
        return false;
    }
}
