package org.xbib.time.units;

import org.xbib.time.TimeUnit;
import org.xbib.time.impl.ResourcesTimeUnit;


public class Second extends ResourcesTimeUnit implements TimeUnit {

    public Second() {
        setMillisPerUnit(1000L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Second";
    }

}
