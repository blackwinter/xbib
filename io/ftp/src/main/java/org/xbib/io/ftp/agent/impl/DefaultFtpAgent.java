package org.xbib.io.ftp.agent.impl;

import org.xbib.io.ftp.FTPClient;
import org.xbib.io.ftp.FTPConfiguration;
import org.xbib.io.ftp.FTPEntry;
import org.xbib.io.ftp.FTPException;
import org.xbib.io.ftp.agent.AbstractFtpAgent;
import org.xbib.io.ftp.agent.FtpAgentQueue;
import org.xbib.io.ftp.agent.FtpFileView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessMode;
import java.nio.file.NoSuchFileException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultFtpAgent extends AbstractFtpAgent {

    private final FTPClient client;

    public DefaultFtpAgent(FtpAgentQueue ftpAgentQueue, FTPConfiguration ftpConfiguration) {
        super(ftpAgentQueue,ftpConfiguration);
        this.client = new FTPClient();
    }

    @Override
    public void connect() throws IOException {
        if (status == Status.CONNECTED) {
            return;
        }
        try {
            client.connect(cfg.getHostname(), cfg.getPort());
            client.login(cfg.getUsername(), cfg.getPassword());
            client.cwd(cfg.getPath());
        } catch (FTPException e) {
            status = Status.DEAD;
            throw new IOException(e);
        }
        status = Status.CONNECTED;
    }

    @Override
    public void disconnect() throws IOException {
        try {
            client.disconnect(true);
        } catch (FTPException e) {
            status = Status.DEAD;
            throw new IOException(e);
        }
    }

    @Override
    protected InputStream openInputStream(String file) throws IOException {
        if (status != Status.CONNECTED) {
            return null;
        }
        try {
            client.openInputStream(file);
        } catch (FTPException e) {
            status = Status.DEAD;
            throw new IOException(e);
        }
        return null;
    }

    @Override
    public FtpFileView getFileView(String name) throws IOException {
        try {
            Map<String,FTPEntry> map = client.list();
            if (map.containsKey(name)) {
                return new DefaultFtpFileView(map.get(name));
            } else {
                throw new NoSuchFileException(name + " not in " + map);
            }
        } catch (FTPException e) {
            status = Status.DEAD;
            throw new IOException(e);
        }
    }

    @Override
    public EnumSet<AccessMode> getAccess(String name) throws IOException {
        try {
            Map<String,FTPEntry> map = client.list(name);
            if (map.containsKey(name)) {
                DefaultFtpFileView ftpFileView = new DefaultFtpFileView(map.get(name));
                return EnumSet.copyOf(ftpFileView.getAccess());
            } else {
                throw new NoSuchFileException(name);
            }
        } catch (FTPException e) {
            status = Status.DEAD;
            throw new IOException(e);
        }
    }

    @Override
    public List<String> getDirectoryNames(String dir) throws IOException {
        try {
            Map<String,FTPEntry> map = client.list(dir);
            return map.entrySet().stream()
                    .filter(e -> e.getValue().isDirectory())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } catch (FTPException e) {
            status = Status.DEAD;
            throw new IOException(e);
        }
    }

    @Override
    public void completeTransfer() throws IOException {
        // do nothing
    }
}
