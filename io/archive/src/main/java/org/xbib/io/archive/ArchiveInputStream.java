package org.xbib.io.archive;

import java.io.IOException;
import java.io.InputStream;

/**
 * Archive input streams <b>MUST</b> override the
 * {@link #read(byte[], int, int)} - or {@link #read()} -
 * method so that reading from the stream generates EOF for the end of
 * data in each entry as well as at the end of the file proper.
 * The {@link #getNextEntry()} method is used to reset the input stream
 * ready for reading the data from the next entry.
 */
public abstract class ArchiveInputStream<E extends ArchiveEntry> extends InputStream {

    /**
     * Returns the next archive entry in this stream.
     *
     * @return the next entry,
     * or {@code null} if there are no more entries
     * @throws java.io.IOException if the next entry could not be read
     */
    public abstract E getNextEntry() throws IOException;

    /**
     * Reads a byte of data. This method will block until enough input is
     * available.
     * <p/>
     * Simply calls the {@link #read(byte[], int, int)} method.
     * <p/>
     * MUST be overridden if the {@link #read(byte[], int, int)} method
     * is not overridden; may be overridden otherwise.
     *
     * @return the byte read, or -1 if end of input is reached
     * @throws IOException if an I/O error has occurred
     */
    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        int num = read(b, 0, 1);
        return num == -1 ? -1 : b[0] & 0xFF;
    }

}
