package org.xbib.io.ftp.agent;

import org.xbib.io.ftp.FTPConfiguration;

/**
 * Factory for {@link FtpAgent} instances
 */
public interface FtpAgentFactory {
    /**
     * Create one agent
     *
     * @param queue the agent queue
     * @param cfg   the FTP agent configuration
     * @return a new agent
     */
    FtpAgent get(final FtpAgentQueue queue, final FTPConfiguration cfg);
}
