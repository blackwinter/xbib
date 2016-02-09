package org.xbib.time.pretty.units;

import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;


public class JustNow extends ResourcesTimeUnit implements TimeUnit {

    public JustNow() {
        setMaxQuantity(1000L * 60L * 5L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "JustNow";
    }

}
