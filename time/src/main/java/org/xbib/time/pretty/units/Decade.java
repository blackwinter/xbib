package org.xbib.time.pretty.units;

import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;


public class Decade extends ResourcesTimeUnit implements TimeUnit {

    public Decade() {
        setMillisPerUnit(315569259747L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Decade";
    }

}
