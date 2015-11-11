/**
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
package org.xbib.charset;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * <p> A Charset for the OCLC|PICA-character-encoding (x-PICA). It decodes
 * x-PICA to Unicode and encodes Unicode to x-PICA. </p>
 *
 */
public class PicaCharset extends Charset {

    /**
     * <code>end-of-record-character</code>
     */
    public static final char END_OF_RECORD = '\u001D';
    /**
     * <code>end-of-field-character</code>
     */
    public static final char END_OF_FIELD = '\u001E';
    /**
     * <code>start-of-subfield-character</code>
     */
    public static final char START_OF_SUBFIELD = '\u001F';
    public final static char[] BYTE_TO_CHAR_MAP = newPicaToUnicodeMap();
    public final static Map<Character, Byte> CHAR_TO_BYTE_MAP = newCharToByteMap();
    private boolean isNFCOutput;

    /**
     * Constructor
     */
    public PicaCharset() {
        this(true);
    }

    /**
     * Constructor
     */
    public PicaCharset(boolean isNFCOuput) {
        super("x-PICA", null);
        this.isNFCOutput = isNFCOuput;
    }

    private static char[] newPicaToUnicodeMap() {
        char[] map = new char[256];

        for (int i = 0; i < 128; i++) {
            map[i] = (char) i;
        }
        /*
         * DNB-internal definitions, needed for conversion from pica+ to mab2
         */
        map[0x80] = MabCharset.TEILFELDTRENNZEICHEN;
        map[0x81] = MabCharset.NICHTSORTIERBEGINNZEICHEN;
        map[0x82] = MabCharset.NICHTSORTIERENDEZEICHEN;
        map[0x83] = '|'; // Füllzeichen
        map[0x84] = 'u'; // Zeichencode
        map[0x85] = 'z'; // Zeichenvorrat

        /* L with stroke */
        map[0xA1] = '\u0141';
        /* O with stroke */
        map[0xA2] = '\u00D8';
        /* D with stroke */
        map[0xA3] = '\u0110';
        /* Capital thorn */
        map[0xA4] = '\u00DE';
        /* Capital ligature AE */
        map[0xA5] = '\u00C6';
        /* Capital ligature OE */
        map[0xA6] = '\u0152';
        /* Modifier letter prime */
        map[0xA7] = '\u02B9';
        /* Middle dot */
        map[0xA8] = '\u00B7';
        /* MUSIC FLAT SIGN */
        map[0xA9] = '\u266D';
        /* Registered sign */
        map[0xAA] = '\u00AE';
        /* Plus-minus sign */
        map[0xAB] = '\u00B1';
        /* Capital letter O with horn */
        map[0xAC] = '\u01A0';
        /* Capital letter U with horn */
        map[0xAD] = '\u01AF';
        /* Modifier letter apostrophe */
        map[0xAE] = '\u02BC';
        /* LATIN CAPITAL LETTER A WITH RING ABOVE */
        map[0xAF] = '\u00C5';
        /* Modifier letter turned comma */
        map[0xB0] = '\u02BB';
        /* Latin small letter l with stroke */
        map[0xB1] = '\u0142';
        /* Latin small letter o with stroke */
        map[0xB2] = '\u00F8';
        /* Latin small letter d with stroke */
        map[0xB3] = '\u0111';
        /* Latin small letter thorn */
        map[0xB4] = '\u00FE';
        /* Latin small ligature ae */
        map[0xB5] = '\u00E6';
        /* Latin small ligature oe */
        map[0xB6] = '\u0153';
        /* modifier letter double prime */
        map[0xB7] = '\u02BA';
        /* latin small letter dotless i */
        map[0xB8] = '\u0131';
        /* pound sign */
        map[0xB9] = '\u00A3';
        /* latin small letter eth */
        map[0xBA] = '\u00F0';
        /* greek small letter alpha */
        map[0xBB] = '\u03B1';
        /* latin small letter o with horn */
        map[0xBC] = '\u01A1';
        /* latin small letter u with horn */
        map[0xBD] = '\u01B0';
        /* latin small letter sharp s */
        map[0xBE] = '\u00DF';
        /* LATIN SMALL LETTER A WITH RING ABOVE */
        map[0xBF] = '\u00E5';
        /* Latin capital ligature IJ */
        map[0xC0] = '\u0132';
        /* Ä */
        map[0xC1] = '\u00C4';
        /* Ö */
        map[0xC2] = '\u00D6';
        /* Ü */
        map[0xC3] = '\u00DC';
        /* LATIN CAPITAL LETTER OPEN O */
        map[0xC4] = '\u0186';
        /* latin capital letter reversed E */
        map[0xC5] = '\u018E';
        /* NOT EQUAL TO */
        map[0xC6] = '\u2260';
        /* RIGHTWARDS ARROW */
        map[0xC7] = '\u2192';
        /* LESS-THAN OR EQUAL TO */
        map[0xC8] = '\u2264';
        /* INFINITY */
        map[0xC9] = '\u221E';
        /* INTEGRAL */
        map[0xCA] = '\u222B';
        /* Multiplication sign */
        map[0xCB] = '\u00D7';
        /* Section sign */
        map[0xCC] = '\u00A7';
        /* SQUARE ROOT */
        map[0xCD] = '\u221A';
        /* GREATER-THAN OR LESS-THAN */
        map[0xCE] = '\u2277';
        /* GREATER-THAN OR EQUAL TO */
        map[0xCF] = '\u2265';
        /* Latin small ligature ij */
        map[0xD0] = '\u0133';
        /* ä */
        map[0xD1] = '\u00E4';
        /* ö */
        map[0xD2] = '\u00F6';
        /* ü */
        map[0xD3] = '\u00FC';
        /* LATIN SMALL LETTER OPEN O */
        map[0xD4] = '\u0254';
        /* Latin small letter reversed e */
        map[0xD5] = '\u01DD';
        /* inverted question mark */
        map[0xD6] = '\u00BF';
        /* inverted exclamation mark */
        map[0xD7] = '\u00A1';
        /* Greek small letter beta */
        map[0xD8] = '\u03B2';
        /* Greek small letter gamma */
        map[0xDA] = '\u03B3';
        /* Greek capital letter pi */
        map[0xDB] = '\u03C0';
        /* Combining hook above */
        map[0xE0] = '\u0309';
        /* COMBINING GRAVE ACCENT */
        map[0xE1] = '\u0300';
        /* COMBINING ACUTE ACCENT */
        map[0xE2] = '\u0301';
        /* COMBINING CIRCUMFLEX ACCENT */
        map[0xE3] = '\u0302';
        /* COMBINING TILDE */
        map[0xE4] = '\u0303';
        /* Macron */
        map[0xE5] = '\u0304';
        /* Combining breve */
        map[0xE6] = '\u0306';
        /* Combining dot above */
        map[0xE7] = '\u0307';
        /* COMBINING DIAERESIS */
        map[0xE8] = '\u0308';
        /* Combining caron */
        map[0xE9] = '\u030C';
        /* Combining ring above */
        map[0xEA] = '\u030A';
        /* COMBINING LIGATURE LEFT HALF */
        map[0xEB] = '\uFE20';
        /* COMBINING LIGATURE RIGHT HALF */
        map[0xEC] = '\uFE21';
        /* combining comma above */
        map[0xED] = '\u0313';
        /* combining double acute accent */
        map[0xEE] = '\u030B';
        /* combining candrabindu */
        map[0xEF] = '\u0310';
        /* Combining cedilla */
        map[0xF0] = '\u0327';
        /* Combining dot below */
        map[0xF2] = '\u0323';
        /* Combining diaeresis below */
        map[0xF3] = '\u0324';
        /* Combining ring below */
        map[0xF4] = '\u0325';
        /* Combining double low line */
        map[0xF5] = '\u0333';
        /* Combining macron below */
        map[0xF6] = '\u0331';
        /* Combining ogonek */
        map[0xF8] = '\u0328';
        /* Combining breve below */
        map[0xF9] = '\u032E';
        /* Combining DOUBLE TILDE RIGHT HALF */
        map[0xFA] = '\uFE23';
        /* Combining DOUBLE TILDE LEFT HALF */
        map[0xFB] = '\uFE22';
        /* Combining comma above right */
        map[0xFE] = '\u0315';
        return map;
    }

    class PicaDecoder extends SingleByteDecoder {

        protected PicaDecoder(Charset cs) {
            super(cs);
        }

        @Override
        public char byteToChar(byte b) {
            return BYTE_TO_CHAR_MAP[(b & 0xFF)];
        }

        @Override
        public boolean isCombiningCharacter(byte b) {
            return (b & 0xFF) >= 0xE0 && (b & 0xFF) <= 0xFE;
        }
    }

    class PicaEncoder extends SingleByteEncoder {

        protected PicaEncoder(Charset cs) {
            super(cs);
        }

        @Override
        public byte charToByte(char c) {
            Byte b = CHAR_TO_BYTE_MAP.get(c);
            if (b == null) {
                return 0;
            }
            return b.byteValue();
        }
    }

    private static Map<Character, Byte> newCharToByteMap() {
        char[] byteToCharMap = BYTE_TO_CHAR_MAP;
        byteToCharMap[0x80] = 0;
        byteToCharMap[0x81] = 0;
        byteToCharMap[0x82] = 0;
        byteToCharMap[0x83] = 0;
        byteToCharMap[0x84] = 0;
        byteToCharMap[0x85] = 0;

        Map<Character, Byte> ret = new HashMap<Character, Byte>(
                byteToCharMap.length);

        for (int i = 0; i < byteToCharMap.length; i++) {
            if (byteToCharMap[i] != 0) {
                ret.put(Character.valueOf(byteToCharMap[i]), Byte.valueOf((byte) i));
            }
        }
        return ret;
    }

    @Override
    public boolean contains(Charset cs) {
        return false;
    }

    @Override
    public CharsetDecoder newDecoder() {
        PicaDecoder ret = new PicaDecoder(this);
        ret.setComposeCharactersAfterConversion(isNFCOutput);
        return ret;
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new PicaEncoder(this);
    }
}
