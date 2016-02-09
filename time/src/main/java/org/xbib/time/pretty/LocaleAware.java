package org.xbib.time.pretty;

import java.util.Locale;

/**
 * An object that behaves differently for various {@link Locale} settings.
 */
public interface LocaleAware<TYPE> {
    /**
     * Set the {@link Locale} for which this instance should behave in.
     */
    TYPE setLocale(Locale locale);

}
