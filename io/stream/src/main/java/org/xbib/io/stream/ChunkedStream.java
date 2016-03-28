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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.LinkedList;
import java.util.Queue;

/**
 * An in-memory buffer that provides OutputStream and InputStream interfaces.
 * <p/>
 * This is more efficient than using ByteArrayOutputStream/ByteArrayInputStream
 * <p/>
 * This implementation usese LinkedList and is not thread safe.
 */
public class ChunkedStream implements Appendable {

    protected static final int DEFAULT_CHUNK_SIZE = 4096;

    private static final char[] EMPTY_CHAR_ARRAY = new char[0];

    protected final Charset charset;

    protected final Queue<StreamChunk> chunks;

    protected StreamChunk currentWriteChunk;

    protected StreamChunk currentReadChunk;

    protected int chunkSize;

    protected StreamOutputStream output;

    protected StreamInputStream input;

    protected Appendable appendable;

    protected int totalBytesUnreadInList;

    protected volatile boolean closed;

    public ChunkedStream() {
        this(DEFAULT_CHUNK_SIZE);
    }

    public ChunkedStream(int chunkSize) {
        this(new LinkedList<StreamChunk>(), chunkSize, Charset.forName("UTF-8"));
    }

    public ChunkedStream(Queue<StreamChunk> list, int chunkSize, Charset encoding) {
        this.charset = encoding;
        this.chunks = list;
        this.chunkSize = chunkSize;
        this.currentWriteChunk = new StreamChunk(chunkSize);
        this.currentReadChunk = null;
        this.output = new StreamOutputStream();
        this.input = new StreamInputStream();
        this.appendable = new OutputStreamWriter(output, charset);
        this.totalBytesUnreadInList = 0;
    }

    public OutputStream getOutputStream() {
        return output;
    }

    public InputStream getInputStream() {
        return input;
    }

    public Appendable getAppendable() {
        return appendable;
    }

    public void writeTo(OutputStream target) throws IOException {
        if (closed) {
            return;
        }
        while (prepareRead() != -1) {
            currentReadChunk.writeTo(target);
        }
    }

    public int available() throws IOException {
        return input.available();
    }

    public byte[] readAsByteArray() throws IOException {
        byte[] buf = new byte[totalBytesUnread()];
        int n = input.read(buf, 0, buf.length);
        return buf;
    }

    public String readAsString() throws IOException {
        int unreadSize = totalBytesUnread();
        if (unreadSize > 0) {
            CharsetDecoder decoder = charset.newDecoder().onMalformedInput(
                    CodingErrorAction.REPLACE).onUnmappableCharacter(
                    CodingErrorAction.REPLACE);
            CharBuffer charbuffer = CharBuffer.allocate(unreadSize);
            ByteBuffer buf = null;
            while (prepareRead() != -1) {
                buf = currentReadChunk.readToNioBuffer();
                boolean endOfInput = (prepareRead() == -1);
                CoderResult result = decoder.decode(buf, charbuffer, endOfInput);
                if (endOfInput) {
                    if (!result.isUnderflow()) {
                        result.throwException();
                    }
                }
            }
            CoderResult result = decoder.flush(charbuffer);
            if (buf != null && buf.hasRemaining()) {
                throw new IllegalStateException("There's a bug here, buffer wasn't read fully.");
            }
            if (!result.isUnderflow()) {
                result.throwException();
            }
            charbuffer.flip();
            String str;
            if (charbuffer.hasArray()) {
                int len = charbuffer.remaining();
                char[] ch = charbuffer.array();
                if (len != ch.length) {
                    ch = subarray(ch, 0, len);
                }
                str = new String(ch);
            } else {
                str = charbuffer.toString();
            }
            return str;
        }
        return null;
    }

    public synchronized void closeProduction() throws IOException {
        if (!closed) {
            output.close();
            closed = true;
            if (currentWriteChunk.bytesUnread() > 0) {
                chunks.offer(currentWriteChunk);
                chunks.offer(null); // poison
            }
        }
    }

    public synchronized void closeConsumption() throws IOException {
        input.close();
    }

    protected void produce(StreamChunk chunk) throws IOException {
        if (closed) {
            throw new IOException("stream closed");
        }
        chunks.offer(chunk);
    }

    protected StreamChunk consume() throws IOException {
        return chunks.poll();
    }

    private char[] subarray(char[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        if (startIndexInclusive < 0) {
            startIndexInclusive = 0;
        }
        if (endIndexExclusive > array.length) {
            endIndexExclusive = array.length;
        }
        int newSize = endIndexExclusive - startIndexInclusive;
        if (newSize <= 0) {
            return EMPTY_CHAR_ARRAY;
        }
        char[] subarray = new char[newSize];
        System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
        return subarray;
    }

    public int totalBytesUnread() {
        int total = totalBytesUnreadInList;
        if (currentReadChunk != null) {
            total += currentReadChunk.bytesUnread();
        }
        if (currentWriteChunk != currentReadChunk && currentWriteChunk != null) {
            total += currentWriteChunk.bytesUnread();
        }
        return total;
    }

    protected int allocateSpace() throws IOException {
        int spaceLeft = currentWriteChunk.spaceLeft();
        if (spaceLeft == 0) {
            produce(currentWriteChunk);
            totalBytesUnreadInList += currentWriteChunk.bytesUnread();
            currentWriteChunk = new StreamChunk(chunkSize);
            spaceLeft = currentWriteChunk.spaceLeft();
        }
        return spaceLeft;
    }

    protected int prepareRead() throws IOException {
        int bytesUnread = (currentReadChunk != null) ? currentReadChunk.bytesUnread() : 0;
        if (bytesUnread == 0) {
            if (!chunks.isEmpty()) {
                currentReadChunk = consume();
                bytesUnread = currentReadChunk.bytesUnread();
                totalBytesUnreadInList -= bytesUnread;
            } else if (currentReadChunk != currentWriteChunk) {
                currentReadChunk = currentWriteChunk;
                bytesUnread = currentReadChunk.bytesUnread();
            } else {
                bytesUnread = -1;
            }
        }
        return bytesUnread;
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        if (closed) {
            throw new IOException("stream closed");
        }
        return appendable.append(csq);
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        if (closed) {
            throw new IOException("stream closed");
        }
        return appendable.append(csq, start, end);
    }

    @Override
    public Appendable append(char c) throws IOException {
        if (closed) {
            throw new IOException("stream closed");
        }
        return appendable.append(c);
    }

    public class StreamChunk {

        private final int size;
        private byte[] buffer;
        private int pointer = 0;
        private int used = 0;

        public StreamChunk(int size) {
            this.size = size;
            buffer = new byte[size];
        }

        ByteBuffer readToNioBuffer() {
            if (pointer < used) {
                ByteBuffer result;
                if (pointer > 0 || used < size) {
                    result = ByteBuffer.wrap(buffer, pointer, used - pointer);
                } else {
                    result = ByteBuffer.wrap(buffer);
                }
                pointer = used;
                return result;
            }
            return null;
        }

        public boolean write(byte b) {
            if (used < size) {
                buffer[used++] = b;
                return true;
            }
            return false;
        }

        public void write(byte[] b, int off, int len) {
            System.arraycopy(b, off, buffer, used, len);
            used = used + len;
        }

        public void read(byte[] b, int off, int len) {
            System.arraycopy(buffer, pointer, b, off, len);
            pointer = pointer + len;
        }

        public void writeTo(OutputStream target) throws IOException {
            if (pointer < used) {
                target.write(buffer, pointer, used - pointer);
                pointer = used;
            }
        }

        public void reset() {
            pointer = 0;
        }

        public int bytesUnread() {
            return used - pointer;
        }

        public int read() {
            if (pointer < used) {
                return buffer[pointer++] & 0xff;
            }
            return -1;
        }

        public int spaceLeft() {
            return size - used;
        }
    }

    class StreamOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            if (closed) {
                throw new IOException("stream closed");
            }
            allocateSpace();
            currentWriteChunk.write((byte) b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (closed) {
                throw new IOException("stream closed");
            }
            if ((off < 0) || (off > b.length) || (len < 0)
                    || ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return;
            }
            int bytesLeft = len;
            int currentOffset = off;
            while (bytesLeft > 0) {
                int spaceLeft = allocateSpace();
                int writeBytes = Math.min(spaceLeft, bytesLeft);
                currentWriteChunk.write(b, currentOffset, writeBytes);
                bytesLeft -= writeBytes;
                currentOffset += writeBytes;
            }
        }

        public ChunkedStream getBuffer() {
            return ChunkedStream.this;
        }
    }

    class StreamInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            prepareRead();
            return currentReadChunk.read();
        }

        @Override
        public int available() throws IOException {
            return totalBytesUnread();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if ((off < 0) || (off > b.length) || (len < 0)
                    || ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return 0;
            }
            int bytesLeft = len;
            int currentOffset = off;
            int bytesUnread = prepareRead();
            int totalBytesRead = 0;
            while (bytesLeft > 0 && bytesUnread != -1) {
                int readBytes = Math.min(bytesUnread, bytesLeft);
                currentReadChunk.read(b, currentOffset, readBytes);
                bytesLeft -= readBytes;
                currentOffset += readBytes;
                totalBytesRead += readBytes;
                bytesUnread = prepareRead();
            }
            if (totalBytesRead > 0) {
                return totalBytesRead;
            }
            return -1;
        }

        public ChunkedStream getBuffer() {
            return ChunkedStream.this;
        }
    }
}