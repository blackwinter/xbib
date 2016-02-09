package org.xbib.time.pretty.units;

import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;


public class Hour extends ResourcesTimeUnit implements TimeUnit {

    public Hour() {
        setMillisPerUnit(1000L * 60L * 60L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Hour";
    }

}
