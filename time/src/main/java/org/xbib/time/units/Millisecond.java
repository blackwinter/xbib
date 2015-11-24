package org.xbib.time.units;

import org.xbib.time.TimeUnit;
import org.xbib.time.impl.ResourcesTimeUnit;


public class Millisecond extends ResourcesTimeUnit implements TimeUnit {

    public Millisecond() {
        setMillisPerUnit(1);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Millisecond";
    }

}
