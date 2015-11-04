package org.xbib.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * The <code>ByteOrderMarkInputStream</code> class wraps an
 * <code>InputStream</code> and detects the presence of a Unicode BOM
 * (Byte Order Mark) at its beginning, as defined by
 * <a href="http://www.faqs.org/rfcs/rfc3629.html">RFC 3629 - UTF-8, a
 * transformation format of ISO 10646</a>
 *
 * The
 * <a href="http://www.unicode.org/unicode/faq/utf_bom.html">Unicode FAQ</a>
 * defines 5 types of BOMs:<ul>
 * <li><pre>00 00 FE FF  = UTF-32, big-endian</pre></li>
 * <li><pre>FF FE 00 00  = UTF-32, little-endian</pre></li>
 * <li><pre>FE FF        = UTF-16, big-endian</pre></li>
 * <li><pre>FF FE        = UTF-16, little-endian</pre></li>
 * <li><pre>EF BB BF     = UTF-8</pre></li>
 * </ul>
 *
 * <p>Use the {@link #getBOM()} method to know whether a BOM has been detected
 * or not.
 * </p>
 * <p>Use the {@link #skipBOM()} method to remove the detected BOM from the
 * wrapped <code>InputStream</code> object.</p>
 */
public class ByteOrderMarkInputStream extends InputStream {

    private final PushbackInputStream in;
    private final ByteOrderMark byteOrderMark;
    private boolean skipped;

    /**
     * Constructs a new <code>UnicodeBOMInputStream</code> that wraps the
     * specified <code>InputStream</code>.
     *
     * @param inputStream an <code>InputStream</code>.
     * @throws IOException          on reading from the specified <code>InputStream</code>
     *                              when trying to detect the Unicode BOM.
     */
    public ByteOrderMarkInputStream(final InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null");
        }
        in = new PushbackInputStream(inputStream, 4);
        final byte bom[] = new byte[4];
        final int read = in.read(bom);
        switch (read) {
            case 4:
                if ((bom[0] == (byte) 0xFF) &&
                        (bom[1] == (byte) 0xFE) &&
                        (bom[2] == (byte) 0x00) &&
                        (bom[3] == (byte) 0x00)) {
                    this.byteOrderMark = ByteOrderMark.UTF_32_LE;
                    break;
                } else if ((bom[0] == (byte) 0x00) &&
                        (bom[1] == (byte) 0x00) &&
                        (bom[2] == (byte) 0xFE) &&
                        (bom[3] == (byte) 0xFF)) {
                    this.byteOrderMark = ByteOrderMark.UTF_32_BE;
                    break;
                }

            case 3:
                if ((bom[0] == (byte) 0xEF) &&
                        (bom[1] == (byte) 0xBB) &&
                        (bom[2] == (byte) 0xBF)) {
                    this.byteOrderMark = ByteOrderMark.UTF_8;
                    break;
                }

            case 2:
                if ((bom[0] == (byte) 0xFF) &&
                        (bom[1] == (byte) 0xFE)) {
                    this.byteOrderMark = ByteOrderMark.UTF_16_LE;
                    break;
                } else if ((bom[0] == (byte) 0xFE) &&
                        (bom[1] == (byte) 0xFF)) {
                    this.byteOrderMark = ByteOrderMark.UTF_16_BE;
                    break;
                }
            default:
                this.byteOrderMark = ByteOrderMark.NONE;
                break;
        }
        if (read > 0) {
            in.unread(bom, 0, read);
        }
    }

    /**
     * Returns the <code>BOM</code> that was detected in the wrapped
     * <code>InputStream</code> object.
     *
     * @return a <code>BOM</code> value.
     */
    public ByteOrderMark getBOM() {
        return byteOrderMark;
    }

    /**
     * Skips the <code>BOM</code> that was found in the wrapped
     * <code>InputStream</code> object.
     *
     * @return this <code>UnicodeBOMInputStream</code>.
     * @throws IOException when trying to skip the BOM from the wrapped
     *                     <code>InputStream</code> object.
     */
    public ByteOrderMarkInputStream skipBOM() throws IOException {
        if (!skipped) {
            in.skip(byteOrderMark.bytes.length);
            skipped = true;
        }
        return this;
    }

    public int read() throws IOException {
        return in.read();
    }

    public int read(final byte b[]) throws IOException {
        return in.read(b, 0, b.length);
    }

    public int read(final byte b[], final int off, final int len) throws IOException,
            NullPointerException {
        return in.read(b, off, len);
    }

    public long skip(final long n) throws IOException {
        return in.skip(n);
    }

    public int available() throws IOException {
        return in.available();
    }

    public void close() throws IOException {
        in.close();
    }

    public void mark(final int readlimit) {
        in.mark(readlimit);
    }

    public void reset() throws IOException {
        in.reset();
    }

    public boolean markSupported() {
        return in.markSupported();
    }

    public static class ByteOrderMark {
        /**
         * NONE.
         */
        public static final ByteOrderMark NONE = new ByteOrderMark(new byte[]{}, "NONE");

        /**
         * UTF-8 BOM (EF BB BF).
         */
        public static final ByteOrderMark UTF_8 = new ByteOrderMark(new byte[]{(byte) 0xEF,
                (byte) 0xBB,
                (byte) 0xBF},
                "UTF-8");

        /**
         * UTF-16, little-endian (FF FE).
         */
        public static final ByteOrderMark UTF_16_LE = new ByteOrderMark(new byte[]{(byte) 0xFF,
                (byte) 0xFE},
                "UTF-16 little-endian");

        /**
         * UTF-16, big-endian (FE FF).
         */
        public static final ByteOrderMark UTF_16_BE = new ByteOrderMark(new byte[]{(byte) 0xFE,
                (byte) 0xFF},
                "UTF-16 big-endian");

        /**
         * UTF-32, little-endian (FF FE 00 00).
         */
        public static final ByteOrderMark UTF_32_LE = new ByteOrderMark(new byte[]{(byte) 0xFF,
                (byte) 0xFE,
                (byte) 0x00,
                (byte) 0x00},
                "UTF-32 little-endian");

        /**
         * UTF-32, big-endian (00 00 FE FF).
         */
        public static final ByteOrderMark UTF_32_BE = new ByteOrderMark(new byte[]{(byte) 0x00,
                (byte) 0x00,
                (byte) 0xFE,
                (byte) 0xFF},
                "UTF-32 big-endian");

        final byte bytes[];

        private final String description;

        private ByteOrderMark(final byte bom[], final String description) {
            this.bytes = bom;
            this.description = description;
        }

        /**
         * Returns a <code>String</code> representation of this <code>BOM</code>
         * value.
         */
        public final String toString() {
            return description;
        }

    }

}