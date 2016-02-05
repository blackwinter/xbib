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
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class SFTPFileSystem extends FileSystem {

    private final Session session;

    private final ChannelSftp channelSftp;

    private final FileSystemProvider fileSystemProvider;

    private final FileStore fileStore;

    private SFTPFileSystem(FileSystemProvider fileSystemProvider, URI uri,
                           String username, String password, String privateKeyPath) throws JSchException {
        this.fileSystemProvider = fileSystemProvider;
        this.fileStore = new SFTPFileStore(uri);
        int port;
        if (uri.getPort() == -1) {
            port = 22;
        } else {
            port = uri.getPort();
        }
        if (username == null) {
            String userInfo = uri.getUserInfo();
            username = userInfo.substring(0, userInfo.indexOf(":"));
            password = userInfo.substring(userInfo.indexOf(":") + 1, userInfo.length());
        }
        JSch jsch = new JSch();
        this.session = jsch.getSession(username, uri.getHost(), port);
        if (password != null && !password.isEmpty()) {
            session.setPassword(password);
        }
        if (privateKeyPath != null) {
            jsch.addIdentity(privateKeyPath);
        }
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no"); // hmmmm
        session.setConfig(config);
        session.connect();
        this.channelSftp = (ChannelSftp) session.openChannel("sftp");
        this.channelSftp.connect();
    }

    protected ChannelSftp getChannelSftp() {
        return channelSftp;
    }

    @Override
    public FileSystemProvider provider() {
        return fileSystemProvider;
    }

    @Override
    public void close() throws IOException {
        this.channelSftp.disconnect();
        this.session.disconnect();
    }

    @Override
    public boolean isOpen() {
        return !this.channelSftp.isClosed();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

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
        return Collections.singletonList(fileStore);
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return Collections.singleton("basic");
    }

    @Override
    public Path getPath(String first, String... more) {
        String path;
        if (more.length == 0) {
            path = first;
        } else {
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
    public PathMatcher getPathMatcher(final String syntaxAndPattern) {
        if (!syntaxAndPattern.contains(":")) {
            throw new IllegalArgumentException("PathMatcher requires input syntax:expression");
        }
        String[] parts = syntaxAndPattern.split(":", 2);
        Pattern pattern;
        if ("glob".equals(parts[0])) {
            pattern = GlobPattern.compile(parts[1]);
        } else if ("regex".equals(parts[0])) {
            pattern = Pattern.compile(parts[1]);
        } else {
            throw new UnsupportedOperationException("Unknown PathMatcher syntax: " + parts[0]);
        }
        return new SFTPPathMatcher(pattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

    public static class Builder {

        private FileSystemProvider fileSystemProvider;
        private URI uri;
        private String username;
        private String password;
        private String privateKeyPath;

        public Builder setProvider(SFTPFileSystemProvider sftpFileSystemProvider) {
            this.fileSystemProvider = sftpFileSystemProvider;
            return this;
        }

        public Builder setURI(URI uri) {
            this.uri = uri;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setPrivateKeyPath(String privateKeyPath) {
            this.privateKeyPath = privateKeyPath;
            return this;
        }

        public SFTPFileSystem build() throws JSchException {
            return new SFTPFileSystem(fileSystemProvider, uri, username, password, privateKeyPath);
        }
    }
}
