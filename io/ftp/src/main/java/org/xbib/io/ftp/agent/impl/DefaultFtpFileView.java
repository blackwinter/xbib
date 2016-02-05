package org.xbib.io.ftp.agent.impl;

import org.xbib.io.ftp.FTPEntry;
import org.xbib.io.ftp.agent.FtpFileView;

import java.io.IOException;
import java.nio.file.AccessMode;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.EnumSet;

public class DefaultFtpFileView implements FtpFileView {

    private final FTPEntry ftpEntry;

    public DefaultFtpFileView(final FTPEntry ftpEntry) {
        this.ftpEntry = ftpEntry;
    }

    @Override
    public String name() {
        return ftpEntry.getName();
    }

    @Override
    public BasicFileAttributes readAttributes() throws IOException {
        return new DefaultFtpFileAttributes(ftpEntry);
    }

    @Override
    public void setTimes(FileTime lastModifiedTime,
                         FileTime lastAccessTime,
                         FileTime createTime)
            throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public Collection<AccessMode> getAccess() {
        // default is read for all
        final EnumSet<AccessMode> ret = EnumSet.of(AccessMode.READ);
        return EnumSet.copyOf(ret);
    }

}
