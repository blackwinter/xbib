package org.xbib.io.http.client.request.body.generator;

import org.xbib.io.http.client.request.body.RandomAccessBody;

import java.io.File;

/**
 * Creates a request body from the contents of a file.
 */
public final class FileBodyGenerator implements BodyGenerator {

    private final File file;
    private final long regionSeek;
    private final long regionLength;

    public FileBodyGenerator(File file) {
        this(file, 0L, file.length());
    }

    public FileBodyGenerator(File file, long regionSeek, long regionLength) {
        this.file = file;
        this.regionLength = regionLength;
        this.regionSeek = regionSeek;
    }

    public File getFile() {
        return file;
    }

    public long getRegionLength() {
        return regionLength;
    }

    public long getRegionSeek() {
        return regionSeek;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RandomAccessBody createBody() {
        throw new UnsupportedOperationException("FileBodyGenerator.createBody isn't used, Netty direclt sends the file");
    }
}
