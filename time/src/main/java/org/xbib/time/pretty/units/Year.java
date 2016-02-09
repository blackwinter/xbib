package org.xbib.time.pretty.units;

import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;


public class Year extends ResourcesTimeUnit implements TimeUnit {

    public Year() {
        setMillisPerUnit(2629743830L * 12L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Year";
    }

}
