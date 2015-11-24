package org.xbib.time.impl;

import org.xbib.time.TimeUnit;


public abstract class ResourcesTimeUnit implements TimeUnit {
    private long maxQuantity = 0;
    private long millisPerUnit = 1;

    /**
     * Return the name of the resource bundle from which this unit's format should be loaded.
     */
    abstract protected String getResourceKeyPrefix();

    protected String getResourceBundleName() {
        return "org.xbib.time.i18n.Resources";
    }

    @Override
    public long getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(long maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    @Override
    public long getMillisPerUnit() {
        return millisPerUnit;
    }

    public void setMillisPerUnit(long millisPerUnit) {
        this.millisPerUnit = millisPerUnit;
    }

}
