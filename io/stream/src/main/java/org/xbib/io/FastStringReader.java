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
package org.xbib.io;

import java.io.IOException;

/**
 * A character stream whose source is a string that is <b>not thread safe</b>
 */
public class FastStringReader extends CharSequenceReader {

    private String str;
    private int length;
    private int next = 0;
    private int mark = 0;

    /**
     * Creates a new string reader.
     *
     * @param s String providing the character stream.
     */
    public FastStringReader(String s) {
        this.str = s;
        this.length = s.length();
    }

    /**
     * Check to make sure that the stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (length == -1) {
            throw new IOException("Stream closed");
        }
    }


    public int length() {
        return length;
    }


    public char charAt(int index) {
        return str.charAt(index);
    }


    public CharSequence subSequence(int start, int end) {
        return str.subSequence(start, end);
    }

    /**
     * Reads a single character.
     *
     * @return The character read, or -1 if the end of the stream has been
     * reached
     * @throws java.io.IOException If an I/O error occurs
     */

    public int read() throws IOException {
        ensureOpen();
        if (next >= length) {
            return -1;
        }
        return str.charAt(next++);
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @param cbuf Destination buffer
     * @param off  Offset at which to start writing characters
     * @param len  Maximum number of characters to read
     * @return The number of characters read, or -1 if the end of the
     * stream has been reached
     * @throws java.io.IOException If an I/O error occurs
     */

    public int read(char cbuf[], int off, int len) throws IOException {
        ensureOpen();
        if (len == 0) {
            return 0;
        }
        if (next >= length) {
            return -1;
        }
        int n = Math.min(length - next, len);
        str.getChars(next, next + n, cbuf, off);
        next += n;
        return n;
    }

    /**
     * Skips the specified number of characters in the stream. Returns
     * the number of characters that were skipped.
     * <p/>
     * <p>The <code>ns</code> parameter may be negative, even though the
     * <code>skip</code> method of the {@link java.io.Reader} superclass throws
     * an exception in this case. Negative values of <code>ns</code> cause the
     * stream to skip backwards. Negative return values indicate a skip
     * backwards. It is not possible to skip backwards past the beginning of
     * the string.
     * <p/>
     * <p>If the entire string has been read or skipped, then this method has
     * no effect and always returns 0.
     *
     * @throws java.io.IOException If an I/O error occurs
     */

    public long skip(long ns) throws IOException {
        ensureOpen();
        if (next >= length) {
            return 0;
        }
        // Bound skip by beginning and end of the source
        long n = Math.min(length - next, ns);
        n = Math.max(-next, n);
        next += n;
        return n;
    }

    /**
     * Tells whether this stream is ready to be read.
     *
     * @return True if the next read() is guaranteed not to block for input
     * @throws java.io.IOException If the stream is closed
     */

    public boolean ready() throws IOException {
        ensureOpen();
        return true;
    }

    /**
     * Tells whether this stream supports the mark() operation, which it does.
     */

    public boolean markSupported() {
        return true;
    }

    /**
     * Marks the present position in the stream.  Subsequent calls to reset()
     * will reposition the stream to this point.
     *
     * @param readAheadLimit Limit on the number of characters that may be
     *                       read while still preserving the mark.  Because
     *                       the stream's input comes from a string, there
     *                       is no actual limit, so this argument must not
     *                       be negative, but is otherwise ignored.
     * @throws IllegalArgumentException If readAheadLimit is < 0
     * @throws java.io.IOException      If an I/O error occurs
     */

    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        ensureOpen();
        mark = next;
    }

    /**
     * Resets the stream to the most recent mark, or to the beginning of the
     * string if it has never been marked.
     *
     * @throws java.io.IOException If an I/O error occurs
     */

    public void reset() throws IOException {
        ensureOpen();
        next = mark;
    }

    /**
     * Closes the stream and releases any system resources associated with
     * it. Once the stream has been closed, further read(),
     * ready(), mark(), or reset() invocations will throw an IOException.
     * Closing a previously closed stream has no effect.
     */
    public void close() {
        length = -1;
    }


    public String toString() {
        return str;
    }
}
