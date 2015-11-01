package org.xbib.io.archive.file;

import org.xbib.io.archive.ArchiveEntry;

import java.util.Date;

public class FileArchiveEntry  implements ArchiveEntry {
    @Override
    public ArchiveEntry setName(String name) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ArchiveEntry setEntrySize(long size) {
        return null;
    }

    @Override
    public long getEntrySize() {
        return 0;
    }

    @Override
    public ArchiveEntry setLastModified(Date date) {
        return null;
    }

    @Override
    public Date getLastModified() {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }
}
