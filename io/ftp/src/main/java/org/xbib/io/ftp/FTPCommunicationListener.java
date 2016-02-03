package org.xbib.io.ftp;

/**
 * This interface describes how to build objects used to intercept any
 * communication between the client and the server. It is useful to catch what
 * happens behind. A FTPCommunicationListener can be added to any FTPClient
 * object by calling its addCommunicationListener() method.
 */
public interface FTPCommunicationListener {

    /**
     * Called every time a telnet triple has been sent over the network to
     * the remote FTP server.
     *
     * @param statement The triple that has been sent.
     */
    void sent(String statement);

    /**
     * Called every time a telnet triple is received by the client.
     *
     * @param statement The received triple.
     */
    void received(String statement);

}
