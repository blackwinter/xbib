package org.xbib.time.pretty;

import org.xbib.time.pretty.i18n.ResourcesTimeFormat;

/**
 * Produces time formats. Currently only to be used on Resource bundle implementations when used in
 * {@link ResourcesTimeFormat} instances.
 */
public interface TimeFormatProvider {
    /**
     * Return the appropriate {@link TimeFormat} for the given {@link TimeUnit}
     */
    TimeFormat getFormatFor(TimeUnit t);
}
