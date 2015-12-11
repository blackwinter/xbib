package org.xbib.time.units;

import org.xbib.time.TimeUnit;
import org.xbib.time.impl.ResourcesTimeUnit;


public class Millennium extends ResourcesTimeUnit implements TimeUnit {

    public Millennium() {
        setMillisPerUnit(31556926000000L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Millennium";
    }

}
