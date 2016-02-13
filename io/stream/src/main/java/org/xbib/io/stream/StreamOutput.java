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

import org.xbib.io.BytesReference;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class StreamOutput extends OutputStream {

    public boolean seekPositionSupported() {
        return false;
    }

    public long position() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void seek(long position) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Writes a single byte.
     */
    public abstract void writeByte(byte b) throws IOException;

    /**
     * Writes an array of bytes.
     *
     * @param b the bytes to write
     */
    public void writeBytes(byte[] b) throws IOException {
        writeBytes(b, 0, b.length);
    }

    /**
     * Writes an array of bytes.
     *
     * @param b      the bytes to write
     * @param length the number of bytes to write
     */
    public void writeBytes(byte[] b, int length) throws IOException {
        writeBytes(b, 0, length);
    }

    /**
     * Writes an array of bytes.
     *
     * @param b      the bytes to write
     * @param offset the offset in the byte array
     * @param length the number of bytes to write
     */
    public abstract void writeBytes(byte[] b, int offset, int length) throws IOException;

    /**
     * Writes the bytes reference, including a length header.
     */
    public void writeBytesReference(BytesReference bytes) throws IOException {
        if (bytes == null) {
            writeVInt(0);
            return;
        }
        writeVInt(bytes.length());
        bytes.writeTo(this);
    }

    public final void writeShort(short v) throws IOException {
        writeByte((byte) (v >> 8));
        writeByte((byte) v);
    }

    /**
     * Writes an int as four bytes.
     */
    public void writeInt(int i) throws IOException {
        writeByte((byte) (i >> 24));
        writeByte((byte) (i >> 16));
        writeByte((byte) (i >> 8));
        writeByte((byte) i);
    }

    /**
     * Writes an int in a variable-length format.  Writes between one and
     * five bytes.  Smaller values take fewer bytes.  Negative numbers are not
     * supported.
     */
    public void writeVInt(int i) throws IOException {
        while ((i & ~0x7F) != 0) {
            writeByte((byte) ((i & 0x7f) | 0x80));
            i >>>= 7;
        }
        writeByte((byte) i);
    }

    /**
     * Writes a long as eight bytes.
     */
    public void writeLong(long i) throws IOException {
        writeInt((int) (i >> 32));
        writeInt((int) i);
    }

    /**
     * Writes an long in a variable-length format.  Writes between one and five
     * bytes.  Smaller values take fewer bytes.  Negative numbers are not
     * supported.
     */
    public void writeVLong(long i) throws IOException {
        while ((i & ~0x7F) != 0) {
            writeByte((byte) ((i & 0x7f) | 0x80));
            i >>>= 7;
        }
        writeByte((byte) i);
    }

    public void writeOptionalString(String str) throws IOException {
        if (str == null) {
            writeBoolean(false);
        } else {
            writeBoolean(true);
            writeString(str);
        }
    }

    public void writeString(String str) throws IOException {
        int charCount = str.length();
        writeVInt(charCount);
        int c;
        for (int i = 0; i < charCount; i++) {
            c = str.charAt(i);
            if (c <= 0x007F) {
                writeByte((byte) c);
            } else if (c > 0x07FF) {
                writeByte((byte) (0xE0 | c >> 12 & 0x0F));
                writeByte((byte) (0x80 | c >> 6 & 0x3F));
                writeByte((byte) (0x80 | c & 0x3F));
            } else {
                writeByte((byte) (0xC0 | c >> 6 & 0x1F));
                writeByte((byte) (0x80 | c & 0x3F));
            }
        }
    }

    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    /**
     * Writes a boolean.
     */
    public void writeBoolean(boolean b) throws IOException {
        byte ZERO = 0;
        byte ONE = 1;
        writeByte(b ? ONE : ZERO);
    }

    /**
     * Forces any buffered output to be written.
     */
    public abstract void flush() throws IOException;

    /**
     * Closes this stream to further operations.
     */
    public abstract void close() throws IOException;

    public abstract void reset() throws IOException;

    @Override
    public void write(int b) throws IOException {
        writeByte((byte) b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        writeBytes(b, off, len);
    }

    public void writeStringArray(String[] array) throws IOException {
        writeVInt(array.length);
        for (String s : array) {
            writeString(s);
        }
    }

    /**
     * Writes a string array, for nullable string, writes it as 0 (empty string).
     */
    public void writeStringArrayNullable(String[] array) throws IOException {
        if (array == null) {
            writeVInt(0);
        } else {
            writeVInt(array.length);
            for (String s : array) {
                writeString(s);
            }
        }
    }

    public void writeMap(Map<String, Object> map) throws IOException {
        writeGenericValue(map);
    }

    @SuppressWarnings("unchecked")
    public void writeGenericValue(Object value) throws IOException {
        if (value == null) {
            writeByte((byte) -1);
            return;
        }
        Class type = value.getClass();
        if (type == String.class) {
            writeByte((byte) 0);
            writeString((String) value);
        } else if (type == Integer.class) {
            writeByte((byte) 1);
            writeInt((Integer) value);
        } else if (type == Long.class) {
            writeByte((byte) 2);
            writeLong((Long) value);
        } else if (type == Float.class) {
            writeByte((byte) 3);
            writeFloat((Float) value);
        } else if (type == Double.class) {
            writeByte((byte) 4);
            writeDouble((Double) value);
        } else if (type == Boolean.class) {
            writeByte((byte) 5);
            writeBoolean((Boolean) value);
        } else if (type == byte[].class) {
            writeByte((byte) 6);
            writeVInt(((byte[]) value).length);
            writeBytes(((byte[]) value));
        } else if (value instanceof List) {
            writeByte((byte) 7);
            List list = (List) value;
            writeVInt(list.size());
            for (Object o : list) {
                writeGenericValue(o);
            }
        } else if (value instanceof Object[]) {
            writeByte((byte) 8);
            Object[] list = (Object[]) value;
            writeVInt(list.length);
            for (Object o : list) {
                writeGenericValue(o);
            }
        } else if (value instanceof Map) {
            if (value instanceof LinkedHashMap) {
                writeByte((byte) 9);
            } else {
                writeByte((byte) 10);
            }
            Map<String, Object> map = (Map<String, Object>) value;
            writeVInt(map.size());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                writeString(entry.getKey());
                writeGenericValue(entry.getValue());
            }
        } else if (type == Byte.class) {
            writeByte((byte) 11);
            writeByte((Byte) value);
        } else if (type == Instant.class) {
            writeByte((byte) 12);
            writeLong(((Instant)value).toEpochMilli());
        } else if (value instanceof BytesReference) {
            writeByte((byte) 14);
            writeBytesReference((BytesReference) value);
        } else {
            throw new IOException("Can't write type [" + type + "]");
        }
    }
}
