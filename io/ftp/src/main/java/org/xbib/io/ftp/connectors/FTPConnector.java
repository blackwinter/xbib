package org.xbib.io.ftp.connectors;

import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * This abstract class is the base for creating a connector. Connectors are used
 * by the client to establish connections with remote servers.
 */
public abstract class FTPConnector implements Closeable {

    protected Logger logger;

    /**
     * Timeout in seconds for connection enstablishing.
     */
    protected int connectionTimeout = 10;

    /**
     * Timeout in seconds for read operations.
     */
    protected int readTimeout = 10;

    /**
     * Timeout in seconds for connection regular closing.
     */
    protected int closeTimeout = 10;

    private Socket dataSocket;

    private Socket commSocket;

    /**
     * Builds the connector.
     *
     * By calling this constructor, the connector's default value for the
     * <em>useSuggestedAddressForDataConnections</em> flag is <em>false</em>.
     */
    protected FTPConnector(Logger logger) {
        this.logger = logger;
    }

    /**
     * Sets the timeout for connection operations.
     *
     * @param connectionTimeout The timeout value in seconds.
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Sets the timeout for read operations.
     *
     * @param readTimeout The timeout value in seconds.
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Sets the timeout for close operations.
     *
     * @param closeTimeout The timeout value in seconds.
     */
    public void setCloseTimeout(int closeTimeout) {
        this.closeTimeout = closeTimeout;
    }

    /**
     * Creates a socket and connects it to the given host for a communication
     * channel. Socket timeouts are automatically set according to the values of
     * {@link FTPConnector#connectionTimeout}, {@link FTPConnector#readTimeout}
     * and {@link FTPConnector#closeTimeout}.
     *
     * If you are extending FTPConnector, consider using this method to
     * establish your socket connection for the communication channel, instead
     * of creating Socket objects, since it is already aware of the timeout
     * values possibly given by the caller.
     *
     * @param host The host for the connection.
     * @param port The port for the connection.
     * @return The connected socket.
     * @throws IOException If connection fails.
     */
    protected Socket createCommSocket(String host, int port)
            throws IOException {
        if (commSocket != null) {
            if (commSocket.isClosed()) {
                logger.debug("closing comm socket {}", commSocket.getLocalAddress());
                commSocket.close();
            }
        }
        commSocket = new Socket();
        commSocket.setKeepAlive(true);
        commSocket.setReuseAddress(true);
        commSocket.setSoTimeout(readTimeout * 1000);
        commSocket.setSoLinger(true, closeTimeout);
        commSocket.connect(new InetSocketAddress(host, port), connectionTimeout * 1000);
        logger.debug("connected to comm socket {} -> {}",
                commSocket.getLocalSocketAddress(),
                commSocket.getRemoteSocketAddress());
        return commSocket;
    }

    /**
     * Creates a socket and connects it to the given host for a data transfer
     * channel. Socket timeouts are automatically set according to the values of
     * {@link FTPConnector#connectionTimeout}, {@link FTPConnector#readTimeout}
     * and {@link FTPConnector#closeTimeout}.
     *
     * If you are extending FTPConnector, consider using this method to
     * establish your socket connection for the communication channel, instead
     * of creating Socket objects, since it is already aware of the timeout
     * values possibly given by the caller.
     *
     * @param host The host for the connection.
     * @param port The port for the connection.
     * @return The connected socket.
     * @throws IOException If connection fails.
     */
    protected Socket createDataSocket(String host, int port)
            throws IOException {
        if (dataSocket != null) {
            if (!dataSocket.isClosed()) {
                logger.debug("closing data socket {}", dataSocket.getLocalAddress());
                dataSocket.close();
            }
        }
        dataSocket = new Socket();
        dataSocket.setSoTimeout(readTimeout * 1000);
        dataSocket.setSoLinger(true, closeTimeout);
        dataSocket.setReuseAddress(true);
        dataSocket.setReceiveBufferSize(8 * 1024);
        dataSocket.setSendBufferSize(8 * 1024);
        dataSocket.connect(new InetSocketAddress(host, port), connectionTimeout * 1000);
        logger.debug("connected to data socket {} -> {}",
                dataSocket.getLocalSocketAddress(),
                dataSocket.getRemoteSocketAddress());
        return dataSocket;
    }

    /**
     * Aborts an ongoing connection attempt
     */
    @Override
    public void close() {
        if (commSocket != null) {
            try {
                if (!commSocket.isClosed()) {
                    commSocket.close();
                    logger.debug("comm socket closed {}", commSocket.getLocalAddress());
                }
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
        if (dataSocket != null) {
            try {
                if (!dataSocket.isClosed()) {
                    dataSocket.close();
                    logger.debug("data socket closed {}", dataSocket.getLocalAddress());
                }
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    /**
     * This methods returns an established connection to a remote host, suitable
     * for a FTP communication channel.
     *
     * @param host The remote host name or address.
     * @param port The remote port.
     * @return The connection with the remote host.
     * @throws IOException If the connection cannot be established.
     */
    public abstract Socket connectForCommunicationChannel(String host, int port)
            throws IOException;

    /**
     * This methods returns an established connection to a remote host, suitable
     * for a FTP data transfer channel.
     *
     * @param host The remote host name or address.
     * @param port The remote port.
     * @return The connection with the remote host.
     * @throws IOException If the connection cannot be established.
     */
    public abstract Socket connectForDataTransferChannel(String host, int port)
            throws IOException;

}
