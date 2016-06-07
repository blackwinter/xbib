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

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class is a buffered character input reader on a stream with separator charatcers.
 * Buffering allows reading from character streams more efficiently. If the default size of the
 * buffer is not practical, another size may be specified. Reading a character
 * from a Reader class usually involves reading a character from its Reader.
 * It is advisable to wrap a SequentialCharStream around those
 * Readers whose read operations may have high latency. For example, the
 * following code
 * <pre>
 * BufferedFieldStreamReader inReader = new BufferedFieldStreamReader(new FileReader(&quot;file.java&quot;));
 * </pre>
 * will buffer input for the file <code>file.java</code>.
 */
public class BufferedFieldStreamReader extends Reader implements FieldStream {

    private Reader in;

    private char[] buf;

    private int marklimit = -1;

    private int count;

    private int markpos = -1;

    private int pos;

    private FieldListener listener;

    private final static int DEFAULT_BUFFER_SIZE = 65536;

    /**
     * Constructs a new SequentialCharStream on the Reader
     * <code>in</code>. The default buffer size (8K) is allocated and all reads
     * can now be filtered through this SequentialCharStream.
     *
     * @param in the Reader to buffer reads on
     */
    public BufferedFieldStreamReader(Reader in) {
        this(in, null);
    }

    /**
     * Constructs a new SequentialCharStream on the Reader
     * <code>in</code>. The default buffer size (8K) is allocated and all reads
     * can now be filtered through this SequentialCharStream.
     *
     * @param in the Reader to buffer reads on
     */
    public BufferedFieldStreamReader(Reader in, FieldListener listener) {
        this(in, DEFAULT_BUFFER_SIZE, listener);
    }

    /**
     * Constructs a new SequentialCharStream on the Reader
     * <code>in</code>. The buffer size is specified by the parameter
     * <code>size</code> and all reads can now be filtered through this
     * SequentialCharStream.
     *
     * @param in the Reader to buffer reads on
     * @param size the size of buffer to allocate
     * @throws IllegalArgumentException if the size is <= 0
     */
    public BufferedFieldStreamReader(Reader in, int size, FieldListener listener) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException();
        }
        this.in = in;
        this.buf = new char[size];
        this.listener = listener;
    }

    /**
     * Close the Reader. This implementation closes the Reader being filtered
     * and releases the buffer used by this reader. If this SequentialCharStream has
     * already been closed, nothing is done.
     *
     * @throws IOException If an error occurs attempting to close this SequentialCharStream
     */
    @Override
    public void close() throws IOException {
        if (listener != null) {
            listener.mark(FieldSeparator.FS);
        }
        synchronized (lock) {
            if (!isClosed()) {
                in.close();
                buf = null;
            }
        }
    }

    /**
     * Set a Mark position in this SequentialCharStream. The parameter
     * <code>readLimit</code> indicates how many characters can be read before a
     * mark is invalidated. Sending reset() will reposition the reader back to
     * the marked position provided
     * <code>readLimit</code> has not been surpassed.
     *
     * @param readlimit an int representing how many characters must be read
     * before invalidating the mark
     * @throws IOException If an error occurs attempting mark this
     * SequentialCharStream
     * @throws IllegalArgumentException If readlimit is < 0
     */
    @Override
    public void mark(int readlimit) throws IOException {
        if (readlimit < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            marklimit = readlimit;
            markpos = pos;
        }
    }

    /**
     * Answers a boolean indicating whether or not this Reader supports mark()
     * and reset(). This implementation answers
     * <code>true</code>.
     *
     * @return
     * <code>true</code> if mark() and reset() are supported,
     * <code>false</code> otherwise
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Reads a single character from this reader and returns the result as an
     * int. The 2 higher-order characters are set to 0. If the end of reader was
     * encountered then return -1. This implementation either returns a
     * character from the buffer or if there are no characters available, fill
     * the buffer then return a character or -1.
     *
     * @return the character read or -1 if end of reader.
     * @throws IOException If the SequentialCharStream is already closed or some other
     * IO error occurs.
     */
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            if (pos < count || fillbuf() != -1) {
                return buf[pos++];
            }
            return -1;
        }
    }

    /**
     * Reads at most
     * <code>length</code> characters from this SequentialCharStream and stores them
     * at
     * <code>offset</code> in the character array
     * <code>buffer</code>. Returns the number of characters actually read or -1
     * if the end of reader was encountered. If all the buffered characters have
     * been used, a mark has not been set, and the requested number of
     * characters is larger than this Readers buffer size, this implementation
     * bypasses the buffer and simply places the results directly into
     * <code>buffer</code>.
     *
     * @param buffer character array to store the read characters
     * @param offset offset in buf to store the read characters
     * @param length maximum number of characters to read
     * @return number of characters read or -1 if end of reader.
     * @throws IOException If the SequentialCharStream is already closed or some other
     * IO error occurs.
     */
    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            if (offset < 0 || offset > buffer.length - length || length < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (length == 0) {
                return 0;
            }
            int required;
            if (pos < count) {
                int copylength = count - pos >= length ? length : count - pos;
                System.arraycopy(buf, pos, buffer, offset, copylength);
                pos += copylength;
                if (copylength == length || !in.ready()) {
                    return copylength;
                }
                offset += copylength;
                required = length - copylength;
            } else {
                required = length;
            }
            while (true) {
                int read;
                if (markpos == -1 && required >= buf.length) {
                    read = in.read(buffer, offset, required);
                    if (read == -1) {
                        return required == length ? -1 : length - required;
                    }
                } else {
                    if (fillbuf() == -1) {
                        return required == length ? -1 : length - required;
                    }
                    read = count - pos >= required ? required : count - pos;
                    System.arraycopy(buf, pos, buffer, offset, read);
                    pos += read;
                }
                required -= read;
                if (required == 0) {
                    return length;
                }
                if (!in.ready()) {
                    return length - required;
                }
                offset += read;
            }
        }
    }

    /**
     * Answers a
     * <code>String</code> representing the next line of text available in this
     * SequentialCharStream. A line is represented by 0 or more characters followed by
     * <code>'\n'</code>,
     * <code>'\r'</code>,
     * <code>'\r\n'</code> or end of stream. The
     * <code>String</code> does not include the newline sequence.
     *
     * @return the contents of the line or null if no characters were read
     * before end of stream.
     * @throws IOException If the SequentialCharStream is already closed or some other
     * IO error occurs.
     */
    public String readLine() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            if ((pos >= count) && (fillbuf() == -1)) {
                return null;
            }
            for (int charPos = pos; charPos < count; charPos++) {
                char ch = buf[charPos];
                if (ch > '\r') {
                    continue;
                }
                if (ch == '\n') {
                    String res = new String(buf, pos, charPos - pos);
                    pos = charPos + 1;
                    return res;
                } else if (ch == '\r') {
                    String res = new String(buf, pos, charPos - pos);
                    pos = charPos + 1;
                    if (((pos < count) || (fillbuf() != -1))
                            && (buf[pos] == '\n')) {
                        pos++;
                    }
                    return res;
                }
            }
            char eol = '\0';
            StringBuilder result = new StringBuilder(64);
            result.append(buf, pos, count - pos);
            pos = count;
            while (true) {
                if (pos >= count) {
                    if (eol == '\n') {
                        return result.toString();
                    }
                    if (fillbuf() == -1) {
                        return result.length() > 0 || eol != '\0' ? result.toString() : null;
                    }
                }
                for (int charPos = pos; charPos < count; charPos++) {
                    if (eol == '\0') {
                        if ((buf[charPos] == '\n' || buf[charPos] == '\r')) {
                            eol = buf[charPos];
                        }
                    } else if (eol == '\r' && (buf[charPos] == '\n')) {
                        if (charPos > pos) {
                            result.append(buf, pos, charPos - pos - 1);
                        }
                        pos = charPos + 1;
                        return result.toString();
                    } else {
                        if (charPos > pos) {
                            result.append(buf, pos, charPos - pos - 1);
                        }
                        pos = charPos;
                        return result.toString();
                    }
                }
                if (eol == '\0') {
                    result.append(buf, pos, count - pos);
                } else {
                    result.append(buf, pos, count - pos - 1);
                }
                pos = count;
            }
        }

    }

    /**
     * Answers a
     * <code>boolean</code> indicating whether or not this Reader is ready to be
     * read without blocking. If the result is
     * <code>true</code>, the next
     * <code>read()</code> will not block. If the result is
     * <code>false</code> this Reader may or may not block when
     * <code>read()</code> is sent.
     *
     * @return
     * <code>true</code> if the receiver will not block when
     * <code>read()</code> is called,
     * <code>false</code> if unknown or blocking will occur.
     * @throws IOException If the SequentialCharStream is already closed or some other
     * IO error occurs.
     */
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(); //$NON-NLS-1$
            }
            return ((count - pos) > 0) || in.ready();
        }
    }

    /**
     * Reset this SequentialCharStream's position to the last
     * <code>mark()</code> location. Invocations of
     * <code>read()/skip()</code> will occur from this new location. If this
     * Reader was not marked, throw IOException.
     *
     * @throws IOException If a problem occurred, the receiver does not support
     * <code>mark()/reset()</code>, or no mark has been set.
     */
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            if (markpos == -1) {
                throw new IOException();
            }
            pos = markpos;
        }
    }

    /**
     * Skips
     * <code>amount</code> number of characters in this Reader. Subsequent
     * <code>read()</code>'s will not return these characters unless
     * <code>reset()</code> is used. Skipping characters may invalidate a mark
     * if marklimit is surpassed.
     *
     * @param amount the maximum number of characters to skip.
     * @return the number of characters actually skipped.
     * @throws IOException If the SequentialCharStream is already closed or some other
     * IO error occurs.
     * @throws IllegalArgumentException If amount is negative
     */
    @Override
    public long skip(long amount) throws IOException {
        if (amount < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            if (amount < 1) {
                return 0;
            }
            if (count - pos >= amount) {
                pos += amount;
                return amount;
            }

            long read = count - pos;
            pos = count;
            while (read < amount) {
                if (fillbuf() == -1) {
                    return read;
                }
                if (count - pos >= amount - read) {
                    pos += amount - read;
                    return amount;
                }
                read += (count - pos);
                pos = count;
            }
            return amount;
        }
    }

    @Override
    public Reader getReader() {
        return in;
    }

    @Override
    public Separable readField() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            if ((pos >= count) && (fillbuf() == -1)) {
                return null;
            }
            for (int charPos = pos; charPos < count; charPos++) {
                char ch = buf[charPos];
                if (isSeparator(ch)) {
                    String res = new String(buf, pos, charPos - pos);
                    if (listener != null) {
                        if (res.length() > 0) {
                            listener.data(res);
                        }
                        listener.mark(ch);
                    }
                    pos = charPos + 1;
                    switch (ch) {
                        case FieldSeparator.US : return new UnitField(res);
                        case FieldSeparator.GS : return new GroupField(res);
                        case FieldSeparator.RS : return new RecordField(res);
                        case FieldSeparator.FS : return new FileField(res);
                        default: // custom separator, like \n
                            return new RecordField(res);
                    }
                }
            }
            char eod = '\0';
            StringBuilder result = new StringBuilder(256);
            result.append(buf, pos, count - pos);
            pos = count;
            while (true) {
                if (isSeparator(eod)) {
                    String s = result.toString();
                    if (listener != null) {
                        listener.data(s);
                    }
                    switch (eod) {
                        case FieldSeparator.US:
                            return new UnitField(s);
                        case FieldSeparator.GS:
                            return new GroupField(s);
                        case FieldSeparator.RS:
                            return new RecordField(s);
                        case FieldSeparator.FS:
                            return new FileField(s);
                    }
                    throw new IOException();
                }
                if (fillbuf() == -1) {
                    if (result.length() > 0 || eod != '\0') {
                        String s = result.toString();
                        if (listener != null) {
                            listener.data(s);
                        }
                        switch (eod) {
                            case FieldSeparator.US:
                                return new UnitField(s);
                            case FieldSeparator.GS:
                                return new GroupField(s);
                            case FieldSeparator.RS:
                                return new RecordField(s);
                            case FieldSeparator.FS:
                                return new FileField(s);
                        }
                        return new FileField(s);
                    } else {
                        return null;
                    }
                }
                for (int charPos = pos; charPos < count; charPos++) {
                    if (eod == '\0') {
                        if (isSeparator(buf[charPos])) {
                            eod = buf[charPos];
                        }
                    } else {
                        if (charPos > pos) {
                            result.append(buf, pos, charPos - pos - 1);
                        }
                        pos = charPos;
                        String s = result.toString();
                        if (listener != null) {
                            listener.data(s);
                            listener.mark(eod);
                        }
                        switch (eod) {
                            case FieldSeparator.US : return new UnitField(s);
                            case FieldSeparator.GS : return new GroupField(s);
                            case FieldSeparator.RS : return new RecordField(s);
                            case FieldSeparator.FS : return new FileField(s);
                        }
                    }
                }
                if (eod == '\0') {
                    result.append(buf, pos, count - pos);
                } else {
                    result.append(buf, pos, count - pos - 1);
                }
                pos = count;
            }
        }
    }

    private int fillbuf() throws IOException {
        if (markpos == -1 || (pos - markpos >= marklimit)) {
            int result = in.read(buf, 0, buf.length);
            if (result > 0) {
                markpos = -1;
                pos = 0;
                count = result;
            }
            return result;
        }
        if (markpos == 0 && marklimit > buf.length) {
            int newLength = buf.length * 2;
            if (newLength > marklimit) {
                newLength = marklimit;
            }
            char[] newbuf = new char[newLength];
            System.arraycopy(buf, 0, newbuf, 0, buf.length);
            buf = newbuf;
        } else if (markpos > 0) {
            System.arraycopy(buf, markpos, buf, 0, buf.length - markpos);
        }
        pos -= markpos;
        count = markpos = 0;
        int charsread = in.read(buf, pos, buf.length - pos);
        count = charsread == -1 ? pos : pos + charsread;
        return charsread;
    }

    /**
     * Answer a boolean indicating whether or not this SequentialCharStream is closed.
     *
     * @return
     * <code>true</code> if this reader is closed,
     * <code>false</code> otherwise
     */
    private boolean isClosed() {
        return buf == null;
    }

    /**
     * Test for separator character.
     * @param ch the character to test
     * @return true if sperator
     */
    protected boolean isSeparator(char ch) {
        return ch == FieldSeparator.FS || ch == FieldSeparator.GS || ch == FieldSeparator.RS || ch == FieldSeparator.US;
    }

    public void setListener(FieldListener listener) {
        this.listener = listener;
    }

    public Stream<Separable> fields() {
        Iterator<Separable> iter = new Iterator<Separable>() {
            Separable nextData = null;

            @Override
            public boolean hasNext() {
                if (nextData != null) {
                    return true;
                } else {
                    try {
                        nextData = readField();
                        return (nextData != null);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            @Override
            public Separable next() {
                if (nextData != null || hasNext()) {
                    Separable data = nextData;
                    nextData = null;
                    return data;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iter, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }
}