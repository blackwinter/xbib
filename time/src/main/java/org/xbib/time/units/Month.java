package org.xbib.time.units;

import org.xbib.time.TimeUnit;
import org.xbib.time.impl.ResourcesTimeUnit;


public class Month extends ResourcesTimeUnit implements TimeUnit {

    public Month() {
        setMillisPerUnit(2629743830L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Month";
    }

}
