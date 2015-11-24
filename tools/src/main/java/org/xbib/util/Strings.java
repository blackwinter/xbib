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

import java.util.Iterator;
import java.util.List;

import static java.lang.System.getProperty;
import static java.util.Arrays.asList;

public final class Strings {

    public static final String EMPTY = "";

    public static final String SINGLE_QUOTE = "'";

    public static final String LINE_SEPARATOR = getProperty("line.separator");

    private Strings() {
        throw new UnsupportedOperationException();
    }


    /**
     * <p>Gives a string consisting of the given character repeated the given number of
     * times.</p>
     *
     * @param ch    the character to repeat
     * @param count how many times to repeat the character
     * @return the resultant string
     */
    public static String repeat(char ch, int count) {
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < count; ++i) {
            buffer.append(ch);
        }

        return buffer.toString();
    }

    /**
     * <p>Tells whether the given string is either {@code} or consists solely of
     * whitespace characters.</p>
     *
     * @param target string to check
     * @return {@code true} if the target string is null or empty
     */
    public static boolean isNullOrEmpty(String target) {
        return target == null || EMPTY.equals(target);
    }

    public static String unquote(String s) {
        if (s != null && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * Gives a string consisting of the elements of a given array of strings, each
     * separated by a given separator string.
     *
     * @param pieces    the strings to join
     * @param separator the separator
     * @return the joined string
     */
    public static String join(String[] pieces, String separator) {
        return join(asList(pieces), separator);
    }

    /**
     * Gives a string consisting of the string representations of the elements of a
     * given array of objects, each separated by a given separator string.
     *
     * @param pieces    the elements whose string representations are to be joined
     * @param separator the separator
     * @return the joined string
     */
    public static String join(List<String> pieces, String separator) {
        StringBuilder buffer = new StringBuilder();

        for (Iterator<String> iter = pieces.iterator(); iter.hasNext(); ) {
            buffer.append(iter.next());

            if (iter.hasNext()) {
                buffer.append(separator);
            }
        }

        return buffer.toString();
    }

    /**
     * <p>Replaces a String with another String inside a larger String, once.</p>
     * <p/>
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     * <p>
     * <pre>
     * StringUtils.replaceOnce(null, *, *)        = null
     * StringUtils.replaceOnce("", *, *)          = ""
     * StringUtils.replaceOnce("any", null, *)    = "any"
     * StringUtils.replaceOnce("any", *, null)    = "any"
     * StringUtils.replaceOnce("any", "", *)      = "any"
     * StringUtils.replaceOnce("aba", "a", null)  = "aba"
     * StringUtils.replaceOnce("aba", "a", "")    = "ba"
     * StringUtils.replaceOnce("aba", "a", "z")   = "zba"
     * </pre>
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace with, may be null
     * @return the text with any replacements processed,
     * <code>null</code> if null String input
     * @see #replace(String text, String searchString, String replacement, int max)
     */
    public static String replaceOnce(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, 1);
    }

    /**
     * <p>Replaces all occurrences of a String within another String.</p>
     * <p/>
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     * <p>
     * <pre>
     * StringUtils.replace(null, *, *)        = null
     * StringUtils.replace("", *, *)          = ""
     * StringUtils.replace("any", null, *)    = "any"
     * StringUtils.replace("any", *, null)    = "any"
     * StringUtils.replace("any", "", *)      = "any"
     * StringUtils.replace("aba", "a", null)  = "aba"
     * StringUtils.replace("aba", "a", "")    = "b"
     * StringUtils.replace("aba", "a", "z")   = "zbz"
     * </pre>
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @return the text with any replacements processed,
     * <code>null</code> if null String input
     * @see #replace(String text, String searchString, String replacement, int max)
     */
    public static String replace(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    /**
     * <p>Replaces a String with another String inside a larger String,
     * for the first <code>max</code> values of the search String.</p>
     * <p/>
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     * <p>
     * <pre>
     * StringUtils.replace(null, *, *, *)         = null
     * StringUtils.replace("", *, *, *)           = ""
     * StringUtils.replace("any", null, *, *)     = "any"
     * StringUtils.replace("any", *, null, *)     = "any"
     * StringUtils.replace("any", "", *, *)       = "any"
     * StringUtils.replace("any", *, *, 0)        = "any"
     * StringUtils.replace("abaa", "a", null, -1) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
     * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
     * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
     * </pre>
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @param max          maximum number of values to replace, or <code>-1</code> if no maximum
     * @return the text with any replacements processed,
     * <code>null</code> if null String input
     */
    public static String replace(String text, String searchString, String replacement, int max) {
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

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

}
