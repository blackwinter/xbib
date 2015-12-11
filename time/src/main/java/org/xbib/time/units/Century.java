package org.xbib.time.units;

import org.xbib.time.TimeUnit;
import org.xbib.time.impl.ResourcesTimeUnit;


public class Century extends ResourcesTimeUnit implements TimeUnit {

    public Century() {
        setMillisPerUnit(3155692597470L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Century";
    }
}
