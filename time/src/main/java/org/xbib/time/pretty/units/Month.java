package org.xbib.time.pretty.units;

import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;


public class Month extends ResourcesTimeUnit implements TimeUnit {

    public Month() {
        setMillisPerUnit(2629743830L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Month";
    }

}
