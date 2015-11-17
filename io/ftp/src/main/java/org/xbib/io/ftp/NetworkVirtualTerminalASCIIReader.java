
package org.xbib.io.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * This is an NVT-ASCII character stream reader.
 */
class NetworkVirtualTerminalASCIIReader extends Reader {

    /**
     * This system line separator chars sequence.
     */
    private static final String SYSTEM_LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * The wrapped stream.
     */
    private InputStream stream;

    /**
     * The underlying reader.
     */
    private Reader reader;

    /**
     * Builds the reader.
     *
     * @param stream      The underlying stream.
     * @param charsetName The name of a supported charset.
     * @throws IOException If an I/O error occurs.
     */
    public NetworkVirtualTerminalASCIIReader(InputStream stream, String charsetName)
            throws IOException {
        this.stream = stream;
        reader = new InputStreamReader(stream, charsetName);
    }

    public void close() throws IOException {
        synchronized (this) {
            reader.close();
        }
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        synchronized (this) {
            return reader.read(cbuf, off, len);
        }
    }

    /**
     * Changes the current charset.
     *
     * @param charsetName The new charset.
     * @throws IOException If I/O error occurs.
     */
    public void changeCharset(String charsetName) throws IOException {
        synchronized (this) {
            reader = new InputStreamReader(stream, charsetName);
        }
    }

    /**
     * Reads a line from the stream.
     *
     * @return The line read, or null if the end of the stream is reached.
     * @throws IOException If an I/O error occurs.
     */
    public String readLine() throws IOException {
        StringBuilder buffer = new StringBuilder();
        int previous = -1;
        int current = -1;
        do {
            int i = reader.read();
            if (i == -1) {
                if (buffer.length() == 0) {
                    return null;
                } else {
                    return buffer.toString();
                }
            }
            previous = current;
            current = i;
            if (/* previous == '\r' && */current == '\n') {
                // End of line.
                return buffer.toString();
            } else if (previous == '\r' && current == 0) {
                // Literal new line.
                buffer.append(SYSTEM_LINE_SEPARATOR);
            } else if (current != 0 && current != '\r') {
                buffer.append((char) current);
            }
        } while (true);
    }

}
