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

import java.util.Arrays;
import java.util.List;

/**
 * Encodes a string into a soundex value. Soundex is an encoding used to relate
 * similar names, but can also be used as a general purpose scheme to find word
 * with similar phonemes.
 *
 */
public class SoundexEncoder implements StringEncoder {

    /**
     * This is a default mapping of the 26 letters used in US english.
     */
    private static final List<Character> US_ENGLISH_MAPPING =
            Arrays.asList('0', '1', '2', '3', '0', '1', '2', '0',
                    '0', '2', '2', '4', '5', '5', '0', '1',
                    '2', '6', '2', '3', '0', '1', '0', '2', '0', '2');
    /**
     * This static variable contains an instance of the Soundex using the
     * US_ENGLISH mapping.
     */
    public static final SoundexEncoder US_ENGLISH = new SoundexEncoder();
    /**
     * Every letter of the alphabet is "mapped" to a numerical value. This char
     * array holds the values to which each letter is mapped. This implementation
     * contains a default map for US_ENGLISH
     */
    private List<Character> soundexMapping;
    /**
     * The maximum length of a Soundex code - Soundex codes are only four
     * characters by definition.
     */
    private int maxLength = 4;

    /**
     * Creates an instance of the Soundex object using the default US_ENGLISH
     * mapping.
     */
    public SoundexEncoder() {
        this(US_ENGLISH_MAPPING);
    }

    /**
     * Creates a soundex instance using a custom mapping. This constructor can be
     * used to customize the mapping, and/or possibly provide an internationalized
     * mapping for a non-Western character set.
     *
     * @param mapping Mapping array to use when finding the corresponding code for a
     *                given character
     */
    public SoundexEncoder(List<Character> mapping) {
        this.soundexMapping = mapping;
    }

    /**
     * Retrieves the Soundex code for a given string
     *
     * @param str String to encode using the Soundex algorithm
     * @return A soundex code for the String supplied
     */
    public String encode(String str) throws EncoderException {
        if (null == str || str.length() == 0) {
            return str;
        }
        char out[] = {'0', '0', '0', '0'};
        char last, mapped;
        int incount = 1, count = 1;
        out[0] = Character.toUpperCase(str.charAt(0));
        last = getMappingCode(str.charAt(0));
        while ((incount < str.length()) && (mapped = getMappingCode(str.charAt(incount++))) != 0 && (count < this.maxLength)) {
            if ((mapped != '0') && (mapped != last)) {
                out[count++] = mapped;
            }
            last = mapped;
        }
        return new String(out);
    }

    /**
     * Used internally by the SoundEx algorithm.
     *
     * @param c character to use to retrieve mapping code
     * @return Mapping code for a particular character
     */
    private char getMappingCode(char c) {
        if (!Character.isLetter(c)) {
            return 0;
        }
        return soundexMapping.get(Character.toUpperCase(c) - 'A');
    }

    /**
     * Returns the maxLength. Standard Soundex
     *
     * @return int
     */
    public int getMaxLength() {
        return this.maxLength;
    }

    /**
     * Sets the maxLength.
     *
     * @param maxLength The maxLength to set
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}