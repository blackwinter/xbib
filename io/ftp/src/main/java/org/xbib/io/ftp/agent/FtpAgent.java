package org.xbib.io.ftp.agent;

import org.xbib.io.ftp.FTPFileSystemProvider;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

/**
 * One FTP client as used by an {@link FTPFileSystemProvider} instance
 *
 * <p><strong>DO NOT implement this class directly!</strong> Extend {@link
 * AbstractFtpAgent} instead.</p>
 *
 * <p>Implementations of this interface do the actual I/O operations with the
 * FTP server; this includes connection and disconnection, login, obtaining the
 * list of files from a directory or opening an {@link InputStream} for a
 * download.</p>
 *
 * @see AbstractFtpAgent
 * @see FtpAgentQueue
 * @see org.xbib.io.ftp.agent.FtpAgentFactory
 */
public interface FtpAgent
        extends Closeable {
    /**
     * Obtain a list of basic file attributes for an FTP entry
     *
     * @param name the path to the file
     * @return the attributes view
     * @throws FileNotFoundException file could not be found
     * @throws IOException           I/O error when communicating with the FTP server
     */
    FtpFileView getFileView(final String name)
            throws IOException;

    /**
     * Shortcut method to obtain privileges to a file for the current FTP user
     *
     * @param name the name to the file
     * @return the list of access modes
     * @throws IOException see {@link #getFileView(String)}
     */
    EnumSet<AccessMode> getAccess(final String name)
            throws IOException;

    /**
     * Obtain the list of directory entries for a remote FTP directory
     *
     * @param dir the directory
     * @return the list of names
     * @throws NotDirectoryException entry is not a directory
     * @throws AccessDeniedException user cannot list entries in this directory
     * @throws IOException           I/O error when communicating with FTP server
     */
    List<String> getDirectoryNames(final String dir)
            throws IOException;

    /**
     * Open an {@link InputStream} to a remote file for download
     *
     * @param path the path of the file to download
     * @return the matching input stream
     * @throws NoSuchFileException   file does not exist
     * @throws AccessDeniedException cannot read the file
     * @throws IOException           I/O error when communicating with FTP server
     * @see #completeTransfer()
     */
    FtpInputStream getInputStream(final Path path)
            throws IOException;

    /**
     * Report whether this agent is not usable anymore
     *
     * @return {@code true} if this agent cannot be used and should be disposed
     */
    boolean isDead();

    /**
     * Finalize a data channel transfer (other than a listing)
     *
     * @throws IOException transfer did not complete successfully
     */
    void completeTransfer()
            throws IOException;

    /**
     * Initiate the connection to the FTP server
     *
     * @throws IOException could not connect: socket error, invalid credentials
     */
    void connect()
            throws IOException;

    /**
     * Terminate a connection to the FTP server
     *
     * @throws IOException improper termination of the connection
     */
    void disconnect()
            throws IOException;
}
