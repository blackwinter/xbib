package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.rdf.Resource;

public class Identifier extends MARCEntity {

    @Override
    public String data(MARCEntityQueue.MARCWorker worker,
                       String predicate, Resource resource, String property, String value) {
        if ("IdentifierZDB".equals(predicate)) {
            if ("value".equals(property)) {
                if (value.startsWith("(DE-599)")) {
                    resource.add("identifierEKI", value.substring(8));
                    return null;
                } else if (value.startsWith("(OCoLC)")) {
                    resource.add("identifierOCLC", value.substring(7).replaceAll("\\-", "").toLowerCase());
                    return null;
                } else {
                    //(NL-LiSWE) = Swets & Zeitlinger
                    /*int pos = value.indexOf(')');
                    String prefix = pos > 0 ? value.substring(1,pos).replaceAll("\\-", "").toUpperCase() : "";
                    value = pos > 0 ? value.substring(pos + 1) : value;
                    resource.add("identifier" + prefix, value.replaceAll("\\-", "").toLowerCase());*/
                    logger.warn("unprocessed identifier: {}", value);
                    return null;
                }
            }
        }
        return value;
    }
}
