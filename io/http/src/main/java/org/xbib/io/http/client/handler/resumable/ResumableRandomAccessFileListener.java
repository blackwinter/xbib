package org.xbib.io.http.client.handler.resumable;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import static org.xbib.io.http.client.util.MiscUtils.closeSilently;

/**
 * A {@link ResumableListener} which use a {@link RandomAccessFile} for storing the received bytes.
 */
public class ResumableRandomAccessFileListener implements ResumableListener {
    private final RandomAccessFile file;

    public ResumableRandomAccessFileListener(RandomAccessFile file) {
        this.file = file;
    }

    /**
     * This method uses the last valid bytes written on disk to position a {@link RandomAccessFile}, allowing
     * resumable file download.
     *
     * @param buffer a {@link ByteBuffer}
     * @throws IOException exception while writing into the file
     */
    public void onBytesReceived(ByteBuffer buffer) throws IOException {
        file.seek(file.length());
        if (buffer.hasArray()) {
            file.write(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
        } else { // if the buffer is direct or backed by a String...
            byte[] b = new byte[buffer.remaining()];
            int pos = buffer.position();
            buffer.get(b);
            buffer.position(pos);
            file.write(b);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onAllBytesReceived() {
        closeSilently(file);
    }

    /**
     * {@inheritDoc}
     */
    public long length() {
        try {
            return file.length();
        } catch (IOException e) {
            return 0;
        }
    }
}
