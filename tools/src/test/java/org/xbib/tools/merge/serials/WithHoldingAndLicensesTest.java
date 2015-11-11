package org.xbib.tools.merge.serials;

import org.testng.annotations.Test;
import org.xbib.etl.support.StatusCodeMapper;
import org.xbib.etl.support.ValueMaps;
import org.xbib.tools.merge.serials.support.BlackListedISIL;
import org.xbib.tools.merge.serials.support.MappedISIL;

import java.io.IOException;
import java.util.Map;

import static org.testng.Assert.assertFalse;

public class WithHoldingAndLicensesTest {

    @Test
    public void test() throws IOException {
        BlackListedISIL isilbl;
        MappedISIL isilMapped;
        StatusCodeMapper statusCodeMapper;

        isilbl = new BlackListedISIL();
        isilbl.buildLookup(getClass().getResourceAsStream("isil.blacklist"));
        assertFalse(isilbl.lookup().isEmpty());

        isilMapped = new MappedISIL();
        isilMapped.buildLookup(getClass().getResourceAsStream("isil.map"));
        assertFalse(isilMapped.lookup().isEmpty());

        Map<String,Object> statuscodes = ValueMaps.getMap(getClass().getClassLoader(),
                "org/xbib/analyzer/mab/status.json", "status");
        statusCodeMapper = new StatusCodeMapper();
        statusCodeMapper.add(statuscodes);
        assertFalse(statusCodeMapper.getMap().isEmpty());
    }

}
