
package org.xbib.template.handlebars.internal;

import java.io.IOException;
import java.io.Writer;

/**
 * A string writer without locking.
 */
class FastStringWriter extends Writer {
    /**
     * The internal buffer.
     */
    private final StringBuilder buffer = new StringBuilder();

    @Override
    public void write(final char[] buffer) throws IOException {
        this.buffer.append(buffer);
    }

    @Override
    public void write(final int c) throws IOException {
        this.buffer.append((char) c);
    }

    @Override
    public void write(final String str) throws IOException {
        this.buffer.append(str);
    }

    @Override
    public void write(final String str, final int off, final int len)
            throws IOException {
        buffer.append(str, off, off + len);
    }

    @Override
    public void write(final char[] buffer, final int off, final int len)
            throws IOException {
        if (off < 0 || off > buffer.length || len < 0
                || off + len > buffer.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        this.buffer.append(buffer, off, len);
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
        buffer.delete(0, buffer.length());
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

}
