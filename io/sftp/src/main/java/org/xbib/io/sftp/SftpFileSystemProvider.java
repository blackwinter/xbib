package org.xbib.io.sftp;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A factory for the SFTP file system
 */
public class SFTPFileSystemProvider extends FileSystemProvider {

    static final String SFTP_SCHEME = "sftp";

    // The pool of Sftp Connection
    private static final Map<URI, SFTPFileSystem> fileSystemPool = new HashMap<URI, SFTPFileSystem>();

    // Checks that the given file is a SftpPath
    static SFTPPath toSftpPath(Path path) {
        if (path == null) {
            throw new NullPointerException();
        }
        if (!(path instanceof SFTPPath)) {
            throw new ProviderMismatchException();
        }
        return (SFTPPath) path;
    }

    @Override
    public String getScheme() {
        return SFTP_SCHEME;
    }

    /**
     * The newFileSystem method is used to create a file system
     */
    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        try {
            SFTPFileSystem sftpFileSystem = new SFTPFileSystem.SftpFileSystemBuilder(this, uri).build();
            if (fileSystemPool.containsKey(uri)) {
                throw new FileSystemAlreadyExistsException();
            } else {
                fileSystemPool.put(uri, sftpFileSystem);
            }
            return sftpFileSystem;
        } catch (JSchException e) {
            throw new IOException(e);
        }
    }

    /**
     * @param path path
     * @param options options
     * @param attrs attributes
     * @return A FileChannel object that allows a file to be read or written in the file system.
     * @throws IOException
     */
    @Override
    public FileChannel newFileChannel(Path path,
                                      Set<? extends OpenOption> options,
                                      FileAttribute<?>... attrs)
            throws IOException {
        return new SFTPFileChannel();
    }

    /**
     * The getFileSystem method is used to retrieve a reference to an existing file system
     *
     * @param uri uri
     * @return a FileSystem
     */
    @Override
    public FileSystem getFileSystem(URI uri) {

        return fileSystemPool.get(uri);

    }

    @Override
    public Path getPath(URI uri) {

        return getFileSystem(uri).getPath(uri.getPath());

    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        try {
            if (options.containsAll(EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))) {
                return new SFTPOverWriteByteChannel(toSftpPath(path));
            } else {
                throw new UnsupportedOperationException();
            }
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to write to file stores by means of an object associated with a read-only file system throws
     * ReadOnlyFileSystemException.
     *
     * @param dir dir
     * @param attrs attrs
     * @throws IOException
     */
    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Path path) throws IOException {

        SFTPPath sftpPath = toSftpPath(path);
        if (Files.exists(sftpPath)) {
            try {

                if (Files.isDirectory(sftpPath)) {

                    sftpPath.getChannelSftp().rmdir(sftpPath.getStringPath());

                } else {

                    sftpPath.getChannelSftp().rm(sftpPath.getStringPath());

                }

            } catch (SftpException e) {
                throw new IOException(e);
            }
        }


    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks the existence, and optionally the accessibility, of a file.
     *
     * @param path path
     * @param modes modes
     * @throws UnsupportedOperationException - an implementation is required to support checking for READ, WRITE, and
     *                                       EXECUTE access. This exception is specified to allow for the Access enum to
     *                                       be extended in future releases.
     * @throws NoSuchFileException           - if a file does not exist (optional specific exception)
     * @throws AccessDeniedException         - the requested access would be denied or the access cannot be determined
     *                                       because the Java virtual machine has insufficient privileges or other
     *                                       reasons. (optional specific exception)
     * @throws IOException                   - if an I/O error occurs
     * @throws SecurityException             - In the case of the default provider, and a security manager is installed,
     *                                       the checkRead is invoked when checking read access to the file or only the
     *                                       existence of the file, the checkWrite is invoked when checking write access
     *                                       to the file, and checkExec is invoked when checking execute access.
     */
    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {

        // check the existence, the read attributes will throw a NoSuchFileException
        Files.readAttributes(path, SFTPPosixFileAttributes.class);

        // Check a little bit the accessibility
        // TODO: check that the connected user has the right to do the operation
        // Not easy as we get only the uid...
        for (AccessMode mode : modes) {
            switch (mode) {
                case READ:
                    break;
                case WRITE:
                    if (path.getFileSystem().isReadOnly()) {
                        throw new AccessDeniedException(toString());
                    }
                    break;
                case EXECUTE:
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }


    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        if (type == BasicFileAttributes.class || type == SFTPBasicFileAttributes.class || type == SFTPPosixFileAttributes.class) {
            return (A) toSftpPath(path).getFileAttributes();
        } else {
            throw new UnsupportedOperationException("The class (" + type + ") is not supported.");
        }

    }

    /**
     * This function is used to test if a file exist
     * If the file doesn't exist, it must throws a IOException, otherwise it exist
     *
     * @param path path
     * @param attributes attributes
     * @param options options
     * @return map
     * @throws IOException
     */
    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }
}
