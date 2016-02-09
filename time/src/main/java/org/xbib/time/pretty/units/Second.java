package org.xbib.time.pretty.units;

import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;


public class Second extends ResourcesTimeUnit implements TimeUnit {

    public Second() {
        setMillisPerUnit(1000L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Second";
    }

}
