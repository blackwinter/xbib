package org.xbib.io.ftp.agent;

import org.xbib.io.ftp.FTPConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Base implementation for an {@link FtpAgent}
 */
public abstract class AbstractFtpAgent implements FtpAgent {

    protected final FTPConfiguration cfg;

    protected Status status = Status.INITIALIZED;

    private final FtpAgentQueue queue;

    /**
     * Protected constructor
     *
     * @param queue which agent queue is linked to this agent
     * @param cfg   the configuration to use
     */
    protected AbstractFtpAgent(final FtpAgentQueue queue,
                               final FTPConfiguration cfg) {
        this.queue = queue;
        this.cfg = cfg;
    }

    /**
     * Open a raw input stream to a remote file
     *
     * @param file path to the remote file
     * @return an input stream
     * @throws NoSuchFileException   file does not exist
     * @throws AccessDeniedException cannot open the file for reading
     * @throws IOException           cannot create the input stream
     */
    protected abstract InputStream openInputStream(final String file)
            throws IOException;

    @Override
    public final FtpInputStream getInputStream(final Path path)
            throws IOException {
        final InputStream stream = openInputStream(path.toString());
        return new FtpInputStream(this, stream);
    }

    @Override
    public final boolean isDead() {
        return status == Status.DEAD;
    }

    @Override
    public final void close() throws IOException {
        queue.pushBack(this);
    }

    /**
     * Status of the agent
     */
    public enum Status {
        /**
         * Agent is initialized, but not connected yet
         */
        INITIALIZED,
        /**
         * Agent is connected and active
         */
        CONNECTED,
        /**
         * Agent was connected but has died (for instance, FTP reply code 421)
         */
        DEAD
    }
}
