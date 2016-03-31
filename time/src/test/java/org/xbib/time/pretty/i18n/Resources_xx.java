package org.xbib.time.pretty.i18n;

import org.xbib.time.pretty.TimeUnitQuantity;
import org.xbib.time.pretty.TimeFormat;
import org.xbib.time.pretty.TimeUnit;
import org.xbib.time.pretty.TimeFormatProvider;
import org.xbib.time.pretty.units.Minute;

import java.util.ListResourceBundle;

public class Resources_xx extends ListResourceBundle implements TimeFormatProvider {

    private static final Object[][] OBJECTS = new Object[][]{};

    @Override
    public Object[][] getContents() {
        return OBJECTS;
    }

    @Override
    public TimeFormat getFormatFor(TimeUnit t) {
        if (t instanceof Minute) {
            return new TimeFormat() {

                @Override
                public String decorate(TimeUnitQuantity timeUnitQuantity, String time) {
                    String result = timeUnitQuantity.getQuantityRounded(50) > 1 ? time + "es" : "e";
                    result += timeUnitQuantity.isInPast() ? " ago" : " from now";
                    return result;
                }

                @Override
                public String decorateUnrounded(TimeUnitQuantity timeUnitQuantity, String time) {
                    String result = timeUnitQuantity.getQuantity() > 1 ? time + "es" : "e";
                    result += timeUnitQuantity.isInPast() ? " ago" : " from now";
                    return result;
                }

                @Override
                public String format(TimeUnitQuantity timeUnitQuantity) {
                    return timeUnitQuantity.getQuantityRounded(50) + " minut";
                }

                @Override
                public String formatUnrounded(TimeUnitQuantity timeUnitQuantity) {
                    return timeUnitQuantity.getQuantity() + " minut";
                }
            };
        }
        return null;
    }

}
