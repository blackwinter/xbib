package org.xbib.time.units;

import org.xbib.time.TimeUnit;
import org.xbib.time.impl.ResourcesTimeUnit;


public class Year extends ResourcesTimeUnit implements TimeUnit {

    public Year() {
        setMillisPerUnit(2629743830L * 12L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Year";
    }

}
