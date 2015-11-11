/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses 
 * or write to the Free Software Foundation, Inc., 51 Franklin Street, 
 * Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * The interactive user interfaces in modified source and object code 
 * versions of this program must display Appropriate Legal Notices, 
 * as required under Section 5 of the GNU Affero General Public License.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public 
 * License, these Appropriate Legal Notices must retain the display of the 
 * "Powered by xbib" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.util;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    public static final String ISO_FORMAT_SECONDS = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final String ISO_FORMAT_DAYS = "yyyy-MM-dd";

    public static final String RFC_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static final Calendar cal = Calendar.getInstance();

    private static final SimpleDateFormat sdf = new SimpleDateFormat();

    /**
     * Number of milliseconds in a standard second.
     */
    public static final long MILLIS_PER_SECOND = 1000;
    /**
     * Number of milliseconds in a standard minute.
     */
    public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    /**
     * Number of milliseconds in a standard hour.
     */
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    /**
     * Number of milliseconds in a standard day.
     */
    public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
    /**
     * the date masks
     */
    private static final String[] DATE_MASKS = {"yyyy-MM-dd'T'HH:mm:ssz", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd",
            "yyyy"};

    public static String formatNow() {
        return formatDateISO(new Date());
    }

    public static String formatDate(Date date, String format) {
        if (date == null) {
            return null;
        }
        synchronized (sdf) {
            sdf.applyPattern(format);
            sdf.setTimeZone(GMT);
            return sdf.format(date);
        }
    }

    public static String formatDateISO(Date date) {
        if (date == null) {
            return null;
        }
        synchronized (sdf) {
            sdf.applyPattern(ISO_FORMAT_SECONDS);
            sdf.setTimeZone(GMT);
            return sdf.format(date);
        }
    }

    public static Date parseDateISO(String value) {
        if (value == null) {
            return null;
        }
        synchronized (sdf) {
            sdf.applyPattern(ISO_FORMAT_SECONDS);
            sdf.setTimeZone(GMT);
            sdf.setLenient(true);
            try {
                return sdf.parse(value);
            } catch (ParseException pe) {
                // skip
            }
            sdf.applyPattern(ISO_FORMAT_DAYS);
            try {
                return sdf.parse(value);
            } catch (ParseException pe) {
                return null;
            }
        }
    }

    public static Date parseDateISO(String value, Date defaultDate) {
        if (value == null) {
            return defaultDate;
        }
        synchronized (sdf) {
            sdf.applyPattern(ISO_FORMAT_SECONDS);
            sdf.setTimeZone(GMT);
            sdf.setLenient(true);
            try {
                return sdf.parse(value);
            } catch (ParseException pe) {
                // skip
            }
            sdf.applyPattern(ISO_FORMAT_DAYS);
            try {
                return sdf.parse(value);
            } catch (ParseException pe) {
                return defaultDate;
            }
        }
    }

    public static String formatDateRFC(Date date) {
        if (date == null) {
            return null;
        }
        synchronized (sdf) {
            sdf.applyPattern(RFC_FORMAT);
            sdf.setTimeZone(GMT);
            return sdf.format(date);
        }
    }

    public static Date parseDateRFC(String value) {
        if (value == null) {
            return null;
        }
        try {
            synchronized (sdf) {
                sdf.applyPattern(RFC_FORMAT);
                sdf.setTimeZone(GMT);
                return sdf.parse(value);
            }
        } catch (ParseException pe) {
            return null;
        }
    }

    public static int getYear() {
        synchronized (cal) {
            cal.setTime(new Date());
            return cal.get(Calendar.YEAR);
        }
    }

    public static String today() {
        synchronized (cal) {
            cal.setTime(new Date());
            return String.format("%04d%02d%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH));
        }
    }

    public static int getYear(Date date) {
        synchronized (cal) {
            cal.setTime(date);
            return cal.get(Calendar.YEAR);
        }
    }

    public static Date midnight() {
        return DateUtil.midnight(new Date());
    }

    public static Date midnight(Date date) {
        synchronized (cal) {
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }
    }

    public static Date min() {
        return new Date(0L);
    }

    public static Date now() {
        return new Date();
    }

    public static Date yesterday() {
        return yesterday(new Date());
    }

    public static Date yesterday(Date date) {
        return days(date, -1);
    }

    public static Date tomorrow() {
        return tomorrow(new Date());
    }

    public static Date tomorrow(Date date) {
        return days(date, 1);
    }

    public static Date years(int years) {
        return years(new Date(), years);
    }

    public static Date years(Date date, int years) {
        synchronized (cal) {
            cal.setTime(date);
            cal.add(Calendar.YEAR, years);
            return cal.getTime();
        }
    }

    public static Date months(int months) {
        return months(new Date(), months);
    }

    public static Date months(Date date, int months) {
        synchronized (cal) {
            cal.setTime(date);
            cal.add(Calendar.MONTH, months);
            return cal.getTime();
        }
    }

    public static Date weeks(int weeks) {
        return weeks(new Date(), weeks);
    }

    public static Date weeks(Date date, int weeks) {
        synchronized (cal) {
            cal.setTime(date);
            cal.add(Calendar.WEEK_OF_YEAR, weeks);
            return cal.getTime();
        }
    }

    public static Date days(int days) {
        return days(new Date(), days);
    }

    public static Date days(Date date, int days) {
        synchronized (cal) {
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_YEAR, days);
            return cal.getTime();
        }
    }

    public static Date hours(int hours) {
        return hours(new Date(), hours);
    }

    public static Date hours(Date date, int hours) {
        synchronized (cal) {
            cal.setTime(date);
            cal.add(Calendar.HOUR_OF_DAY, hours);
            return cal.getTime();
        }
    }

    public static Date minutes(int minutes) {
        return minutes(new Date(), minutes);
    }

    public static Date minutes(Date date, int minutes) {
        synchronized (cal) {
            cal.setTime(date);
            cal.add(Calendar.MINUTE, minutes);
            return cal.getTime();
        }
    }

    public static Date seconds(int seconds) {
        return seconds(new Date(), seconds);
    }

    public static Date seconds(Date date, int seconds) {
        synchronized (cal) {
            cal.setTime(date);
            cal.add(Calendar.MINUTE, seconds);
            return cal.getTime();
        }
    }

    public static Date parseDate(Object o) {
        synchronized (sdf) {
            sdf.setTimeZone(GMT);
            sdf.setLenient(true);
            if (o instanceof Date) {
                return (Date) o;
            } else if (o instanceof Long) {
                Long longvalue = (Long) o;
                String s = Long.toString(longvalue);
                sdf.applyPattern(DATE_MASKS[3]);
                Date d = sdf.parse(s, new ParsePosition(0));
                if (d != null) {
                    return d;
                }
            } else if (o instanceof String) {
                String value = (String) o;
                for (String DATE_MASK : DATE_MASKS) {
                    sdf.applyPattern(DATE_MASK);
                    Date d = sdf.parse(value, new ParsePosition(0));
                    if (d != null) {
                        return d;
                    }
                }
            }
            return null;
        }
    }
}
