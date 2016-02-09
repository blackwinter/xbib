package org.xbib.time.pretty.units;

import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;


public class Millisecond extends ResourcesTimeUnit implements TimeUnit {

    public Millisecond() {
        setMillisPerUnit(1);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Millisecond";
    }

}
