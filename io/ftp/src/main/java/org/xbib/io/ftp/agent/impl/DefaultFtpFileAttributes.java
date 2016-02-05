package org.xbib.io.ftp.agent.impl;

import org.xbib.io.ftp.FTPEntry;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class DefaultFtpFileAttributes implements BasicFileAttributes {

    private final FTPEntry ftpEntry;

    public DefaultFtpFileAttributes(FTPEntry ftpEntry) {
        this.ftpEntry = ftpEntry;
    }

    @Override
    public FileTime lastModifiedTime() {
        return FileTime.fromMillis(ftpEntry.getModifiedDate().getTime());
    }

    @Override
    public FileTime lastAccessTime() {
        return lastModifiedTime();
    }

    @Override
    public FileTime creationTime() {
        return lastModifiedTime();
    }

    @Override
    public boolean isRegularFile() {
        return ftpEntry.isRegularFile();
    }

    @Override
    public boolean isDirectory() {
        return ftpEntry.isDirectory();
    }

    @Override
    public boolean isSymbolicLink() {
        return ftpEntry.isSymbolicLink();
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public long size() {
        return ftpEntry.getSize();
    }

    @Override
    public Object fileKey() {
        return null;
    }
}
