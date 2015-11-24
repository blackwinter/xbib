package org.xbib.time.units;

import org.xbib.time.TimeUnit;
import org.xbib.time.impl.ResourcesTimeUnit;


public class Hour extends ResourcesTimeUnit implements TimeUnit {

    public Hour() {
        setMillisPerUnit(1000L * 60L * 60L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Hour";
    }

}
