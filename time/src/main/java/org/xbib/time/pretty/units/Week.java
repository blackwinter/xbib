package org.xbib.time.pretty.units;

import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;


public class Week extends ResourcesTimeUnit implements TimeUnit {

    public Week() {
        setMillisPerUnit(1000L * 60L * 60L * 24L * 7L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Week";
    }

}
