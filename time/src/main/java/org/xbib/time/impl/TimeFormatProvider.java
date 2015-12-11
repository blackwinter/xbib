package org.xbib.time.impl;

import org.xbib.time.TimeFormat;
import org.xbib.time.TimeUnit;

/**
 * Produces time formats. Currently only to be used on Resource bundle implementations when used in
 * {@link ResourcesTimeFormat} instances.
 */
public interface TimeFormatProvider {
    /**
     * Return the appropriate {@link TimeFormat} for the given {@link TimeUnit}
     */
    public TimeFormat getFormatFor(TimeUnit t);
}
