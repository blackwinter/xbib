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

import org.xbib.time.pretty.PrettyTime;

import java.util.ArrayList;
import java.util.List;

public class FormatUtil {

    private static final String EMPTY = "";
    private static final Object y = "y";
    private static final Object M = "M";
    private static final Object d = "d";
    private static final Object H = "H";
    private static final Object m = "m";
    private static final Object s = "s";
    private static final Object S = "S";

    /**
     * Number of milliseconds in a standard second.
     */
    private static final long MILLIS_PER_SECOND = 1000;
    /**
     * Number of milliseconds in a standard minute.
     */
    private static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    /**
     * Number of milliseconds in a standard hour.
     */
    private static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    /**
     * Number of milliseconds in a standard day.
     */
    private static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

    private static final String[] BYTES = {" B", " kB", " MB", " GB", " TB", " PB", " EB", " ZB", " YB"};
    private static final String[] BYTES_PER_SECOND = {" B/s"," kB/s"," MB/s"," GB/s"," TB/s"," PB/s"," EB/s"," ZB/s"," YB/s"};
    private static final String[] DOCS_PER_SECOND = {" dps"," kdps"," Mdps"," Gdps"," Tdps"," Pdps"," Edps"," Zdps"," Ydps"};



    /*public static String convertFileSize(double size) {
        return convertFileSize(size, Locale.getDefault());
    }

    public static String convertFileSize(double size, Locale locale) {
        String strSize;
        long kb = 1024;
        long mb = 1024 * kb;
        long gb = 1024 * mb;
        long tb = 1024 * gb;

        NumberFormat formatter = NumberFormat.getNumberInstance(locale);
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);

        if (size < kb) {
            strSize = size + " bytes";
        } else if (size < mb) {
            strSize = formatter.format(size / kb) + " KB";
        } else if (size < gb) {
            strSize = formatter.format(size / mb) + " MB";
        } else if (size < tb) {
            strSize = formatter.format(size / gb) + " GB";
        } else {
            strSize = formatter.format(size / tb) + " TB";
        }
        return strSize;
    }*/

    /**
     * Format byte size (file size as example) into a string,
     * with two digits after dot and actual measure (MB, GB or other).
     * @param size value to format
     * @return formatted string in bytes, kB, MB or other.
     */
    public static String formatSize(long size) {
        return format(size, BYTES, 1024);
    }

    public static String formatSize(double size) {
        return format(size, BYTES, 1024);
    }

    /**
     * Format speed values (copy speed as example) into a string
     * with two digits after dot and actual measure (MB/s, GB/s or other).
     * @param speed value to format
     * @return formatted string in bytes/s, kB/s, MB/s or other.
     */
    public static String formatSpeed(long speed) {
        return format(speed, BYTES_PER_SECOND, 1024);
    }

    public static String formatSpeed(double speed) {
        return format(speed, BYTES_PER_SECOND, 1024);
    }

    public static String formatDocumentSpeed(long speed) {
        return format(speed, DOCS_PER_SECOND, 1024);
    }

    public static String formatDocumentSpeed(double speed) {
        return format(speed, DOCS_PER_SECOND, 1024);
    }
    /**
     * Format any value without string appending.
     * @param size value to format
     * @param measureUnits array of strings to use as measurement units. Use BYTES_PER_SECOND as example.
     * @param measureQuantity quantiry, required to step into next unit. Like 1024 for bytes, 1000 for meters or 100 for centiry.
     * @return formatted size with measure unit
     */
    private static String format(long size, String[] measureUnits, int measureQuantity) {
        if (size <= 0) {
            return null;
        }
        if (size < measureQuantity) {
            return size + measureUnits[0];
        }
        int i = 1;
        double d = size;
        while ((d = d / measureQuantity) > (measureQuantity - 1) ) {
            i++;
        }
        long l = (long) (d * 100);
        d = (double) l / 100;
        if (i < measureUnits.length) {
            return d + measureUnits[i];
        }
        return String.valueOf(size);
    }

    private static String format(double d, String[] measureUnits, int measureQuantity) {
        if (d <= 0.0d) {
            return null;
        }
        if (d < measureQuantity) {
            return d + measureUnits[0];
        }
        int i = 1;
        while ((d = d / measureQuantity) > (measureQuantity - 1) ) {
            i++;
        }
        long l = (long) (d * 100);
        d = (double) l / 100;
        if (i < measureUnits.length) {
            return d + measureUnits[i];
        }
        return String.valueOf(d);
    }

    private final static PrettyTime pretty = new PrettyTime();

    public static String formatMillis(long millis) {
        return pretty.format(pretty.calculateDuration(millis));
    }

    public static String formatDurationWords(
            long durationMillis,
            boolean suppressLeadingZeroElements,
            boolean suppressTrailingZeroElements) {

        // This method is generally replacable by the format method, but
        // there are a series of tweaks and special cases that require
        // trickery to replicate.
        String duration = formatDuration(durationMillis, "d' days 'H' hours 'm' minutes 's' seconds'");
        if (suppressLeadingZeroElements) {
            // this is a temporary marker on the front. Like ^ in regexp.
            duration = " " + duration;
            String tmp = replaceOnce(duration, " 0 days", "");
            if (tmp.length() != duration.length()) {
                duration = tmp;
                tmp = replaceOnce(duration, " 0 hours", "");
                if (tmp.length() != duration.length()) {
                    duration = tmp;
                    tmp = replaceOnce(duration, " 0 minutes", "");
                    duration = tmp;
                    if (tmp.length() != duration.length()) {
                        duration = replaceOnce(tmp, " 0 seconds", "");
                    }
                }
            }
            if (duration.length() != 0) {
                // strip the space off again
                duration = duration.substring(1);
            }
        }
        if (suppressTrailingZeroElements) {
            String tmp = replaceOnce(duration, " 0 seconds", "");
            if (tmp.length() != duration.length()) {
                duration = tmp;
                tmp = replaceOnce(duration, " 0 minutes", "");
                if (tmp.length() != duration.length()) {
                    duration = tmp;
                    tmp = replaceOnce(duration, " 0 hours", "");
                    if (tmp.length() != duration.length()) {
                        duration = replaceOnce(tmp, " 0 days", "");
                    }
                }
            }
        }
        // handle plurals
        duration = " " + duration;
        duration = replaceOnce(duration, " 1 seconds", " 1 second");
        duration = replaceOnce(duration, " 1 minutes", " 1 minute");
        duration = replaceOnce(duration, " 1 hours", " 1 hour");
        duration = replaceOnce(duration, " 1 days", " 1 day");
        return duration.trim();
    }

    public static String formatDuration(long durationMillis, String format) {
        List<Token> tokens = lexx(format);
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        int milliseconds = 0;

        if (Token.containsTokenWithValue(tokens, d)) {
            days = (int) (durationMillis / MILLIS_PER_DAY);
            durationMillis = durationMillis - (days * MILLIS_PER_DAY);
        }
        if (Token.containsTokenWithValue(tokens, H)) {
            hours = (int) (durationMillis / MILLIS_PER_HOUR);
            durationMillis = durationMillis - (hours * MILLIS_PER_HOUR);
        }
        if (Token.containsTokenWithValue(tokens, m)) {
            minutes = (int) (durationMillis / MILLIS_PER_MINUTE);
            durationMillis = durationMillis - (minutes * MILLIS_PER_MINUTE);
        }
        if (Token.containsTokenWithValue(tokens, s)) {
            seconds = (int) (durationMillis / MILLIS_PER_SECOND);
            durationMillis = durationMillis - (seconds * MILLIS_PER_SECOND);
        }
        if (Token.containsTokenWithValue(tokens, S)) {
            milliseconds = (int) durationMillis;
        }
        return format(tokens, 0, 0, days, hours, minutes, seconds, milliseconds);
    }

    /**
     * <p>The internal method to do the formatting.</p>
     *
     * @param tokens       the tokens
     * @param years        the number of years
     * @param months       the number of months
     * @param days         the number of days
     * @param hours        the number of hours
     * @param minutes      the number of minutes
     * @param seconds      the number of seconds
     * @param milliseconds the number of millis
     * @return the formatted string
     */
    private static String format(List<Token> tokens, int years, int months, int days, int hours, int minutes, int seconds,
                         int milliseconds) {
        StringBuilder buffer = new StringBuilder();
        boolean lastOutputSeconds = false;
        for (Token token : tokens) {
            Object value = token.getValue();
            if (value instanceof StringBuilder) {
                buffer.append(value.toString());
            } else {
                if (y.equals(value)) {
                    buffer.append(Integer.toString(years));
                    lastOutputSeconds = false;
                } else if (M.equals(value)) {
                    buffer.append(Integer.toString(months));
                    lastOutputSeconds = false;
                } else if (d.equals(value)) {
                    buffer.append(Integer.toString(days));
                    lastOutputSeconds = false;
                } else if (H.equals(value)) {
                    buffer.append(Integer.toString(hours));
                    lastOutputSeconds = false;
                } else if (m.equals(value)) {
                    buffer.append(Integer.toString(minutes));
                    lastOutputSeconds = false;
                } else if (s.equals(value)) {
                    buffer.append(Integer.toString(seconds));
                    lastOutputSeconds = true;
                } else if (S.equals(value)) {
                    if (lastOutputSeconds) {
                        milliseconds += 1000;
                        String str = Integer.toString(milliseconds);
                        buffer.append(str.substring(1));
                    } else {
                        buffer.append(Integer.toString(milliseconds));
                    }
                    lastOutputSeconds = false;
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Parses a classic date format string into Tokens
     *
     * @param format to parse
     * @return array of Token[]
     */
    private static List<Token> lexx(String format) {
        char[] array = format.toCharArray();
        List<Token> list = new ArrayList<>(array.length);
        boolean inLiteral = false;
        StringBuilder buffer = null;
        Token previous = null;
        for (char ch : array) {
            if (inLiteral && ch != '\'') {
                buffer.append(ch);
                continue;
            }
            Object value = null;
            switch (ch) {
                case '\'':
                    if (inLiteral) {
                        buffer = null;
                        inLiteral = false;
                    } else {
                        buffer = new StringBuilder();
                        list.add(new Token(buffer));
                        inLiteral = true;
                    }
                    break;
                case 'y':
                    value = y;
                    break;
                case 'M':
                    value = M;
                    break;
                case 'd':
                    value = d;
                    break;
                case 'H':
                    value = H;
                    break;
                case 'm':
                    value = m;
                    break;
                case 's':
                    value = s;
                    break;
                case 'S':
                    value = S;
                    break;
                default:
                    if (buffer == null) {
                        buffer = new StringBuilder();
                        list.add(new Token(buffer));
                    }
                    buffer.append(ch);
            }

            if (value != null) {
                if (previous != null && value.equals(previous.getValue())) {
                    previous.increment();
                } else {
                    Token token = new Token(value);
                    list.add(token);
                    previous = token;
                }
                buffer = null;
            }
        }
        return list;
    }

    private static String replaceOnce(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, 1);
    }

    private static String replace(String text, String searchString, String replacement, int max) {
        if (isNullOrEmpty(text) || isNullOrEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == -1) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = (increase < 0 ? 0 : increase);
        increase *= (max < 0 ? 16 : (max > 64 ? 64 : max));
        StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != -1) {
            buf.append(text.substring(start, end)).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    private static boolean isNullOrEmpty(String target) {
        return target == null || EMPTY.equals(target);
    }


    /**
     * Element that is parsed from the format pattern.
     */
    static class Token {

        private Object value;
        private int count;
        /**
         * Wraps a token around a value. A value would be something like a 'Y'.
         *
         * @param value to wrap
         */
        Token(Object value) {
            this.value = value;
            this.count = 1;
        }

        /**
         * Helper method to determine if a set of tokens contain a value
         *
         * @param tokens set to look in
         * @param value  to look for
         * @return boolean <code>true</code> if contained
         */
        static boolean containsTokenWithValue(List<Token> tokens, Object value) {
            for (Token token : tokens) {
                if (token.getValue() == value) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Adds another one of the value
         */
        void increment() {
            count++;
        }

        /**
         * Gets the current number of values represented
         *
         * @return int number of values represented
         */
        int getCount() {
            return count;
        }

        /**
         * Gets the particular value this token represents.
         *
         * @return Object value
         */
        Object getValue() {
            return value;
        }

        /**
         * Supports equality of this Token to another Token.
         *
         * @param obj Object to consider equality of
         * @return boolean <code>true</code> if equal
         */
        public boolean equals(Object obj) {
            if (obj instanceof Token) {
                Token tok = (Token) obj;
                if (this.value.getClass() != tok.value.getClass()) {
                    return false;
                }
                if (this.count != tok.count) {
                    return false;
                }
                if (this.value instanceof StringBuilder) {
                    return this.value.toString().equals(tok.value.toString());
                } else if (this.value instanceof Number) {
                    return this.value.equals(tok.value);
                } else {
                    return this.value == tok.value;
                }
            }
            return false;
        }

        /**
         * Returns a hashcode for the token equal to the
         * hashcode for the token's value. Thus 'TT' and 'TTTT'
         * will have the same hashcode.
         *
         * @return The hashcode for the token
         */
        public int hashCode() {
            return this.value.hashCode();
        }
    }

}
