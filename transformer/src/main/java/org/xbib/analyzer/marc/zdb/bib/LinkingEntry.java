package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.rdf.Resource;

import java.util.Map;

public class LinkingEntry extends MARCEntity {

    public LinkingEntry(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String data(MARCEntityQueue.MARCWorker worker,
                       String predicate, Resource resource, String property, String value) {
        if ("id".equals(property) ) {
            if (value.startsWith("(DE-600)")) {
                resource.add("identifierZDB", value.substring(8).replaceAll("\\-","").toLowerCase());
                return null;
            } else if (value.startsWith("(DE-101)")) {
                // DNB-ID 'X' always upper case(!)
                resource.add("identifierDNB", value.substring(8).replaceAll("\\-","").toUpperCase());
                return null;
            }
            return value.replaceAll("\\-","").toLowerCase();
        } else if ("title".equals(property)) {
            return value.replace('\u0098', '\u00ac').replace('\u009c', '\u00ac');
        }
        return value;
    }
}
