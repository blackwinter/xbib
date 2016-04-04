package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;

import java.io.IOException;
import java.util.Map;

public class GeneralInformation extends MARCEntity {

    private Map<String,Object> codes;

    private Map<String,Object> continuingresource;

    public GeneralInformation(Map<String,Object> params) {
        super(params);
        this.codes= (Map<String,Object>)params.get("codes");
        this.continuingresource= (Map<String,Object>)params.get("continuingresource");
    }

    /**
     * Example
     * "991118d19612006xx z||p|r ||| 0||||0ger c"
     *
     * "091130||||||||||||||||ger|||||||"
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean fields(MARCEntityQueue.MARCWorker worker, FieldList fields) throws IOException {
        for (Field field: fields) {
            String data = field.data();
            if (data == null) {
                continue;
            }
            String date1 = data.length() > 11 ? data.substring(7,11) : "0000";
            worker.state().getResource().add("date1", check(date1));
            String date2 = data.length() > 15 ? data.substring(11,15) : "0000";
            worker.state().getResource().add("date2", check(date2));
            for (int i = 0; i < data.length(); i++) {
                String ch = data.substring(i, i+1);
                if ("|".equals(ch) || " ".equals(ch)) {
                    continue;
                }
                if (codes != null) {
                    Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
                    if (q != null) {
                        String predicate = (String) q.get("_predicate");

                        String code = (String) q.get(ch);
                        if (code == null) {
                            logger.warn("unmapped code {} in field {} predicate {}", ch, field, predicate);
                        }
                        worker.state().getResource().add(predicate, code);
                    }
                }
                if (continuingresource != null) {
                    Map<String, Object> q = (Map<String, Object>) continuingresource.get(Integer.toString(i));
                    if (q != null) {
                        String predicate = (String) q.get("_predicate");
                        String code = (String) q.get(ch);
                        if (code == null) {
                            logger.warn("unmapped code {} in field {} predicate {}", ch, field, predicate);
                        }
                        worker.state().getResource().add(predicate, code);
                    }

                }
            }
        }
        return false;
    }

    // check for valid date, else return null
    private Integer check(String date) {
        try {
            int d = Integer.parseInt(date);
            if (d < 1450) {
                if (d > 0) {
                    logger.warn("very early date ignored: {}", d);
                }
                return null;
            }
            if (d == 9999) {
                return null;
            }
            return d;
        } catch (Exception e) {
            return null;
        }
    }
}
