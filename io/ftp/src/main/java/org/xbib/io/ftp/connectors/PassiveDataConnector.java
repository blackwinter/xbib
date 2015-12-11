package org.xbib.io.ftp.connectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

public class PassiveDataConnector implements DataConnector {

    private final static Logger logger = LogManager.getLogger("org.xbib.io.ftp");

    private FTPConnector connector;
    private String pasvHost;
    private int pasvPort;
    private Socket socket;

    public PassiveDataConnector(FTPConnector connector, String pasvHost, int pasvPort) {
        this.connector = connector;
        this.pasvHost = pasvHost;
        this.pasvPort = pasvPort;
    }

    @Override
    public Socket openDataConnection() throws IOException {
        close();
        this.socket = connector.connectForDataTransferChannel(pasvHost, pasvPort);
        return this.socket;
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                    logger.debug("passive data socket closed {}", socket.getLocalAddress());
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
