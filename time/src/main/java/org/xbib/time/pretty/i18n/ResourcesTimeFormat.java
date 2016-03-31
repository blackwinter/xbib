package org.xbib.time.pretty.i18n;

import org.xbib.time.pretty.TimeUnitQuantity;
import org.xbib.time.pretty.LocaleAware;
import org.xbib.time.pretty.SimpleTimeFormat;
import org.xbib.time.pretty.TimeFormat;
import org.xbib.time.pretty.TimeFormatProvider;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Represents a simple method of formatting a specific {@link TimeUnitQuantity} of time
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
    public String decorate(TimeUnitQuantity timeUnitQuantity, String time) {
        return override == null ? super.decorate(timeUnitQuantity, time) : override.decorate(timeUnitQuantity, time);
    }

    @Override
    public String decorateUnrounded(TimeUnitQuantity timeUnitQuantity, String time) {
        return override == null ? super.decorateUnrounded(timeUnitQuantity, time) : override.decorateUnrounded(timeUnitQuantity, time);
    }

    @Override
    public String format(TimeUnitQuantity timeUnitQuantity) {
        return override == null ? super.format(timeUnitQuantity) : override.format(timeUnitQuantity);
    }

    @Override
    public String formatUnrounded(TimeUnitQuantity timeUnitQuantity) {
        return override == null ? super.formatUnrounded(timeUnitQuantity) : override.formatUnrounded(timeUnitQuantity);
    }
}