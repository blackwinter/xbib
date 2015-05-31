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
package org.xbib.strings.encode;

import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A base form encoder
 */
public class BaseformEncoder {

    private static final Pattern p = Pattern.compile("[^\\p{L}\\p{Space}]");

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final Charset ISO88591 = Charset.forName("ISO-8859-1");

    public static String normalizedFromUTF8(String name) {
        String s = Normalizer.normalize(name, Normalizer.Form.NFD);
        s = p.matcher(s).replaceAll(""); // remove diacritics, accents, etc.
        s = s.toLowerCase(Locale.ENGLISH); // just english lowercase rules (JVM independent)
        return s;
    }

    public static String normalizedFromISO88591(String name) {
        String s;
        s = Normalizer.normalize(new String(name.getBytes(ISO88591), UTF8), Normalizer.Form.NFC);
        s = p.matcher(s).replaceAll("");
        s = s.toLowerCase(Locale.ENGLISH);
        return s;
    }

}
