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

import org.xbib.text.language.Lang;

import java.text.AttributedString;
import java.util.Arrays;

/**
 * Bidi guessing algorithms
 */
public class Bidi {

    public enum Direction {
        UNSPECIFIED, LTR, RTL
    }

    ;

    private static final String[] RTL_LANGS = {"ar", "dv", "fa", "he", "ps", "syr", "ur", "yi"};

    private static final String[] RTL_SCRIPTS =
            {"arab", "avst", "hebr", "hung", "lydi", "mand", "mani", "mero", "mong", "nkoo", "orkh", "phlv", "phnx",
                    "samr", "syrc", "syre", "syrj", "syrn", "tfng", "thaa"};
    // charset encodings that one may typically expect to be RTL
    private static final String[] RTL_ENCODINGS =
            {"iso-8859-6", "iso-8859-6-bidi", "iso-8859-6-i", "iso-ir-127", "ecma-114", "asmo-708", "arabic",
                    "csisolatinarabic", "windows-1256", "ibm-864", "macarabic", "macfarsi", "iso-8859-8-i", "iso-8859-8-bidi",
                    "windows-1255", "iso-8859-8", "ibm-862", "machebrew", "asmo-449", "iso-9036", "arabic7", "iso-ir-89",
                    "csiso89asmo449", "iso-unicode-ibm-1264", "csunicodeibm1264", "iso_8859-8:1988", "iso-ir-138", "hebrew",
                    "csisolatinhebrew", "iso-unicode-ibm-1265", "csunicodeibm1265", "cp862", "862", "cspc862latinhebrew"};

    /**
     * Algorithm that will determine text direction by looking at the characteristics of the language tag. If the tag
     * uses a language or script that is known to be RTL, then Direction.RTL will be returned
     */
    public static Direction guessDirectionFromLanguage(Lang lang) {
        if (lang.getScript() != null) {
            String script = lang.getScript().getName();
            if (Arrays.binarySearch(RTL_SCRIPTS, script.toLowerCase()) > -1) {
                return Direction.RTL;
            }
        }
        String primary = lang.getLanguage().getName();
        if (Arrays.binarySearch(RTL_LANGS, primary.toLowerCase()) > -1) {
            return Direction.RTL;
        }
        return Direction.UNSPECIFIED;
    }

    /**
     * Algorithm that will determine text direction by looking at the character set encoding. If the charset is
     * typically used for RTL languages, Direction.RTL will be returned
     */
    public static Direction guessDirectionFromEncoding(String charset) {
        if (charset == null) {
            return Direction.UNSPECIFIED;
        }
        charset = charset.replace('_', '-');
        Arrays.sort(RTL_ENCODINGS);
        if (Arrays.binarySearch(RTL_ENCODINGS, charset.toLowerCase()) > -1) {
            return Direction.RTL;
        }
        return Direction.UNSPECIFIED;
    }

    /**
     * Algorithm that analyzes properties of the text to determine text direction. If the majority of characters in the
     * text are RTL characters, then Direction.RTL will be returned.
     */
    public static Direction guessDirectionFromTextProperties(String text) {
        if (text != null && text.length() > 0) {
            if (text.charAt(0) == 0x200F) {
                return Direction.RTL; // if using the unicode right-to-left mark
            }
            if (text.charAt(0) == 0x200E) {
                return Direction.LTR; // if using the unicode left-to-right mark
            }
            int c = 0;
            for (int n = 0; n < text.length(); n++) {
                char ch = text.charAt(n);
                if (java.text.Bidi.requiresBidi(new char[]{ch}, 0, 1)) {
                    c++;
                } else {
                    c--;
                }
            }
            return c > 0 ? Direction.RTL : Direction.LTR;
        }
        return Direction.UNSPECIFIED;
    }

    /**
     * Algorithm that defers to the Java Bidi implementation to determine text direction.
     */
    public static Direction guessDirectionFromJavaBidi(String text) {
        if (text != null) {
            AttributedString s = new AttributedString(text);
            java.text.Bidi bidi = new java.text.Bidi(s.getIterator());
            return bidi.baseIsLeftToRight() ? Direction.LTR : Direction.RTL;
        }
        return Direction.UNSPECIFIED;
    }
}
