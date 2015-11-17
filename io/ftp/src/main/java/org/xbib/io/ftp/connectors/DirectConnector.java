package org.xbib.io.ftp.connectors;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

/**
 * The DirectConnector connects the remote host with a straight socket
 * connection, using no proxy.
 *
 * The connector's default value for the
 * <em>useSuggestedAddressForDataConnections</em> flag is <em>false</em>.
 */
public class DirectConnector extends FTPConnector {

    public DirectConnector(Logger logger) {
        super(logger);
    }

    public Socket connectForCommunicationChannel(String host, int port)
            throws IOException {
        return createCommSocket(host, port);
    }

    public Socket connectForDataTransferChannel(String host, int port)
            throws IOException {
        return createDataSocket(host, port);
    }

}
