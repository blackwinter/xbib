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

import org.xbib.io.stream.BytesStreamInput;
import org.xbib.io.stream.StreamInput;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

public class BytesArray implements BytesReference {

    public static final byte[] EMPTY_ARRAY = new byte[0];

    public static final BytesArray EMPTY = new BytesArray(EMPTY_ARRAY, 0, 0);

    public static final Charset UTF8 = Charset.forName("UTF-8");

    protected byte[] bytes;

    protected int offset;

    protected int length;

    public BytesArray(String bytes) {
        this(toBytes(bytes));
    }
    
    private static byte[] toBytes(String bytes) {
        return bytes.getBytes(UTF8);
    }

    public BytesArray(byte[] bytes) {
        this.bytes = bytes;
        this.offset = 0;
        this.length = bytes.length;
    }

    public BytesArray(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    
    public byte get(int index) {
        return bytes[offset + index];
    }

    
    public int length() {
        return length;
    }

    
    public BytesReference slice(int from, int length) {
        if (from < 0 || (from + length) > this.length) {
            throw new IllegalArgumentException("can't slice a buffer with length [" + this.length + "], with slice parameters from [" + from + "], length [" + length + "]");
        }
        return new BytesArray(bytes, offset + from, length);
    }

    public StreamInput streamInput() {
        return new BytesStreamInput(bytes, offset, length, false);
    }

    public void writeTo(OutputStream os) throws IOException {
        os.write(bytes, offset, length);
    }

    public byte[] toBytes() {
        if (offset == 0 && bytes.length == length) {
            return bytes;
        }
        return Arrays.copyOfRange(bytes, offset, offset + length);
    }

    
    public BytesArray toBytesArray() {
        return this;
    }

    
    public BytesArray copyBytesArray() {
        return new BytesArray(Arrays.copyOfRange(bytes, offset, offset + length));
    }

    
    public boolean hasArray() {
        return true;
    }

    
    public byte[] array() {
        return bytes;
    }

    
    public int arrayOffset() {
        return offset;
    }

    
    public String toUtf8() {
        if (length == 0) {
            return "";
        }
        return new String(bytes, offset, length, UTF8);
    }

    
    public boolean equals(Object obj) {
        return bytesEquals((BytesArray) obj);
    }

    public boolean bytesEquals(BytesArray other) {
        if (length == other.length) {
            int otherUpto = other.offset;
            final byte[] otherBytes = other.bytes;
            final int end = offset + length;
            for (int upto = offset; upto < end; upto++, otherUpto++) {
                if (bytes[upto] != otherBytes[otherUpto]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    
    public int hashCode() {
        int result = 0;
        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            result = 31 * result + bytes[i];
        }
        return result;
    }


    /**
     * Returns an array size >= minTargetSize, generally
     * over-allocating exponentially to achieve amortized
     * linear-time cost as the array grows.
     * <p/>
     * NOTE: this was originally borrowed from Python 2.4.2
     * listobject.c sources (attribution in LICENSE.txt), but
     * has now been substantially changed based on
     * discussions from java-dev thread with subject "Dynamic
     * array reallocation algorithms", started on Jan 12
     * 2010.
     *
     * @param minTargetSize   Minimum required value to be returned.
     * @param bytesPerElement Bytes used by each element of
     *                        the array.
     */

    public static int oversize(int minTargetSize, int bytesPerElement) {

        if (minTargetSize < 0) {
            // catch usage that accidentally overflows int
            throw new IllegalArgumentException("invalid array size " + minTargetSize);
        }

        if (minTargetSize == 0) {
            // wait until at least one element is requested
            return 0;
        }

        // asymptotic exponential growth by 1/8th, favors
        // spending a bit more CPU to not tie up too much wasted
        // RAM:
        int extra = minTargetSize >> 3;

        if (extra < 3) {
            // for very small arrays, where constant overhead of
            // realloc is presumably relatively high, we grow
            // faster
            extra = 3;
        }

        int newSize = minTargetSize + extra;

        // add 7 to allow for worst case byte alignment addition below:
        if (newSize + 7 < 0) {
            // int overflowed -- return max allowed array size
            return Integer.MAX_VALUE;
        }

        if (JRE_IS_64BIT) {
            // round up to 8 byte alignment in 64bit env
            switch (bytesPerElement) {
                case 8:
                    // no rounding
                    return newSize;
                case 4:
                    // round up to multiple of 2
                    return (newSize + 1) & 0x7ffffffe;
                case 2:
                    // round up to multiple of 4
                    return (newSize + 3) & 0x7ffffffc;
                case 1:
                    // round up to multiple of 8
                    return (newSize + 7) & 0x7ffffff8;
                default:
                    // odd (invalid?) size
                    return newSize;
            }
        } else {
            // round up to 4 byte alignment in 64bit env
            switch (bytesPerElement) {
                case 8:
                case 4:
                    // no rounding
                    return newSize;
                case 2:
                    // round up to multiple of 2
                    return (newSize + 1) & 0x7ffffffe;
                case 1:
                    // round up to multiple of 4
                    return (newSize + 3) & 0x7ffffffc;
                default:
                    // odd (invalid?) size
                    return newSize;
            }
        }
    }

    public static final boolean JRE_IS_64BIT;
    static {
        String oa_arch = System.getProperty("os.arch");
        String sun_arch = System.getProperty("sun.arch.data.model");
        if (sun_arch != null) {
            JRE_IS_64BIT = sun_arch.contains("64");
        } else {
            JRE_IS_64BIT = oa_arch != null && oa_arch.contains("64");
        }
    }

}