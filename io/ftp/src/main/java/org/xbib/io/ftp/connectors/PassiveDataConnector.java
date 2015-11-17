package org.xbib.io.ftp.connectors;

import java.io.IOException;
import java.net.Socket;

public class PassiveDataConnector implements DataConnector {

    private FTPConnector connector;
    private String pasvHost;
    private int pasvPort;

    public PassiveDataConnector(FTPConnector connector, String pasvHost, int pasvPort) {
        this.connector = connector;
        this.pasvHost = pasvHost;
        this.pasvPort = pasvPort;
    }

    @Override
    public Socket openDataConnection() throws IOException {
        return connector.connectForDataTransferChannel(pasvHost, pasvPort);
    }

    @Override
    public void close() throws IOException {

    }
}
