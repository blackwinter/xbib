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
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.text.Normalizer;

public abstract class SingleByteEncoder extends CharsetEncoder {

    private boolean decomposeCharactersBeforeConversion = true;

    /**
     * @return Returns the decomposeCharactersBeforeConversion.
     */
    public boolean isDecomposeCharactersBeforeConversion() {
        return this.decomposeCharactersBeforeConversion;
    }

    /**
     * @param decomposeCharactersBeforeConversion
     *            The decomposeCharactersBeforeConversion to set.
     */
    public void setDecomposeCharactersBeforeConversion(
            boolean decomposeCharactersBeforeConversion) {
        this.decomposeCharactersBeforeConversion = decomposeCharactersBeforeConversion;
    }

    protected SingleByteEncoder(Charset cs) {
        super(cs, 1.0f, 1.0f);
    }

    @Override
    protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
        CharBuffer inputBuffer = CharBuffer.allocate(30);
        while (in.hasRemaining()) {
            char c = in.get();
            String charAsString;
            if (isDecomposeCharactersBeforeConversion()) {
                charAsString = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
            } else {
                charAsString = String.valueOf(c);
            }
            if (out.remaining() < inputBuffer.position()
                    + charAsString.length()) {
                /*
                 * output buffer is to small --> reset position and return
                 * OVERFLOW
                 */
                in.position(in.position() - inputBuffer.position() - 1);
                return CoderResult.OVERFLOW;
            }

            if (inputBuffer.position() > 0 && !isCombiningCharacter(c)) {
                /*
                 * convert characters in reverse order
                 */
                for (int i = inputBuffer.position() - 1; i >= 0; i--) {
                    convert(inputBuffer.get(i), out);
                }
                inputBuffer.clear();
            }
            inputBuffer.append(charAsString);
        }

        if (inputBuffer.position() == 1) {
            convert(inputBuffer.get(), out);
        } else if (inputBuffer.position() > 1) {
            for (int i = inputBuffer.position() - 1; i >= 0; i--) {
                convert(inputBuffer.get(i), out);
            }
        }

        return CoderResult.UNDERFLOW;
    }

    public abstract byte charToByte(char c);

    public boolean isCombiningCharacter(char c) {
        return c >= '\u0300' && c <= '\u036F';
    }

    private void convert(char c, ByteBuffer out) {
        byte c_ = charToByte(c);
        if (c_ != 0) {
            out.put(c_);
        } else {
            out.put(replacement());
        }
    }
}
