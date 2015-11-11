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
 *//**
 * Copyright (C) 2004  J&uuml;rgen Kett, Die Deutsche Bibliothek,
 * (http://www.ddb.de, mailto:kett@dbf.ddb.de)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, 
 * Boston, MA  02111-1307, USA.
 *
 */
package org.xbib.charset;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * mit einigen trivialen Ergänzungen in den Blöcken A-D
 */
public class MabCharset extends Charset {

    /**
     * <code>SATZENDEZEICHEN</code>
     */
    public static final char SATZENDEZEICHEN = '\u001D';
    /**
     * <code>FELDENDEZEICHEN</code>
     */
    public static final char FELDENDEZEICHEN = '\u001E';
    /**
     * <code>UNTERFELDBEGINNZEICHEN</code>
     */
    public static final char UNTERFELDBEGINNZEICHEN = '\u001F';
    /**
     * <code>NICHTSORTIERBEGINNZEICHEN</code>
     */
    public static final char NICHTSORTIERBEGINNZEICHEN = '\u0098';
    /**
     * <code>NICHTSORTIERENDEZEICHEN</code>
     */
    public static final char NICHTSORTIERENDEZEICHEN = '\u009C';
    /**
     * <code>STICHWORTBEGINNZEICHEN</code>
     */
    public static final char STICHWORTBEGINNZEICHEN = '{';
    /**
     * <code>STICHWORTENDEZEICHEN</code>
     */
    public static final char STICHWORTENDEZEICHEN = '}';
    /**
     * <code>TEILFELDTRENNZEICHEN</code>
     */
    public static final char TEILFELDTRENNZEICHEN = '\u2021';

    public final static char[] byteToCharTable = newMabCharsetMap();

    public final static Map<Character, Byte> charToByteTable = newMabByteToCharMap();

    private boolean isNFCOutput;

    public MabCharset() {
        this(true);
    }

    public MabCharset(boolean isNFCOutput) {
        super("x-MAB", null);
        this.isNFCOutput = isNFCOutput;
    }

    private static Map<Character, Byte> newMabByteToCharMap() {
        Map<Character, Byte> ret = new HashMap<Character, Byte>(
                byteToCharTable.length);
        for (int i = 0; i < byteToCharTable.length; i++) {
            if (byteToCharTable[i] != 0) {
                ret.put(Character.valueOf(byteToCharTable[i]), Byte.valueOf((byte) i));
            }
        }
        return ret;
    }

    class MabDecoder extends SingleByteDecoder {

        protected MabDecoder(Charset cs) {
            super(cs);
        }

        @Override
        public char byteToChar(byte b) {
            return byteToCharTable[(b & 0xFF)];
        }

        @Override
        public boolean isCombiningCharacter(byte b) {
            return (b & 0xFF) > 0xC0 && (b & 0xFF) < 0xDF;
        }
    }

    class MabEncoder extends SingleByteEncoder {

        protected MabEncoder(Charset cs) {
            super(cs);
        }

        @Override
        public byte charToByte(char c) {
            Byte b = charToByteTable.get(c);
            if (b == null) {
                return 0;
            }
            return b.byteValue();
        }
    }

    private static char[] newMabCharsetMap() {
        char[] map = new char[256];

        for (int i = 0; i < 128; i++) {
            map[i] = (char) i;
        }
        map[0x88] = MabCharset.NICHTSORTIERBEGINNZEICHEN;
        map[0x89] = MabCharset.NICHTSORTIERENDEZEICHEN;

        // A-Block
        map[0xA1] = '\u00A1'; // INVERTED EXCLAMATION MARK
        map[0xA2] = '\u201E'; // Double Low-9 Quotation Mark
        map[0xA3] = '\u00A3'; // Pound Sign
        map[0xA4] = '\u0024'; // Dollar Sign
        map[0xA5] = '\u00A5'; // YEN SIGN
        map[0xA6] = '\u2020'; // Dagger
        map[0xA7] = '\u00A7'; // SECTION SIGN
        map[0xA8] = '\u2032'; // Prime
        map[0xA9] = '\u2018'; // Left Single Quotation Mark
        map[0xAA] = '\u201C'; // Left Double Quotation Mark
        map[0xAB] = '\u00AB'; // LEFT-POINTING DOUBLE ANGLE QUOTATION MARK (LEFT POINTING GUILLEMET)      
        map[0xAC] = '\u266D'; // Music Flat Sign
        map[0xAD] = '\u00A9'; // Copyright Sign
        map[0xAE] = '\u2117'; // Sound Recording Copyright
        map[0xAF] = '\u00AE'; // Registered Sign

        // B-Block
        map[0xB0] = '\u02BB'; // Modifier Letter Turned Comma
        map[0xB1] = '\u02BC'; // Modifier Letter Apostrophe
        map[0xB2] = '\u201A'; // Single Low-9 Quotation Mark
        map[0xB6] = MabCharset.TEILFELDTRENNZEICHEN;
        map[0xB7] = '\u00B7'; //         
        map[0xB8] = '\u2033'; // Double Prime
        map[0xB9] = '\u2019'; // Right Single Quotation Mark
        map[0xBA] = '\u201D'; // Right Double Quotation Mark
        map[0xBB] = '\u00BB'; //
        map[0xBC] = '\u266F'; // Music Sharp Sign !!!!NACHFRAGEN
        map[0xBD] = '\u02B9'; // Modifier Letter Prime
        map[0xBE] = '\u02BA'; // Modifier Letter Double Prime
        map[0xBF] = '\u00BF'; //

        // C-Block
        map[0xC0] = '\u0309'; // Combining Hook above
        map[0xC1] = '\u0300'; // Combining Grave Accent
        map[0xC2] = '\u0301'; // Combining Acute Accent
        map[0xC3] = '\u0302'; // Combining Circumflex Accent
        map[0xC4] = '\u0303'; // Combining Tilde
        map[0xC5] = '\u0304'; // Combining Macron
        map[0xC6] = '\u0306'; // Combining Breve
        map[0xC7] = '\u0307'; // Combining Dot Above
        map[0xC8] = '\u0308'; // Trema -> Combining Diaeresis
        map[0xC9] = '\u0308'; // Umlaut -> Combining Diaeresis
        map[0xCA] = '\u030A'; // Combining Ring Above
        map[0xCB] = '\u0315'; // Combining Comma Above Right
        map[0xCC] = '\u0312'; // Combining Turned Comma Above
        map[0xCD] = '\u030B'; // Combining Double Acute Accent
        map[0xCE] = '\u031B'; // Combining Horn
        map[0xCF] = '\u030C'; // Combining Caron

        // D-Block
        map[0xD0] = '\u0327'; // Combining Cedilla
        map[0xD1] = '\u031C'; // Combining Left Half Ring Below
        map[0xD2] = '\u0326'; // Combining Comma Below
        map[0xD3] = '\u0328'; // Combining Ogonek
        map[0xD4] = '\u0325'; // Combining Ring Below
        map[0xD5] = '\u032E'; // Combining Breve Below
        map[0xD6] = '\u0323'; // Combining Dot Below
        map[0xD7] = '\u0324'; // Combining Diaeresis Below
        map[0xD8] = '\u0332'; // Combining Low Line
        map[0xD9] = '\u0333'; // Combining Double Low Line
        map[0xDA] = '\u0329'; // Combining Vertical Line Below
        map[0xDB] = '\u032D'; // Combining Circumflex Accent Below
        map[0xDD] = '\uFE20'; // Combining Ligature Left Half
        map[0xDE] = '\uFE21'; // Combining Ligature Right Half
        map[0xDF] = '\uFE23'; // Combining Double Tilde Right Half

        // E-Block
        map[0xE1] = '\u00C6'; // Latin Capital Letter AE
        map[0xE2] = '\u0110'; // Latin Capital Letter D with Stroke
        map[0xE6] = '\u0132'; // Latin Capital Ligature IJ
        map[0xE8] = '\u0141'; // Latin Capital Letter L with Stroke
        map[0xE9] = '\u00D8'; // Latin Capital Letter O with Stroke
        map[0xEA] = '\u0152'; // Latin Capital Ligature OE
        map[0xEC] = '\u00DE'; // Latin Capital Letter Thorn

        // F-Block
        map[0xF1] = '\u00E6'; // Latin Small Letter AE
        map[0xF2] = '\u0111'; // Latin Small Letter D with Stroke
        map[0xF3] = '\u00F0'; // Latin Small Letter ETH
        map[0xF5] = '\u0131'; // Latin Small Letter Dotless I
        map[0xF6] = '\u0133'; // Latin Small Ligature IJ
        map[0xF8] = '\u0142'; // Latin Small Letter L with Stroke
        map[0xF9] = '\u00F8'; // Latin Small Letter O with Stroke
        map[0xFA] = '\u0153'; // Latin Small Ligature OE
        map[0xFB] = '\u00DF'; // Latin Small Letter Sharp S
        map[0xFC] = '\u00FE'; // Latin Small Letter Thorn
        return map;
    }

    @Override
    public boolean contains(Charset cs) {
        return false;
    }

    @Override
    public CharsetDecoder newDecoder() {
        MabDecoder ret = new MabDecoder(this);
        ret.setComposeCharactersAfterConversion(this.isNFCOutput);
        return ret;
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new MabEncoder(this);
    }
}
