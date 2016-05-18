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

import org.xbib.text.data.UnicodeCharacterDatabase;

import java.io.IOException;

/**
 * Performs Unicode Normalization (Form D,C,KD and KC)
 */
public final class Normalizer {

    private enum Mask {

        NONE, COMPATIBILITY, COMPOSITION
    }

    public enum Form {

        D, C(Mask.COMPOSITION), KD(Mask.COMPATIBILITY), KC(Mask.COMPATIBILITY, Mask.COMPOSITION);
        private int mask = 0;

        Form(Mask... masks) {
            for (Mask mask : masks) {
                this.mask |= (mask.ordinal());
            }
        }

        public boolean isCompatibility() {
            return (mask & (Mask.COMPATIBILITY.ordinal())) != 0;
        }

        public boolean isCanonical() {
            return !isCompatibility();
        }

        public boolean isComposition() {
            return (mask & (Mask.COMPOSITION.ordinal())) != 0;
        }
    }

    private Normalizer() {
    }

    /**
     * Normalize the string using NFKC
     */
    public static String normalize(CharSequence source) {
        return normalize(source, Form.KC);
    }

    /**
     * Normalize the string using the specified Form
     */
    public static String normalize(CharSequence source, Form form) {
        return normalize(source, form, new StringBuilder());
    }

    /**
     * Normalize the string into the given StringBuilder using the given Form
     */
    public static String normalize(CharSequence source, Form form, StringBuilder buf) {
        if (source == null) {
            return null;
        }
        if (source.length() != 0) {
            try {
                decompose(source, form, buf);
                compose(form, buf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return buf.toString();
    }

    private static void decompose(CharSequence source, Form form, StringBuilder buf) throws IOException {
        StringBuilder internal = new StringBuilder();
        CodepointIterator ci = CodepointIterator.forCharSequence(source);
        boolean canonical = form.isCanonical();
        while (ci.hasNext()) {
            Codepoint c = ci.next();
            internal.setLength(0);
            UnicodeCharacterDatabase.decompose(c.getValue(), canonical, internal);
            CodepointIterator ii = CodepointIterator.forCharSequence(internal);
            while (ii.hasNext()) {
                Codepoint ch = ii.next();
                int i = findInsertionPoint(buf, ch.getValue());
                buf.insert(i, CharUtils.toString(ch.getValue()));
            }
        }

    }

    private static int findInsertionPoint(StringBuilder buf, int c) {
        int cc = UnicodeCharacterDatabase.getCanonicalClass(c);
        int i = buf.length();
        if (cc != 0) {
            int ch;
            for (; i > 0; i -= CharUtils.length(c)) {
                ch = CharUtils.codepointAt(buf, i - 1).getValue();
                if (UnicodeCharacterDatabase.getCanonicalClass(ch) <= cc) {
                    break;
                }
            }
        }
        return i;
    }

    private static void compose(Form form, StringBuilder buf) throws IOException {
        if (!form.isComposition()) {
            return;
        }
        int pos = 0;
        int lc = CharUtils.codepointAt(buf, pos).getValue();
        int cpos = CharUtils.length(lc);
        int lcc = UnicodeCharacterDatabase.getCanonicalClass(lc);
        if (lcc != 0) {
            lcc = 256;
        }
        int len = buf.length();
        int c;
        for (int dpos = cpos; dpos < buf.length(); dpos += CharUtils.length(c)) {
            c = CharUtils.codepointAt(buf, dpos).getValue();
            int cc = UnicodeCharacterDatabase.getCanonicalClass(c);
            int composite = UnicodeCharacterDatabase.getPairComposition(lc, c);
            if (composite != '\uFFFF' && (lcc < cc || lcc == 0)) {
                CharUtils.setChar(buf, pos, composite);
                lc = composite;
            } else {
                if (cc == 0) {
                    pos = cpos;
                    lc = c;
                }
                lcc = cc;
                CharUtils.setChar(buf, cpos, c);
                if (buf.length() != len) {
                    dpos += buf.length() - len;
                    len = buf.length();
                }
                cpos += CharUtils.length(c);
            }
        }
        buf.setLength(cpos);
    }
}
