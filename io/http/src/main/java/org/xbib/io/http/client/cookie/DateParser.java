package org.xbib.io.http.client.cookie;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * A parser for <a href="http://tools.ietf.org/html/rfc2616#section-3.3">RFC2616
 * Date format</a>.
 */
public final class DateParser {

    private static final DateTimeFormatter PROPER_FORMAT_RFC822 = DateTimeFormatter.RFC_1123_DATE_TIME;
    // give up on pre 2000 dates
    private static final DateTimeFormatter OBSOLETE_FORMAT1_RFC850 = DateTimeFormatter.ofPattern("EEEE, dd-MMM-yy HH:mm:ss z", Locale.ENGLISH);
    private static final DateTimeFormatter OBSOLETE_FORMAT2_ANSIC = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy", Locale.ENGLISH);

    private static Date parseZonedDateTimeSilent(String text, DateTimeFormatter formatter) {
        try {
            return Date.from(ZonedDateTime.parse(text, formatter).toInstant());
        } catch (Exception e) {
            return null;
        }
    }

    private static Date parseDateTimeSilent(String text, DateTimeFormatter formatter) {
        try {
            return Date.from(LocalDateTime.parse(text, formatter).toInstant(ZoneOffset.UTC));
        } catch (Exception e) {
            return null;
        }
    }

    public static Date parse(String text) {
        Date date = parseZonedDateTimeSilent(text, PROPER_FORMAT_RFC822);
        if (date == null) {
            date = parseZonedDateTimeSilent(text, OBSOLETE_FORMAT1_RFC850);
        }
        if (date == null) {
            date = parseDateTimeSilent(text, OBSOLETE_FORMAT2_ANSIC);
        }
        return date;
    }
}
