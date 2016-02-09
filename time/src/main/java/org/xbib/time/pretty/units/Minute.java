package org.xbib.time.pretty.units;

import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;


public class Minute extends ResourcesTimeUnit implements TimeUnit {

    public Minute() {
        setMillisPerUnit(1000L * 60L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Minute";
    }

}
