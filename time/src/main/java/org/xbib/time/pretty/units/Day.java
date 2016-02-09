package org.xbib.time.pretty.units;

import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;


public class Day extends ResourcesTimeUnit implements TimeUnit {

    public Day() {
        setMillisPerUnit(1000L * 60L * 60L * 24L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Day";
    }

}
