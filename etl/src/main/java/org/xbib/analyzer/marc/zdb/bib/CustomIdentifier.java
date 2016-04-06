package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.rdf.Resource;

import java.util.Map;

public class CustomIdentifier extends MARCEntity {

    public CustomIdentifier(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String data(MARCEntityQueue.MARCWorker worker,
                       String predicate, Resource resource, String property, String value) {
        if ("IdentifierZDB".equals(predicate)) {
            if ("value".equals(property)) {
                resource.add("identifierZDB",value.replaceAll("\\-", "").toLowerCase());
                return null;
            }
        } else if ("IdentifierDNB".equals(predicate)) {
            if ("value".equals(property)) {
                resource.add("identifierDNB",value.replaceAll("\\-", "").toLowerCase());
                return null;
            }
        }
        return value;
    }
}
