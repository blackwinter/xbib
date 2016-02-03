package org.xbib.io.ftp;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

public final class FTPFileStore extends FileStore {
    private final String name;

    public FTPFileStore(final URI uri) {
        name = uri.toString();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String type() {
        return "ftp";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public long getTotalSpace()
            throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUsableSpace()
            throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUnallocatedSpace()
            throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean supportsFileAttributeView(
            final Class<? extends FileAttributeView> type) {
        return type == BasicFileAttributeView.class;
    }

    @Override
    public boolean supportsFileAttributeView(final String name) {
        return "basic".equals(name);
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(
            final Class<V> type) {
        return null;
    }

    @Override
    public Object getAttribute(final String attribute)
            throws IOException {
        throw new UnsupportedOperationException("no attributes are supported");
    }
}
