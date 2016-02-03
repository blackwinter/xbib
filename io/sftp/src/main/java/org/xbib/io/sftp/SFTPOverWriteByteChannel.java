package org.xbib.io.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * A channel to write a new file
 *
 * SFTP knows only three transfer mode RESUME, APPEND, OVERWRITE
 */
public class SFTPOverWriteByteChannel implements SeekableByteChannel {

    private final SFTPFileProgressMonitor monitor;

    private final WritableByteChannel writableByteChannel;

    protected SFTPOverWriteByteChannel(SFTPPath path) throws SftpException {
        monitor = new SFTPFileProgressMonitor();
        writableByteChannel = Channels.newChannel(path.getChannelSftp().put(path.getStringPath(), monitor, ChannelSftp.OVERWRITE));
    }

    public int read(ByteBuffer dst) throws IOException {
        throw new NonReadableChannelException();
    }

    public int write(ByteBuffer src) throws IOException {
        return writableByteChannel.write(src);
    }

    public long position() throws IOException {
        return monitor.getCount();
    }

    public SeekableByteChannel position(long newPosition) throws IOException {
        throw new UnsupportedOperationException("With SFTP you cannot change the position");
    }

    public long size() throws IOException {
        throw new UnsupportedOperationException();
    }

    public SeekableByteChannel truncate(long size) throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean isOpen() {
        return writableByteChannel.isOpen();
    }

    public void close() throws IOException {
        writableByteChannel.close();
    }
}
