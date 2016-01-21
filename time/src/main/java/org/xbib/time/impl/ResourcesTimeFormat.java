package org.xbib.time.impl;

import org.xbib.time.Duration;
import org.xbib.time.LocaleAware;
import org.xbib.time.TimeFormat;
import org.xbib.time.format.SimpleTimeFormat;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Represents a simple method of formatting a specific {@link Duration} of time
 */
public class ResourcesTimeFormat extends SimpleTimeFormat implements TimeFormat, LocaleAware<ResourcesTimeFormat> {
    private final ResourcesTimeUnit unit;
    private TimeFormat override;

    public ResourcesTimeFormat(ResourcesTimeUnit unit) {
        this.unit = unit;
    }

    @Override
    public ResourcesTimeFormat setLocale(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(unit.getResourceBundleName(), locale);

        if (bundle instanceof TimeFormatProvider) {
            TimeFormat format = ((TimeFormatProvider) bundle).getFormatFor(unit);
            if (format != null) {
                this.override = format;
            }
        } else {
            override = null;
        }

        if (override == null) {
            setPattern(bundle.getString(unit.getResourceKeyPrefix() + "Pattern"));
            setFuturePrefix(bundle.getString(unit.getResourceKeyPrefix() + "FuturePrefix"));
            setFutureSuffix(bundle.getString(unit.getResourceKeyPrefix() + "FutureSuffix"));
            setPastPrefix(bundle.getString(unit.getResourceKeyPrefix() + "PastPrefix"));
            setPastSuffix(bundle.getString(unit.getResourceKeyPrefix() + "PastSuffix"));

            setSingularName(bundle.getString(unit.getResourceKeyPrefix() + "SingularName"));
            setPluralName(bundle.getString(unit.getResourceKeyPrefix() + "PluralName"));

            String key = unit.getResourceKeyPrefix() + "FuturePluralName";
            if (bundle.containsKey(key)) {
                setFuturePluralName(bundle.getString(key));
            }
            key = unit.getResourceKeyPrefix() + "FutureSingularName";
            if (bundle.containsKey(key)) {
                setFutureSingularName((bundle.getString(key)));
            }
            key = unit.getResourceKeyPrefix() + "PastPluralName";
            if (bundle.containsKey(key)) {
                setPastPluralName((bundle.getString(key)));
            }
            key = unit.getResourceKeyPrefix() + "PastSingularName";
            if (bundle.containsKey(key)) {
                setPastSingularName((bundle.getString(key)));
            }
        }
        return this;
    }

    @Override
    public String decorate(Duration duration, String time) {
        return override == null ? super.decorate(duration, time) : override.decorate(duration, time);
    }

    @Override
    public String decorateUnrounded(Duration duration, String time) {
        return override == null ? super.decorateUnrounded(duration, time) : override.decorateUnrounded(duration, time);
    }

    @Override
    public String format(Duration duration) {
        return override == null ? super.format(duration) : override.format(duration);
    }

    @Override
    public String formatUnrounded(Duration duration) {
        return override == null ? super.formatUnrounded(duration) : override.formatUnrounded(duration);
    }
}