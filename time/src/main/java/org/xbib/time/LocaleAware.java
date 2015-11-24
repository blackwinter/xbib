package org.xbib.time;

import java.util.Locale;

/**
 * An object that behaves differently for various {@link Locale} settings.
 */
public interface LocaleAware<TYPE> {
    /**
     * Set the {@link Locale} for which this instance should behave in.
     */
    public TYPE setLocale(Locale locale);

}
