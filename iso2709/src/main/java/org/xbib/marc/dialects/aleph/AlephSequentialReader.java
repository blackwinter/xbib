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
package org.xbib.marc.dialects.aleph;

import org.xbib.io.field.FieldSeparator;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

/**
 * Aleph sequential format reader. This format is a derivative of MARC.
 *
 */
public class AlephSequentialReader extends FilterReader {

    private LinkedList<Integer> pushback;

    private boolean atlinestart;

    private boolean atfield;

    private boolean ended;

    public AlephSequentialReader(Reader reader) {
        super(reader);
        pushback = new LinkedList<Integer>();
        atlinestart = true;
        atfield = false;
        ended = false;
    }

    @Override
    public int read() throws IOException {
        int ch;
        ch = !pushback.isEmpty() ? pushback.pop() : in.read();
        // insert a last GS if end of stream reached
        if (ch == -1) {
            if (!ended) {
                ended = true;
                return FieldSeparator.GS; // close data
            } else {
                return ch;
            }
        }
        if (atlinestart) {
            atlinestart = false;
            atfield = false;
            // slurp sequential number
            while (ch >= '0' && ch <= '9') {
                ch = in.read();
            }
            ch = in.read();
            // catch alphabetical tags, check for LDR, emit GS if found
            if (ch >= 'A' && ch <= 'Z') {
                int ch2 = in.read();
                if (ch2 >= 'A' && ch2 <= 'Z') {
                    int ch3 = in.read();
                    if (ch3 >= 'A' && ch3 <= 'Z') {
                        in.read(); //skip ind1
                        in.read(); //skip ind2
                        in.read(); //skip blank
                        in.read(); //skip 'L' (FMT value)
                        in.read(); //skip blank
                        if (ch == 'L' && ch2 == 'D' && ch3 == 'R') {
                            return FieldSeparator.GS;
                        } else {
                            pushback.addLast(ch2);
                            pushback.addLast(ch3);
                        }
                    } else {
                        pushback.addLast(ch2);
                        pushback.addLast(ch3);
                    }
                } else {
                    pushback.addLast(ch2);
                }
            } else if (ch >= '0' && ch <= '9') {
                // catch numerical field designators
                int ch2 = in.read();
                if (ch2 >= '0' && ch2 <= '9') {
                    int ch3 = in.read();
                    if (ch3 >= '0' && ch3 <= '9') {
                        int ind1 = in.read();
                        int ind2 = in.read();
                        in.read(); //skip blank
                        in.read(); //skip 'L' (FMT value)
                        in.read(); //skip blank
                        pushback.addLast(ch2);
                        pushback.addLast(ch3);
                        if (!(ch == '0' && ch2 == '0')) { // do not attach indicators at control field
                            pushback.addLast(ind1);
                            pushback.addLast(ind2);
                        }
                        atfield = true;
                    } else {
                        pushback.addLast(ch2);
                        pushback.addLast(ch3);
                    }
                } else {
                    pushback.addLast(ch2);
                }
            } else {
                throw new IOException("wrong field designator character: " + ch);
            }
        }
        if (atfield) {
            if (ch == '$') {
                int ch2 = in.read();
                if (ch2 == '$') {
                    return FieldSeparator.US;
                } else {
                    pushback.addLast(ch2);
                }
            }
        }
        if (ch == '\n') {
            atfield = false;
            atlinestart = true;
            // slurp all subsequent line terminators
            ch = in.read();
            while (ch == '\n' || ch == '\r') {
                ch = in.read();
            }
            pushback.addLast(ch);
            ch = FieldSeparator.RS;
        }
        return ch;
    }

    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            int ch = read();
            if (ch == -1) {
                return (i == 0) ? -1 : i;
            } else {
                cbuf[i + off] = (char) ch;
            }
        }
        return len;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public boolean ready() throws IOException {
        if (!pushback.isEmpty()) {
            return true;
        } else {
            return in.ready();
        }
    }
}
