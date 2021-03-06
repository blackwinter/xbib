package org.xbib.tools.merge.holdingslicenses;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.etl.support.StatusCodeMapper;
import org.xbib.etl.support.ValueMaps;
import org.xbib.tools.merge.holdingslicenses.support.BlackListedISIL;
import org.xbib.tools.merge.holdingslicenses.support.MappedISIL;

import java.io.IOException;
import java.util.Map;

public class WithHoldingAndLicensesResourcesTest extends Assert {

    @Test
    @SuppressWarnings("unchecked")
    public void testResources() throws IOException {
        BlackListedISIL isilbl;
        MappedISIL isilMapped;
        StatusCodeMapper statusCodeMapper;

        isilbl = new BlackListedISIL();
        isilbl.buildLookup(getClass().getResourceAsStream("isil.blacklist"));
        assertFalse(isilbl.lookup().isEmpty());

        isilMapped = new MappedISIL();
        isilMapped.buildLookup(getClass().getResourceAsStream("isil.map"));
        assertFalse(isilMapped.lookup().isEmpty());

        ValueMaps valueMaps = new ValueMaps();
        Map<String,Object> statuscodes = valueMaps.getMap("org/xbib/analyzer/mab/status.json", "status");
        statusCodeMapper = new StatusCodeMapper();
        statusCodeMapper.add(statuscodes);
        assertFalse(statusCodeMapper.getMap().isEmpty());
    }

}
