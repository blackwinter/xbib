
package org.xbib.io.ftp.connectors;

import org.apache.logging.log4j.LogManager;
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


    /**
     * The socket of an ongoing connection attempt for a communication channel.
     */
    private Socket socket;

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
        socket = new Socket();
        socket.setKeepAlive(true);
        socket.setReuseAddress(true);
        socket.setSoTimeout(readTimeout * 1000);
        socket.setSoLinger(true, closeTimeout);
        socket.connect(new InetSocketAddress(host, port), connectionTimeout * 1000);
        logger.info("connected to comm socket {} -> {}",
                socket.getLocalSocketAddress(),
                socket.getRemoteSocketAddress());
        return socket;
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
        Socket socket = new Socket();
        socket.setSoTimeout(readTimeout * 1000);
        socket.setSoLinger(true, closeTimeout);
        socket.setReuseAddress(true);
        socket.setReceiveBufferSize(8 * 1024);
        socket.setSendBufferSize(8 * 1024);
        socket.connect(new InetSocketAddress(host, port), connectionTimeout * 1000);
        logger.info("connected to data socket {} -> {}",
                socket.getLocalSocketAddress(),
                socket.getRemoteSocketAddress());
        return socket;
    }

    /**
     * Aborts an ongoing connection attempt for a communication channel.
     */
    @Override
    public void close() {
        if (socket != null) {
            try {
                socket.close();
                logger.info("socket closed {}", socket.getLocalAddress());
            } catch (Throwable t) {
                //
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
