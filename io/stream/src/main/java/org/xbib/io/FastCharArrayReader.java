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
import java.io.Reader;

public class FastCharArrayReader extends Reader {

    /**
     * The character buffer.
     */
    protected char buf[];

    /**
     * The current buffer position.
     */
    protected int pos;

    /**
     * The position of mark in buffer.
     */
    protected int markedPos = 0;

    /**
     * The index of the end of this buffer.  There is not valid
     * data at or beyond this index.
     */
    protected int count;

    /**
     * Creates a CharArrayReader from the specified array of chars.
     *
     * @param buf Input buffer (not copied)
     */
    public FastCharArrayReader(char buf[]) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    /**
     * Creates a CharArrayReader from the specified array of chars.
     * <p/>
     * <p> The resulting reader will start reading at the given
     * <tt>offset</tt>.  The total number of <tt>char</tt> values that can be
     * read from this reader will be either <tt>length</tt> or
     * <tt>buf.length-offset</tt>, whichever is smaller.
     *
     * @param buf    Input buffer (not copied)
     * @param offset Offset of the first char to read
     * @param length Number of chars to read
     * @throws IllegalArgumentException If <tt>offset</tt> is negative or greater than
     *                                  <tt>buf.length</tt>, or if <tt>length</tt> is negative, or if
     *                                  the sum of these two values is negative.
     */
    public FastCharArrayReader(char buf[], int offset, int length) {
        if ((offset < 0) || (offset > buf.length) || (length < 0) ||
                ((offset + length) < 0)) {
            throw new IllegalArgumentException();
        }
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.markedPos = offset;
    }

    /**
     * Checks to make sure that the stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (buf == null) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * Reads a single character.
     *
     * @throws java.io.IOException If an I/O error occurs
     */
    public int read() throws IOException {
        ensureOpen();
        if (pos >= count) {
            return -1;
        } else {
            return buf[pos++];
        }
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @param b   Destination buffer
     * @param off Offset at which to start storing characters
     * @param len Maximum number of characters to read
     * @return The actual number of characters read, or -1 if
     * the end of the stream has been reached
     * @throws java.io.IOException If an I/O error occurs
     */
    public int read(char b[], int off, int len) throws IOException {
        ensureOpen();
        if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        if (pos >= count) {
            return -1;
        }
        if (pos + len > count) {
            len = count - pos;
        }
        if (len <= 0) {
            return 0;
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    /**
     * Skips characters.  Returns the number of characters that were skipped.
     * <p/>
     * <p>The <code>n</code> parameter may be negative, even though the
     * <code>skip</code> method of the {@link java.io.Reader} superclass throws
     * an exception in this case. If <code>n</code> is negative, then
     * this method does nothing and returns <code>0</code>.
     *
     * @param n The number of characters to skip
     * @return The number of characters actually skipped
     * @throws java.io.IOException If the stream is closed, or an I/O error occurs
     */
    public long skip(long n) throws IOException {
        ensureOpen();
        if (pos + n > count) {
            n = count - pos;
        }
        if (n < 0) {
            return 0;
        }
        pos += n;
        return n;
    }

    /**
     * Tells whether this stream is ready to be read.  Character-array readers
     * are always ready to be read.
     *
     * @throws java.io.IOException If an I/O error occurs
     */
    public boolean ready() throws IOException {
        ensureOpen();
        return (count - pos) > 0;
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
     *                       the stream's input comes from a character array,
     *                       there is no actual limit; hence this argument is
     *                       ignored.
     * @throws java.io.IOException If an I/O error occurs
     */
    public void mark(int readAheadLimit) throws IOException {
        ensureOpen();
        markedPos = pos;
    }

    /**
     * Resets the stream to the most recent mark, or to the beginning if it has
     * never been marked.
     *
     * @throws java.io.IOException If an I/O error occurs
     */
    public void reset() throws IOException {
        ensureOpen();
        pos = markedPos;
    }

    /**
     * Closes the stream and releases any system resources associated with
     * it.  Once the stream has been closed, further read(), ready(),
     * mark(), reset(), or skip() invocations will throw an IOException.
     * Closing a previously closed stream has no effect.
     */
    public void close() {
        buf = null;
    }
}
