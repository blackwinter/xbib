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

import java.io.IOException;

/**
 * Implementation of the Punycode encoding scheme used by IDNA
 */
public final class Punycode {

    static final int base = 0x24; // 36
    static final int tmin = 0x01; // 1
    static final int tmax = 0x1A; // 26
    static final int skew = 0x26; // 38
    static final int damp = 0x02BC; // 700
    static final int initial_bias = 0x48; // 72
    static final int initial_n = 0x80; // 0x80
    static final int delimiter = 0x2D; // 0x2D

    Punycode() {
    }

    private static boolean basic(int cp) {
        return cp < 0x80;
    }

    private static boolean delim(int cp) {
        return cp == delimiter;
    }

    private static boolean flagged(int bcp) {
        return (bcp - 65) < 26;
    }

    private static int decode_digit(int cp) {
        return (cp - 48 < 10) ? cp - 22 : (cp - 65 < 26) ? cp - 65 : (cp - 97 < 26) ? cp - 97 : base;
    }

    private static int t(boolean c) {
        return (c) ? 1 : 0;
    }

    private static int encode_digit(int d, boolean upper) {
        return (d + 22 + 75 * t(d < 26)) - (t(upper) << 5);
    }

    private static int adapt(int delta, int numpoints, boolean firsttime) {
        int k;
        delta = (firsttime) ? delta / damp : delta >> 1;
        delta += delta / numpoints;
        for (k = 0; delta > ((base - tmin) * tmax) / 2; k += base) {
            delta /= base - tmin;
        }
        return k + (base - tmin + 1) * delta / (delta + skew);
    }

    public static String encode(char[] chars, boolean[] case_flags) throws IOException {
        StringBuilder buf = new StringBuilder();
        CodepointIterator ci = CodepointIterator.forCharArray(chars);
        int n, delta, h, b, bias, m, q, k, t;
        n = initial_n;
        delta = 0;
        bias = initial_bias;
        int i = -1;
        while (ci.hasNext()) {
            i = ci.next().getValue();
            if (basic(i)) {
                if (case_flags != null) {
                } else {
                    buf.append((char) i);
                }
            }
        }
        h = b = buf.length();
        if (b > 0) {
            buf.append((char) delimiter);
        }
        while (h < chars.length) {
            ci.position(0);
            i = -1;
            m = Integer.MAX_VALUE;
            while (ci.hasNext()) {
                i = ci.next().getValue();
                if (i >= n && i < m) {
                    m = i;
                }
            }
            if (m - n > (Integer.MAX_VALUE - delta) / (h + 1)) {
                throw new IOException("Overflow");
            }
            delta += (m - n) * (h + 1);
            n = m;
            ci.position(0);
            i = -1;
            while (ci.hasNext()) {
                i = ci.next().getValue();
                if (i < n) {
                    if (++delta == 0) {
                        throw new IOException("Overflow");
                    }
                }
                if (i == n) {
                    for (q = delta, k = base; ; k += base) {
                        t = k <= bias ? tmin : k >= bias + tmax ? tmax : k - bias;
                        if (q < t) {
                            break;
                        }
                        buf.append((char) encode_digit(t + (q - t) % (base - t), false));
                        q = (q - t) / (base - t);
                    }
                    buf.append((char) encode_digit(q, (case_flags != null) ? case_flags[ci.position() - 1] : false));
                    bias = adapt(delta, h + 1, h == b);
                    delta = 0;
                    ++h;
                }
            }
            ++delta;
            ++n;
        }
        return buf.toString();
    }

    public static String encode(String s) {
        try {
            if (s == null) {
                return null;
            }
            return encode(s.toCharArray(), null).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decode(String s) {
        try {
            if (s == null) {
                return null;
            }
            return decode(s.toCharArray(), null).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decode(char[] chars, boolean[] case_flags) throws IOException {
        StringBuilder buf = new StringBuilder();
        int n, out, i, bias, b, j, in, oldi, w, k, digit, t;
        n = initial_n;
        out = i = 0;
        bias = initial_bias;
        for (b = j = 0; j < chars.length; ++j) {
            if (delim(chars[j])) {
                b = j;
            }
        }
        for (j = 0; j < b; ++j) {
            if (case_flags != null) {
                case_flags[out] = flagged(chars[j]);
            }
            if (!basic(chars[j])) {
                throw new IOException("Bad Input");
            }
            buf.append(chars[j]);
        }
        out = buf.length();
        in = (b > 0) ? b + 1 : 0;
        while (in < chars.length) {
            for (oldi = i, w = 1, k = base; ; k += base) {
                if (in > chars.length) {
                    throw new IOException("Bad input");
                }
                digit = decode_digit(chars[in++]);
                if (digit >= base) {
                    throw new IOException("Bad input");
                }
                if (digit > (Integer.MAX_VALUE - i) / w) {
                    throw new IOException("Overflow");
                }
                i += digit * w;
                t = (k <= bias) ? tmin : (k >= bias + tmax) ? tmax : k - bias;
                if (digit < t) {
                    break;
                }
                if (w > Integer.MAX_VALUE / (base - t)) {
                    throw new IOException("Overflow");
                }
                w *= (base - t);
            }
            bias = adapt(i - oldi, out + 1, oldi == 0);
            if (i / (out + 1) > Integer.MAX_VALUE - n) {
                throw new IOException("Overflow");
            }
            n += i / (out + 1);
            i %= (out + 1);
            if (case_flags != null) {
                System.arraycopy( // not sure if this is right
                        case_flags,
                        i,
                        case_flags,
                        i + CharUtils.length(n),
                        case_flags.length - i);
            }
            CharUtils.insert(buf, i++, n);
            out++;
        }
        return buf.toString();
    }

}
