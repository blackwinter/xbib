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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.CharsetDecoder;
import java.text.Normalizer;

public abstract class SingleByteDecoder extends CharsetDecoder {

    private boolean composeCharactersAfterConversion = true;

    /**
     * @return Returns the composeCharactersAfterConversion.
     */
    public boolean isComposeCharactersAfterConversion() {
        return this.composeCharactersAfterConversion;
    }

    /**
     * @param composeCharactersAfterConversion
     *            The composeCharactersAfterConversion to set.
     */
    public void setComposeCharactersAfterConversion(
            boolean composeCharactersAfterConversion) {
        this.composeCharactersAfterConversion = composeCharactersAfterConversion;
    }

    protected SingleByteDecoder(Charset cs) {
        super(cs, 1.0f, 1.0f);
    }

    @Override
    protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
        ByteBuffer inputBuffer = ByteBuffer.allocate(30);
        while (in.hasRemaining()) {
            byte c = in.get();
            inputBuffer.put(c);

            StringBuilder convertedInputBuffer = null;
            if (!isCombiningCharacter(c)) {
                convertedInputBuffer = new StringBuilder();
                for (int i = inputBuffer.position() - 1; i >= 0; i--) {
                    char convertedCharacter = byteToChar(inputBuffer.get(i));
                    String convertedCharacterAsString;
                    if (convertedCharacter == 0) {
                        convertedCharacterAsString = replacement();
                    } else {
                        convertedCharacterAsString = String
                                .valueOf(convertedCharacter);
                    }

                    convertedInputBuffer.append(convertedCharacterAsString);
                }

                if (this.composeCharactersAfterConversion) {
                    convertedInputBuffer = new StringBuilder(Normalizer.normalize(convertedInputBuffer.toString(), Normalizer.Form.NFC));
                }
                
            }
            if (convertedInputBuffer != null) {
                if (out.remaining() < convertedInputBuffer.length()) {
                    /*
                     * output buffer is to small --> reset position and return
                     * OVERFLOW
                     */
                    in.position(in.position() - inputBuffer.position());
                    return CoderResult.OVERFLOW;
                }
                out.append(convertedInputBuffer);
                inputBuffer.clear();
            }

        }
        return CoderResult.UNDERFLOW;
    }

    public abstract boolean isCombiningCharacter(byte c);

    public abstract char byteToChar(byte b);

}
