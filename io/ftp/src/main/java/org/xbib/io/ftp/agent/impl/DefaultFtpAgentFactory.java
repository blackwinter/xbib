package org.xbib.io.ftp.agent.impl;

import org.xbib.io.ftp.FTPConfiguration;
import org.xbib.io.ftp.agent.FtpAgent;
import org.xbib.io.ftp.agent.FtpAgentFactory;
import org.xbib.io.ftp.agent.FtpAgentQueue;

public class DefaultFtpAgentFactory  implements FtpAgentFactory {
    @Override
    public FtpAgent get(FtpAgentQueue queue, FTPConfiguration cfg) {
        return new DefaultFtpAgent(queue, cfg);
    }
}
