package org.xbib.time.pretty.i18n;

import org.xbib.time.pretty.TimeUnit;

public abstract class ResourcesTimeUnit implements TimeUnit {
    private long maxQuantity = 0;
    private long millisPerUnit = 1;

    abstract protected String getResourceKeyPrefix();

    protected String getResourceBundleName() {
        return Resources.class.getName();
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
