package org.xbib.time.pretty.units;

import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;


public class Millennium extends ResourcesTimeUnit implements TimeUnit {

    public Millennium() {
        setMillisPerUnit(31556926000000L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Millennium";
    }

}
