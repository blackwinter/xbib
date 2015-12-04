
package org.xbib.graphics.barcode.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

/**
 * Utility functions for I/O operations.
 */
public class IOUtil {

    /**
     * Copies the contents of an InputStream to an OutputStream.
     * @param in the input stream
     * @param out the output stream
     * @throws IOException if an I/O error occurs
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int n = 0;
        while (-1 != (n = in.read(buf))) {
            out.write(buf, 0, n);
        }
    }

    /**
     * Closes an {@link InputStream}. It ignores any exceptions happening while closing the
     * stream.
     * @param in the input stream
     */
    public static void closeQuietly(InputStream in) {
        try {
            in.close();
        } catch (IOException ioe) {
            //ignore
        }
    }

    /**
     * Closes a {@link Reader}. It ignores any exceptions happening while closing the
     * stream.
     * @param reader the reader
     */
    public static void closeQuietly(Reader reader) {
        try {
            reader.close();
        } catch (IOException ioe) {
            //ignore
        }
    }

}
