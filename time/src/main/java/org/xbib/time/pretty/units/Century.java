package org.xbib.time.pretty.units;

import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;


public class Century extends ResourcesTimeUnit implements TimeUnit {

    public Century() {
        setMillisPerUnit(3155692597470L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "Century";
    }
}
