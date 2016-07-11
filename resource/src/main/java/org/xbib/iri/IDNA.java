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
package org.xbib.iri;

import org.xbib.text.CharUtils;
import org.xbib.text.CharUtils.Profile;
import org.xbib.text.Nameprep;
import org.xbib.text.Punycode;

import java.io.IOException;

/**
 * Provides an Internationized Domain Name implementation
 */
public final class IDNA implements Cloneable {

    private final String regname;

    public IDNA(String regname) {
        this.regname = toUnicode(regname);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String toASCII() {
        return toASCII(regname);
    }

    public String toUnicode() {
        return toUnicode(regname);
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((regname == null) ? 0 : regname.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IDNA other = (IDNA) obj;
        if (regname == null) {
            if (other.regname != null) {
                return false;
            }
        } else if (!regname.equals(other.regname)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toUnicode();
    }

    public static boolean equals(String idn1, String idn2) {
        return toUnicode(idn1).equals(toUnicode(idn2));
    }

    public static String toASCII(String regname) {
        try {
            if (regname == null) {
                return null;
            }
            if (regname.length() == 0) {
                return regname;
            }
            String[] labels = regname.split("\\\u002E");
            StringBuilder buf = new StringBuilder();
            for (String label : labels) {
                label = Nameprep.prep(label);
                char[] chars = label.toCharArray();
                CharUtils.verifyNot(chars, Profile.STD3ASCIIRULES);
                if (chars[0] == '\u002D' || chars[chars.length - 1] == '\u002D') {
                    throw new IOException("ToASCII violation");
                }
                if (!CharUtils.inRange(chars, (char) 0x000, (char) 0x007F)) {
                    if (label.startsWith("xn--")) {
                        throw new IOException("ToASCII violation");
                    }
                    String pc = "xn--" + Punycode.encode(chars, null);
                    chars = pc.toCharArray();
                }
                if (chars.length > 63) {
                    throw new IOException("ToASCII violation");
                }
                if (buf.length() > 0) {
                    buf.append('\u002E');
                }
                buf.append(chars);
            }
            return buf.toString();
        } catch (IOException e) {
            return regname;
        }
    }

    public static String toUnicode(String regname) {
        if (regname == null) {
            return null;
        }
        if (regname.length() == 0) {
            return regname;
        }
        String[] labels = regname.split("\\\u002E");
        StringBuilder buf = new StringBuilder();
        for (String label : labels) {
            char[] chars = label.toCharArray();
            if (!CharUtils.inRange(chars, (char) 0x000, (char) 0x007F)) {
                label = Nameprep.prep(label);
                chars = label.toCharArray();
            }
            if (label.startsWith("xn--")) {
                label = Punycode.decode(label.substring(4));
                chars = label.toCharArray();
            }
            if (buf.length() > 0) {
                buf.append('\u002E');
            }
            buf.append(chars);
        }
        String check = toASCII(buf.toString());
        return check.equalsIgnoreCase(regname) ? buf.toString() : regname;
    }

}
