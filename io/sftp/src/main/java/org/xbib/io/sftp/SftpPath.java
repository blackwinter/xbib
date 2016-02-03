package org.xbib.io.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

/**
 * A Path implementation for SFTP.
 * An object that may be used to locate a file in a file system.
 * It will typically represent a system dependent file stringPath.
 */
class SFTPPath implements Path {

    protected static final String ROOT_PREFIX = "/";

    protected final String PATH_SEPARATOR; // To avoid duplicate. It is initialized with the value of FileSystem.getSeparator();

    private final SFTPFileSystem sftpFileSystem;

    private String stringPath;

    private SFTPPath(SFTPFileSystem sftpFileSystem, String stringPath) {

        this.sftpFileSystem = sftpFileSystem;
        this.stringPath = stringPath;
        this.PATH_SEPARATOR = sftpFileSystem.getSeparator();

    }

    /**
     * A static constructor
     * You canm also get a stringPath from a provider with an URI.
     *
     * @param sftpFileSystem file system
     * @param path path
     * @return path
     */
    protected static Path get(SFTPFileSystem sftpFileSystem, String path) {
        return new SFTPPath(sftpFileSystem, path);
    }

    public FileSystem getFileSystem() {
        return this.sftpFileSystem;
    }

    public boolean isAbsolute() {
        return this.stringPath.startsWith(ROOT_PREFIX);

    }

    public Path getRoot() {
        throw new UnsupportedOperationException();
    }

    public Path getFileName() {
        throw new UnsupportedOperationException();
    }

    public Path getParent() {
        throw new UnsupportedOperationException();
    }

    public int getNameCount() {
        throw new UnsupportedOperationException();
    }

    public Path getName(int index) {
        throw new UnsupportedOperationException();
    }

    public Path subpath(int beginIndex, int endIndex) {
        throw new UnsupportedOperationException();
    }

    public boolean startsWith(Path other) {
        throw new UnsupportedOperationException();
    }

    public boolean startsWith(String other) {
        throw new UnsupportedOperationException();
    }

    public boolean endsWith(Path other) {
        throw new UnsupportedOperationException();
    }

    public boolean endsWith(String other) {
        throw new UnsupportedOperationException();
    }

    public Path normalize() {
        throw new UnsupportedOperationException();
    }

    public Path resolve(Path other) {
        throw new UnsupportedOperationException();
    }

    public Path resolve(String other) {
        throw new UnsupportedOperationException();
    }

    public Path resolveSibling(Path other) {
        throw new UnsupportedOperationException();
    }

    public Path resolveSibling(String other) {
        throw new UnsupportedOperationException();
    }

    public Path relativize(Path other) {
        throw new UnsupportedOperationException();
    }

    public URI toUri() {
        throw new UnsupportedOperationException();
    }

    public Path toAbsolutePath() {
        if (this.isAbsolute()) {
            return this;
        } else {
            try {
                return get(sftpFileSystem, sftpFileSystem.getChannelSftp().getHome() + sftpFileSystem.getSeparator() + this.stringPath);
            } catch (SftpException e) {
                throw new RuntimeException(e);
            }

        }

    }

    public Path toRealPath(LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    public File toFile() {
        throw new UnsupportedOperationException();
    }

    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Iterator<Path> iterator() {
        throw new UnsupportedOperationException();
    }

    public int compareTo(Path other) {
        throw new UnsupportedOperationException();
    }

    protected SFTPPosixFileAttributes getFileAttributes() throws IOException {
        return new SFTPPosixFileAttributes(this);
    }

    public String toString() {
        return this.stringPath;
    }

    /**
     * A shortcut to get the ChannelSftp saved in the file systen object
     *
     * @return ChannelSftp
     */
    protected ChannelSftp getChannelSftp() {
        return this.sftpFileSystem.getChannelSftp();
    }

    /**
     * String Path representation used internally to make all Sftp operations
     *
     * @return string path
     */
    protected String getStringPath() {
        return stringPath;
    }
}