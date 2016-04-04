package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.rdf.Resource;

public class Subject extends MARCEntity {

    @Override
    public String data(MARCEntityQueue.MARCWorker worker,
                       String predicate, Resource resource, String property, String value) {
        if ("identifier".equals(property)) {
            if (value.startsWith("(DE-588)")) {
                resource.add("identifierGND", value.substring(8).replaceAll("\\-","").toLowerCase());
                return null;
            } else if (value.startsWith("(DE-600)")) {
                resource.add("identifierZDB", value.substring(8).replaceAll("\\-","").toLowerCase());
                return null;
            } else if (value.startsWith("(DE-101)")) {
                resource.add("identifierDNB", value.substring(8).replaceAll("\\-","").toLowerCase());
                return null;
            }
            return value.replaceAll("\\-","").toLowerCase();
        }
        return value;
    }

}
