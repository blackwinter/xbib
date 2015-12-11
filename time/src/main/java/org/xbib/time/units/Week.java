package org.xbib.time.units;

import org.xbib.time.TimeUnit;
import org.xbib.time.impl.ResourcesTimeUnit;


public class Week extends ResourcesTimeUnit implements TimeUnit {

    public Week() {
        setMillisPerUnit(1000L * 60L * 60L * 24L * 7L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Week";
    }

}
