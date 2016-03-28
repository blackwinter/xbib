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
package org.xbib.io.stream;

import org.xbib.io.BytesArray;
import org.xbib.io.BytesReference;

import java.io.EOFException;
import java.io.IOException;

public class BytesStreamInput extends StreamInput {

    private final boolean unsafe;
    protected byte buf[];
    protected int pos;
    protected int count;

    public BytesStreamInput(BytesReference bytes) {
        if (!bytes.hasArray()) {
            bytes = bytes.toBytesArray();
        }
        this.buf = bytes.array();
        this.pos = bytes.arrayOffset();
        this.count = bytes.length();
        this.unsafe = false;
    }

    public BytesStreamInput(byte buf[], boolean unsafe) {
        this(buf, 0, buf.length, unsafe);
    }

    public BytesStreamInput(byte buf[], int offset, int length, boolean unsafe) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.unsafe = unsafe;
    }

    @Override
    public BytesReference readBytesReference(int length) throws IOException {
        if (unsafe) {
            return super.readBytesReference(length);
        }
        BytesArray bytes = new BytesArray(buf, pos, length);
        pos += length;
        return bytes;
    }

    @Override
    public long skip(long n) throws IOException {
        if (pos + n > count) {
            n = count - pos;
        }
        if (n < 0) {
            return 0;
        }
        pos += n;
        return n;
    }

    public int position() {
        return this.pos;
    }

    @Override
    public int read() throws IOException {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
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

    public byte[] underlyingBuffer() {
        return buf;
    }

    @Override
    public byte readByte() throws IOException {
        if (pos >= count) {
            throw new EOFException();
        }
        return buf[pos++];
    }

    @Override
    public void readBytes(byte[] b, int offset, int len) throws IOException {
        if (len == 0) {
            return;
        }
        if (pos >= count) {
            throw new EOFException();
        }
        if (pos + len > count) {
            len = count - pos;
        }
        if (len <= 0) {
            throw new EOFException();
        }
        System.arraycopy(buf, pos, b, offset, len);
        pos += len;
    }

    @Override
    public void reset() throws IOException {
        pos = 0;
    }

    @Override
    public void close() throws IOException {
        // nothing to do here...
    }
}
