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
package org.xbib.text;

public class Sanitizer {

    public static final String SANITIZE_PATTERN = "[^A-Za-z0-9\\%!$&\\\\'()*+,;=_]+";

    public static String sanitize(String slug) {
        return sanitize(slug, null, false, null, SANITIZE_PATTERN);
    }

    public static String sanitize(String slug, String filler) {
        return sanitize(slug, filler, false, null, SANITIZE_PATTERN);
    }

    public static String sanitize(String slug, String filler, boolean lower) {
        return sanitize(slug, filler, lower, null, SANITIZE_PATTERN);
    }

    public static String sanitize(String slug, String filler, String pattern) {
        return sanitize(slug, filler, false, null, pattern);
    }

    public static String sanitize(String slug, String filler, boolean lower, String pattern) {
        return sanitize(slug, filler, lower, null, pattern);
    }

    public static String sanitize(String slug, String filler, boolean lower, Normalizer.Form form) {
        return sanitize(slug, filler, lower, form, SANITIZE_PATTERN);
    }

    /**
     * Used to sanitize a string. Optionally performs Unicode Form KC normalization on a string to break extended
     * characters down, then replaces non alphanumeric characters with a specified filler replacement.
     *
     * @param slug   The source string
     * @param filler The replacement string
     * @param lower  True if the result should be lowercase
     * @param form   Unicode Normalization form to use (or null)
     */
    public static String sanitize(String slug, String filler, boolean lower, Normalizer.Form form, String pattern) {
        if (slug == null) {
            return null;
        }
        if (lower) {
            slug = slug.toLowerCase();
        }
        if (form != null) {
            try {
                slug = Normalizer.normalize(slug, form);
            } catch (Exception e) {
                // ignore
            }
        }
        slug = slug.replaceAll("\\s+", "_");
        if (filler != null) {
            slug = slug.replaceAll(pattern, filler);
        } else {
            slug = UrlEncoding.encode(slug, PathNoDelimFilter);
        }
        return slug;
    }

    private static final Filter PathNoDelimFilter = new Filter() {
        public boolean accept(int c) {
            return !(CharUtils.isAlphaDigit(c) || c == '-'
                    || c == '.'
                    || c == '_'
                    || c == '~'
                    || c == '&'
                    || c == '='
                    || c == '+'
                    || c == '$'
                    || c == ','
                    || c == ';' || c == '%');
        }
    };
}
