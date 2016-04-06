package org.xbib.analyzer.marc.zdb.hol;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.rdf.Resource;

import java.util.Map;

public class CustomIdentifier extends MARCEntity {

    public CustomIdentifier(Map<String, Object> params) {
        super(params);
    }

    /**
     * Construct purified ZDB-ID for fast term search.
     *
     * Type flag DE-600 is after the value:
     *
     * tag=016 ind=7  subf=a data=13-9
     * tag=016 ind=7  subf=2 data=DE-600
     *
     * Idea: if type flag indicates ZDB, look up existing value, and add a property 'identifierZDB'.
     *
     */

    @Override
    public String data(MARCEntityQueue.MARCWorker worker,
                       String predicate, Resource resource, String property, String value) {
        if ("IdentifierZDB".equals(value) && "type".equals(property)) {
            String v = resource.objects("value").get(0).toString();
            resource.add("identifierZDB", v.replaceAll("\\-", "").toLowerCase());
            return value;
        }
        return value;
    }
}
