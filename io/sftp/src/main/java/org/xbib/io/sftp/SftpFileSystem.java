package org.xbib.io.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by gerard on 20-11-2015.
 * A file system that you obtain with the factor {@link SFTPFileSystemProvider}
 * The default file system, obtained by invoking the FileSystems.getDefault method, provides access to the file system
 * that is accessible to the Java virtual machine.
 */
public class SFTPFileSystem extends FileSystem {

    private final Session session;

    private final ChannelSftp channelSftp;

    private final SFTPFileSystemProvider sftpFileSystemProvider;
    /**
     * A file system is open upon creation
     *
     * @param sftpFileSystemBuilder the file system builder
     */
    private SFTPFileSystem(SftpFileSystemBuilder sftpFileSystemBuilder) throws JSchException {

        // Uri
        URI uri = sftpFileSystemBuilder.uri;
        this.sftpFileSystemProvider = sftpFileSystemBuilder.sftpFileSystemProvider;

        // Extract the user and the password
        String userInfo = uri.getUserInfo();
        String user = userInfo.substring(0, userInfo.indexOf(":"));
        String password = userInfo.substring(userInfo.indexOf(":") + 1, userInfo.length());
        int port;
        if (uri.getPort() == -1) {
            port = 22;
        } else {
            port = uri.getPort();
        }
        JSch jsch = new JSch();
        this.session = jsch.getSession(user, uri.getHost(), port);
        session.setPassword(password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        this.channelSftp = (ChannelSftp) session.openChannel("sftp");
        this.channelSftp.connect();
    }

    /**
     * Is used in the system file provider
     * to check the access
     *
     * @return ChannelSftp
     */
    protected ChannelSftp getChannelSftp() {
        return channelSftp;
    }

    @Override
    public FileSystemProvider provider() {

        return this.sftpFileSystemProvider;
    }

    /**
     * A file system is open upon creation and can be closed by invoking its close method. Once closed, any further
     * attempt to access objects in the file system cause ClosedFileSystemException to be thrown.
     * Closing a file system causes all open channels, watch services, and other closeable objects associated with the
     * file system to be closed.
     */
    @Override
    public void close() throws IOException {
        this.channelSftp.disconnect();
        this.session.disconnect();
    }

    /**
     * A file system is open upon creation and can be closed by invoking its close method. Once closed, any further
     * attempt to access objects in the file system cause ClosedFileSystemException to be thrown.
     */
    @Override
    public boolean isOpen() {

        return !this.channelSftp.isClosed();
    }

    /**
     * Whether or not a file system provides read-only access is established when the FileSystem is created and can be
     * tested by invoking its isReadOnly
     *
     * @return boolean if true
     */
    @Override
    public boolean isReadOnly() {

        throw new UnsupportedOperationException();

    }

    /**
     * The name separator is used to separate names in a path string.
     *
     * @return the separator
     */
    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        ArrayList<Path> rootDirectories = new ArrayList<Path>();
        Path rootPath = SFTPPath.get(this, SFTPPath.ROOT_PREFIX);
        rootDirectories.add(rootPath);
        return rootDirectories;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getPath(String first, String... more) {
        String path;
        if (more.length == 0) {
            path = first;
        } else {
            // Build the path from the list of directory
            StringBuilder sb = new StringBuilder();
            sb.append(first);
            for (String segment : more) {
                if (segment.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append('/');
                    }
                    sb.append(segment);
                }
            }
            path = sb.toString();
        }
        return SFTPPath.get(this, path);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

    public static class SftpFileSystemBuilder {

        private final SFTPFileSystemProvider sftpFileSystemProvider;
        private final URI uri;

        public SftpFileSystemBuilder(SFTPFileSystemProvider sftpFileSystemProvider, URI uri) {
            this.sftpFileSystemProvider = sftpFileSystemProvider;
            this.uri = uri;
        }

        public SFTPFileSystem build() throws JSchException {
            return new SFTPFileSystem(this);
        }

    }
}
