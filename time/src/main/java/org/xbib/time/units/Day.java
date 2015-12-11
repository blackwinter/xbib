package org.xbib.time.units;

import org.xbib.time.TimeUnit;
import org.xbib.time.impl.ResourcesTimeUnit;


public class Day extends ResourcesTimeUnit implements TimeUnit {

    public Day() {
        setMillisPerUnit(1000L * 60L * 60L * 24L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Day";
    }

}
