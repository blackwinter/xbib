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

import java.io.ByteArrayInputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;

/**
 * Performs URL Percent Encoding
 */
public final class UrlEncoding {

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private UrlEncoding() {
    }

    private static void encode(Appendable sb, byte... bytes) {
        encode(sb, 0, bytes.length, bytes);
    }

    private static void encode(Appendable sb, int offset, int length, byte... bytes) {
        try {
            for (int n = offset, i = 0; n < bytes.length && i < length; n++, i++) {
                byte c = bytes[n];
                sb.append("%");
                sb.append(HEX[(c >> 4) & 0x0f]);
                sb.append(HEX[c & 0x0f]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encode(char... chars) {
        return encode(chars, 0, chars.length, DEFAULT_ENCODING, new Filter[0]);
    }

    public static String encode(char[] chars, Filter Filter) {
        return encode(chars, 0, chars.length, DEFAULT_ENCODING, new Filter[]{Filter});
    }

    public static String encode(char[] chars, Filter... filters) {
        return encode(chars, 0, chars.length, DEFAULT_ENCODING, filters);
    }

    public static String encode(char[] chars, String enc) {
        return encode(chars, 0, chars.length, enc, new Filter[0]);
    }

    public static String encode(char[] chars, String enc, Filter Filter) {
        return encode(chars, 0, chars.length, enc, new Filter[]{Filter});
    }

    public static String encode(char[] chars, String enc, Filter... filters) {
        return encode(chars, 0, chars.length, enc, filters);
    }

    public static String encode(char[] chars, int offset, int length) {
        return encode(chars, offset, length, DEFAULT_ENCODING, new Filter[0]);
    }

    public static String encode(char[] chars, int offset, int length, String enc) {
        return encode(chars, offset, length, enc, new Filter[0]);
    }

    public static String encode(char[] chars, int offset, int length, Filter Filter) {
        return encode(chars, offset, length, DEFAULT_ENCODING, new Filter[]{Filter});
    }

    public static String encode(char[] chars, int offset, int length, Filter... filters) {
        return encode(chars, offset, length, DEFAULT_ENCODING, filters);
    }

    public static String encode(char[] chars, int offset, int length, String enc, Filter Filter) {
        return encode(chars, offset, length, enc, new Filter[]{Filter});
    }

    public static String encode(char[] chars, int offset, int length, String enc, Filter... filters) {
        try {
            return encode((CharSequence) CharBuffer.wrap(chars, offset, length), enc, filters);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encode(InputStream in) throws IOException {
        StringBuilder buf = new StringBuilder();
        byte[] chunk = new byte[1024];
        int r = -1;
        while ((r = in.read(chunk)) > -1) {
            encode(buf, 0, r, chunk);
        }
        return buf.toString();
    }

    public static String encode(InputStream in, String charset) throws IOException {
        return encode(in, charset, DEFAULT_ENCODING, new Filter[0]);
    }

    public static String encode(InputStream in, String charset, Filter Filter) throws IOException {
        return encode(in, charset, DEFAULT_ENCODING, new Filter[]{Filter});
    }

    public static String encode(InputStream in, String charset, String enc) throws IOException {
        return encode(in, charset, enc, new Filter[0]);
    }

    public static String encode(InputStream in, String charset, String enc, Filter Filter) throws IOException {
        return encode(in, charset, enc, new Filter[]{Filter});
    }

    public static String encode(InputStream in, String charset, String enc, Filter... filters) throws IOException {
        return encode(new InputStreamReader(in, charset), enc, filters);
    }

    public static String encode(InputStream in, String charset, Filter... filters) throws IOException {
        return encode(new InputStreamReader(in, charset), DEFAULT_ENCODING, filters);
    }

    public static String encode(Reader reader) throws IOException {
        return encode(reader, DEFAULT_ENCODING, new Filter[0]);
    }

    public static String encode(Readable readable) throws IOException {
        return encode(readable, DEFAULT_ENCODING, new Filter[0]);
    }

    public static String encode(Reader reader, String enc) throws IOException {
        return encode(reader, enc, new Filter[0]);
    }

    public static String encode(Readable readable, String enc) throws IOException {
        return encode(readable, enc, new Filter[0]);
    }

    public static String encode(Reader reader, String enc, Filter Filter) throws IOException {
        return encode(reader, enc, new Filter[]{Filter});
    }

    public static String encode(Reader reader, Filter Filter) throws IOException {
        return encode(reader, DEFAULT_ENCODING, new Filter[]{Filter});
    }

    public static String encode(Reader reader, Filter... filters) throws IOException {
        return encode(reader, DEFAULT_ENCODING, filters);
    }

    public static String encode(Readable readable, String enc, Filter Filter) throws IOException {
        return encode(readable, enc, new Filter[]{Filter});
    }

    public static String encode(Readable readable, Filter Filter) throws IOException {
        return encode(readable, DEFAULT_ENCODING, new Filter[]{Filter});
    }

    public static String encode(Readable readable, Filter... filters) throws IOException {
        return encode(readable, DEFAULT_ENCODING, filters);
    }

    private static void processChars(StringBuilder sb, CharBuffer chars, String enc, Filter... filters)
            throws IOException {
        for (int n = 0; n < chars.length(); n++) {
            char c = chars.charAt(n);
            if (!CharUtils.isHighSurrogate(c) && check(c, filters)) {
                encode(sb, String.valueOf(c).getBytes(enc));
            } else if (CharUtils.isHighSurrogate(c)) {
                if (check(c, filters)) {
                    String buf = String.valueOf(c) + chars.charAt(++n);
                    byte[] b = buf.getBytes(enc);
                    encode(sb, b);
                } else {
                    sb.append(c);
                    sb.append(chars.charAt(++n));
                }
            } else {
                sb.append(c);
            }
        }
    }

    public static String encode(Readable readable, String enc, Filter... filters) throws IOException {
        StringBuilder sb = new StringBuilder();
        CharBuffer chars = CharBuffer.allocate(1024);
        while (readable.read(chars) > -1) {
            chars.flip();
            processChars(sb, chars, enc, filters);
        }
        return sb.toString();
    }

    public static String encode(Reader reader, String enc, Filter... filters) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] chunk = new char[1024];
        int r = -1;
        while ((r = reader.read(chunk)) > -1) {
            processChars(sb, CharBuffer.wrap(chunk, 0, r), enc, filters);
        }
        return sb.toString();
    }

    public static String encode(byte... bytes) {
        StringBuilder buf = new StringBuilder();
        encode(buf, bytes);
        return buf.toString();
    }

    public static String encode(byte[] bytes, int off, int len) {
        StringBuilder buf = new StringBuilder();
        encode(buf, off, len, bytes);
        return buf.toString();
    }

    public static String encode(CharSequence s) {
        return encode(s, Filter.NONOPFILTER);
    }

    public static String encode(CharSequence s, Filter Filter) {
        return encode(s, new Filter[]{Filter});
    }

    public static String encode(CharSequence s, Filter... filters) {
        try {
            if (s == null) {
                return null;
            }
            return encode(s, "utf-8", filters);
        } catch (UnsupportedEncodingException e) {
            return null; // shouldn't happen
        }
    }

    public static String encode(CharSequence s, int offset, int length) {
        return encode(s, offset, length, Filter.NONOPFILTER);
    }

    public static String encode(CharSequence s, int offset, int length, Filter Filter) {
        return encode(s, offset, length, new Filter[]{Filter});
    }

    public static String encode(CharSequence s, int offset, int length, Filter... filters) {
        try {
            if (s == null) {
                return null;
            }
            return encode(s, offset, length, "utf-8", filters);
        } catch (UnsupportedEncodingException e) {
            return null; // shouldn't happen
        }
    }

    private static boolean check(int codepoint, Filter... filters) {
        for (Filter Filter : filters) {
            if (Filter.accept(codepoint)) {
                return true;
            }
        }
        return false;
    }

    public static String encode(CharSequence s, int offset, int length, String enc, Filter... filters)
            throws UnsupportedEncodingException {
        int end = Math.min(s.length(), offset + length);
        CharSequence seq = s.subSequence(offset, end);
        return encode(seq, enc, filters);
    }

    public static String encode(CharSequence s, String enc, Filter... filters) throws UnsupportedEncodingException {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        for (int n = 0; n < s.length(); n++) {
            char c = s.charAt(n);
            if (!CharUtils.isHighSurrogate(c) && check(c, filters)) {
                encode(sb, String.valueOf(c).getBytes(enc));
            } else if (CharUtils.isHighSurrogate(c)) {
                if (check(c, filters)) {
                    StringBuilder buf = new StringBuilder();
                    buf.append(c);
                    buf.append(s.charAt(++n));
                    byte[] b = buf.toString().getBytes(enc);
                    encode(sb, b);
                } else {
                    sb.append(c);
                    sb.append(s.charAt(++n));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String decode(String e, String enc) throws IOException {
        char[] buf = new char[e.length()];
        try (DecodingReader r = new DecodingReader(e.getBytes(enc), enc)) {
            int l = r.read(buf);
            e = new String(buf, 0, l);
        }
        return e;
    }

    public static String decode(String e) {
        try {
            return decode(e, "utf-8");
        } catch (Exception ex) {
            return e;
        }
    }


    public static class DecodingReader extends FilterReader {

        public DecodingReader(byte[] buf, String enc) throws UnsupportedEncodingException {
            this(new ByteArrayInputStream(buf), enc);
        }

        public DecodingReader(InputStream in, String enc) throws UnsupportedEncodingException {
            this(new InputStreamReader(in, enc));
        }

        public DecodingReader(Reader in) {
            super(in);
        }

        public int read() throws IOException {
            int c = super.read();
            if (c == '%') {
                int c1 = super.read();
                int c2 = super.read();
                return decode((char) c1, (char) c2);
            } else {
                return c;
            }
        }

        @Override
        public int read(char[] b, int off, int len) throws IOException {
            int n = off;
            int i = -1;
            while ((i = read()) != -1 && n < off + len) {
                b[n++] = (char) i;
            }
            return n - off;
        }

        @Override
        public int read(char[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public long skip(long n) throws IOException {
            long i = 0;
            for (; i < n; i++) {
                read();
            }
            return i;
        }
    }

    private static byte decode(char c, int shift) {
        return (byte) ((((c >= '0' && c <= '9') ? c - '0' : (c >= 'A' && c <= 'F') ? c - 'A' + 10
                : (c >= 'a' && c <= 'f') ? c - 'a' + 10 : -1) & 0xf) << shift);
    }

    private static byte decode(char c1, char c2) {
        return (byte) (decode(c1, 4) | decode(c2, 0));
    }

}
