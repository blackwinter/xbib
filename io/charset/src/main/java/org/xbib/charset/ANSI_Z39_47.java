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

import java.io.CharArrayWriter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import java.util.HashMap;

/**
 * ANSI Z39.47/Ansel charset decoder
 *
 */
public class ANSI_Z39_47 extends Charset {

    private final static HashMap encodeMap = new HashMap();
    private final static HashMap decodeMap = new HashMap();
    private Charset encodeCharset;

    public ANSI_Z39_47() {
        super("ANSI_Z39_47", CharsetProvider.aliasesFor("ANSI_Z39_47"));
        encodeCharset = Charset.forName("UTF-8");
    }

    static void charTable(char from, char to, char[] code) {
        int i = 0;

        for (char c = from; c <= to; c++) {
            if (code[i] != '\u0000') {
                encodeMap.put(Character.valueOf(code[i]), Character.valueOf(c));
                decodeMap.put(Character.valueOf(c), Character.valueOf(code[i]));
            }
            i++;
        }
    }
    private final static Object EXISTS = new Object();

    static void composeTable(String[] mappings) {

        for (int i = 0; i < mappings.length; i += 2) {
            String key = mappings[i];
            String value = mappings[i + 1];
            if (key.length() < 2) {
                continue;
            }
        }
    }

    public boolean contains(Charset charset) {
        return charset instanceof ANSI_Z39_47;
    }

    public CharsetEncoder newEncoder() {
        return new Z39_47_Encoder(this, encodeCharset.newEncoder());
    }

    public CharsetDecoder newDecoder() {
        return new Z39_47_Decoder(this, encodeCharset.newDecoder());
    }

    private static class Z39_47_Encoder extends CharsetEncoder {

        private CharsetEncoder baseEncoder;

        Z39_47_Encoder(Charset cs, CharsetEncoder baseEncoder) {
            super(cs, baseEncoder.averageBytesPerChar(),
                    baseEncoder.maxBytesPerChar());
            this.baseEncoder = baseEncoder;
        }

        protected CoderResult encodeLoop(CharBuffer cb, ByteBuffer bb) {
            CharBuffer tmpcb = CharBuffer.allocate(cb.remaining());

            while (cb.hasRemaining()) {
                tmpcb.put(cb.get());
            }

            tmpcb.rewind();

            for (int pos = tmpcb.position(); pos < tmpcb.limit(); pos++) {
                char c = tmpcb.get(pos);
                Character mapChar = (Character) encodeMap.get(new Character(c));

                if (mapChar != null) {
                    tmpcb.put(pos, mapChar.charValue());
                }
            }

            baseEncoder.reset();
            CoderResult cr = baseEncoder.encode(tmpcb, bb, true);
            cb.position(cb.position() - tmpcb.remaining());
            return cr;
        }
    }

    private static class Z39_47_Decoder extends CharsetDecoder {

        Z39_47_Decoder(Charset cs, CharsetDecoder baseDecoder) {
            super(cs, baseDecoder.averageCharsPerByte(),
                    baseDecoder.maxCharsPerByte());
        }

        private boolean isDiacritical(char ch) {
            return (ch >= 0xE0 && ch <= 0xFF);
        }

        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            CharArrayWriter w = new CharArrayWriter();
            CharArrayWriter diacritics = new CharArrayWriter();

            int pos = in.position();
            int bufpos = 0;
            while (in.hasRemaining()) {
                byte b = in.get();
                char oldChar = (char) (b & 0xFF);
                Character mapChar = (Character) decodeMap.get(Character.valueOf(oldChar));
                char ch = mapChar != null ? mapChar.charValue() : oldChar;
                if (isDiacritical(oldChar)) {
                    diacritics.write(ch);
                } else {
                    // save the char
                    w.write(ch);
                    // add diacritics if exist.
                    // diacritics must be appended in Unicode, but are
                    // prepended in Z39.47
                    if (diacritics.toString().length() > 0) {
                        try {
                            w.write(diacritics.toCharArray());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // reset diacritics temp buffer
                        diacritics = new CharArrayWriter();
                    }
                // character sequence plus diacritics complete.
                }
            }
            char[] buf = w.toCharArray();
            for (char ch : buf) {
                if (!out.hasRemaining()) {
                    in.position(pos - 1);
                    return CoderResult.OVERFLOW;
                }
                out.put(ch);
            }
            return CoderResult.UNDERFLOW;
        }
    }
    

    static {
        ANSI_Z39_47.charTable('\u00a0', '\u00ff',
                new char[]{
                    '\u00a0', '\u0141', '\u00d8', '\u0110', '\u00de', '\u00c6',
                    '\u0152', '\u02b9', '\u00b7', '\u266d', '\u00ae', '\u00b1',
                    '\u01a0', '\u01af', '\u02bc', '\u00af', '\u02bb', '\u0142',
                    '\u00f8', '\u0111', '\u00f8', '\u00e6', '\u0153', '\u02ba',
                    '\u0131', '\u00a3', '\u00f0', '\u00bb', '\u01a1', '\u01b0',
                    '\u00be', '\u00bf', '\u00b0', '\u2113', '\u2117', '\u00a9',
                    '\u266f', '\u00bf', '\u00a1', '\u00c7', '\u00c8', '\u00c9',
                    '\u00ca', '\u00cb', '\u00cc', '\u00cd', '\u00ce', '\u00df',
                    '\u00d0', '\u00d1', '\u00d2', '\u00d3', '\u00d4', '\u00d5',
                    '\u00d6', '\u00d7', '\u00d8', '\u00d9', '\u00da', '\u00db',
                    '\u00dc', '\u00dd', '\u00de', '\u00df', '\u0309', '\u0300',
                    '\u0301', '\u0302', '\u0303', '\u0304', '\u0306', '\u0307',
                    '\u0308', '\u030c', '\u030a', '\ufe20', '\ufe21', '\u0315',
                    '\u030b', '\u0310', '\u0327', '\u0328', '\u0323', '\u0324',
                    '\u0325', '\u0333', '\u0332', '\u0326', '\u0321', '\u032e',
                    '\ufe22', '\ufe23', '\u00fc', '\u00fd', '\u0313', '\u00ff'
                });
        ANSI_Z39_47.composeTable(
                new String[]{
                    "\u0041\u0300", "\u00C0",
                    "\u0041\u0301", "\u00C1",
                    "\u0041\u0302", "\u00C2",
                    "\u0041\u0303", "\u00C3",
                    "\u0041\u0308", "\u00C4",
                    "\u0041\u030A", "\u00C5",
                    "\u0043\u0327", "\u00C7",
                    "\u0045\u0300", "\u00C8",
                    "\u0045\u0301", "\u00C9",
                    "\u0045\u0302", "\u00CA",
                    "\u0045\u0308", "\u00CB",
                    "\u0049\u0300", "\u00CC",
                    "\u0049\u0301", "\u00CD",
                    "\u0049\u0302", "\u00CE",
                    "\u0049\u0308", "\u00CF",
                    "\u004E\u0303", "\u00D1",
                    "\u004F\u0300", "\u00D2",
                    "\u004F\u0301", "\u00D3",
                    "\u004F\u0302", "\u00D4",
                    "\u004F\u0303", "\u00D5",
                    "\u004F\u0308", "\u00D6",
                    "\u0055\u0300", "\u00D9",
                    "\u0055\u0301", "\u00DA",
                    "\u0055\u0302", "\u00DB",
                    "\u0055\u0308", "\u00DC",
                    "\u0059\u0301", "\u00DD",
                    "\u0061\u0300", "\u00E0",
                    "\u0061\u0301", "\u00E1",
                    "\u0061\u0302", "\u00E2",
                    "\u0061\u0303", "\u00E3",
                    "\u0061\u0308", "\u00E4",
                    "\u0061\u030A", "\u00E5",
                    "\u0063\u0327", "\u00E7",
                    "\u0065\u0300", "\u00E8",
                    "\u0065\u0301", "\u00E9",
                    "\u0065\u0302", "\u00EA",
                    "\u0065\u0308", "\u00EB",
                    "\u0069\u0300", "\u00EC",
                    "\u0069\u0301", "\u00ED",
                    "\u0069\u0302", "\u00EE",
                    "\u0069\u0308", "\u00EF",
                    "\u006E\u0303", "\u00F1",
                    "\u006F\u0300", "\u00F2",
                    "\u006F\u0301", "\u00F3",
                    "\u006F\u0302", "\u00F4",
                    "\u006F\u0303", "\u00F5",
                    "\u006F\u0308", "\u00F6",
                    "\u0075\u0300", "\u00F9",
                    "\u0075\u0301", "\u00FA",
                    "\u0075\u0302", "\u00FB",
                    "\u0075\u0308", "\u00FC",
                    "\u0079\u0301", "\u00FD",
                    "\u0079\u0308", "\u00FF",
                    "\u0041\u0304", "\u0100",
                    "\u0061\u0304", "\u0101",
                    "\u0041\u0306", "\u0102",
                    "\u0061\u0306", "\u0103",
                    "\u0041\u0328", "\u0104",
                    "\u0061\u0328", "\u0105",
                    "\u0043\u0301", "\u0106",
                    "\u0063\u0301", "\u0107",
                    "\u0043\u0302", "\u0108",
                    "\u0063\u0302", "\u0109",
                    "\u0043\u0307", "\u010A",
                    "\u0063\u0307", "\u010B",
                    "\u0043\u030C", "\u010C",
                    "\u0063\u030C", "\u010D",
                    "\u0044\u030C", "\u010E",
                    "\u0064\u030C", "\u010F",
                    "\u0045\u0304", "\u0112",
                    "\u0065\u0304", "\u0113",
                    "\u0045\u0306", "\u0114",
                    "\u0065\u0306", "\u0115",
                    "\u0045\u0307", "\u0116",
                    "\u0065\u0307", "\u0117",
                    "\u0045\u0328", "\u0118",
                    "\u0065\u0328", "\u0119",
                    "\u0045\u030C", "\u011A",
                    "\u0065\u030C", "\u011B",
                    "\u0047\u0302", "\u011C",
                    "\u0067\u0302", "\u011D",
                    "\u0047\u0306", "\u011E",
                    "\u0067\u0306", "\u011F",
                    "\u0047\u0307", "\u0120",
                    "\u0067\u0307", "\u0121",
                    "\u0047\u0327", "\u0122",
                    "\u0067\u0327", "\u0123",
                    "\u0048\u0302", "\u0124",
                    "\u0068\u0302", "\u0125",
                    "\u0049\u0303", "\u0128",
                    "\u0069\u0303", "\u0129",
                    "\u0049\u0304", "\u012A",
                    "\u0069\u0304", "\u012B",
                    "\u0049\u0306", "\u012C",
                    "\u0069\u0306", "\u012D",
                    "\u0049\u0328", "\u012E",
                    "\u0069\u0328", "\u012F",
                    "\u0049\u0307", "\u0130",
                    "\u004A\u0302", "\u0134",
                    "\u006A\u0302", "\u0135",
                    "\u004B\u0327", "\u0136",
                    "\u006B\u0327", "\u0137",
                    "\u004C\u0301", "\u0139",
                    "\u006C\u0301", "\u013A",
                    "\u004C\u0327", "\u013B",
                    "\u006C\u0327", "\u013C",
                    "\u004C\u030C", "\u013D",
                    "\u006C\u030C", "\u013E",
                    "\u004E\u0301", "\u0143",
                    "\u006E\u0301", "\u0144",
                    "\u004E\u0327", "\u0145",
                    "\u006E\u0327", "\u0146",
                    "\u004E\u030C", "\u0147",
                    "\u006E\u030C", "\u0148",
                    "\u004F\u0304", "\u014C",
                    "\u006F\u0304", "\u014D",
                    "\u004F\u0306", "\u014E",
                    "\u006F\u0306", "\u014F",
                    "\u004F\u030B", "\u0150",
                    "\u006F\u030B", "\u0151",
                    "\u0052\u0301", "\u0154",
                    "\u0072\u0301", "\u0155",
                    "\u0052\u0327", "\u0156",
                    "\u0072\u0327", "\u0157",
                    "\u0052\u030C", "\u0158",
                    "\u0072\u030C", "\u0159",
                    "\u0053\u0301", "\u015A",
                    "\u0073\u0301", "\u015B",
                    "\u0053\u0302", "\u015C",
                    "\u0073\u0302", "\u015D",
                    "\u0053\u0327", "\u015E",
                    "\u0073\u0327", "\u015F",
                    "\u0053\u030C", "\u0160",
                    "\u0073\u030C", "\u0161",
                    "\u0054\u0327", "\u0162",
                    "\u0074\u0327", "\u0163",
                    "\u0054\u030C", "\u0164",
                    "\u0074\u030C", "\u0165",
                    "\u0055\u0303", "\u0168",
                    "\u0075\u0303", "\u0169",
                    "\u0055\u0304", "\u016A",
                    "\u0075\u0304", "\u016B",
                    "\u0055\u0306", "\u016C",
                    "\u0075\u0306", "\u016D",
                    "\u0055\u030A", "\u016E",
                    "\u0075\u030A", "\u016F",
                    "\u0055\u030B", "\u0170",
                    "\u0075\u030B", "\u0171",
                    "\u0055\u0328", "\u0172",
                    "\u0075\u0328", "\u0173",
                    "\u0057\u0302", "\u0174",
                    "\u0077\u0302", "\u0175",
                    "\u0059\u0302", "\u0176",
                    "\u0079\u0302", "\u0177",
                    "\u0059\u0308", "\u0178",
                    "\u005A\u0301", "\u0179",
                    "\u007A\u0301", "\u017A",
                    "\u005A\u0307", "\u017B",
                    "\u007A\u0307", "\u017C",
                    "\u005A\u030C", "\u017D",
                    "\u007A\u030C", "\u017E",
                    "\u004F\u031B", "\u01A0",
                    "\u006F\u031B", "\u01A1",
                    "\u0055\u031B", "\u01AF",
                    "\u0075\u031B", "\u01B0",
                    "\u01F1\u030C", "\u01C4",
                    "\u01F3\u030C", "\u01C6",
                    "\u0041\u030C", "\u01CD",
                    "\u0061\u030C", "\u01CE",
                    "\u0049\u030C", "\u01CF",
                    "\u0069\u030C", "\u01D0",
                    "\u004F\u030C", "\u01D1",
                    "\u006F\u030C", "\u01D2",
                    "\u0055\u030C", "\u01D3",
                    "\u0075\u030C", "\u01D4",
                    "\u0308\u0304", "\u01D5",
                    "\u0055\u0308\u0304", "\u01D5",
                    "\u0308\u0304", "\u01D6",
                    "\u0075\u0308\u0304", "\u01D6",
                    "\u0308\u0301", "\u01D7",
                    "\u0055\u0308\u0301", "\u01D7",
                    "\u0308\u0301", "\u01D8",
                    "\u0075\u0308\u0301", "\u01D8",
                    "\u0308\u030C", "\u01D9",
                    "\u0055\u0308\u030C", "\u01D9",
                    "\u0308\u030C", "\u01DA",
                    "\u0075\u0308\u030C", "\u01DA",
                    "\u0308\u0300", "\u01DB",
                    "\u0055\u0308\u0300", "\u01DB",
                    "\u0308\u0300", "\u01DC",
                    "\u0075\u0308\u0300", "\u01DC",
                    "\u0308\u0304", "\u01DE",
                    "\u0041\u0308\u0304", "\u01DE",
                    "\u0308\u0304", "\u01DF",
                    "\u0061\u0308\u0304", "\u01DF",
                    "\u0307\u0304", "\u01E0",
                    "\u0041\u0307\u0304", "\u01E0",
                    "\u0307\u0304", "\u01E1",
                    "\u0061\u0307\u0304", "\u01E1",
                    "\u00C6\u0304", "\u01E2",
                    "\u00E6\u0304", "\u01E3",
                    "\u0047\u030C", "\u01E6",
                    "\u0067\u030C", "\u01E7",
                    "\u004B\u030C", "\u01E8",
                    "\u006B\u030C", "\u01E9",
                    "\u004F\u0328", "\u01EA",
                    "\u006F\u0328", "\u01EB",
                    "\u0328\u0304", "\u01EC",
                    "\u004F\u0328\u0304", "\u01EC",
                    "\u0328\u0304", "\u01ED",
                    "\u006F\u0328\u0304", "\u01ED",
                    "\u01B7\u030C", "\u01EE",
                    "\u0292\u030C", "\u01EF",
                    "\u006A\u030C", "\u01F0",
                    "\u0047\u0301", "\u01F4",
                    "\u0067\u0301", "\u01F5",
                    "\u030A\u0301", "\u01FA",
                    "\u0041\u030A\u0301", "\u01FA",
                    "\u030A\u0301", "\u01FB",
                    "\u0061\u030A\u0301", "\u01FB",
                    "\u00C6\u0301", "\u01FC",
                    "\u00E6\u0301", "\u01FD",
                    "\u0041\u030F", "\u0200",
                    "\u0061\u030F", "\u0201",
                    "\u0041\u0311", "\u0202",
                    "\u0061\u0311", "\u0203",
                    "\u0045\u030F", "\u0204",
                    "\u0065\u030F", "\u0205",
                    "\u0045\u0311", "\u0206",
                    "\u0065\u0311", "\u0207",
                    "\u0049\u030F", "\u0208",
                    "\u0069\u030F", "\u0209",
                    "\u0049\u0311", "\u020A",
                    "\u0069\u0311", "\u020B",
                    "\u004F\u030F", "\u020C",
                    "\u006F\u030F", "\u020D",
                    "\u004F\u0311", "\u020E",
                    "\u006F\u0311", "\u020F",
                    "\u0052\u030F", "\u0210",
                    "\u0072\u030F", "\u0211",
                    "\u0052\u0311", "\u0212",
                    "\u0072\u0311", "\u0213",
                    "\u0055\u030F", "\u0214",
                    "\u0075\u030F", "\u0215",
                    "\u0055\u0311", "\u0216",
                    "\u0075\u0311", "\u0217",
                    "\u0041\u0325", "\u1E00",
                    "\u0061\u0325", "\u1E01",
                    "\u0042\u0307", "\u1E02",
                    "\u0062\u0307", "\u1E03",
                    "\u0042\u0323", "\u1E04",
                    "\u0062\u0323", "\u1E05",
                    "\u0042\u0332", "\u1E06",
                    "\u0062\u0332", "\u1E07",
                    "\u0327\u0301", "\u1E08",
                    "\u0043\u0327\u0301", "\u1E08",
                    "\u0327\u0301", "\u1E09",
                    "\u0063\u0327\u0301", "\u1E09",
                    "\u0044\u0307", "\u1E0A",
                    "\u0064\u0307", "\u1E0B",
                    "\u0044\u0323", "\u1E0C",
                    "\u0064\u0323", "\u1E0D",
                    "\u0044\u0332", "\u1E0E",
                    "\u0064\u0332", "\u1E0F",
                    "\u0044\u0327", "\u1E10",
                    "\u0064\u0327", "\u1E11",
                    "\u0044\u032D", "\u1E12",
                    "\u0064\u032D", "\u1E13",
                    "\u0304\u0300", "\u1E14",
                    "\u0045\u0304\u0300", "\u1E14",
                    "\u0304\u0300", "\u1E15",
                    "\u0065\u0304\u0300", "\u1E15",
                    "\u0304\u0301", "\u1E16",
                    "\u0045\u0304\u0301", "\u1E16",
                    "\u0304\u0301", "\u1E17",
                    "\u0065\u0304\u0301", "\u1E17",
                    "\u0045\u032D", "\u1E18",
                    "\u0065\u032D", "\u1E19",
                    "\u0045\u0330", "\u1E1A",
                    "\u0065\u0330", "\u1E1B",
                    "\u0327\u0306", "\u1E1C",
                    "\u0045\u0327\u0306", "\u1E1C",
                    "\u0327\u0306", "\u1E1D",
                    "\u0065\u0327\u0306", "\u1E1D",
                    "\u0046\u0307", "\u1E1E",
                    "\u0066\u0307", "\u1E1F",
                    "\u0047\u0304", "\u1E20",
                    "\u0067\u0304", "\u1E21",
                    "\u0048\u0307", "\u1E22",
                    "\u0068\u0307", "\u1E23",
                    "\u0048\u0323", "\u1E24",
                    "\u0068\u0323", "\u1E25",
                    "\u0048\u0308", "\u1E26",
                    "\u0068\u0308", "\u1E27",
                    "\u0048\u0327", "\u1E28",
                    "\u0068\u0327", "\u1E29",
                    "\u0048\u032E", "\u1E2A",
                    "\u0068\u032E", "\u1E2B",
                    "\u0049\u0330", "\u1E2C",
                    "\u0069\u0330", "\u1E2D",
                    "\u0308\u0301", "\u1E2E",
                    "\u0049\u0308\u0301", "\u1E2E",
                    "\u0308\u0301", "\u1E2F",
                    "\u0069\u0308\u0301", "\u1E2F",
                    "\u004B\u0301", "\u1E30",
                    "\u006B\u0301", "\u1E31",
                    "\u004B\u0323", "\u1E32",
                    "\u006B\u0323", "\u1E33",
                    "\u004B\u0332", "\u1E34",
                    "\u006B\u0332", "\u1E35",
                    "\u004C\u0323", "\u1E36",
                    "\u006C\u0323", "\u1E37",
                    "\u0323\u0304", "\u1E38",
                    "\u004C\u0323\u0304", "\u1E38",
                    "\u0323\u0304", "\u1E39",
                    "\u006C\u0323\u0304", "\u1E39",
                    "\u004C\u0332", "\u1E3A",
                    "\u006C\u0332", "\u1E3B",
                    "\u004C\u032D", "\u1E3C",
                    "\u006C\u032D", "\u1E3D",
                    "\u004D\u0301", "\u1E3E",
                    "\u006D\u0301", "\u1E3F",
                    "\u004D\u0307", "\u1E40",
                    "\u006D\u0307", "\u1E41",
                    "\u004D\u0323", "\u1E42",
                    "\u006D\u0323", "\u1E43",
                    "\u004E\u0307", "\u1E44",
                    "\u006E\u0307", "\u1E45",
                    "\u004E\u0323", "\u1E46",
                    "\u006E\u0323", "\u1E47",
                    "\u004E\u0332", "\u1E48",
                    "\u006E\u0332", "\u1E49",
                    "\u004E\u032D", "\u1E4A",
                    "\u006E\u032D", "\u1E4B",
                    "\u0303\u0301", "\u1E4C",
                    "\u004F\u0303\u0301", "\u1E4C",
                    "\u0303\u0301", "\u1E4D",
                    "\u006F\u0303\u0301", "\u1E4D",
                    "\u0303\u0308", "\u1E4E",
                    "\u004F\u0303\u0308", "\u1E4E",
                    "\u0303\u0308", "\u1E4F",
                    "\u006F\u0303\u0308", "\u1E4F",
                    "\u0304\u0300", "\u1E50",
                    "\u004F\u0304\u0300", "\u1E50",
                    "\u0304\u0300", "\u1E51",
                    "\u006F\u0304\u0300", "\u1E51",
                    "\u0304\u0301", "\u1E52",
                    "\u004F\u0304\u0301", "\u1E52",
                    "\u0304\u0301", "\u1E53",
                    "\u006F\u0304\u0301", "\u1E53",
                    "\u0050\u0301", "\u1E54",
                    "\u0070\u0301", "\u1E55",
                    "\u0050\u0307", "\u1E56",
                    "\u0070\u0307", "\u1E57",
                    "\u0052\u0307", "\u1E58",
                    "\u0072\u0307", "\u1E59",
                    "\u0052\u0323", "\u1E5A",
                    "\u0072\u0323", "\u1E5B",
                    "\u0323\u0304", "\u1E5C",
                    "\u0052\u0323\u0304", "\u1E5C",
                    "\u0323\u0304", "\u1E5D",
                    "\u0072\u0323\u0304", "\u1E5D",
                    "\u0052\u0332", "\u1E5E",
                    "\u0072\u0332", "\u1E5F",
                    "\u0053\u0307", "\u1E60",
                    "\u0073\u0307", "\u1E61",
                    "\u0053\u0323", "\u1E62",
                    "\u0073\u0323", "\u1E63",
                    "\u0301\u0307", "\u1E64",
                    "\u0053\u0301\u0307", "\u1E64",
                    "\u0301\u0307", "\u1E65",
                    "\u0073\u0301\u0307", "\u1E65",
                    "\u030C\u0307", "\u1E66",
                    "\u0053\u030C\u0307", "\u1E66",
                    "\u030C\u0307", "\u1E67",
                    "\u0073\u030C\u0307", "\u1E67",
                    "\u0323\u0307", "\u1E68",
                    "\u0053\u0323\u0307", "\u1E68",
                    "\u0323\u0307", "\u1E69",
                    "\u0073\u0323\u0307", "\u1E69",
                    "\u0054\u0307", "\u1E6A",
                    "\u0074\u0307", "\u1E6B",
                    "\u0054\u0323", "\u1E6C",
                    "\u0074\u0323", "\u1E6D",
                    "\u0054\u0332", "\u1E6E",
                    "\u0074\u0332", "\u1E6F",
                    "\u0054\u032D", "\u1E70",
                    "\u0074\u032D", "\u1E71",
                    "\u0055\u0324", "\u1E72",
                    "\u0075\u0324", "\u1E73",
                    "\u0055\u0330", "\u1E74",
                    "\u0075\u0330", "\u1E75",
                    "\u0055\u032D", "\u1E76",
                    "\u0075\u032D", "\u1E77",
                    "\u0303\u0301", "\u1E78",
                    "\u0055\u0303\u0301", "\u1E78",
                    "\u0303\u0301", "\u1E79",
                    "\u0075\u0303\u0301", "\u1E79",
                    "\u0304\u0308", "\u1E7A",
                    "\u0055\u0304\u0308", "\u1E7A",
                    "\u0304\u0308", "\u1E7B",
                    "\u0075\u0304\u0308", "\u1E7B",
                    "\u0056\u0303", "\u1E7C",
                    "\u0076\u0303", "\u1E7D",
                    "\u0056\u0323", "\u1E7E",
                    "\u0076\u0323", "\u1E7F",
                    "\u0057\u0300", "\u1E80",
                    "\u0077\u0300", "\u1E81",
                    "\u0057\u0301", "\u1E82",
                    "\u0077\u0301", "\u1E83",
                    "\u0057\u0308", "\u1E84",
                    "\u0077\u0308", "\u1E85",
                    "\u0057\u0307", "\u1E86",
                    "\u0077\u0307", "\u1E87",
                    "\u0057\u0323", "\u1E88",
                    "\u0077\u0323", "\u1E89",
                    "\u0058\u0307", "\u1E8A",
                    "\u0078\u0307", "\u1E8B",
                    "\u0058\u0308", "\u1E8C",
                    "\u0078\u0308", "\u1E8D",
                    "\u0059\u0307", "\u1E8E",
                    "\u0079\u0307", "\u1E8F",
                    "\u005A\u0302", "\u1E90",
                    "\u007A\u0302", "\u1E91",
                    "\u005A\u0323", "\u1E92",
                    "\u007A\u0323", "\u1E93",
                    "\u005A\u0332", "\u1E94",
                    "\u007A\u0332", "\u1E95",
                    "\u0068\u0332", "\u1E96",
                    "\u0074\u0308", "\u1E97",
                    "\u0077\u030A", "\u1E98",
                    "\u0079\u030A", "\u1E99",
                    "\u017F\u0307", "\u1E9B",
                    "\u0041\u0323", "\u1EA0",
                    "\u0061\u0323", "\u1EA1",
                    "\u0041\u0309", "\u1EA2",
                    "\u0061\u0309", "\u1EA3",
                    "\u0302\u0301", "\u1EA4",
                    "\u0041\u0302\u0301", "\u1EA4",
                    "\u0302\u0301", "\u1EA5",
                    "\u0061\u0302\u0301", "\u1EA5",
                    "\u0302\u0300", "\u1EA6",
                    "\u0041\u0302\u0300", "\u1EA6",
                    "\u0302\u0300", "\u1EA7",
                    "\u0061\u0302\u0300", "\u1EA7",
                    "\u0302\u0309", "\u1EA8",
                    "\u0041\u0302\u0309", "\u1EA8",
                    "\u0302\u0309", "\u1EA9",
                    "\u0061\u0302\u0309", "\u1EA9",
                    "\u0302\u0303", "\u1EAA",
                    "\u0041\u0302\u0303", "\u1EAA",
                    "\u0302\u0303", "\u1EAB",
                    "\u0061\u0302\u0303", "\u1EAB",
                    "\u0302\u0323", "\u1EAC",
                    "\u0041\u0302\u0323", "\u1EAC",
                    "\u0302\u0323", "\u1EAD",
                    "\u0061\u0302\u0323", "\u1EAD",
                    "\u0306\u0301", "\u1EAE",
                    "\u0041\u0306\u0301", "\u1EAE",
                    "\u0306\u0301", "\u1EAF",
                    "\u0061\u0306\u0301", "\u1EAF",
                    "\u0306\u0300", "\u1EB0",
                    "\u0041\u0306\u0300", "\u1EB0",
                    "\u0306\u0300", "\u1EB1",
                    "\u0061\u0306\u0300", "\u1EB1",
                    "\u0306\u0309", "\u1EB2",
                    "\u0041\u0306\u0309", "\u1EB2",
                    "\u0306\u0309", "\u1EB3",
                    "\u0061\u0306\u0309", "\u1EB3",
                    "\u0306\u0303", "\u1EB4",
                    "\u0041\u0306\u0303", "\u1EB4",
                    "\u0306\u0303", "\u1EB5",
                    "\u0061\u0306\u0303", "\u1EB5",
                    "\u0306\u0323", "\u1EB6",
                    "\u0041\u0306\u0323", "\u1EB6",
                    "\u0306\u0323", "\u1EB7",
                    "\u0061\u0306\u0323", "\u1EB7",
                    "\u0045\u0323", "\u1EB8",
                    "\u0065\u0323", "\u1EB9",
                    "\u0045\u0309", "\u1EBA",
                    "\u0065\u0309", "\u1EBB",
                    "\u0045\u0303", "\u1EBC",
                    "\u0065\u0303", "\u1EBD",
                    "\u0302\u0301", "\u1EBE",
                    "\u0045\u0302\u0301", "\u1EBE",
                    "\u0302\u0301", "\u1EBF",
                    "\u0065\u0302\u0301", "\u1EBF",
                    "\u0302\u0300", "\u1EC0",
                    "\u0045\u0302\u0300", "\u1EC0",
                    "\u0302\u0300", "\u1EC1",
                    "\u0065\u0302\u0300", "\u1EC1",
                    "\u0302\u0309", "\u1EC2",
                    "\u0045\u0302\u0309", "\u1EC2",
                    "\u0302\u0309", "\u1EC3",
                    "\u0065\u0302\u0309", "\u1EC3",
                    "\u0302\u0303", "\u1EC4",
                    "\u0045\u0302\u0303", "\u1EC4",
                    "\u0302\u0303", "\u1EC5",
                    "\u0065\u0302\u0303", "\u1EC5",
                    "\u0302\u0323", "\u1EC6",
                    "\u0045\u0302\u0323", "\u1EC6",
                    "\u0302\u0323", "\u1EC7",
                    "\u0065\u0302\u0323", "\u1EC7",
                    "\u0049\u0309", "\u1EC8",
                    "\u0069\u0309", "\u1EC9",
                    "\u0049\u0323", "\u1ECA",
                    "\u0069\u0323", "\u1ECB",
                    "\u004F\u0323", "\u1ECC",
                    "\u006F\u0323", "\u1ECD",
                    "\u004F\u0309", "\u1ECE",
                    "\u006F\u0309", "\u1ECF",
                    "\u0302\u0301", "\u1ED0",
                    "\u004F\u0302\u0301", "\u1ED0",
                    "\u0302\u0301", "\u1ED1",
                    "\u006F\u0302\u0301", "\u1ED1",
                    "\u0302\u0300", "\u1ED2",
                    "\u004F\u0302\u0300", "\u1ED2",
                    "\u0302\u0300", "\u1ED3",
                    "\u006F\u0302\u0300", "\u1ED3",
                    "\u0302\u0309", "\u1ED4",
                    "\u004F\u0302\u0309", "\u1ED4",
                    "\u0302\u0309", "\u1ED5",
                    "\u006F\u0302\u0309", "\u1ED5",
                    "\u0302\u0303", "\u1ED6",
                    "\u004F\u0302\u0303", "\u1ED6",
                    "\u0302\u0303", "\u1ED7",
                    "\u006F\u0302\u0303", "\u1ED7",
                    "\u0302\u0323", "\u1ED8",
                    "\u004F\u0302\u0323", "\u1ED8",
                    "\u0302\u0323", "\u1ED9",
                    "\u006F\u0302\u0323", "\u1ED9",
                    "\u031B\u0301", "\u1EDA",
                    "\u004F\u031B\u0301", "\u1EDA",
                    "\u031B\u0301", "\u1EDB",
                    "\u006F\u031B\u0301", "\u1EDB",
                    "\u031B\u0300", "\u1EDC",
                    "\u004F\u031B\u0300", "\u1EDC",
                    "\u031B\u0300", "\u1EDD",
                    "\u006F\u031B\u0300", "\u1EDD",
                    "\u031B\u0309", "\u1EDE",
                    "\u004F\u031B\u0309", "\u1EDE",
                    "\u031B\u0309", "\u1EDF",
                    "\u006F\u031B\u0309", "\u1EDF",
                    "\u031B\u0303", "\u1EE0",
                    "\u004F\u031B\u0303", "\u1EE0",
                    "\u031B\u0303", "\u1EE1",
                    "\u006F\u031B\u0303", "\u1EE1",
                    "\u031B\u0323", "\u1EE2",
                    "\u004F\u031B\u0323", "\u1EE2",
                    "\u031B\u0323", "\u1EE3",
                    "\u006F\u031B\u0323", "\u1EE3",
                    "\u0055\u0323", "\u1EE4",
                    "\u0075\u0323", "\u1EE5",
                    "\u0055\u0309", "\u1EE6",
                    "\u0075\u0309", "\u1EE7",
                    "\u031B\u0301", "\u1EE8",
                    "\u0055\u031B\u0301", "\u1EE8",
                    "\u031B\u0301", "\u1EE9",
                    "\u0075\u031B\u0301", "\u1EE9",
                    "\u031B\u0300", "\u1EEA",
                    "\u0055\u031B\u0300", "\u1EEA",
                    "\u031B\u0300", "\u1EEB",
                    "\u0075\u031B\u0300", "\u1EEB",
                    "\u031B\u0309", "\u1EEC",
                    "\u0055\u031B\u0309", "\u1EEC",
                    "\u031B\u0309", "\u1EED",
                    "\u0075\u031B\u0309", "\u1EED",
                    "\u031B\u0303", "\u1EEE",
                    "\u0055\u031B\u0303", "\u1EEE",
                    "\u031B\u0303", "\u1EEF",
                    "\u0075\u031B\u0303", "\u1EEF",
                    "\u031B\u0323", "\u1EF0",
                    "\u0055\u031B\u0323", "\u1EF0",
                    "\u031B\u0323", "\u1EF1",
                    "\u0075\u031B\u0323", "\u1EF1",
                    "\u0059\u0300", "\u1EF2",
                    "\u0079\u0300", "\u1EF3",
                    "\u0059\u0323", "\u1EF4",
                    "\u0079\u0323", "\u1EF5",
                    "\u0059\u0309", "\u1EF6",
                    "\u0079\u0309", "\u1EF7",
                    "\u0059\u0303", "\u1EF8",
                    "\u0079\u0303", "\u1EF9"
                });

    }
}
/* step 1
#created: 2001-03-19
A1=0141#latin capital letter L with stroke
A2=00D8#latin capital letter O with stroke
A3=0110#latin capital letter D with stroke
A4=00DE#latin capital letter thorn
A5=00C6#latin capital letter AE
A6=0152#latin capital ligature OE
A7=02B9#modified letter prime
A8=00B7#middle dot
A9=266D#music flat sign
AA=00AE#registered sign
AB=00B1#plus-minus sign
AC=01A0#latin capital letter O with horn
AD=01AF#latin capital letter U with horn
AE=02BC#modifier letter apostrophe
B0=02BB#modifier letter turned comma
B1=0142#latin small letter L with stroke
B2=00F8#latin small letter O with stroke
B3=0111#latin small letter D with stroke
B4=00FE#latin small letter thorn
B5=00E6#latin small letter AE
B6=0153#latin small ligature OE
B7=02BA#modified letter double prime
B8=0131#latin small letter dotless i
B9=00A3#pound sign
BA=00F0#latin small letter eth
BC=01A1#latin small letter O with horn
BD=01B0#latin small letter U with horn
C0=00B0#degree sign
C1=2113#script small L
C2=2117#sound recording copyright
C3=00A9#copyright sign
C4=266F#music sharp sign
C5=00BF#inverted question mark
C6=00A1#inverted exclamation mark
CF=00DF#latin small letter sharp S
E0=0309#combining hook above
E1=0300#combining grave accent
E2=0301#combining acute accent
E3=0302#combining circumflex accent
E4=0303#combining tilde
E5=0304#combining macron
E6=0306#combining breve
E7=0307#combining dot above
E8=0308#combining diaeresis
E9=030C#combining caron
EA=030A#combining ring above
EB=FE20#combining ligature left half
EC=FE21#combining ligature right half
ED=0315#combining comma above right
EE=030B#combining double acute accent
EF=0310#combining candrabindu
F0=0327#combining cedilla
F1=0328#combining ogonek
F2=0323#combining dot below
F3=0324#combining diaeresis below
F4=0325#combining ring below
F5=0333#combining double low line
F6=0332#combining low line
F7=0326#combining comma below
F8=0321#combining ogonek
F9=032E#combining breve below
FA=FE22#combining double tilde left half
FB=FE23#combining double tilde right half
FE=0313#combining comma above

step 2


#created: 20 january 1998
0041+0300=00C0# LATIN CAPITAL LETTER A WITH GRAVE = LATIN CAPITAL LETTER A + COMBINING GRAVE ACCENT
0041+0301=00C1# LATIN CAPITAL LETTER A WITH ACUTE = LATIN CAPITAL LETTER A + COMBINING ACUTE ACCENT
0041+0302=00C2# LATIN CAPITAL LETTER A WITH CIRCUMFLEX = LATIN CAPITAL LETTER A + COMBINING CIRCUMFLEX ACCENT
0041+0303=00C3# LATIN CAPITAL LETTER A WITH TILDE = LATIN CAPITAL LETTER A + COMBINING TILDE
0041+0308=00C4# LATIN CAPITAL LETTER A WITH DIAERESIS = LATIN CAPITAL LETTER A + COMBINING DIAERESIS
0041+030A=00C5# LATIN CAPITAL LETTER A WITH RING ABOVE = LATIN CAPITAL LETTER A + COMBINING RING ABOVE
0043+0327=00C7# LATIN CAPITAL LETTER C WITH CEDILLA = LATIN CAPITAL LETTER C + COMBINING CEDILLA
0045+0300=00C8# LATIN CAPITAL LETTER E WITH GRAVE = LATIN CAPITAL LETTER E + COMBINING GRAVE ACCENT
0045+0301=00C9# LATIN CAPITAL LETTER E WITH ACUTE = LATIN CAPITAL LETTER E + COMBINING ACUTE ACCENT
0045+0302=00CA# LATIN CAPITAL LETTER E WITH CIRCUMFLEX = LATIN CAPITAL LETTER E + COMBINING CIRCUMFLEX ACCENT
0045+0308=00CB# LATIN CAPITAL LETTER E WITH DIAERESIS = LATIN CAPITAL LETTER E + COMBINING DIAERESIS
0049+0300=00CC# LATIN CAPITAL LETTER I WITH GRAVE = LATIN CAPITAL LETTER I + COMBINING GRAVE ACCENT
0049+0301=00CD# LATIN CAPITAL LETTER I WITH ACUTE = LATIN CAPITAL LETTER I + COMBINING ACUTE ACCENT
0049+0302=00CE# LATIN CAPITAL LETTER I WITH CIRCUMFLEX = LATIN CAPITAL LETTER I + COMBINING CIRCUMFLEX ACCENT
0049+0308=00CF# LATIN CAPITAL LETTER I WITH DIAERESIS = LATIN CAPITAL LETTER I + COMBINING DIAERESIS
004E+0303=00D1# LATIN CAPITAL LETTER N WITH TILDE = LATIN CAPITAL LETTER N + COMBINING TILDE
004F+0300=00D2# LATIN CAPITAL LETTER O WITH GRAVE = LATIN CAPITAL LETTER O + COMBINING GRAVE ACCENT
004F+0301=00D3# LATIN CAPITAL LETTER O WITH ACUTE = LATIN CAPITAL LETTER O + COMBINING ACUTE ACCENT
004F+0302=00D4# LATIN CAPITAL LETTER O WITH CIRCUMFLEX = LATIN CAPITAL LETTER O + COMBINING CIRCUMFLEX ACCENT
004F+0303=00D5# LATIN CAPITAL LETTER O WITH TILDE = LATIN CAPITAL LETTER O + COMBINING TILDE
004F+0308=00D6# LATIN CAPITAL LETTER O WITH DIAERESIS = LATIN CAPITAL LETTER O + COMBINING DIAERESIS
0055+0300=00D9# LATIN CAPITAL LETTER U WITH GRAVE = LATIN CAPITAL LETTER U + COMBINING GRAVE ACCENT
0055+0301=00DA# LATIN CAPITAL LETTER U WITH ACUTE = LATIN CAPITAL LETTER U + COMBINING ACUTE ACCENT
0055+0302=00DB# LATIN CAPITAL LETTER U WITH CIRCUMFLEX = LATIN CAPITAL LETTER U + COMBINING CIRCUMFLEX ACCENT
0055+0308=00DC# LATIN CAPITAL LETTER U WITH DIAERESIS = LATIN CAPITAL LETTER U + COMBINING DIAERESIS
0059+0301=00DD# LATIN CAPITAL LETTER Y WITH ACUTE = LATIN CAPITAL LETTER Y + COMBINING ACUTE ACCENT
0061+0300=00E0# LATIN SMALL LETTER A WITH GRAVE = LATIN SMALL LETTER A + COMBINING GRAVE ACCENT
0061+0301=00E1# LATIN SMALL LETTER A WITH ACUTE = LATIN SMALL LETTER A + COMBINING ACUTE ACCENT
0061+0302=00E2# LATIN SMALL LETTER A WITH CIRCUMFLEX = LATIN SMALL LETTER A + COMBINING CIRCUMFLEX ACCENT
0061+0303=00E3# LATIN SMALL LETTER A WITH TILDE = LATIN SMALL LETTER A + COMBINING TILDE
0061+0308=00E4# LATIN SMALL LETTER A WITH DIAERESIS = LATIN SMALL LETTER A + COMBINING DIAERESIS
0061+030A=00E5# LATIN SMALL LETTER A WITH RING ABOVE = LATIN SMALL LETTER A + COMBINING RING ABOVE
0063+0327=00E7# LATIN SMALL LETTER C WITH CEDILLA = LATIN SMALL LETTER C + COMBINING CEDILLA
0065+0300=00E8# LATIN SMALL LETTER E WITH GRAVE = LATIN SMALL LETTER E + COMBINING GRAVE ACCENT
0065+0301=00E9# LATIN SMALL LETTER E WITH ACUTE = LATIN SMALL LETTER E + COMBINING ACUTE ACCENT
0065+0302=00EA# LATIN SMALL LETTER E WITH CIRCUMFLEX = LATIN SMALL LETTER E + COMBINING CIRCUMFLEX ACCENT
0065+0308=00EB# LATIN SMALL LETTER E WITH DIAERESIS = LATIN SMALL LETTER E + COMBINING DIAERESIS
0069+0300=00EC# LATIN SMALL LETTER I WITH GRAVE = LATIN SMALL LETTER I + COMBINING GRAVE ACCENT
0069+0301=00ED# LATIN SMALL LETTER I WITH ACUTE = LATIN SMALL LETTER I + COMBINING ACUTE ACCENT
0069+0302=00EE# LATIN SMALL LETTER I WITH CIRCUMFLEX = LATIN SMALL LETTER I + COMBINING CIRCUMFLEX ACCENT
0069+0308=00EF# LATIN SMALL LETTER I WITH DIAERESIS = LATIN SMALL LETTER I + COMBINING DIAERESIS
006E+0303=00F1# LATIN SMALL LETTER N WITH TILDE = LATIN SMALL LETTER N + COMBINING TILDE
006F+0300=00F2# LATIN SMALL LETTER O WITH GRAVE = LATIN SMALL LETTER O + COMBINING GRAVE ACCENT
006F+0301=00F3# LATIN SMALL LETTER O WITH ACUTE = LATIN SMALL LETTER O + COMBINING ACUTE ACCENT
006F+0302=00F4# LATIN SMALL LETTER O WITH CIRCUMFLEX = LATIN SMALL LETTER O + COMBINING CIRCUMFLEX ACCENT
006F+0303=00F5# LATIN SMALL LETTER O WITH TILDE = LATIN SMALL LETTER O + COMBINING TILDE
006F+0308=00F6# LATIN SMALL LETTER O WITH DIAERESIS = LATIN SMALL LETTER O + COMBINING DIAERESIS
0075+0300=00F9# LATIN SMALL LETTER U WITH GRAVE = LATIN SMALL LETTER U + COMBINING GRAVE ACCENT
0075+0301=00FA# LATIN SMALL LETTER U WITH ACUTE = LATIN SMALL LETTER U + COMBINING ACUTE ACCENT
0075+0302=00FB# LATIN SMALL LETTER U WITH CIRCUMFLEX = LATIN SMALL LETTER U + COMBINING CIRCUMFLEX ACCENT
0075+0308=00FC# LATIN SMALL LETTER U WITH DIAERESIS = LATIN SMALL LETTER U + COMBINING DIAERESIS
0079+0301=00FD# LATIN SMALL LETTER Y WITH ACUTE = LATIN SMALL LETTER Y + COMBINING ACUTE ACCENT
0079+0308=00FF# LATIN SMALL LETTER Y WITH DIAERESIS = LATIN SMALL LETTER Y + COMBINING DIAERESIS
0041+0304=0100# LATIN CAPITAL LETTER A WITH MACRON = LATIN CAPITAL LETTER A + COMBINING MACRON
0061+0304=0101# LATIN SMALL LETTER A WITH MACRON = LATIN SMALL LETTER A + COMBINING MACRON
0041+0306=0102# LATIN CAPITAL LETTER A WITH BREVE = LATIN CAPITAL LETTER A + COMBINING BREVE
0061+0306=0103# LATIN SMALL LETTER A WITH BREVE = LATIN SMALL LETTER A + COMBINING BREVE
0041+0328=0104# LATIN CAPITAL LETTER A WITH OGONEK = LATIN CAPITAL LETTER A + COMBINING OGONEK
0061+0328=0105# LATIN SMALL LETTER A WITH OGONEK = LATIN SMALL LETTER A + COMBINING OGONEK
0043+0301=0106# LATIN CAPITAL LETTER C WITH ACUTE = LATIN CAPITAL LETTER C + COMBINING ACUTE ACCENT
0063+0301=0107# LATIN SMALL LETTER C WITH ACUTE = LATIN SMALL LETTER C + COMBINING ACUTE ACCENT
0043+0302=0108# LATIN CAPITAL LETTER C WITH CIRCUMFLEX = LATIN CAPITAL LETTER C + COMBINING CIRCUMFLEX ACCENT
0063+0302=0109# LATIN SMALL LETTER C WITH CIRCUMFLEX = LATIN SMALL LETTER C + COMBINING CIRCUMFLEX ACCENT
0043+0307=010A# LATIN CAPITAL LETTER C WITH DOT ABOVE = LATIN CAPITAL LETTER C + COMBINING DOT ABOVE
0063+0307=010B# LATIN SMALL LETTER C WITH DOT ABOVE = LATIN SMALL LETTER C + COMBINING DOT ABOVE
0043+030C=010C# LATIN CAPITAL LETTER C WITH CARON = LATIN CAPITAL LETTER C + COMBINING CARON
0063+030C=010D# LATIN SMALL LETTER C WITH CARON = LATIN SMALL LETTER C + COMBINING CARON
0044+030C=010E# LATIN CAPITAL LETTER D WITH CARON = LATIN CAPITAL LETTER D + COMBINING CARON
0064+030C=010F# LATIN SMALL LETTER D WITH CARON = LATIN SMALL LETTER D + COMBINING CARON
0045+0304=0112# LATIN CAPITAL LETTER E WITH MACRON = LATIN CAPITAL LETTER E + COMBINING MACRON
0065+0304=0113# LATIN SMALL LETTER E WITH MACRON = LATIN SMALL LETTER E + COMBINING MACRON
0045+0306=0114# LATIN CAPITAL LETTER E WITH BREVE = LATIN CAPITAL LETTER E + COMBINING BREVE
0065+0306=0115# LATIN SMALL LETTER E WITH BREVE = LATIN SMALL LETTER E + COMBINING BREVE
0045+0307=0116# LATIN CAPITAL LETTER E WITH DOT ABOVE = LATIN CAPITAL LETTER E + COMBINING DOT ABOVE
0065+0307=0117# LATIN SMALL LETTER E WITH DOT ABOVE = LATIN SMALL LETTER E + COMBINING DOT ABOVE
0045+0328=0118# LATIN CAPITAL LETTER E WITH OGONEK = LATIN CAPITAL LETTER E + COMBINING OGONEK
0065+0328=0119# LATIN SMALL LETTER E WITH OGONEK = LATIN SMALL LETTER E + COMBINING OGONEK
0045+030C=011A# LATIN CAPITAL LETTER E WITH CARON = LATIN CAPITAL LETTER E + COMBINING CARON
0065+030C=011B# LATIN SMALL LETTER E WITH CARON = LATIN SMALL LETTER E + COMBINING CARON
0047+0302=011C# LATIN CAPITAL LETTER G WITH CIRCUMFLEX = LATIN CAPITAL LETTER G + COMBINING CIRCUMFLEX ACCENT
0067+0302=011D# LATIN SMALL LETTER G WITH CIRCUMFLEX = LATIN SMALL LETTER G + COMBINING CIRCUMFLEX ACCENT
0047+0306=011E# LATIN CAPITAL LETTER G WITH BREVE = LATIN CAPITAL LETTER G + COMBINING BREVE
0067+0306=011F# LATIN SMALL LETTER G WITH BREVE = LATIN SMALL LETTER G + COMBINING BREVE
0047+0307=0120# LATIN CAPITAL LETTER G WITH DOT ABOVE = LATIN CAPITAL LETTER G + COMBINING DOT ABOVE
0067+0307=0121# LATIN SMALL LETTER G WITH DOT ABOVE = LATIN SMALL LETTER G + COMBINING DOT ABOVE
0047+0327=0122# LATIN CAPITAL LETTER G WITH CEDILLA = LATIN CAPITAL LETTER G + COMBINING CEDILLA
0067+0327=0123# LATIN SMALL LETTER G WITH CEDILLA = LATIN SMALL LETTER G + COMBINING CEDILLA
0048+0302=0124# LATIN CAPITAL LETTER H WITH CIRCUMFLEX = LATIN CAPITAL LETTER H + COMBINING CIRCUMFLEX ACCENT
0068+0302=0125# LATIN SMALL LETTER H WITH CIRCUMFLEX = LATIN SMALL LETTER H + COMBINING CIRCUMFLEX ACCENT
0049+0303=0128# LATIN CAPITAL LETTER I WITH TILDE = LATIN CAPITAL LETTER I + COMBINING TILDE
0069+0303=0129# LATIN SMALL LETTER I WITH TILDE = LATIN SMALL LETTER I + COMBINING TILDE
0049+0304=012A# LATIN CAPITAL LETTER I WITH MACRON = LATIN CAPITAL LETTER I + COMBINING MACRON
0069+0304=012B# LATIN SMALL LETTER I WITH MACRON = LATIN SMALL LETTER I + COMBINING MACRON
0049+0306=012C# LATIN CAPITAL LETTER I WITH BREVE = LATIN CAPITAL LETTER I + COMBINING BREVE
0069+0306=012D# LATIN SMALL LETTER I WITH BREVE = LATIN SMALL LETTER I + COMBINING BREVE
0049+0328=012E# LATIN CAPITAL LETTER I WITH OGONEK = LATIN CAPITAL LETTER I + COMBINING OGONEK
0069+0328=012F# LATIN SMALL LETTER I WITH OGONEK = LATIN SMALL LETTER I + COMBINING OGONEK
0049+0307=0130# LATIN CAPITAL LETTER I WITH DOT ABOVE = LATIN CAPITAL LETTER I + COMBINING DOT ABOVE
004A+0302=0134# LATIN CAPITAL LETTER J WITH CIRCUMFLEX = LATIN CAPITAL LETTER J + COMBINING CIRCUMFLEX ACCENT
006A+0302=0135# LATIN SMALL LETTER J WITH CIRCUMFLEX = LATIN SMALL LETTER J + COMBINING CIRCUMFLEX ACCENT
004B+0327=0136# LATIN CAPITAL LETTER K WITH CEDILLA = LATIN CAPITAL LETTER K + COMBINING CEDILLA
006B+0327=0137# LATIN SMALL LETTER K WITH CEDILLA = LATIN SMALL LETTER K + COMBINING CEDILLA
004C+0301=0139# LATIN CAPITAL LETTER L WITH ACUTE = LATIN CAPITAL LETTER L + COMBINING ACUTE ACCENT
006C+0301=013A# LATIN SMALL LETTER L WITH ACUTE = LATIN SMALL LETTER L + COMBINING ACUTE ACCENT
004C+0327=013B# LATIN CAPITAL LETTER L WITH CEDILLA = LATIN CAPITAL LETTER L + COMBINING CEDILLA
006C+0327=013C# LATIN SMALL LETTER L WITH CEDILLA = LATIN SMALL LETTER L + COMBINING CEDILLA
004C+030C=013D# LATIN CAPITAL LETTER L WITH CARON = LATIN CAPITAL LETTER L + COMBINING CARON
006C+030C=013E# LATIN SMALL LETTER L WITH CARON = LATIN SMALL LETTER L + COMBINING CARON
004E+0301=0143# LATIN CAPITAL LETTER N WITH ACUTE = LATIN CAPITAL LETTER N + COMBINING ACUTE ACCENT
006E+0301=0144# LATIN SMALL LETTER N WITH ACUTE = LATIN SMALL LETTER N + COMBINING ACUTE ACCENT
004E+0327=0145# LATIN CAPITAL LETTER N WITH CEDILLA = LATIN CAPITAL LETTER N + COMBINING CEDILLA
006E+0327=0146# LATIN SMALL LETTER N WITH CEDILLA = LATIN SMALL LETTER N + COMBINING CEDILLA
004E+030C=0147# LATIN CAPITAL LETTER N WITH CARON = LATIN CAPITAL LETTER N + COMBINING CARON
006E+030C=0148# LATIN SMALL LETTER N WITH CARON = LATIN SMALL LETTER N + COMBINING CARON
004F+0304=014C# LATIN CAPITAL LETTER O WITH MACRON = LATIN CAPITAL LETTER O + COMBINING MACRON
006F+0304=014D# LATIN SMALL LETTER O WITH MACRON = LATIN SMALL LETTER O + COMBINING MACRON
004F+0306=014E# LATIN CAPITAL LETTER O WITH BREVE = LATIN CAPITAL LETTER O + COMBINING BREVE
006F+0306=014F# LATIN SMALL LETTER O WITH BREVE = LATIN SMALL LETTER O + COMBINING BREVE
004F+030B=0150# LATIN CAPITAL LETTER O WITH DOUBLE ACUTE = LATIN CAPITAL LETTER O + COMBINING DOUBLE ACUTE ACCENT
006F+030B=0151# LATIN SMALL LETTER O WITH DOUBLE ACUTE = LATIN SMALL LETTER O + COMBINING DOUBLE ACUTE ACCENT
0052+0301=0154# LATIN CAPITAL LETTER R WITH ACUTE = LATIN CAPITAL LETTER R + COMBINING ACUTE ACCENT
0072+0301=0155# LATIN SMALL LETTER R WITH ACUTE = LATIN SMALL LETTER R + COMBINING ACUTE ACCENT
0052+0327=0156# LATIN CAPITAL LETTER R WITH CEDILLA = LATIN CAPITAL LETTER R + COMBINING CEDILLA
0072+0327=0157# LATIN SMALL LETTER R WITH CEDILLA = LATIN SMALL LETTER R + COMBINING CEDILLA
0052+030C=0158# LATIN CAPITAL LETTER R WITH CARON = LATIN CAPITAL LETTER R + COMBINING CARON
0072+030C=0159# LATIN SMALL LETTER R WITH CARON = LATIN SMALL LETTER R + COMBINING CARON
0053+0301=015A# LATIN CAPITAL LETTER S WITH ACUTE = LATIN CAPITAL LETTER S + COMBINING ACUTE ACCENT
0073+0301=015B# LATIN SMALL LETTER S WITH ACUTE = LATIN SMALL LETTER S + COMBINING ACUTE ACCENT
0053+0302=015C# LATIN CAPITAL LETTER S WITH CIRCUMFLEX = LATIN CAPITAL LETTER S + COMBINING CIRCUMFLEX ACCENT
0073+0302=015D# LATIN SMALL LETTER S WITH CIRCUMFLEX = LATIN SMALL LETTER S + COMBINING CIRCUMFLEX ACCENT
0053+0327=015E# LATIN CAPITAL LETTER S WITH CEDILLA = LATIN CAPITAL LETTER S + COMBINING CEDILLA
0073+0327=015F# LATIN SMALL LETTER S WITH CEDILLA = LATIN SMALL LETTER S + COMBINING CEDILLA
0053+030C=0160# LATIN CAPITAL LETTER S WITH CARON = LATIN CAPITAL LETTER S + COMBINING CARON
0073+030C=0161# LATIN SMALL LETTER S WITH CARON = LATIN SMALL LETTER S + COMBINING CARON
0054+0327=0162# LATIN CAPITAL LETTER T WITH CEDILLA = LATIN CAPITAL LETTER T + COMBINING CEDILLA
0074+0327=0163# LATIN SMALL LETTER T WITH CEDILLA = LATIN SMALL LETTER T + COMBINING CEDILLA
0054+030C=0164# LATIN CAPITAL LETTER T WITH CARON = LATIN CAPITAL LETTER T + COMBINING CARON
0074+030C=0165# LATIN SMALL LETTER T WITH CARON = LATIN SMALL LETTER T + COMBINING CARON
0055+0303=0168# LATIN CAPITAL LETTER U WITH TILDE = LATIN CAPITAL LETTER U + COMBINING TILDE
0075+0303=0169# LATIN SMALL LETTER U WITH TILDE = LATIN SMALL LETTER U + COMBINING TILDE
0055+0304=016A# LATIN CAPITAL LETTER U WITH MACRON = LATIN CAPITAL LETTER U + COMBINING MACRON
0075+0304=016B# LATIN SMALL LETTER U WITH MACRON = LATIN SMALL LETTER U + COMBINING MACRON
0055+0306=016C# LATIN CAPITAL LETTER U WITH BREVE = LATIN CAPITAL LETTER U + COMBINING BREVE
0075+0306=016D# LATIN SMALL LETTER U WITH BREVE = LATIN SMALL LETTER U + COMBINING BREVE
0055+030A=016E# LATIN CAPITAL LETTER U WITH RING ABOVE = LATIN CAPITAL LETTER U + COMBINING RING ABOVE
0075+030A=016F# LATIN SMALL LETTER U WITH RING ABOVE = LATIN SMALL LETTER U + COMBINING RING ABOVE
0055+030B=0170# LATIN CAPITAL LETTER U WITH DOUBLE ACUTE = LATIN CAPITAL LETTER U + COMBINING DOUBLE ACUTE ACCENT
0075+030B=0171# LATIN SMALL LETTER U WITH DOUBLE ACUTE = LATIN SMALL LETTER U + COMBINING DOUBLE ACUTE ACCENT
0055+0328=0172# LATIN CAPITAL LETTER U WITH OGONEK = LATIN CAPITAL LETTER U + COMBINING OGONEK
0075+0328=0173# LATIN SMALL LETTER U WITH OGONEK = LATIN SMALL LETTER U + COMBINING OGONEK
0057+0302=0174# LATIN CAPITAL LETTER W WITH CIRCUMFLEX = LATIN CAPITAL LETTER W + COMBINING CIRCUMFLEX ACCENT
0077+0302=0175# LATIN SMALL LETTER W WITH CIRCUMFLEX = LATIN SMALL LETTER W + COMBINING CIRCUMFLEX ACCENT
0059+0302=0176# LATIN CAPITAL LETTER Y WITH CIRCUMFLEX = LATIN CAPITAL LETTER Y + COMBINING CIRCUMFLEX ACCENT
0079+0302=0177# LATIN SMALL LETTER Y WITH CIRCUMFLEX = LATIN SMALL LETTER Y + COMBINING CIRCUMFLEX ACCENT
0059+0308=0178# LATIN CAPITAL LETTER Y WITH DIAERESIS = LATIN CAPITAL LETTER Y + COMBINING DIAERESIS
005A+0301=0179# LATIN CAPITAL LETTER Z WITH ACUTE = LATIN CAPITAL LETTER Z + COMBINING ACUTE ACCENT
007A+0301=017A# LATIN SMALL LETTER Z WITH ACUTE = LATIN SMALL LETTER Z + COMBINING ACUTE ACCENT
005A+0307=017B# LATIN CAPITAL LETTER Z WITH DOT ABOVE = LATIN CAPITAL LETTER Z + COMBINING DOT ABOVE
007A+0307=017C# LATIN SMALL LETTER Z WITH DOT ABOVE = LATIN SMALL LETTER Z + COMBINING DOT ABOVE
005A+030C=017D# LATIN CAPITAL LETTER Z WITH CARON = LATIN CAPITAL LETTER Z + COMBINING CARON
007A+030C=017E# LATIN SMALL LETTER Z WITH CARON = LATIN SMALL LETTER Z + COMBINING CARON
004F+031B=01A0# LATIN CAPITAL LETTER O WITH HORN = LATIN CAPITAL LETTER O + COMBINING HORN
006F+031B=01A1# LATIN SMALL LETTER O WITH HORN = LATIN SMALL LETTER O + COMBINING HORN
0055+031B=01AF# LATIN CAPITAL LETTER U WITH HORN = LATIN CAPITAL LETTER U + COMBINING HORN
0075+031B=01B0# LATIN SMALL LETTER U WITH HORN = LATIN SMALL LETTER U + COMBINING HORN
01F1+030C=01C4# LATIN CAPITAL LETTER DZ WITH CARON = LATIN CAPITAL LETTER DZ + COMBINING CARON
01F3+030C=01C6# LATIN SMALL LETTER DZ WITH CARON = LATIN SMALL LETTER DZ + COMBINING CARON
0041+030C=01CD# LATIN CAPITAL LETTER A WITH CARON = LATIN CAPITAL LETTER A + COMBINING CARON
0061+030C=01CE# LATIN SMALL LETTER A WITH CARON = LATIN SMALL LETTER A + COMBINING CARON
0049+030C=01CF# LATIN CAPITAL LETTER I WITH CARON = LATIN CAPITAL LETTER I + COMBINING CARON
0069+030C=01D0# LATIN SMALL LETTER I WITH CARON = LATIN SMALL LETTER I + COMBINING CARON
004F+030C=01D1# LATIN CAPITAL LETTER O WITH CARON = LATIN CAPITAL LETTER O + COMBINING CARON
006F+030C=01D2# LATIN SMALL LETTER O WITH CARON = LATIN SMALL LETTER O + COMBINING CARON
0055+030C=01D3# LATIN CAPITAL LETTER U WITH CARON = LATIN CAPITAL LETTER U + COMBINING CARON
0075+030C=01D4# LATIN SMALL LETTER U WITH CARON = LATIN SMALL LETTER U + COMBINING CARON
0055+0308+0304=01D5# LATIN CAPITAL LETTER U WITH DIAERESIS AND MACRON = LATIN CAPITAL LETTER U + COMBINING DIAERESIS + COMBINING MACRON
0075+0308+0304=01D6# LATIN SMALL LETTER U WITH DIAERESIS AND MACRON = LATIN SMALL LETTER U + COMBINING DIAERESIS + COMBINING MACRON
0055+0308+0301=01D7# LATIN CAPITAL LETTER U WITH DIAERESIS AND ACUTE = LATIN CAPITAL LETTER U + COMBINING DIAERESIS + COMBINING ACUTE ACCENT
0075+0308+0301=01D8# LATIN SMALL LETTER U WITH DIAERESIS AND ACUTE = LATIN SMALL LETTER U + COMBINING DIAERESIS + COMBINING ACUTE ACCENT
0055+0308+030C=01D9# LATIN CAPITAL LETTER U WITH DIAERESIS AND CARON = LATIN CAPITAL LETTER U + COMBINING DIAERESIS + COMBINING CARON
0075+0308+030C=01DA# LATIN SMALL LETTER U WITH DIAERESIS AND CARON = LATIN SMALL LETTER U + COMBINING DIAERESIS + COMBINING CARON
0055+0308+0300=01DB# LATIN CAPITAL LETTER U WITH DIAERESIS AND GRAVE = LATIN CAPITAL LETTER U + COMBINING DIAERESIS + COMBINING GRAVE ACCENT
0075+0308+0300=01DC# LATIN SMALL LETTER U WITH DIAERESIS AND GRAVE = LATIN SMALL LETTER U + COMBINING DIAERESIS + COMBINING GRAVE ACCENT
0041+0308+0304=01DE# LATIN CAPITAL LETTER A WITH DIAERESIS AND MACRON = LATIN CAPITAL LETTER A + COMBINING DIAERESIS + COMBINING MACRON
0061+0308+0304=01DF# LATIN SMALL LETTER A WITH DIAERESIS AND MACRON = LATIN SMALL LETTER A + COMBINING DIAERESIS + COMBINING MACRON
0041+0307+0304=01E0# LATIN CAPITAL LETTER A WITH DOT ABOVE AND MACRON = LATIN CAPITAL LETTER A + COMBINING DOT ABOVE + COMBINING MACRON
0061+0307+0304=01E1# LATIN SMALL LETTER A WITH DOT ABOVE AND MACRON = LATIN SMALL LETTER A + COMBINING DOT ABOVE + COMBINING MACRON
00C6+0304=01E2# LATIN CAPITAL LETTER AE WITH MACRON = LATIN CAPITAL LETTER AE + COMBINING MACRON
00E6+0304=01E3# LATIN SMALL LETTER AE WITH MACRON = LATIN SMALL LETTER AE + COMBINING MACRON
0047+030C=01E6# LATIN CAPITAL LETTER G WITH CARON = LATIN CAPITAL LETTER G + COMBINING CARON
0067+030C=01E7# LATIN SMALL LETTER G WITH CARON = LATIN SMALL LETTER G + COMBINING CARON
004B+030C=01E8# LATIN CAPITAL LETTER K WITH CARON = LATIN CAPITAL LETTER K + COMBINING CARON
006B+030C=01E9# LATIN SMALL LETTER K WITH CARON = LATIN SMALL LETTER K + COMBINING CARON
004F+0328=01EA# LATIN CAPITAL LETTER O WITH OGONEK = LATIN CAPITAL LETTER O + COMBINING OGONEK
006F+0328=01EB# LATIN SMALL LETTER O WITH OGONEK = LATIN SMALL LETTER O + COMBINING OGONEK
004F+0328+0304=01EC# LATIN CAPITAL LETTER O WITH OGONEK AND MACRON = LATIN CAPITAL LETTER O + COMBINING OGONEK + COMBINING MACRON
006F+0328+0304=01ED# LATIN SMALL LETTER O WITH OGONEK AND MACRON = LATIN SMALL LETTER O + COMBINING OGONEK + COMBINING MACRON
01B7+030C=01EE# LATIN CAPITAL LETTER EZH WITH CARON = LATIN CAPITAL LETTER EZH + COMBINING CARON
0292+030C=01EF# LATIN SMALL LETTER EZH WITH CARON = LATIN SMALL LETTER EZH + COMBINING CARON
006A+030C=01F0# LATIN SMALL LETTER J WITH CARON = LATIN SMALL LETTER J + COMBINING CARON
0047+0301=01F4# LATIN CAPITAL LETTER G WITH ACUTE = LATIN CAPITAL LETTER G + COMBINING ACUTE ACCENT
0067+0301=01F5# LATIN SMALL LETTER G WITH ACUTE = LATIN SMALL LETTER G + COMBINING ACUTE ACCENT
0041+030A+0301=01FA# LATIN CAPITAL LETTER A WITH RING ABOVE AND ACUTE = LATIN CAPITAL LETTER A + COMBINING RING ABOVE + COMBINING ACUTE ACCENT
0061+030A+0301=01FB# LATIN SMALL LETTER A WITH RING ABOVE AND ACUTE = LATIN SMALL LETTER A + COMBINING RING ABOVE + COMBINING ACUTE ACCENT
00C6+0301=01FC# LATIN CAPITAL LETTER AE WITH ACUTE = LATIN CAPITAL LETTER AE + COMBINING ACUTE ACCENT
00E6+0301=01FD# LATIN SMALL LETTER AE WITH ACUTE = LATIN SMALL LETTER AE + COMBINING ACUTE ACCENT
0041+030F=0200# LATIN CAPITAL LETTER A WITH DOUBLE GRAVE = LATIN CAPITAL LETTER A + COMBINING DOUBLE GRAVE ACCENT
0061+030F=0201# LATIN SMALL LETTER A WITH DOUBLE GRAVE = LATIN SMALL LETTER A + COMBINING DOUBLE GRAVE ACCENT
0041+0311=0202# LATIN CAPITAL LETTER A WITH INVERTED BREVE = LATIN CAPITAL LETTER A + COMBINING INVERTED BREVE
0061+0311=0203# LATIN SMALL LETTER A WITH INVERTED BREVE = LATIN SMALL LETTER A + COMBINING INVERTED BREVE
0045+030F=0204# LATIN CAPITAL LETTER E WITH DOUBLE GRAVE = LATIN CAPITAL LETTER E + COMBINING DOUBLE GRAVE ACCENT
0065+030F=0205# LATIN SMALL LETTER E WITH DOUBLE GRAVE = LATIN SMALL LETTER E + COMBINING DOUBLE GRAVE ACCENT
0045+0311=0206# LATIN CAPITAL LETTER E WITH INVERTED BREVE = LATIN CAPITAL LETTER E + COMBINING INVERTED BREVE
0065+0311=0207# LATIN SMALL LETTER E WITH INVERTED BREVE = LATIN SMALL LETTER E + COMBINING INVERTED BREVE
0049+030F=0208# LATIN CAPITAL LETTER I WITH DOUBLE GRAVE = LATIN CAPITAL LETTER I + COMBINING DOUBLE GRAVE ACCENT
0069+030F=0209# LATIN SMALL LETTER I WITH DOUBLE GRAVE = LATIN SMALL LETTER I + COMBINING DOUBLE GRAVE ACCENT
0049+0311=020A# LATIN CAPITAL LETTER I WITH INVERTED BREVE = LATIN CAPITAL LETTER I + COMBINING INVERTED BREVE
0069+0311=020B# LATIN SMALL LETTER I WITH INVERTED BREVE = LATIN SMALL LETTER I + COMBINING INVERTED BREVE
004F+030F=020C# LATIN CAPITAL LETTER O WITH DOUBLE GRAVE = LATIN CAPITAL LETTER O + COMBINING DOUBLE GRAVE ACCENT
006F+030F=020D# LATIN SMALL LETTER O WITH DOUBLE GRAVE = LATIN SMALL LETTER O + COMBINING DOUBLE GRAVE ACCENT
004F+0311=020E# LATIN CAPITAL LETTER O WITH INVERTED BREVE = LATIN CAPITAL LETTER O + COMBINING INVERTED BREVE
006F+0311=020F# LATIN SMALL LETTER O WITH INVERTED BREVE = LATIN SMALL LETTER O + COMBINING INVERTED BREVE
0052+030F=0210# LATIN CAPITAL LETTER R WITH DOUBLE GRAVE = LATIN CAPITAL LETTER R + COMBINING DOUBLE GRAVE ACCENT
0072+030F=0211# LATIN SMALL LETTER R WITH DOUBLE GRAVE = LATIN SMALL LETTER R + COMBINING DOUBLE GRAVE ACCENT
0052+0311=0212# LATIN CAPITAL LETTER R WITH INVERTED BREVE = LATIN CAPITAL LETTER R + COMBINING INVERTED BREVE
0072+0311=0213# LATIN SMALL LETTER R WITH INVERTED BREVE = LATIN SMALL LETTER R + COMBINING INVERTED BREVE
0055+030F=0214# LATIN CAPITAL LETTER U WITH DOUBLE GRAVE = LATIN CAPITAL LETTER U + COMBINING DOUBLE GRAVE ACCENT
0075+030F=0215# LATIN SMALL LETTER U WITH DOUBLE GRAVE = LATIN SMALL LETTER U + COMBINING DOUBLE GRAVE ACCENT
0055+0311=0216# LATIN CAPITAL LETTER U WITH INVERTED BREVE = LATIN CAPITAL LETTER U + COMBINING INVERTED BREVE
0075+0311=0217# LATIN SMALL LETTER U WITH INVERTED BREVE = LATIN SMALL LETTER U + COMBINING INVERTED BREVE
0041+0325=1E00# LATIN CAPITAL LETTER A WITH RING BELOW = LATIN CAPITAL LETTER A + COMBINING RING BELOW
0061+0325=1E01# LATIN SMALL LETTER A WITH RING BELOW = LATIN SMALL LETTER A + COMBINING RING BELOW
0042+0307=1E02# LATIN CAPITAL LETTER B WITH DOT ABOVE = LATIN CAPITAL LETTER B + COMBINING DOT ABOVE
0062+0307=1E03# LATIN SMALL LETTER B WITH DOT ABOVE = LATIN SMALL LETTER B + COMBINING DOT ABOVE
0042+0323=1E04# LATIN CAPITAL LETTER B WITH DOT BELOW = LATIN CAPITAL LETTER B + COMBINING DOT BELOW
0062+0323=1E05# LATIN SMALL LETTER B WITH DOT BELOW = LATIN SMALL LETTER B + COMBINING DOT BELOW
0042+0332=1E06# LATIN CAPITAL LETTER B WITH LINE BELOW = LATIN CAPITAL LETTER B + COMBINING LOW LINE
0062+0332=1E07# LATIN SMALL LETTER B WITH LINE BELOW = LATIN SMALL LETTER B + COMBINING LOW LINE
0043+0327+0301=1E08# LATIN CAPITAL LETTER C WITH CEDILLA AND ACUTE = LATIN CAPITAL LETTER C + COMBINING CEDILLA + COMBINING ACUTE ACCENT
0063+0327+0301=1E09# LATIN SMALL LETTER C WITH CEDILLA AND ACUTE = LATIN SMALL LETTER C + COMBINING CEDILLA + COMBINING ACUTE ACCENT
0044+0307=1E0A# LATIN CAPITAL LETTER D WITH DOT ABOVE = LATIN CAPITAL LETTER D + COMBINING DOT ABOVE
0064+0307=1E0B# LATIN SMALL LETTER D WITH DOT ABOVE = LATIN SMALL LETTER D + COMBINING DOT ABOVE
0044+0323=1E0C# LATIN CAPITAL LETTER D WITH DOT BELOW = LATIN CAPITAL LETTER D + COMBINING DOT BELOW
0064+0323=1E0D# LATIN SMALL LETTER D WITH DOT BELOW = LATIN SMALL LETTER D + COMBINING DOT BELOW
0044+0332=1E0E# LATIN CAPITAL LETTER D WITH LINE BELOW = LATIN CAPITAL LETTER D + COMBINING LOW LINE
0064+0332=1E0F# LATIN SMALL LETTER D WITH LINE BELOW = LATIN SMALL LETTER D + COMBINING LOW LINE
0044+0327=1E10# LATIN CAPITAL LETTER D WITH CEDILLA = LATIN CAPITAL LETTER D + COMBINING CEDILLA
0064+0327=1E11# LATIN SMALL LETTER D WITH CEDILLA = LATIN SMALL LETTER D + COMBINING CEDILLA
0044+032D=1E12# LATIN CAPITAL LETTER D WITH CIRCUMFLEX BELOW = LATIN CAPITAL LETTER D + COMBINING CIRCUMFLEX ACCENT BELOW
0064+032D=1E13# LATIN SMALL LETTER D WITH CIRCUMFLEX BELOW = LATIN SMALL LETTER D + COMBINING CIRCUMFLEX ACCENT BELOW
0045+0304+0300=1E14# LATIN CAPITAL LETTER E WITH MACRON AND GRAVE = LATIN CAPITAL LETTER E + COMBINING MACRON + COMBINING GRAVE ACCENT
0065+0304+0300=1E15# LATIN SMALL LETTER E WITH MACRON AND GRAVE = LATIN SMALL LETTER E + COMBINING MACRON + COMBINING GRAVE ACCENT
0045+0304+0301=1E16# LATIN CAPITAL LETTER E WITH MACRON AND ACUTE = LATIN CAPITAL LETTER E + COMBINING MACRON + COMBINING ACUTE ACCENT
0065+0304+0301=1E17# LATIN SMALL LETTER E WITH MACRON AND ACUTE = LATIN SMALL LETTER E + COMBINING MACRON + COMBINING ACUTE ACCENT
0045+032D=1E18# LATIN CAPITAL LETTER E WITH CIRCUMFLEX BELOW = LATIN CAPITAL LETTER E + COMBINING CIRCUMFLEX ACCENT BELOW
0065+032D=1E19# LATIN SMALL LETTER E WITH CIRCUMFLEX BELOW = LATIN SMALL LETTER E + COMBINING CIRCUMFLEX ACCENT BELOW
0045+0330=1E1A# LATIN CAPITAL LETTER E WITH TILDE BELOW = LATIN CAPITAL LETTER E + COMBINING TILDE BELOW
0065+0330=1E1B# LATIN SMALL LETTER E WITH TILDE BELOW = LATIN SMALL LETTER E + COMBINING TILDE BELOW
0045+0327+0306=1E1C# LATIN CAPITAL LETTER E WITH CEDILLA AND BREVE = LATIN CAPITAL LETTER E + COMBINING CEDILLA + COMBINING BREVE
0065+0327+0306=1E1D# LATIN SMALL LETTER E WITH CEDILLA AND BREVE = LATIN SMALL LETTER E + COMBINING CEDILLA + COMBINING BREVE
0046+0307=1E1E# LATIN CAPITAL LETTER F WITH DOT ABOVE = LATIN CAPITAL LETTER F + COMBINING DOT ABOVE
0066+0307=1E1F# LATIN SMALL LETTER F WITH DOT ABOVE = LATIN SMALL LETTER F + COMBINING DOT ABOVE
0047+0304=1E20# LATIN CAPITAL LETTER G WITH MACRON = LATIN CAPITAL LETTER G + COMBINING MACRON
0067+0304=1E21# LATIN SMALL LETTER G WITH MACRON = LATIN SMALL LETTER G + COMBINING MACRON
0048+0307=1E22# LATIN CAPITAL LETTER H WITH DOT ABOVE = LATIN CAPITAL LETTER H + COMBINING DOT ABOVE
0068+0307=1E23# LATIN SMALL LETTER H WITH DOT ABOVE = LATIN SMALL LETTER H + COMBINING DOT ABOVE
0048+0323=1E24# LATIN CAPITAL LETTER H WITH DOT BELOW = LATIN CAPITAL LETTER H + COMBINING DOT BELOW
0068+0323=1E25# LATIN SMALL LETTER H WITH DOT BELOW = LATIN SMALL LETTER H + COMBINING DOT BELOW
0048+0308=1E26# LATIN CAPITAL LETTER H WITH DIAERESIS = LATIN CAPITAL LETTER H + COMBINING DIAERESIS
0068+0308=1E27# LATIN SMALL LETTER H WITH DIAERESIS = LATIN SMALL LETTER H + COMBINING DIAERESIS
0048+0327=1E28# LATIN CAPITAL LETTER H WITH CEDILLA = LATIN CAPITAL LETTER H + COMBINING CEDILLA
0068+0327=1E29# LATIN SMALL LETTER H WITH CEDILLA = LATIN SMALL LETTER H + COMBINING CEDILLA
0048+032E=1E2A# LATIN CAPITAL LETTER H WITH BREVE BELOW = LATIN CAPITAL LETTER H + COMBINING BREVE BELOW
0068+032E=1E2B# LATIN SMALL LETTER H WITH BREVE BELOW = LATIN SMALL LETTER H + COMBINING BREVE BELOW
0049+0330=1E2C# LATIN CAPITAL LETTER I WITH TILDE BELOW = LATIN CAPITAL LETTER I + COMBINING TILDE BELOW
0069+0330=1E2D# LATIN SMALL LETTER I WITH TILDE BELOW = LATIN SMALL LETTER I + COMBINING TILDE BELOW
0049+0308+0301=1E2E# LATIN CAPITAL LETTER I WITH DIAERESIS AND ACUTE = LATIN CAPITAL LETTER I + COMBINING DIAERESIS + COMBINING ACUTE ACCENT
0069+0308+0301=1E2F# LATIN SMALL LETTER I WITH DIAERESIS AND ACUTE = LATIN SMALL LETTER I + COMBINING DIAERESIS + COMBINING ACUTE ACCENT
004B+0301=1E30# LATIN CAPITAL LETTER K WITH ACUTE = LATIN CAPITAL LETTER K + COMBINING ACUTE ACCENT
006B+0301=1E31# LATIN SMALL LETTER K WITH ACUTE = LATIN SMALL LETTER K + COMBINING ACUTE ACCENT
004B+0323=1E32# LATIN CAPITAL LETTER K WITH DOT BELOW = LATIN CAPITAL LETTER K + COMBINING DOT BELOW
006B+0323=1E33# LATIN SMALL LETTER K WITH DOT BELOW = LATIN SMALL LETTER K + COMBINING DOT BELOW
004B+0332=1E34# LATIN CAPITAL LETTER K WITH LINE BELOW = LATIN CAPITAL LETTER K + COMBINING LOW LINE
006B+0332=1E35# LATIN SMALL LETTER K WITH LINE BELOW = LATIN SMALL LETTER K + COMBINING LOW LINE
004C+0323=1E36# LATIN CAPITAL LETTER L WITH DOT BELOW = LATIN CAPITAL LETTER L + COMBINING DOT BELOW
006C+0323=1E37# LATIN SMALL LETTER L WITH DOT BELOW = LATIN SMALL LETTER L + COMBINING DOT BELOW
004C+0323+0304=1E38# LATIN CAPITAL LETTER L WITH DOT BELOW AND MACRON = LATIN CAPITAL LETTER L + COMBINING DOT BELOW + COMBINING MACRON
006C+0323+0304=1E39# LATIN SMALL LETTER L WITH DOT BELOW AND MACRON = LATIN SMALL LETTER L + COMBINING DOT BELOW + COMBINING MACRON
004C+0332=1E3A# LATIN CAPITAL LETTER L WITH LINE BELOW = LATIN CAPITAL LETTER L + COMBINING LOW LINE
006C+0332=1E3B# LATIN SMALL LETTER L WITH LINE BELOW = LATIN SMALL LETTER L + COMBINING LOW LINE
004C+032D=1E3C# LATIN CAPITAL LETTER L WITH CIRCUMFLEX BELOW = LATIN CAPITAL LETTER L + COMBINING CIRCUMFLEX ACCENT BELOW
006C+032D=1E3D# LATIN SMALL LETTER L WITH CIRCUMFLEX BELOW = LATIN SMALL LETTER L + COMBINING CIRCUMFLEX ACCENT BELOW
004D+0301=1E3E# LATIN CAPITAL LETTER M WITH ACUTE = LATIN CAPITAL LETTER M + COMBINING ACUTE ACCENT
006D+0301=1E3F# LATIN SMALL LETTER M WITH ACUTE = LATIN SMALL LETTER M + COMBINING ACUTE ACCENT
004D+0307=1E40# LATIN CAPITAL LETTER M WITH DOT ABOVE = LATIN CAPITAL LETTER M + COMBINING DOT ABOVE
006D+0307=1E41# LATIN SMALL LETTER M WITH DOT ABOVE = LATIN SMALL LETTER M + COMBINING DOT ABOVE
004D+0323=1E42# LATIN CAPITAL LETTER M WITH DOT BELOW = LATIN CAPITAL LETTER M + COMBINING DOT BELOW
006D+0323=1E43# LATIN SMALL LETTER M WITH DOT BELOW = LATIN SMALL LETTER M + COMBINING DOT BELOW
004E+0307=1E44# LATIN CAPITAL LETTER N WITH DOT ABOVE = LATIN CAPITAL LETTER N + COMBINING DOT ABOVE
006E+0307=1E45# LATIN SMALL LETTER N WITH DOT ABOVE = LATIN SMALL LETTER N + COMBINING DOT ABOVE
004E+0323=1E46# LATIN CAPITAL LETTER N WITH DOT BELOW = LATIN CAPITAL LETTER N + COMBINING DOT BELOW
006E+0323=1E47# LATIN SMALL LETTER N WITH DOT BELOW = LATIN SMALL LETTER N + COMBINING DOT BELOW
004E+0332=1E48# LATIN CAPITAL LETTER N WITH LINE BELOW = LATIN CAPITAL LETTER N + COMBINING LOW LINE
006E+0332=1E49# LATIN SMALL LETTER N WITH LINE BELOW = LATIN SMALL LETTER N + COMBINING LOW LINE
004E+032D=1E4A# LATIN CAPITAL LETTER N WITH CIRCUMFLEX BELOW = LATIN CAPITAL LETTER N + COMBINING CIRCUMFLEX ACCENT BELOW
006E+032D=1E4B# LATIN SMALL LETTER N WITH CIRCUMFLEX BELOW = LATIN SMALL LETTER N + COMBINING CIRCUMFLEX ACCENT BELOW
004F+0303+0301=1E4C# LATIN CAPITAL LETTER O WITH TILDE AND ACUTE = LATIN CAPITAL LETTER O + COMBINING TILDE + COMBINING ACUTE ACCENT
006F+0303+0301=1E4D# LATIN SMALL LETTER O WITH TILDE AND ACUTE = LATIN SMALL LETTER O + COMBINING TILDE + COMBINING ACUTE ACCENT
004F+0303+0308=1E4E# LATIN CAPITAL LETTER O WITH TILDE AND DIAERESIS = LATIN CAPITAL LETTER O + COMBINING TILDE + COMBINING DIAERESIS
006F+0303+0308=1E4F# LATIN SMALL LETTER O WITH TILDE AND DIAERESIS = LATIN SMALL LETTER O + COMBINING TILDE + COMBINING DIAERESIS
004F+0304+0300=1E50# LATIN CAPITAL LETTER O WITH MACRON AND GRAVE = LATIN CAPITAL LETTER O + COMBINING MACRON + COMBINING GRAVE ACCENT
006F+0304+0300=1E51# LATIN SMALL LETTER O WITH MACRON AND GRAVE = LATIN SMALL LETTER O + COMBINING MACRON + COMBINING GRAVE ACCENT
004F+0304+0301=1E52# LATIN CAPITAL LETTER O WITH MACRON AND ACUTE = LATIN CAPITAL LETTER O + COMBINING MACRON + COMBINING ACUTE ACCENT
006F+0304+0301=1E53# LATIN SMALL LETTER O WITH MACRON AND ACUTE = LATIN SMALL LETTER O + COMBINING MACRON + COMBINING ACUTE ACCENT
0050+0301=1E54# LATIN CAPITAL LETTER P WITH ACUTE = LATIN CAPITAL LETTER P + COMBINING ACUTE ACCENT
0070+0301=1E55# LATIN SMALL LETTER P WITH ACUTE = LATIN SMALL LETTER P + COMBINING ACUTE ACCENT
0050+0307=1E56# LATIN CAPITAL LETTER P WITH DOT ABOVE = LATIN CAPITAL LETTER P + COMBINING DOT ABOVE
0070+0307=1E57# LATIN SMALL LETTER P WITH DOT ABOVE = LATIN SMALL LETTER P + COMBINING DOT ABOVE
0052+0307=1E58# LATIN CAPITAL LETTER R WITH DOT ABOVE = LATIN CAPITAL LETTER R + COMBINING DOT ABOVE
0072+0307=1E59# LATIN SMALL LETTER R WITH DOT ABOVE = LATIN SMALL LETTER R + COMBINING DOT ABOVE
0052+0323=1E5A# LATIN CAPITAL LETTER R WITH DOT BELOW = LATIN CAPITAL LETTER R + COMBINING DOT BELOW
0072+0323=1E5B# LATIN SMALL LETTER R WITH DOT BELOW = LATIN SMALL LETTER R + COMBINING DOT BELOW
0052+0323+0304=1E5C# LATIN CAPITAL LETTER R WITH DOT BELOW AND MACRON = LATIN CAPITAL LETTER R + COMBINING DOT BELOW + COMBINING MACRON
0072+0323+0304=1E5D# LATIN SMALL LETTER R WITH DOT BELOW AND MACRON = LATIN SMALL LETTER R + COMBINING DOT BELOW + COMBINING MACRON
0052+0332=1E5E# LATIN CAPITAL LETTER R WITH LINE BELOW = LATIN CAPITAL LETTER R + COMBINING LOW LINE
0072+0332=1E5F# LATIN SMALL LETTER R WITH LINE BELOW = LATIN SMALL LETTER R + COMBINING LOW LINE
0053+0307=1E60# LATIN CAPITAL LETTER S WITH DOT ABOVE = LATIN CAPITAL LETTER S + COMBINING DOT ABOVE
0073+0307=1E61# LATIN SMALL LETTER S WITH DOT ABOVE = LATIN SMALL LETTER S + COMBINING DOT ABOVE
0053+0323=1E62# LATIN CAPITAL LETTER S WITH DOT BELOW = LATIN CAPITAL LETTER S + COMBINING DOT BELOW
0073+0323=1E63# LATIN SMALL LETTER S WITH DOT BELOW = LATIN SMALL LETTER S + COMBINING DOT BELOW
0053+0301+0307=1E64# LATIN CAPITAL LETTER S WITH ACUTE AND DOT ABOVE = LATIN CAPITAL LETTER S + COMBINING ACUTE ACCENT + COMBINING DOT ABOVE
0073+0301+0307=1E65# LATIN SMALL LETTER S WITH ACUTE AND DOT ABOVE = LATIN SMALL LETTER S + COMBINING ACUTE ACCENT + COMBINING DOT ABOVE
0053+030C+0307=1E66# LATIN CAPITAL LETTER S WITH CARON AND DOT ABOVE = LATIN CAPITAL LETTER S + COMBINING CARON + COMBINING DOT ABOVE
0073+030C+0307=1E67# LATIN SMALL LETTER S WITH CARON AND DOT ABOVE = LATIN SMALL LETTER S + COMBINING CARON + COMBINING DOT ABOVE
0053+0323+0307=1E68# LATIN CAPITAL LETTER S WITH DOT BELOW AND DOT ABOVE = LATIN CAPITAL LETTER S + COMBINING DOT BELOW + COMBINING DOT ABOVE
0073+0323+0307=1E69# LATIN SMALL LETTER S WITH DOT BELOW AND DOT ABOVE = LATIN SMALL LETTER S + COMBINING DOT BELOW + COMBINING DOT ABOVE
0054+0307=1E6A# LATIN CAPITAL LETTER T WITH DOT ABOVE = LATIN CAPITAL LETTER T + COMBINING DOT ABOVE
0074+0307=1E6B# LATIN SMALL LETTER T WITH DOT ABOVE = LATIN SMALL LETTER T + COMBINING DOT ABOVE
0054+0323=1E6C# LATIN CAPITAL LETTER T WITH DOT BELOW = LATIN CAPITAL LETTER T + COMBINING DOT BELOW
0074+0323=1E6D# LATIN SMALL LETTER T WITH DOT BELOW = LATIN SMALL LETTER T + COMBINING DOT BELOW
0054+0332=1E6E# LATIN CAPITAL LETTER T WITH LINE BELOW = LATIN CAPITAL LETTER T + COMBINING LOW LINE
0074+0332=1E6F# LATIN SMALL LETTER T WITH LINE BELOW = LATIN SMALL LETTER T + COMBINING LOW LINE
0054+032D=1E70# LATIN CAPITAL LETTER T WITH CIRCUMFLEX BELOW = LATIN CAPITAL LETTER T + COMBINING CIRCUMFLEX ACCENT BELOW
0074+032D=1E71# LATIN SMALL LETTER T WITH CIRCUMFLEX BELOW = LATIN SMALL LETTER T + COMBINING CIRCUMFLEX ACCENT BELOW
0055+0324=1E72# LATIN CAPITAL LETTER U WITH DIAERESIS BELOW = LATIN CAPITAL LETTER U + COMBINING DIAERESIS BELOW
0075+0324=1E73# LATIN SMALL LETTER U WITH DIAERESIS BELOW = LATIN SMALL LETTER U + COMBINING DIAERESIS BELOW
0055+0330=1E74# LATIN CAPITAL LETTER U WITH TILDE BELOW = LATIN CAPITAL LETTER U + COMBINING TILDE BELOW
0075+0330=1E75# LATIN SMALL LETTER U WITH TILDE BELOW = LATIN SMALL LETTER U + COMBINING TILDE BELOW
0055+032D=1E76# LATIN CAPITAL LETTER U WITH CIRCUMFLEX BELOW = LATIN CAPITAL LETTER U + COMBINING CIRCUMFLEX ACCENT BELOW
0075+032D=1E77# LATIN SMALL LETTER U WITH CIRCUMFLEX BELOW = LATIN SMALL LETTER U + COMBINING CIRCUMFLEX ACCENT BELOW
0055+0303+0301=1E78# LATIN CAPITAL LETTER U WITH TILDE AND ACUTE = LATIN CAPITAL LETTER U + COMBINING TILDE + COMBINING ACUTE ACCENT
0075+0303+0301=1E79# LATIN SMALL LETTER U WITH TILDE AND ACUTE = LATIN SMALL LETTER U + COMBINING TILDE + COMBINING ACUTE ACCENT
0055+0304+0308=1E7A# LATIN CAPITAL LETTER U WITH MACRON AND DIAERESIS = LATIN CAPITAL LETTER U + COMBINING MACRON + COMBINING DIAERESIS
0075+0304+0308=1E7B# LATIN SMALL LETTER U WITH MACRON AND DIAERESIS = LATIN SMALL LETTER U + COMBINING MACRON + COMBINING DIAERESIS
0056+0303=1E7C# LATIN CAPITAL LETTER V WITH TILDE = LATIN CAPITAL LETTER V + COMBINING TILDE
0076+0303=1E7D# LATIN SMALL LETTER V WITH TILDE = LATIN SMALL LETTER V + COMBINING TILDE
0056+0323=1E7E# LATIN CAPITAL LETTER V WITH DOT BELOW = LATIN CAPITAL LETTER V + COMBINING DOT BELOW
0076+0323=1E7F# LATIN SMALL LETTER V WITH DOT BELOW = LATIN SMALL LETTER V + COMBINING DOT BELOW
0057+0300=1E80# LATIN CAPITAL LETTER W WITH GRAVE = LATIN CAPITAL LETTER W + COMBINING GRAVE ACCENT
0077+0300=1E81# LATIN SMALL LETTER W WITH GRAVE = LATIN SMALL LETTER W + COMBINING GRAVE ACCENT
0057+0301=1E82# LATIN CAPITAL LETTER W WITH ACUTE = LATIN CAPITAL LETTER W + COMBINING ACUTE ACCENT
0077+0301=1E83# LATIN SMALL LETTER W WITH ACUTE = LATIN SMALL LETTER W + COMBINING ACUTE ACCENT
0057+0308=1E84# LATIN CAPITAL LETTER W WITH DIAERESIS = LATIN CAPITAL LETTER W + COMBINING DIAERESIS
0077+0308=1E85# LATIN SMALL LETTER W WITH DIAERESIS = LATIN SMALL LETTER W + COMBINING DIAERESIS
0057+0307=1E86# LATIN CAPITAL LETTER W WITH DOT ABOVE = LATIN CAPITAL LETTER W + COMBINING DOT ABOVE
0077+0307=1E87# LATIN SMALL LETTER W WITH DOT ABOVE = LATIN SMALL LETTER W + COMBINING DOT ABOVE
0057+0323=1E88# LATIN CAPITAL LETTER W WITH DOT BELOW = LATIN CAPITAL LETTER W + COMBINING DOT BELOW
0077+0323=1E89# LATIN SMALL LETTER W WITH DOT BELOW = LATIN SMALL LETTER W + COMBINING DOT BELOW
0058+0307=1E8A# LATIN CAPITAL LETTER X WITH DOT ABOVE = LATIN CAPITAL LETTER X + COMBINING DOT ABOVE
0078+0307=1E8B# LATIN SMALL LETTER X WITH DOT ABOVE = LATIN SMALL LETTER X + COMBINING DOT ABOVE
0058+0308=1E8C# LATIN CAPITAL LETTER X WITH DIAERESIS = LATIN CAPITAL LETTER X + COMBINING DIAERESIS
0078+0308=1E8D# LATIN SMALL LETTER X WITH DIAERESIS = LATIN SMALL LETTER X + COMBINING DIAERESIS
0059+0307=1E8E# LATIN CAPITAL LETTER Y WITH DOT ABOVE = LATIN CAPITAL LETTER Y + COMBINING DOT ABOVE
0079+0307=1E8F# LATIN SMALL LETTER Y WITH DOT ABOVE = LATIN SMALL LETTER Y + COMBINING DOT ABOVE
005A+0302=1E90# LATIN CAPITAL LETTER Z WITH CIRCUMFLEX = LATIN CAPITAL LETTER Z + COMBINING CIRCUMFLEX ACCENT
007A+0302=1E91# LATIN SMALL LETTER Z WITH CIRCUMFLEX = LATIN SMALL LETTER Z + COMBINING CIRCUMFLEX ACCENT
005A+0323=1E92# LATIN CAPITAL LETTER Z WITH DOT BELOW = LATIN CAPITAL LETTER Z + COMBINING DOT BELOW
007A+0323=1E93# LATIN SMALL LETTER Z WITH DOT BELOW = LATIN SMALL LETTER Z + COMBINING DOT BELOW
005A+0332=1E94# LATIN CAPITAL LETTER Z WITH LINE BELOW = LATIN CAPITAL LETTER Z + COMBINING LOW LINE
007A+0332=1E95# LATIN SMALL LETTER Z WITH LINE BELOW = LATIN SMALL LETTER Z + COMBINING LOW LINE
0068+0332=1E96# LATIN SMALL LETTER H WITH LINE BELOW = LATIN SMALL LETTER H + COMBINING LOW LINE
0074+0308=1E97# LATIN SMALL LETTER T WITH DIAERESIS = LATIN SMALL LETTER T + COMBINING DIAERESIS
0077+030A=1E98# LATIN SMALL LETTER W WITH RING ABOVE = LATIN SMALL LETTER W + COMBINING RING ABOVE
0079+030A=1E99# LATIN SMALL LETTER Y WITH RING ABOVE = LATIN SMALL LETTER Y + COMBINING RING ABOVE
017F+0307=1E9B# LATIN SMALL LETTER LONG S WITH DOT ABOVE = LATIN SMALL LETTER LONG S + COMBINING DOT ABOVE
0041+0323=1EA0# LATIN CAPITAL LETTER A WITH DOT BELOW = LATIN CAPITAL LETTER A + COMBINING DOT BELOW
0061+0323=1EA1# LATIN SMALL LETTER A WITH DOT BELOW = LATIN SMALL LETTER A + COMBINING DOT BELOW
0041+0309=1EA2# LATIN CAPITAL LETTER A WITH HOOK ABOVE = LATIN CAPITAL LETTER A + COMBINING HOOK ABOVE
0061+0309=1EA3# LATIN SMALL LETTER A WITH HOOK ABOVE = LATIN SMALL LETTER A + COMBINING HOOK ABOVE
0041+0302+0301=1EA4# LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND ACUTE = LATIN CAPITAL LETTER A + COMBINING CIRCUMFLEX ACCENT + COMBINING ACUTE ACCENT
0061+0302+0301=1EA5# LATIN SMALL LETTER A WITH CIRCUMFLEX AND ACUTE = LATIN SMALL LETTER A + COMBINING CIRCUMFLEX ACCENT + COMBINING ACUTE ACCENT
0041+0302+0300=1EA6# LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND GRAVE = LATIN CAPITAL LETTER A + COMBINING CIRCUMFLEX ACCENT + COMBINING GRAVE ACCENT
0061+0302+0300=1EA7# LATIN SMALL LETTER A WITH CIRCUMFLEX AND GRAVE = LATIN SMALL LETTER A + COMBINING CIRCUMFLEX ACCENT + COMBINING GRAVE ACCENT
0041+0302+0309=1EA8# LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND HOOK ABOVE = LATIN CAPITAL LETTER A + COMBINING CIRCUMFLEX ACCENT + COMBINING HOOK ABOVE
0061+0302+0309=1EA9# LATIN SMALL LETTER A WITH CIRCUMFLEX AND HOOK ABOVE = LATIN SMALL LETTER A + COMBINING CIRCUMFLEX ACCENT + COMBINING HOOK ABOVE
0041+0302+0303=1EAA# LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND TILDE = LATIN CAPITAL LETTER A + COMBINING CIRCUMFLEX ACCENT + COMBINING TILDE
0061+0302+0303=1EAB# LATIN SMALL LETTER A WITH CIRCUMFLEX AND TILDE = LATIN SMALL LETTER A + COMBINING CIRCUMFLEX ACCENT + COMBINING TILDE
0041+0302+0323=1EAC# LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND DOT BELOW = LATIN CAPITAL LETTER A + COMBINING CIRCUMFLEX ACCENT + COMBINING DOT BELOW
0061+0302+0323=1EAD# LATIN SMALL LETTER A WITH CIRCUMFLEX AND DOT BELOW = LATIN SMALL LETTER A + COMBINING CIRCUMFLEX ACCENT + COMBINING DOT BELOW
0041+0306+0301=1EAE# LATIN CAPITAL LETTER A WITH BREVE AND ACUTE = LATIN CAPITAL LETTER A + COMBINING BREVE + COMBINING ACUTE ACCENT
0061+0306+0301=1EAF# LATIN SMALL LETTER A WITH BREVE AND ACUTE = LATIN SMALL LETTER A + COMBINING BREVE + COMBINING ACUTE ACCENT
0041+0306+0300=1EB0# LATIN CAPITAL LETTER A WITH BREVE AND GRAVE = LATIN CAPITAL LETTER A + COMBINING BREVE + COMBINING GRAVE ACCENT
0061+0306+0300=1EB1# LATIN SMALL LETTER A WITH BREVE AND GRAVE = LATIN SMALL LETTER A + COMBINING BREVE + COMBINING GRAVE ACCENT
0041+0306+0309=1EB2# LATIN CAPITAL LETTER A WITH BREVE AND HOOK ABOVE = LATIN CAPITAL LETTER A + COMBINING BREVE + COMBINING HOOK ABOVE
0061+0306+0309=1EB3# LATIN SMALL LETTER A WITH BREVE AND HOOK ABOVE = LATIN SMALL LETTER A + COMBINING BREVE + COMBINING HOOK ABOVE
0041+0306+0303=1EB4# LATIN CAPITAL LETTER A WITH BREVE AND TILDE = LATIN CAPITAL LETTER A + COMBINING BREVE + COMBINING TILDE
0061+0306+0303=1EB5# LATIN SMALL LETTER A WITH BREVE AND TILDE = LATIN SMALL LETTER A + COMBINING BREVE + COMBINING TILDE
0041+0306+0323=1EB6# LATIN CAPITAL LETTER A WITH BREVE AND DOT BELOW = LATIN CAPITAL LETTER A + COMBINING BREVE + COMBINING DOT BELOW
0061+0306+0323=1EB7# LATIN SMALL LETTER A WITH BREVE AND DOT BELOW = LATIN SMALL LETTER A + COMBINING BREVE + COMBINING DOT BELOW
0045+0323=1EB8# LATIN CAPITAL LETTER E WITH DOT BELOW = LATIN CAPITAL LETTER E + COMBINING DOT BELOW
0065+0323=1EB9# LATIN SMALL LETTER E WITH DOT BELOW = LATIN SMALL LETTER E + COMBINING DOT BELOW
0045+0309=1EBA# LATIN CAPITAL LETTER E WITH HOOK ABOVE = LATIN CAPITAL LETTER E + COMBINING HOOK ABOVE
0065+0309=1EBB# LATIN SMALL LETTER E WITH HOOK ABOVE = LATIN SMALL LETTER E + COMBINING HOOK ABOVE
0045+0303=1EBC# LATIN CAPITAL LETTER E WITH TILDE = LATIN CAPITAL LETTER E + COMBINING TILDE
0065+0303=1EBD# LATIN SMALL LETTER E WITH TILDE = LATIN SMALL LETTER E + COMBINING TILDE
0045+0302+0301=1EBE# LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND ACUTE = LATIN CAPITAL LETTER E + COMBINING CIRCUMFLEX ACCENT + COMBINING ACUTE ACCENT
0065+0302+0301=1EBF# LATIN SMALL LETTER E WITH CIRCUMFLEX AND ACUTE = LATIN SMALL LETTER E + COMBINING CIRCUMFLEX ACCENT + COMBINING ACUTE ACCENT
0045+0302+0300=1EC0# LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND GRAVE = LATIN CAPITAL LETTER E + COMBINING CIRCUMFLEX ACCENT + COMBINING GRAVE ACCENT
0065+0302+0300=1EC1# LATIN SMALL LETTER E WITH CIRCUMFLEX AND GRAVE = LATIN SMALL LETTER E + COMBINING CIRCUMFLEX ACCENT + COMBINING GRAVE ACCENT
0045+0302+0309=1EC2# LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND HOOK ABOVE = LATIN CAPITAL LETTER E + COMBINING CIRCUMFLEX ACCENT + COMBINING HOOK ABOVE
0065+0302+0309=1EC3# LATIN SMALL LETTER E WITH CIRCUMFLEX AND HOOK ABOVE = LATIN SMALL LETTER E + COMBINING CIRCUMFLEX ACCENT + COMBINING HOOK ABOVE
0045+0302+0303=1EC4# LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND TILDE = LATIN CAPITAL LETTER E + COMBINING CIRCUMFLEX ACCENT + COMBINING TILDE
0065+0302+0303=1EC5# LATIN SMALL LETTER E WITH CIRCUMFLEX AND TILDE = LATIN SMALL LETTER E + COMBINING CIRCUMFLEX ACCENT + COMBINING TILDE
0045+0302+0323=1EC6# LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND DOT BELOW = LATIN CAPITAL LETTER E + COMBINING CIRCUMFLEX ACCENT + COMBINING DOT BELOW
0065+0302+0323=1EC7# LATIN SMALL LETTER E WITH CIRCUMFLEX AND DOT BELOW = LATIN SMALL LETTER E + COMBINING CIRCUMFLEX ACCENT + COMBINING DOT BELOW
0049+0309=1EC8# LATIN CAPITAL LETTER I WITH HOOK ABOVE = LATIN CAPITAL LETTER I + COMBINING HOOK ABOVE
0069+0309=1EC9# LATIN SMALL LETTER I WITH HOOK ABOVE = LATIN SMALL LETTER I + COMBINING HOOK ABOVE
0049+0323=1ECA# LATIN CAPITAL LETTER I WITH DOT BELOW = LATIN CAPITAL LETTER I + COMBINING DOT BELOW
0069+0323=1ECB# LATIN SMALL LETTER I WITH DOT BELOW = LATIN SMALL LETTER I + COMBINING DOT BELOW
004F+0323=1ECC# LATIN CAPITAL LETTER O WITH DOT BELOW = LATIN CAPITAL LETTER O + COMBINING DOT BELOW
006F+0323=1ECD# LATIN SMALL LETTER O WITH DOT BELOW = LATIN SMALL LETTER O + COMBINING DOT BELOW
004F+0309=1ECE# LATIN CAPITAL LETTER O WITH HOOK ABOVE = LATIN CAPITAL LETTER O + COMBINING HOOK ABOVE
006F+0309=1ECF# LATIN SMALL LETTER O WITH HOOK ABOVE = LATIN SMALL LETTER O + COMBINING HOOK ABOVE
004F+0302+0301=1ED0# LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND ACUTE = LATIN CAPITAL LETTER O + COMBINING CIRCUMFLEX ACCENT + COMBINING ACUTE ACCENT
006F+0302+0301=1ED1# LATIN SMALL LETTER O WITH CIRCUMFLEX AND ACUTE = LATIN SMALL LETTER O + COMBINING CIRCUMFLEX ACCENT + COMBINING ACUTE ACCENT
004F+0302+0300=1ED2# LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND GRAVE = LATIN CAPITAL LETTER O + COMBINING CIRCUMFLEX ACCENT + COMBINING GRAVE ACCENT
006F+0302+0300=1ED3# LATIN SMALL LETTER O WITH CIRCUMFLEX AND GRAVE = LATIN SMALL LETTER O + COMBINING CIRCUMFLEX ACCENT + COMBINING GRAVE ACCENT
004F+0302+0309=1ED4# LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND HOOK ABOVE = LATIN CAPITAL LETTER O + COMBINING CIRCUMFLEX ACCENT + COMBINING HOOK ABOVE
006F+0302+0309=1ED5# LATIN SMALL LETTER O WITH CIRCUMFLEX AND HOOK ABOVE = LATIN SMALL LETTER O + COMBINING CIRCUMFLEX ACCENT + COMBINING HOOK ABOVE
004F+0302+0303=1ED6# LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND TILDE = LATIN CAPITAL LETTER O + COMBINING CIRCUMFLEX ACCENT + COMBINING TILDE
006F+0302+0303=1ED7# LATIN SMALL LETTER O WITH CIRCUMFLEX AND TILDE = LATIN SMALL LETTER O + COMBINING CIRCUMFLEX ACCENT + COMBINING TILDE
004F+0302+0323=1ED8# LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND DOT BELOW = LATIN CAPITAL LETTER O + COMBINING CIRCUMFLEX ACCENT + COMBINING DOT BELOW
006F+0302+0323=1ED9# LATIN SMALL LETTER O WITH CIRCUMFLEX AND DOT BELOW = LATIN SMALL LETTER O + COMBINING CIRCUMFLEX ACCENT + COMBINING DOT BELOW
004F+031B+0301=1EDA# LATIN CAPITAL LETTER O WITH HORN AND ACUTE = LATIN CAPITAL LETTER O + COMBINING HORN + COMBINING ACUTE ACCENT
006F+031B+0301=1EDB# LATIN SMALL LETTER O WITH HORN AND ACUTE = LATIN SMALL LETTER O + COMBINING HORN + COMBINING ACUTE ACCENT
004F+031B+0300=1EDC# LATIN CAPITAL LETTER O WITH HORN AND GRAVE = LATIN CAPITAL LETTER O + COMBINING HORN + COMBINING GRAVE ACCENT
006F+031B+0300=1EDD# LATIN SMALL LETTER O WITH HORN AND GRAVE = LATIN SMALL LETTER O + COMBINING HORN + COMBINING GRAVE ACCENT
004F+031B+0309=1EDE# LATIN CAPITAL LETTER O WITH HORN AND HOOK ABOVE = LATIN CAPITAL LETTER O + COMBINING HORN + COMBINING HOOK ABOVE
006F+031B+0309=1EDF# LATIN SMALL LETTER O WITH HORN AND HOOK ABOVE = LATIN SMALL LETTER O + COMBINING HORN + COMBINING HOOK ABOVE
004F+031B+0303=1EE0# LATIN CAPITAL LETTER O WITH HORN AND TILDE = LATIN CAPITAL LETTER O + COMBINING HORN + COMBINING TILDE
006F+031B+0303=1EE1# LATIN SMALL LETTER O WITH HORN AND TILDE = LATIN SMALL LETTER O + COMBINING HORN + COMBINING TILDE
004F+031B+0323=1EE2# LATIN CAPITAL LETTER O WITH HORN AND DOT BELOW = LATIN CAPITAL LETTER O + COMBINING HORN + COMBINING DOT BELOW
006F+031B+0323=1EE3# LATIN SMALL LETTER O WITH HORN AND DOT BELOW = LATIN SMALL LETTER O + COMBINING HORN + COMBINING DOT BELOW
0055+0323=1EE4# LATIN CAPITAL LETTER U WITH DOT BELOW = LATIN CAPITAL LETTER U + COMBINING DOT BELOW
0075+0323=1EE5# LATIN SMALL LETTER U WITH DOT BELOW = LATIN SMALL LETTER U + COMBINING DOT BELOW
0055+0309=1EE6# LATIN CAPITAL LETTER U WITH HOOK ABOVE = LATIN CAPITAL LETTER U + COMBINING HOOK ABOVE
0075+0309=1EE7# LATIN SMALL LETTER U WITH HOOK ABOVE = LATIN SMALL LETTER U + COMBINING HOOK ABOVE
0055+031B+0301=1EE8# LATIN CAPITAL LETTER U WITH HORN AND ACUTE = LATIN CAPITAL LETTER U + COMBINING HORN + COMBINING ACUTE ACCENT
0075+031B+0301=1EE9# LATIN SMALL LETTER U WITH HORN AND ACUTE = LATIN SMALL LETTER U + COMBINING HORN + COMBINING ACUTE ACCENT
0055+031B+0300=1EEA# LATIN CAPITAL LETTER U WITH HORN AND GRAVE = LATIN CAPITAL LETTER U + COMBINING HORN + COMBINING GRAVE ACCENT
0075+031B+0300=1EEB# LATIN SMALL LETTER U WITH HORN AND GRAVE = LATIN SMALL LETTER U + COMBINING HORN + COMBINING GRAVE ACCENT
0055+031B+0309=1EEC# LATIN CAPITAL LETTER U WITH HORN AND HOOK ABOVE = LATIN CAPITAL LETTER U + COMBINING HORN + COMBINING HOOK ABOVE
0075+031B+0309=1EED# LATIN SMALL LETTER U WITH HORN AND HOOK ABOVE = LATIN SMALL LETTER U + COMBINING HORN + COMBINING HOOK ABOVE
0055+031B+0303=1EEE# LATIN CAPITAL LETTER U WITH HORN AND TILDE = LATIN CAPITAL LETTER U + COMBINING HORN + COMBINING TILDE
0075+031B+0303=1EEF# LATIN SMALL LETTER U WITH HORN AND TILDE = LATIN SMALL LETTER U + COMBINING HORN + COMBINING TILDE
0055+031B+0323=1EF0# LATIN CAPITAL LETTER U WITH HORN AND DOT BELOW = LATIN CAPITAL LETTER U + COMBINING HORN + COMBINING DOT BELOW
0075+031B+0323=1EF1# LATIN SMALL LETTER U WITH HORN AND DOT BELOW = LATIN SMALL LETTER U + COMBINING HORN + COMBINING DOT BELOW
0059+0300=1EF2# LATIN CAPITAL LETTER Y WITH GRAVE = LATIN CAPITAL LETTER Y + COMBINING GRAVE ACCENT
0079+0300=1EF3# LATIN SMALL LETTER Y WITH GRAVE = LATIN SMALL LETTER Y + COMBINING GRAVE ACCENT
0059+0323=1EF4# LATIN CAPITAL LETTER Y WITH DOT BELOW = LATIN CAPITAL LETTER Y + COMBINING DOT BELOW
0079+0323=1EF5# LATIN SMALL LETTER Y WITH DOT BELOW = LATIN SMALL LETTER Y + COMBINING DOT BELOW
0059+0309=1EF6# LATIN CAPITAL LETTER Y WITH HOOK ABOVE = LATIN CAPITAL LETTER Y + COMBINING HOOK ABOVE
0079+0309=1EF7# LATIN SMALL LETTER Y WITH HOOK ABOVE = LATIN SMALL LETTER Y + COMBINING HOOK ABOVE
0059+0303=1EF8# LATIN CAPITAL LETTER Y WITH TILDE = LATIN CAPITAL LETTER Y + COMBINING TILDE
0079+0303=1EF9# LATIN SMALL LETTER Y WITH TILDE = LATIN SMALL LETTER Y + COMBINING TILDE

 */
