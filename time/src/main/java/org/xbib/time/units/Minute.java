package org.xbib.time.units;

import org.xbib.time.TimeUnit;
import org.xbib.time.impl.ResourcesTimeUnit;


public class Minute extends ResourcesTimeUnit implements TimeUnit {

    public Minute() {
        setMillisPerUnit(1000L * 60L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Minute";
    }

}
