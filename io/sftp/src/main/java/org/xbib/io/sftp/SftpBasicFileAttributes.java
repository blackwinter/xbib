package org.xbib.io.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;


public class SFTPBasicFileAttributes implements BasicFileAttributes {

    public SftpATTRS attrs;

    protected SFTPBasicFileAttributes(SFTPPath path) throws IOException {
        try {
            this.attrs = ((SFTPFileSystem) path.getFileSystem()).getChannelSftp().stat(path.toString());
        } catch (SftpException e) {
            if (!(e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)) {
                throw new NoSuchFileException("The file doesn't exist");
            } else {
                throw new IOException("Unable to get the file attributes of (" + path.toString() + ")", e);
            }
        }
    }

    public FileTime lastModifiedTime() {
        return FileTime.fromMillis((long) this.attrs.getMTime() * 1000 );
    }

    public FileTime lastAccessTime() {
        return FileTime.fromMillis((long) this.attrs.getATime() * 1000);
    }

    /**
     * Creation Time is not in the specification
     * http://tools.ietf.org/html/draft-ietf-secsh-filexfer-02#section-5
     *
     * @return {@link SFTPBasicFileAttributes#lastModifiedTime() the last modified time }
     */
    public FileTime creationTime() {
        //LOGGER.warning("The creation time of a file doesn't exist in the SSH protocol, returning the last modified time");
        return lastModifiedTime();
    }

    public boolean isRegularFile() {
        return !this.attrs.isDir();
    }

    public boolean isDirectory() {
        return this.attrs.isDir();
    }

    public boolean isSymbolicLink() {
        return this.attrs.isLink();
    }

    public boolean isOther() {
        return false;
    }

    public long size() {
        return this.attrs.getSize();
    }

    public Object fileKey() {
        return null;
    }
}
