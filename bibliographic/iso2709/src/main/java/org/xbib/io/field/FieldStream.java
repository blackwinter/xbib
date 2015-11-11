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
package org.xbib.io.field;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

/**
 * An object for iterating over structured data streams with information
 * separators.
 *
 * The information separators of the C0 control group are defined in:
 * - ANSI X3.4-1967 (ASCII)
 * - IETF RFC 20 (Vint Cerf, 1969)
 * - ISO-646:1972 
 * - ECMA-6 3rd revision August 1973
 * - ECMA-48
 * - ISO/IEC 6429
 * - CCITT International Telegraph Alphabet Number 5 (ITA-5)
 *
 * From ASCII-1967:
 * "Can be used as delimiters to mark fields of data structures.
 * If used for hierarchical levels, US is the lowest level (dividing
 * plain-text data items), while RS, GS, and FS are of increasing level
 * to divide groups made up of items of the level beneath it."
 *
 *  Form IETF RFC 20:
 * "Information Separator: A character which is used to separate
 *  and qualify information in a logical sense.  There is a group of four
 *  such characters, which are to be used in a hierarchical order."
 *
 * From ECMA-48 (ISO/IEC 6429):
 *
 * "Each information separator is given two names. The names,
 * INFORMATION SEPARATOR FOUR (IS4), INFORMATION SEPARATOR THREE (IS3),
 * INFORMATION SEPARATOR TWO (IS2), and INFORMATION SEPARATOR ONE (IS1)
 * are the general names. The names FILE SEPARATOR (FS), GROUP SEPARATOR (GS),
 * RECORD SEPARATOR (RS), and UNIT SEPARATOR (US) are the specific names and
 * are intended mainly for applications where the information separators are
 * used hierarchically. The ascending order is then US, RS, GS, FS.
 * In this case, data normally delimited by a particular separator cannot
 * be split by a higher-order separator but will be considered as delimited by
 * any other higher-order separator.
 * In ISO/IEC 10538, IS3 and IS4 are given the names PAGE TERMINATOR (PT)
 * and DOCUMENT TERMINATOR (DT), respectively and may be used to reset
 * presentation attributes to the default state."
 *
 */
public interface FieldStream extends Closeable {

    /**
     * Read character
     * @return the character
     * @throws IOException
     */
    int read() throws IOException;

    /**
     * Read characters
     * @param c the character array
     * @param off the offset
     * @param len the length
     * @return the character as abn array
     * @throws IOException
     */
    int read(char c[], int off, int len) throws IOException;

    /**
     * Skip characters
     * @param n the position
     * @return the new position
     * @throws IOException
     */
    long skip(long n) throws IOException;

    /**
     * Is this stream ready?
     * @return true if ready
     * @throws IOException
     */
    boolean ready() throws IOException;

    /**
     * Is mark() method supported?
     * @return true if supported
     */
    boolean markSupported();

    /**
     * Mark position for reset()
     * @param readAheadLimit the read ahead limit
     * @throws IOException
     */
    void mark(int readAheadLimit) throws IOException;

    /**
     * Reset stream to a marked position
     * @throws IOException
     */
    void reset() throws IOException;

    /**
     * Read data chunk
     *
     * @throws IOException
     */
    Separable readField() throws IOException;

    /**
     * Get the underlying reader
     * @return the underlying reader
     */
    Reader getReader();

}
