package org.xbib.time.pretty.i18n;

import org.xbib.time.pretty.Duration;
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
                public String decorate(Duration duration, String time) {
                    String result = duration.getQuantityRounded(50) > 1 ? time + "es" : "e";
                    result += duration.isInPast() ? " ago" : " from now";
                    return result;
                }

                @Override
                public String decorateUnrounded(Duration duration, String time) {
                    String result = duration.getQuantity() > 1 ? time + "es" : "e";
                    result += duration.isInPast() ? " ago" : " from now";
                    return result;
                }

                @Override
                public String format(Duration duration) {
                    return duration.getQuantityRounded(50) + " minut";
                }

                @Override
                public String formatUnrounded(Duration duration) {
                    return duration.getQuantity() + " minut";
                }
            };
        }
        return null;
    }

}
