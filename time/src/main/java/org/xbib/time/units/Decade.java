package org.xbib.time.units;

import org.xbib.time.TimeUnit;
import org.xbib.time.impl.ResourcesTimeUnit;


public class Decade extends ResourcesTimeUnit implements TimeUnit {

    public Decade() {
        setMillisPerUnit(315569259747L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Decade";
    }

}
