package org.xbib.time.units;

import org.xbib.time.TimeUnit;
import org.xbib.time.impl.ResourcesTimeUnit;


public class JustNow extends ResourcesTimeUnit implements TimeUnit {

    public JustNow() {
        setMaxQuantity(1000L * 60L * 5L);
    }

    @Override
    protected String getResourceKeyPrefix() {
        return "JustNow";
    }

}
