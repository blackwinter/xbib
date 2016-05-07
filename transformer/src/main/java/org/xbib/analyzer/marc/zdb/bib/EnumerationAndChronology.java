package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.etl.support.EnumerationAndChronologyHelper;
import org.xbib.marc.Field;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class EnumerationAndChronology extends MARCEntity {

    private List<Pattern> movingwallPatterns;

    public EnumerationAndChronology(Map<String,Object> params) {
        super(params);
        List<String> movingwalls = (List<String>) params.get("movingwall");
        if (movingwalls != null) {
            List<Pattern> p = new LinkedList<>();
            for (String movingwall : movingwalls) {
                p.add(Pattern.compile(movingwall));
            }
            setMovingwallPatterns(p);
        }
    }

    public void setMovingwallPatterns(List<Pattern> p) {
        this.movingwallPatterns = p;
    }

    public List<Pattern> getMovingwallPatterns() {
        return this.movingwallPatterns;
    }

    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker, FieldList fields) throws IOException {
        EnumerationAndChronologyHelper eac = new EnumerationAndChronologyHelper();
        for (Field field : fields) {
            String data = field.data();
            if (data == null || data.isEmpty()) {
                continue;
            }
            if ("a".equals(field.subfieldId())) {
                worker.getWorkerState().getResource().add("TextualEnumerationAndChronology", data);
                Resource r = worker.getWorkerState().getResource().newResource("EnumerationAndChronology");
                Resource parsedHoldings = eac.parse(data, r, getMovingwallPatterns());
                if (!parsedHoldings.isEmpty()) {
                    Set<Integer> dates = eac.dates(r.id(), parsedHoldings);
                    for (Integer date : dates) {
                        worker.getWorkerState().getResource().add("Dates", date);
                    }
                }
            }
        }
        return false;
    }
}
