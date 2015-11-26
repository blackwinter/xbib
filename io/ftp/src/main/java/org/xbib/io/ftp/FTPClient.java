package org.xbib.io.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.ftp.connectors.ActiveDataConnector;
import org.xbib.io.ftp.connectors.DataConnector;
import org.xbib.io.ftp.connectors.DirectConnector;
import org.xbib.io.ftp.connectors.FTPConnector;
import org.xbib.io.ftp.connectors.PassiveDataConnector;
import org.xbib.io.ftp.listparsers.DOSListParser;
import org.xbib.io.ftp.listparsers.EPLFListParser;
import org.xbib.io.ftp.listparsers.FTPListParser;
import org.xbib.io.ftp.listparsers.MLSDListParser;
import org.xbib.io.ftp.listparsers.NetWareListParser;
import org.xbib.io.ftp.listparsers.UnixListParser;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class FTPClient {

    private final static Logger logger = LogManager.getLogger("org.xbib.io.ftp");

    private final static Logger communicationlogger = LogManager.getLogger("org.xbib.io.ftp");

    /**
     * The constant for the FTP security level.
     */
    public static final int SECURITY_FTP = 0;

    /**
     * The constant for the FTPS (FTP over implicit TLS/SSL) security level.
     */
    public static final int SECURITY_FTPS = 1;

    /**
     * The constant for the FTPES (FTP over explicit TLS/SSL) security level.
     */
    public static final int SECURITY_FTPES = 2;

    /**
     * The constant for the TEXTUAL file transfer type. It means that the data
     * sent or received is treated as textual information. This implies charset
     * conversion during the transfer.
     */
    public static final int TYPE_TEXTUAL = 1;

    /**
     * The constant for the BINARY file transfer type. It means that the data
     * sent or received is treated as a binary stream. The data is taken "as
     * is", without any charset conversion.
     */
    public static final int TYPE_BINARY = 2;

    /**
     * The constant for the MLSD policy that causes the client to use the MLSD
     * command instead of LIST, but only if the MLSD command is explicitly
     * supported by the server (the support is tested with the FEAT command).
     */
    public static final int MLSD_IF_SUPPORTED = 0;

    /**
     * The constant for the MLSD policy that causes the client to use always the
     * MLSD command instead of LIST, also if the MLSD command is not explicitly
     * supported by the server (the support is tested with the FEAT command).
     */
    public static final int MLSD_ALWAYS = 1;

    /**
     * The constant for the MLSD policy that causes the client to use always the
     * LIST command, also if the MLSD command is explicitly supported by the
     * server (the support is tested with the FEAT command).
     */
    public static final int MLSD_NEVER = 2;

    /**
     * The size of the buffer used when sending or receiving data.
     */
    private static final int SEND_AND_RECEIVE_BUFFER_SIZE = 8 * 1024;

    /**
     * The DateFormat object used to parse the reply to a MDTM command.
     */
    private static final DateFormat MDTM_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * The RegExp Pattern object used to parse the reply to a PASV command.
     */
    private static final Pattern PASV_PATTERN = Pattern.compile("\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3}");

    /**
     * The RegExp Pattern object used to parse the reply to a PWD command.
     */
    private static final Pattern PWD_PATTERN = Pattern.compile("\"/.*\"");
    /**
     * Lock object used for synchronization.
     */
    private final Object lock = new Object();
    /**
     * Lock object used for synchronization in abort operations.
     */
    private final Object abortLock = new Object();
    /**
     * The connector used to connect the remote host.
     */
    private FTPConnector connector;
    /**
     * The SSL socket factory used to negotiate SSL connections.
     */
    private SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    /**
     * The FTPCommunicationListener objects registered on the client.
     */
    private List<FTPCommunicationListener> communicationListeners = new ArrayList<>();
    /**
     * The FTPListParser objects registered on the client.
     */
    private List<FTPListParser> listParsers = new ArrayList<>();
    /**
     * The FTPListParser used successfully during previous connection-scope list
     * operations.
     */
    private FTPListParser parser = null;
    /**
     * If the client is connected, it reports the remote host name or address.
     */
    private String host = null;
    /**
     * If the client is connected, it reports the remote port number.
     */
    private int port = 0;
    /**
     * The security level. The value should be one of SECURITY_FTP,
     * SECURITY_FTPS and SECURITY_FTPES constants. Default value is
     * SECURITY_FTP.
     */
    private int security = SECURITY_FTP;
    /**
     * If the client is authenticated, it reports the authentication username.
     */
    private String username;
    /**
     * If the client is authenticated, it reports the authentication password.
     */
    private String password;
    /**
     * The flag reporting the connection status.
     */
    private boolean connected = false;
    /**
     * The flag reporting the authentication status.
     */
    private boolean authenticated = false;
    /**
     * The flag for the passive FTP data transfer mode. Default value is true,
     * cause it's usually the preferred FTP operating mode.
     */
    private boolean passive = true;

    private int type = TYPE_BINARY;
    /**
     * The MLSD command policy. The value should be one of
     * {@link FTPClient#MLSD_IF_SUPPORTED}, {@link FTPClient#MLSD_ALWAYS} and
     * {@link FTPClient#MLSD_NEVER} constants. Default value is
     * MLSD_IF_SUPPORTED.
     */
    private int mlsdPolicy = MLSD_IF_SUPPORTED;
    /**
     * If this value is greater than 0, the auto-noop feature is enabled. If
     * positive, the field is used as a timeout value (expressed in
     * milliseconds). If autoNoopDelay milliseconds has passed without any
     * communication between the client and the server, a NOOP command is
     * automaticaly sent to the server by the client.
     */
    private long autoNoopTimeout = 0;
    /**
     * The auto noop timer thread.
     */
    private AutoNoopTimer autoNoopTimer;
    /**
     * The system time (in millis) of the moment when the next auto noop command
     * should be issued.
     */
    private long nextAutoNoopTime;
    /**
     * A flag used to mark whether the connected server supports the resume of
     * broken transfers.
     */
    private boolean restSupported = false;
    /**
     * The name of the charset used to establish textual communications. If not
     * null the client will use always the given charset. If null the client
     * tries to auto-detect the server charset. If this attempt fails the client
     * will use the machine current charset.
     */
    private String charset = null;
    /**
     * This flag enables and disables the use of compression (ZLIB) during data
     * transfers. Compression is enabled when both this flag is true and the
     * server supports compressed transfers.
     */
    private boolean compressionEnabled = false;
    /**
     * A flag used to mark whether the connected server supports UTF-8 pathnames
     * encoding.
     */
    private boolean utf8Supported = false;
    /**
     * A flag used to mark whether the connected server supports the MLSD
     * command (RFC 3659).
     */
    private boolean mlsdSupported = false;
    /**
     * A flag used to mark whether the connected server supports the MODE Z
     * command.
     */
    private boolean modezSupported = false;
    /**
     * A flag used to mark whether MODE Z is enabled.
     */
    private boolean modezEnabled = false;
    /**
     * This flag indicates whether the data channel is encrypted.
     */
    private boolean dataChannelEncrypted = false;
    /**
     * This flag reports if there's any ongoing abortable data transfer
     * operation. Its value should be accessed only under the eye of the
     * abortLock synchronization object.
     */
    private boolean ongoingDataTransfer = false;

    /**
     * This flag turns to true when any data transfer stream is closed due to an
     * abort request.
     */
    private boolean aborted = false;
    /**
     * This flags tells if the reply to an ABOR command waits to be consumed.
     */
    private boolean consumeAborCommandReply = false;
    /**
     * The communication channel established with the server.
     */
    private FTPCommunicationChannel communication = null;

    /**
     * Builds and initializes the client.
     */
    public FTPClient() {
        addListParser(new UnixListParser());
        addListParser(new DOSListParser());
        addListParser(new EPLFListParser());
        addListParser(new NetWareListParser());
        addListParser(new MLSDListParser());
    }

    /**
     * This method returns the connector used to connect the remote host.
     *
     * @return The connector used to connect the remote host.
     */
    public FTPConnector getConnector() {
        return connector;
    }

    /**
     * This method sets the connector used to connect the remote host.
     *
     * Default one is a DirectConnector instance.
     *
     * @param connector The connector used to connect the remote host.
     */
    public void setConnector(FTPConnector connector) {
        this.connector = connector;
    }

    /**
     * Returns the SSL socket factory used to negotiate SSL connections.
     *
     * @return The SSL socket factory used to negotiate SSL connections.
     */
    public SSLSocketFactory getSSLSocketFactory() {
        synchronized (lock) {
            return sslSocketFactory;
        }
    }

    /**
     * Sets the SSL socket factory used to negotiate SSL connections.
     *
     * @param sslSocketFactory The SSL socket factory used to negotiate SSL connections.
     */
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        synchronized (lock) {
            this.sslSocketFactory = sslSocketFactory;
        }
    }

    /**
     * Returns the security level used by the client in the connection.
     *
     * @return The security level, which could be one of the SECURITY_FTP,
     * SECURITY_FTPS and SECURITY_FTPES costants.
     */
    public int getSecurity() {
        return security;
    }

    /**
     * Sets the security level for the connection. This method should be called
     * before starting a connection with a server. The security level must be
     * expressed using one of the SECURITY_FTP, SECURITY_FTPS and SECURITY_FTPES
     * costants.
     *
     * SECURITY_FTP, which is the default value, applies the basic FTP security
     * level.
     *
     * SECURITY_FTPS applies the FTPS security level, which is FTP over implicit
     * TLS/SSL.
     *
     * SECURITY_FTPES applies the FTPES security level, which is FTP over
     * explicit TLS/SSL.
     *
     * @param security The security level.
     * @throws IllegalArgumentException If the supplied security level is not valid.
     */
    public void setSecurity(int security) throws IllegalArgumentException {
        if (security != SECURITY_FTP && security != SECURITY_FTPS && security != SECURITY_FTPES) {
            throw new IllegalArgumentException("Invalid security");
        }
        synchronized (lock) {
            if (connected) {
                throw new IllegalArgumentException("The security level of the connection can't be changed while the client is connected");
            }
            this.security = security;
        }
    }

    /**
     * Applies SSL encryption to an already open socket.
     *
     * @param socket The already established socket.
     * @param host   The logical destination host.
     * @param port   The logical destination port.
     * @return The SSL socket.
     * @throws IOException If the SSL negotiation fails.
     */
    private Socket ssl(Socket socket, String host, int port) throws IOException {
        return sslSocketFactory.createSocket(socket, host, port, true);
    }

    /**
     * This method returns the value suggesting how the client encode and decode
     * the contents during a data transfer.
     *
     * @return The type as a numeric value.
     * {@link FTPClient#TYPE_BINARY} and {@link FTPClient#TYPE_TEXTUAL}.
     */
    public int getType() {
        synchronized (lock) {
            return type;
        }
    }

    /**
     * This methods sets how to treat the contents during a file transfer.
     *
     * The type supplied should be one of TYPE_AUTO, TYPE_TEXTUAL or TYPE_BINARY
     * constants. Default value is TYPE_AUTO.
     *
     * {@link FTPClient#TYPE_TEXTUAL} means that the data sent or received is
     * treated as textual information. This implies charset conversion during
     * the transfer.
     *
     * {@link FTPClient#TYPE_BINARY} means that the data sent or received is
     * treated as a binary stream. The data is taken "as is", without any
     * charset conversion.
     *
     * @param type The type.
     * @throws IllegalArgumentException If the supplied type is not valid.
     */
    public void setType(int type) throws IllegalArgumentException {
        if (type != TYPE_BINARY && type != TYPE_TEXTUAL) {
            throw new IllegalArgumentException("Invalid type");
        }
        synchronized (lock) {
            this.type = type;
        }
    }

    /**
     * This method returns the value suggesting how the client chooses whether
     * to use or not the MLSD command (RFC 3659) instead of the base LIST
     * command.
     *
     * @return The MLSD policy as a numeric value. The value could be compared
     * to the constants {@link FTPClient#MLSD_IF_SUPPORTED},
     * {@link FTPClient#MLSD_ALWAYS} and {@link FTPClient#MLSD_NEVER}.
     */
    public int getMLSDPolicy() {
        synchronized (lock) {
            return mlsdPolicy;
        }
    }

    /**
     * This method lets the user control how the client chooses whether to use
     * or not the MLSD command (RFC 3659) instead of the base LIST command.
     *
     * The type supplied should be one of MLSD_IF_SUPPORTED, MLSD_ALWAYS or
     * MLSD_NEVER constants. Default value is MLSD_IF_SUPPORTED.
     *
     * {@link FTPClient#MLSD_IF_SUPPORTED} means that the client should use the
     * MLSD command only if it is explicitly supported by the server.
     *
     * {@link FTPClient#MLSD_ALWAYS} means that the client should use always the
     * MLSD command, also if the MLSD command is not explicitly supported by the
     * server
     *
     * {@link FTPClient#MLSD_NEVER} means that the client should use always only
     * the LIST command, also if the MLSD command is explicitly supported by the
     * server.
     *
     * The support for the MLSD command is tested by the client after the
     * connection to the remote server, with the FEAT command.
     *
     * @param mlsdPolicy The MLSD policy.
     * @throws IllegalArgumentException If the supplied MLSD policy value is not valid.
     */
    public void setMLSDPolicy(int mlsdPolicy) throws IllegalArgumentException {
        if (type != MLSD_IF_SUPPORTED && type != MLSD_ALWAYS && type != MLSD_NEVER) {
            throw new IllegalArgumentException("Invalid MLSD policy");
        }
        synchronized (lock) {
            this.mlsdPolicy = mlsdPolicy;
        }
    }

    /**
     * Returns the name of the charset used to establish textual communications.
     * If not null the client will use always the given charset. If null the
     * client tries to auto-detect the server charset. If this attempt fails the
     * client will use the machine current charset.
     *
     * @return The name of the charset used to establish textual communications.
     */
    public String getCharset() {
        synchronized (lock) {
            return charset;
        }
    }

    /**
     * Sets the name of the charset used to establish textual communications. If
     * not null the client will use always the given charset. If null the client
     * tries to auto-detect the server charset. If this attempt fails the client
     * will use the machine current charset.
     *
     * @param charset The name of the charset used to establish textual
     *                communications.
     */
    public void setCharset(String charset) {
        synchronized (lock) {
            this.charset = charset;
            if (connected) {
                try {
                    communication.changeCharset(pickCharset());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Checks whether the connected server explicitly supports resuming of
     * broken data transfers.
     *
     * @return true if the server supports resuming, false otherwise.
     */
    public boolean isResumeSupported() {
        synchronized (lock) {
            return restSupported;
        }
    }

    /**
     * Checks whether the connected remote FTP server supports compressed data
     * transfers (uploads, downloads, list operations etc.). If so, the
     * compression of any subsequent data transfer (upload, download, list etc.)
     * can be compressed, saving bandwidth. To enable compression call
     * {@link FTPClient#setCompressionEnabled(boolean)} .
     *
     * The returned value is not significant if the client is not connected and
     * authenticated.
     *
     * @return <em>true</em> if compression of data transfers is supported on
     * the server-side, <em>false</em> otherwise.
     */
    public boolean isCompressionSupported() {
        return modezSupported;
    }

    /**
     * Checks whether the use of compression is enabled on the client-side.
     *
     * Please note that compressed transfers are actually enabled only if both
     * this method and {@link FTPClient#isCompressionSupported()} return
     * <em>true</em>.
     *
     * @return <em>true</em> if compression is enabled, <em>false</em>
     * otherwise.
     */
    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    /**
     * Enables or disables the use of compression during any subsequent data
     * transfer. Compression is enabled when both the supplied value and the
     * {@link FTPClient#isCompressionSupported()}) returned value are
     * <em>true</em>.
     *
     * The default value is <em>false</em>.
     *
     * @param compressionEnabled <em>true</em> to enable the use of compression during any
     *                           subsequent file transfer, <em>false</em> to disable the
     *                           feature.
     */
    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    /**
     * This method tests if this client is authenticated.
     *
     * @return true if this client is authenticated, false otherwise.
     */
    public boolean isAuthenticated() {
        synchronized (lock) {
            return authenticated;
        }
    }

    /**
     * This method tests if this client is connected to a remote FTP server.
     *
     * @return true if this client is connected to a remote FTP server, false
     * otherwise.
     */
    public boolean isConnected() {
        synchronized (lock) {
            return connected;
        }
    }

    /**
     * This method tests if this client works in passive FTP mode.
     *
     * @return true if this client is configured to work in passive FTP mode.
     */
    public boolean isPassive() {
        return passive;
    }

    /**
     * This method enables/disables the use of the passive mode.
     *
     * @param passive If true the passive mode is enabled.
     */
    public void setPassive(boolean passive) {
        this.passive = passive;
    }

    /**
     * If the client is connected, it reports the remote host name or address.
     *
     * @return The remote host name or address.
     */
    public String getHost() {
        return host;
    }

    /**
     * If the client is connected, it reports the remote port number.
     *
     * @return The remote port number.
     */
    public int getPort() {
        return port;
    }

    /**
     * If the client is authenticated, it reports the authentication password.
     *
     * @return The authentication password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * If the client is authenticated, it reports the authentication username.
     *
     * @return The authentication username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the duration of the auto-noop timeout, in milliseconds. If 0 or
     * less, the auto-noop feature is disabled.
     *
     * @return The duration of the auto-noop timeout, in milliseconds. If 0 or
     * less, the auto-noop feature is disabled.
     */
    public long getAutoNoopTimeout() {
        return autoNoopTimeout;
    }

    /**
     * Enable and disable the auto-noop feature.
     *
     * If the supplied value is greater than 0, the auto-noop feature is
     * enabled, otherwise it is disabled. If positive, the field is used as a
     * timeout value (expressed in milliseconds). If autoNoopDelay milliseconds
     * has passed without any communication between the client and the server, a
     * NOOP command is automaticaly sent to the server by the client.
     *
     * The default value for the auto noop delay is 0 (disabled).
     *
     * @param autoNoopTimeout The duration of the auto-noop timeout, in milliseconds. If 0
     *                        or less, the auto-noop feature is disabled.
     */
    public void setAutoNoopTimeout(long autoNoopTimeout) {
        synchronized (lock) {
            if (connected && authenticated) {
                stopAutoNoopTimer();
            }
            long oldValue = this.autoNoopTimeout;
            this.autoNoopTimeout = autoNoopTimeout;
            if (oldValue != 0 && autoNoopTimeout != 0 && nextAutoNoopTime > 0) {
                nextAutoNoopTime = nextAutoNoopTime - (oldValue - autoNoopTimeout);
            }
            if (connected && authenticated) {
                startAutoNoopTimer();
            }
        }
    }

    /**
     * This method adds a FTPCommunicationListener to the object.
     *
     * @param listener The listener.
     */
    public void addCommunicationListener(FTPCommunicationListener listener) {
        synchronized (lock) {
            communicationListeners.add(listener);
            if (communication != null) {
                communication.addCommunicationListener(listener);
            }
        }
    }

    /**
     * This method removes a FTPCommunicationListener previously added to the
     * object.
     *
     * @param listener The listener to be removed.
     */
    public void removeCommunicationListener(FTPCommunicationListener listener) {
        synchronized (lock) {
            communicationListeners.remove(listener);
            if (communication != null) {
                communication.removeCommunicationListener(listener);
            }
        }
    }

    /**
     * This method returns a list with all the {@link FTPCommunicationListener}
     * used by the client.
     *
     * @return A list with all the FTPCommunicationListener used by the client.
     */
    public FTPCommunicationListener[] getCommunicationListeners() {
        synchronized (lock) {
            int size = communicationListeners.size();
            FTPCommunicationListener[] ret = new FTPCommunicationListener[size];
            for (int i = 0; i < size; i++) {
                ret[i] = communicationListeners.get(i);
            }
            return ret;
        }
    }

    /**
     * This method adds a {@link FTPListParser} to the object.
     *
     * @param listParser The list parser.
     */
    public void addListParser(FTPListParser listParser) {
        synchronized (lock) {
            listParsers.add(listParser);
        }
    }

    /**
     * This method removes a {@link FTPListParser} previously added to the
     * object.
     *
     * @param listParser The list parser to be removed.
     */
    public void removeListParser(FTPListParser listParser) {
        synchronized (lock) {
            listParsers.remove(listParser);
        }
    }

    /**
     * This method returns a list with all the {@link FTPListParser} used by the
     * client.
     *
     * @return A list with all the FTPListParsers used by the client.
     */
    public FTPListParser[] getListParsers() {
        synchronized (lock) {
            int size = listParsers.size();
            FTPListParser[] ret = new FTPListParser[size];
            for (int i = 0; i < size; i++) {
                ret[i] = listParsers.get(i);
            }
            return ret;
        }
    }

    /**
     * This method connects the client to the remote FTP host, using the default
     * port value 21 (990 if security level is set to FTPS, see
     * {@link FTPClient#setSecurity(int)}).
     *
     * @param host The hostname of the remote server.
     * @return The server welcome message, one line per array element.
     * @throws IOException              If an I/O occurs.
     * @throws FTPException             If the server refuses the connection.
     */
    public List<String> connect(String host) throws IOException, FTPException {
        int def;
        if (security == SECURITY_FTPS) {
            def = 990;
        } else {
            def = 21;
        }
        return connect(host, def);
    }

    /**
     * This method connects the client to the remote FTP host.
     *
     * @param host The host name or address of the remote server.
     * @param port The port listened by the remote server.
     * @return The server welcome message, one line per array element.
     * @throws IOException              If an I/O occurs.
     * @throws FTPException             If the server refuses the connection.
     */
    public List<String> connect(String host, int port) throws IOException, FTPException {
        synchronized (lock) {
            // Is this client already connected to any host?
            if (connected) {
                throw new IOException("Client already connected to "
                        + host + " on port " + port);
            }
            Socket connection = null;
            try {
                if (this.connector != null) {
                    this.connector.close();
                }
                this.connector = new DirectConnector(communicationlogger);
                connection = connector.connectForCommunicationChannel(host, port);
                if (security == SECURITY_FTPS) {
                    connection = ssl(connection, host, port);
                }
                communication = new FTPCommunicationChannel(communicationlogger, connection, pickCharset());
                for (FTPCommunicationListener communicationListener : communicationListeners) {
                    communication.addCommunicationListener(communicationListener);
                }
                FTPReply wm = communication.readFTPReply();
                if (!wm.isSuccessCode()) {
                    throw new FTPException(wm);
                }
                this.connected = true;
                this.authenticated = false;
                this.parser = null;
                this.host = host;
                this.port = port;
                this.username = null;
                this.password = null;
                this.utf8Supported = false;
                this.restSupported = false;
                this.mlsdSupported = false;
                this.modezSupported = false;
                this.dataChannelEncrypted = false;
                return wm.getMessages();
            } finally {
                if (!connected) {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (Throwable t) {
                            //
                        }
                    }
                }
            }
        }
    }

    /**
     * Closes the current connection. It can be called by a secondary
     * thread while the client is blocked in a <em>connect()</em> call. The
     * connect() method will exit with an {@link IOException}.
     */
    public void close() {
        connector.close();
        connector = null;
    }

    /**
     * This method disconnects from the remote server, optionally performing the
     * QUIT procedure.
     *
     * @param sendQuitCommand If true the QUIT procedure with the server will be performed,
     *                        otherwise the connection is abruptly closed by the client
     *                        without sending any advice to the server.
     * @throws IOException              If an I/O occurs (can be thrown only if sendQuitCommand is
     *                                  true).
     * @throws FTPException             If the server refuses the QUIT command (can be thrown only if
     *                                  sendQuitCommand is true).
     */
    public void disconnect(boolean sendQuitCommand)
            throws IOException, FTPException {
        synchronized (lock) {
            if (!connected) {
                throw new FTPException("client not connected");
            }
            if (authenticated) {
                stopAutoNoopTimer();
            }
            if (sendQuitCommand) {
                communication.sendFTPCommand("QUIT");
                FTPReply r = communication.readFTPReply();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
            communication.close();
            communication = null;
            connector.close();
            connector = null;
            connected = false;
        }
    }

    public void login()
            throws IOException, FTPException {
        login("anonymous", "foobar@foo.bar", null);
    }

    /**
     * This method authenticates the user against the server.
     *
     * @param username The username.
     * @param password The password (if none set it to null).
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If login fails.
     */
    public void login(String username, String password)
            throws IOException, FTPException {
        login(username, password, null);
    }

    /**
     * This method authenticates the user against the server.
     *
     * @param username The username.
     * @param password The password (if none set it to null).
     * @param account  The account (if none set it to null). Be careful: some servers
     *                 don't implement this feature.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If login fails.
     */
    public void login(String username, String password, String account)
            throws  IOException, FTPException {
        synchronized (lock) {
            if (!connected) {
                throw new FTPException("client not connected");
            }
            if (security == SECURITY_FTPES) {
                communication.sendFTPCommand("AUTH TLS");
                FTPReply r = communication.readFTPReply();
                if (r.isSuccessCode()) {
                    communication.ssl(sslSocketFactory);
                } else {
                    communication.sendFTPCommand("AUTH SSL");
                    r = communication.readFTPReply();
                    if (r.isSuccessCode()) {
                        communication.ssl(sslSocketFactory);
                    } else {
                        throw new FTPException(r.getCode(), "SECURITY_FTPES cannot be applied: " +
                                "the server refused both AUTH TLS and AUTH SSL commands");
                    }
                }
            }
            authenticated = false;
            boolean passwordRequired;
            boolean accountRequired;
            communication.sendFTPCommand("USER " + username);
            FTPReply r = communication.readFTPReply();
            switch (r.getCode()) {
                case 230:
                    passwordRequired = false;
                    accountRequired = false;
                    break;
                case 331:
                    passwordRequired = true;
                    accountRequired = false;
                    break;
                case 332:
                    passwordRequired = false;
                    accountRequired = true;
                    break;
                default:
                    throw new FTPException(r);
            }
            if (passwordRequired) {
                if (password == null) {
                    throw new FTPException(331);
                }
                communication.sendFTPCommand("PASS " + password);
                r = communication.readFTPReply();
                switch (r.getCode()) {
                    case 230:
                        accountRequired = false;
                        break;
                    case 332:
                        accountRequired = true;
                        break;
                    default:
                        throw new FTPException(r);
                }
            }
            if (accountRequired) {
                if (account == null) {
                    throw new FTPException(332);
                }
                communication.sendFTPCommand("ACCT " + account);
                r = communication.readFTPReply();
                switch (r.getCode()) {
                    case 230:
                        break;
                    default:
                        throw new FTPException(r);
                }
            }
            this.authenticated = true;
            this.username = username;
            this.password = password;
        }
        postLoginOperations();
        startAutoNoopTimer();
    }

    private void ensureConnected() throws FTPException {
        if (!connected) {
            throw new FTPException("client not connected");
        }
        if (!authenticated) {
            throw new FTPException("client not authenticated");
        }
    }

    /**
     * Performs some post-login operations, such trying to detect server support
     * for utf8.
     *
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If login fails.
     */
    private void postLoginOperations()
            throws IOException, FTPException {
        synchronized (lock) {
            utf8Supported = false;
            restSupported = false;
            mlsdSupported = false;
            modezSupported = false;
            dataChannelEncrypted = false;
            communication.sendFTPCommand("FEAT");
            FTPReply r = communication.readFTPReply();
            if (r.getCode() == 211) {
                List<String> lines = r.getMessages();
                for (int i = 1; i < lines.size() - 1; i++) {
                    String feat = lines.get(i).trim().toUpperCase();
                    if ("REST STREAM".equalsIgnoreCase(feat)) {
                        restSupported = true;
                        continue;
                    }
                    if ("UTF8".equalsIgnoreCase(feat)) {
                        utf8Supported = true;
                        communication.changeCharset("UTF-8");
                        continue;
                    }
                    if ("MLSD".equalsIgnoreCase(feat)) {
                        mlsdSupported = true;
                        continue;
                    }
                    if ("MODE Z".equalsIgnoreCase(feat) || feat.startsWith("MODE Z ")) {
                        modezSupported = true;
                    }
                }
            }
            if (utf8Supported) {
                communication.sendFTPCommand("OPTS UTF8 ON");
                communication.readFTPReply();
            }
            if (security == SECURITY_FTPS || security == SECURITY_FTPES) {
                communication.sendFTPCommand("PBSZ 0");
                communication.readFTPReply();
                communication.sendFTPCommand("PROT P");
                FTPReply reply = communication.readFTPReply();
                if (reply.isSuccessCode()) {
                    dataChannelEncrypted = true;
                }
            }
        }
    }

    /**
     * This method performs a logout operation for the current user, leaving the
     * connection open, thus it can be used to start a new user session. Be
     * careful with this: some FTP servers don't implement this feature, even
     * though it is a standard FTP one.
     *
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void rein() throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("REIN");
            FTPReply r = communication.readFTPReply();
            if (r.isSuccessCode()) {
                stopAutoNoopTimer();
                authenticated = false;
                username = null;
                password = null;
            }
        }
    }

    /**
     * This method performs a "noop" operation with the server.
     *
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If login fails.
     */
    public void noop() throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            try {
                communication.sendFTPCommand("NOOP");
                FTPReply r = communication.readFTPReply();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            } finally {
                touchAutoNoopTimer();
            }
        }
    }

    /**
     * This method sends a SITE specific command to the server.
     *
     * @param command The site command.
     * @return The reply supplied by the server, parsed and served in an object
     * way mode.
     * @throws IOException              If a I/O error occurs.
     */
    public FTPReply site(String command)
            throws IOException, FTPException {
        synchronized (lock) {
            if (!connected) {
                throw new FTPException("client not connected");
            }
            communication.sendFTPCommand("SITE " + command);
            touchAutoNoopTimer();
            return communication.readFTPReply();
        }
    }

    /**
     * Call this method to switch the user current account. Be careful with
     * this: some FTP servers don't implement this feature, even though it is a
     * standard FTP one.
     *
     * @param account The account.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If login fails.
     */
    public void acct(String account) throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("ACCT " + account);
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    /**
     * This method asks and returns the current working directory.
     *
     * @return path The path to the current working directory.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public String pwd() throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("PWD");
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            List<String> messages = r.getMessages();
            if (messages.size() != 1) {
                throw new FTPException("illegal reply");
            }
            Matcher m = PWD_PATTERN.matcher(messages.get(0));
            if (m.find()) {
                return messages.get(0).substring(m.start() + 1, m.end() - 1);
            } else {
                throw new FTPException("illegal reply");
            }
        }
    }

    /**
     * This method changes the current working directory.
     *
     * @param path The path to the new working directory.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void cwd(String path) throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("CWD " + path);
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    /**
     * This method changes the current working directory to the parent one.
     *
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void cdup() throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("CDUP");
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    /**
     * This method asks and returns the last modification date of a file or
     * directory.
     *
     * @param path The path to the file or the directory.
     * @return The file/directory last modification date.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public Date mdtm(String path) throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            // Sends the MDTM command.
            communication.sendFTPCommand("MDTM " + path);
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            List<String> messages = r.getMessages();
            if (messages.size() != 1) {
                throw new FTPException("illegal reply");
            } else {
                try {
                    return MDTM_DATE_FORMAT.parse(messages.get(0));
                } catch (ParseException e) {
                    throw new FTPException("illegal reply");
                }
            }
        }
    }

    /**
     * This method asks and returns a file size in bytes.
     *
     * @param path The path to the file.
     * @return The file size in bytes.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public long size(String path) throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("TYPE I");
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            communication.sendFTPCommand("SIZE " + path);
            r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            List<String> messages = r.getMessages();
            if (messages.size() != 1) {
                throw new FTPException("illegal reply");
            } else {
                try {
                    return Long.parseLong(messages.get(0));
                } catch (Throwable t) {
                    throw new FTPException("illegal reply");
                }
            }
        }
    }

    /**
     * This method renames a remote file or directory. It can also be used to
     * move a file or a directory.
     *
     * In example:
     *
     * <pre>
     * client.rename(&quot;oldname&quot;, &quot;newname&quot;); // This one renames
     * </pre>
     *
     * <pre>
     * client.rename(&quot;the/old/path/oldname&quot;, &quot;/a/new/path/newname&quot;); // This one moves
     * </pre>
     *
     * @param oldPath The current path of the file (or directory).
     * @param newPath The new path for the file (or directory).
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void rename(String oldPath, String newPath)
            throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("RNFR " + oldPath);
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (r.getCode() != 350) {
                throw new FTPException(r);
            }
            communication.sendFTPCommand("RNTO " + newPath);
            r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    /**
     * This method deletes a remote file.
     *
     * @param path The path to the file.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void dele(String path)
            throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("DELE " + path);
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    /**
     * This method deletes a remote directory.
     *
     * @param path The path to the directory.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void rmd(String path)
            throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("RMD " + path);
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    /**
     * This method creates a new remote directory in the current working one.
     *
     * @param directoryName The name of the new directory.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void mkd(String directoryName)
            throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("MKD " + directoryName);
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    /**
     * This method calls the HELP command on the remote server, returning a list
     * of lines with the help contents.
     *
     * @return The help contents, splitted by line.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public List<String> help() throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("HELP");
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            return r.getMessages();
        }
    }

    /**
     * This method returns the remote server status, as the result of a FTP STAT
     * command.
     *
     * @return The remote server status, splitted by line.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public List<String> stat() throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("STAT");
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            return r.getMessages();
        }
    }

    /**
     * This method lists the entries of the current working directory parsing
     * the reply to a FTP LIST command.
     *
     * The response to the LIST command is parsed through the FTPListParser
     * objects registered on the client. Some
     * standard parsers are already registered on every FTPClient object created. If
     * they don't work in your case (a FTPListParseException is thrown), you can
     * build your own parser implementing the FTPListParser interface and add it
     * to the client by calling its addListParser() method.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The list() method will break with a
     * FTPAbortedException.
     *
     * @return The list of the files (and directories) in the current working
     * directory.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public Map<String,FTPEntry> list() throws IOException, FTPException {
        return list(null);
    }

    /**
     * This method lists the entries of the current working directory parsing
     * the reply to a FTP LIST command.
     *
     * The response to the LIST command is parsed through the FTPListParser
     * objects registered on the client. Some standard parsers are already
     * registered on every FTPClient object created. If
     * they don't work in your case (a FTPListParseException is thrown), you can
     * build your own parser implementing the FTPListParser interface and add it
     * to the client by calling its addListParser() method.
     *
     * Calling this method blocks the current thread until the operation is
     * completed.
     *
     * @param fileSpec A file filter string. Depending on the server implementation,
     *                 wildcard characters could be accepted.
     * @return The list of the files (and directories) in the current working
     * directory.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public Map<String,FTPEntry> list(String fileSpec) throws IOException, FTPException {
        ensureConnected();
        Map<String,FTPEntry> ret = null;
        synchronized (lock) {
            communication.sendFTPCommand("TYPE A");
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            DataConnector connection = openDataTransferChannel();
            boolean mlsdCommand;
            if (mlsdPolicy == MLSD_IF_SUPPORTED) {
                mlsdCommand = mlsdSupported;
            } else {
                mlsdCommand = mlsdPolicy == MLSD_ALWAYS;
            }
            String command = mlsdCommand ? "MLSD" : "LIST";
            if (fileSpec != null && fileSpec.length() > 0) {
                command += " " + fileSpec;
            }
            List<String> lines = new ArrayList<>();
            boolean wasAborted = false;
            InputStream dataTransferInputStream = null;
            communication.sendFTPCommand(command);
            try {
                Socket socket = connection.openDataConnection();
                synchronized (abortLock) {
                    ongoingDataTransfer = true;
                    aborted = false;
                    consumeAborCommandReply = false;
                }
                NetworkVirtualTerminalASCIIReader dataReader = null;
                try {
                    dataTransferInputStream = socket.getInputStream();
                    if (modezEnabled) {
                        dataTransferInputStream = new InflaterInputStream(dataTransferInputStream);
                    }
                    dataReader = new NetworkVirtualTerminalASCIIReader(dataTransferInputStream, mlsdCommand ? "UTF-8" : pickCharset());
                    String line;
                    while ((line = dataReader.readLine()) != null) {
                        if (line.length() > 0) {
                            lines.add(line);
                        }
                    }
                } catch (IOException e) {
                    synchronized (abortLock) {
                        if (aborted) {
                            throw new FTPException("aborted");
                        } else {
                            throw e;
                        }
                    }
                } finally {
                    if (dataReader != null) {
                        try {
                            dataReader.close();
                        } catch (Throwable t) {
                            logger.error(t.getMessage(), t);
                        }
                    }
                    try {
                        if (!socket.isClosed()) {
                            socket.close();
                            logger.debug("socket close {}", socket.getLocalAddress());
                        }
                    } catch (Throwable t) {
                        logger.error(t.getMessage(), t);
                    }
                    synchronized (abortLock) {
                        wasAborted = aborted;
                        ongoingDataTransfer = false;
                        aborted = false;
                    }
                }
            } finally {
                r = communication.readFTPReply();
                touchAutoNoopTimer();
                if (r.getCode() != 150 && r.getCode() != 125) {
                    throw new FTPException(r);
                }
                // Consumes the result reply of the transfer.
                r = communication.readFTPReply();
                if (!wasAborted && r.getCode() != 226) {
                    throw new FTPException(r);
                }
                // ABOR command response (if needed).
                if (consumeAborCommandReply) {
                    communication.readFTPReply();
                    consumeAborCommandReply = false;
                }
            }
            if (mlsdCommand) {
                MLSDListParser parser = new MLSDListParser();
                ret = parser.parse(lines).stream()
                        .collect(Collectors.toMap(FTPEntry::getName, entry -> entry));
            } else {
                if (parser != null) {
                    try {
                        ret = parser.parse(lines).stream()
                                .collect(Collectors.toMap(FTPEntry::getName, entry -> entry));
                    } catch (FTPException e) {
                        parser = null;
                    }
                }
                if (ret == null) {
                    for (Object listParser : listParsers) {
                        FTPListParser aux = (FTPListParser) listParser;
                        try {
                            ret = aux.parse(lines).stream()
                                    .collect(Collectors.toMap(FTPEntry::getName, entry -> entry));
                            parser = aux;
                            break;
                        } catch (FTPException e) {
                            // ignore
                        }
                    }
                }
            }
            if (ret == null) {
                throw new FTPException("parse");
            } else {
                return ret;
            }
        }
    }

    /**
     * This method lists the entries of the current working directory with a FTP
     * NLST command.
     *
     * The response consists in an array of string, each one reporting the name
     * of a file or a directory placed in the current working directory. For a
     * more detailed directory listing procedure look at the list() method.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The listNames() method will break with a
     * FTPAbortedException.
     *
     * @return The list of the files (and directories) in the current working
     * directory.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public List<String> nlst() throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            communication.sendFTPCommand("TYPE A");
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            List<String> lines = new ArrayList<>();
            boolean wasAborted = false;
            InputStream dataTransferInputStream = null;
            DataConnector connection = openDataTransferChannel();
            communication.sendFTPCommand("NLST");
            try {
                Socket socket = connection.openDataConnection();
                connection.close();
                synchronized (abortLock) {
                    ongoingDataTransfer = true;
                    aborted = false;
                    consumeAborCommandReply = false;
                }
                NetworkVirtualTerminalASCIIReader dataReader = null;
                try {
                    dataTransferInputStream = socket.getInputStream();
                    if (modezEnabled) {
                        dataTransferInputStream = new InflaterInputStream(dataTransferInputStream);
                    }
                    dataReader = new NetworkVirtualTerminalASCIIReader(dataTransferInputStream, pickCharset());
                    String line;
                    while ((line = dataReader.readLine()) != null) {
                        if (line.length() > 0) {
                            lines.add(line);
                        }
                    }
                    logger.debug("lines={}", lines);
                } catch (IOException e) {
                    synchronized (abortLock) {
                        if (aborted) {
                            throw new FTPException("aborted");
                        } else {
                            throw e;
                        }
                    }
                } finally {
                    if (dataReader != null) {
                        try {
                            dataReader.close();
                        } catch (Throwable t) {
                            logger.error(t.getMessage(), t);
                        }
                    }
                    try {
                        if (!socket.isClosed()) {
                            socket.close();
                            logger.debug("socket close {}", socket.getLocalAddress());
                        }
                    } catch (Throwable t) {
                        logger.error(t.getMessage(), t);
                    }
                    synchronized (abortLock) {
                        wasAborted = aborted;
                        ongoingDataTransfer = false;
                        aborted = false;
                    }
                }
            } finally {
                r = communication.readFTPReply();
                if (r.getCode() != 150 && r.getCode() != 125) {
                    throw new FTPException(r);
                }
                r = communication.readFTPReply();
                if (!wasAborted && r.getCode() != 226) {
                    throw new FTPException(r);
                }
                if (consumeAborCommandReply) {
                    communication.readFTPReply();
                    consumeAborCommandReply = false;
                }
            }
            return lines;
        }
    }

    /**
     * This method uploads a file to the remote server.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The method will break with a
     * FTPAbortedException.
     *
     * @param input The file to upload.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void stor(String name, InputStream input) throws IOException, FTPException {
        stor(name, input, 0L, 0L, null);
    }

    /**
     * This method uploads a file to the remote server.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The method will break with a
     * FTPAbortedException.
     *
     * @param input     The file to upload.
     * @param listener The listener for the operation. Could be null.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void stor(String name, InputStream input, FTPDataTransferListener listener)
            throws IOException, FTPException {
        stor(name, input, 0L, 0L, listener);
    }

    /**
     * This method uploads a file to the remote server.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The method will break with a
     * FTPAbortedException.
     *
     * @param input      The file to upload.
     * @param restartAt The restart point (number of bytes already uploaded). Use
     *                  {@link FTPClient#isResumeSupported()} to check if the server
     *                  supports resuming of broken data transfers.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void stor(String name, InputStream input, long restartAt) throws IOException, FTPException {
        stor(name, input, restartAt, 0L, null);
    }

    /**
     * This method uploads a content to the remote server.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The method will break with a
     * FTPAbortedException.
     *
     * @param name     The name of the remote file.
     * @param inputStream  The source of data.
     * @param restartAt    The restart point (number of bytes already uploaded). Use
     *                     {@link FTPClient#isResumeSupported()} to check if the server
     *                     supports resuming of broken data transfers.
     * @param streamOffset The offset to skip in the stream.
     * @param listener     The listener for the operation. Could be null.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void stor(String name, InputStream inputStream,
                       long restartAt, long streamOffset, FTPDataTransferListener listener)
            throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            int tp = type;
            if (tp == TYPE_TEXTUAL) {
                communication.sendFTPCommand("TYPE A");
            } else if (tp == TYPE_BINARY) {
                communication.sendFTPCommand("TYPE I");
            }
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            DataConnector connection = openDataTransferChannel();
            if (restSupported || restartAt > 0) {
                communication.sendFTPCommand("REST " + restartAt);
                r = communication.readFTPReply();
                touchAutoNoopTimer();
                if (r.getCode() != 350 && ((r.getCode() != 501 && r.getCode() != 502) || restartAt > 0)) {
                    throw new FTPException(r);
                }
            }
            boolean wasAborted = false;
            communication.sendFTPCommand("STOR " + name);
            OutputStream outputStream = null;
            try {
                Socket socket = connection.openDataConnection();
                synchronized (abortLock) {
                    ongoingDataTransfer = true;
                    aborted = false;
                    consumeAborCommandReply = false;
                }
                try {
                    inputStream.skip(streamOffset);
                    outputStream = new BufferedOutputStream(socket.getOutputStream(), SEND_AND_RECEIVE_BUFFER_SIZE);
                    if (modezEnabled) {
                        logger.debug("using compressed output");
                        outputStream = new DeflaterOutputStream(outputStream);
                    }
                    if (listener != null) {
                        listener.started();
                    }
                    if (tp == TYPE_TEXTUAL) {
                        Reader reader = new InputStreamReader(inputStream);
                        Writer writer = new OutputStreamWriter(outputStream, pickCharset());
                        char[] buffer = new char[SEND_AND_RECEIVE_BUFFER_SIZE];
                        int l;
                        while ((l = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, l);
                            writer.flush();
                            if (listener != null) {
                                listener.transferred(l);
                            }
                        }
                    } else if (tp == TYPE_BINARY) {
                        byte[] buffer = new byte[SEND_AND_RECEIVE_BUFFER_SIZE];
                        int l;
                        while ((l = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, l);
                            if (listener != null) {
                                listener.transferred(l);
                            }
                        }
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    synchronized (abortLock) {
                        if (aborted) {
                            if (listener != null) {
                                listener.aborted();
                            }
                            throw new FTPException("aborted");
                        } else {
                            if (listener != null) {
                                listener.failed();
                            }
                            throw e;
                        }
                    }
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (Throwable t) {
                            logger.error(t.getMessage(), t);
                        }
                    }
                    try {
                        if (!socket.isClosed()) {
                            socket.close();
                            logger.debug("socket closed {}", socket.getLocalAddress());
                        }
                    } catch (Throwable t) {
                        logger.error(t.getMessage(), t);
                    }
                    synchronized (abortLock) {
                        wasAborted = aborted;
                        ongoingDataTransfer = false;
                        aborted = false;
                    }
                }
            } finally {
                r = communication.readFTPReply();
                touchAutoNoopTimer();
                if (r.getCode() != 150 && r.getCode() != 125) {
                    throw new FTPException(r);
                }
                r = communication.readFTPReply();
                if (!wasAborted && r.getCode() != 226) {
                    throw new FTPException(r);
                }
                if (consumeAborCommandReply) {
                    communication.readFTPReply();
                    consumeAborCommandReply = false;
                }
            }
            if (listener != null) {
                listener.completed();
            }
        }
    }

    /**
     * This method appends the contents of a local file to an existing file on
     * the remote server.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The method will break with a
     * FTPAbortedException.
     *
     * @param input The local file whose contents will be appended to the remote
     *             file.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void appe(String name, InputStream input)
            throws IOException, FTPException {
        appe(name, input, null);
    }

    /**
     * This method uploads a file to the remote server.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The method will break with a
     * FTPAbortedException.
     *
     * @param input     The local file whose contents will be appended to the remote
     *                 file.
     * @param listener The listener for the operation. Could be null.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void appe(String name, InputStream input, FTPDataTransferListener listener)
            throws IOException, FTPException {
        append(name, input, 0, listener);
    }

    /**
     * This method appends data to an existing file on the remote server.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The method will break with a
     * FTPAbortedException.
     *
     * @param name     The name of the remote file.
     * @param inputStream  The source of data.
     * @param streamOffset The offset to skip in the stream.
     * @param listener     The listener for the operation. Could be null.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void append(String name, InputStream inputStream,
                       long streamOffset, FTPDataTransferListener listener)
            throws IOException, FTPException {
        ensureConnected();
        synchronized (lock) {
            int tp = type;
            if (tp == TYPE_TEXTUAL) {
                communication.sendFTPCommand("TYPE A");
            } else if (tp == TYPE_BINARY) {
                communication.sendFTPCommand("TYPE I");
            }
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            boolean wasAborted = false;
            DataConnector connection = openDataTransferChannel();
            OutputStream outputStream = null;
            communication.sendFTPCommand("APPE " + name);
            try {
                Socket socket = connection.openDataConnection();
                synchronized (abortLock) {
                    ongoingDataTransfer = true;
                    aborted = false;
                    consumeAborCommandReply = false;
                }
                try {
                    inputStream.skip(streamOffset);
                    outputStream = socket.getOutputStream();
                    if (modezEnabled) {
                        outputStream = new DeflaterOutputStream(outputStream);
                    }
                    if (listener != null) {
                        listener.started();
                    }
                    if (tp == TYPE_TEXTUAL) {
                        Reader reader = new InputStreamReader(inputStream);
                        Writer writer = new OutputStreamWriter(
                                outputStream, pickCharset());
                        char[] buffer = new char[SEND_AND_RECEIVE_BUFFER_SIZE];
                        int l;
                        while ((l = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, l);
                            writer.flush();
                            if (listener != null) {
                                listener.transferred(l);
                            }
                        }
                    } else if (tp == TYPE_BINARY) {
                        byte[] buffer = new byte[SEND_AND_RECEIVE_BUFFER_SIZE];
                        int l;
                        while ((l = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, l);
                            outputStream.flush();
                            if (listener != null) {
                                listener.transferred(l);
                            }
                        }
                    }
                } catch (IOException e) {
                    synchronized (abortLock) {
                        if (aborted) {
                            if (listener != null) {
                                listener.aborted();
                            }
                            throw new FTPException("aborted");
                        } else {
                            if (listener != null) {
                                listener.failed();
                            }
                            throw e;
                        }
                    }
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (Throwable t) {
                            logger.error(t.getMessage(), t);
                        }
                    }
                    try {
                        if (!socket.isClosed()) {
                            socket.close();
                            logger.debug("socket closed {}", socket.getLocalAddress());
                        }
                    } catch (Throwable t) {
                        logger.error(t.getMessage(), t);
                    }
                    synchronized (abortLock) {
                        wasAborted = aborted;
                        ongoingDataTransfer = false;
                        aborted = false;
                    }
                }
            } finally {
                r = communication.readFTPReply();
                touchAutoNoopTimer();
                if (r.getCode() != 150 && r.getCode() != 125) {
                    throw new FTPException(r);
                }
                // Consumes the result reply of the transfer.
                r = communication.readFTPReply();
                if (!wasAborted && r.getCode() != 226) {
                    throw new FTPException(r);
                }
                // ABOR command response (if needed).
                if (consumeAborCommandReply) {
                    communication.readFTPReply();
                    consumeAborCommandReply = false;
                }
            }
            // Notifies the listener.
            if (listener != null) {
                listener.completed();
            }
        }
    }

    /**
     * This method downloads a remote file from the server to a local file.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The method will break with a
     * FTPAbortedException.
     *
     * @param name The name of the file to download.
     * @param out      The local file.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void retr(String name, OutputStream out)
            throws IOException, FTPException {
        retr(name, out, 0, null);
    }

    /**
     * This method downloads a remote file from the server to a local file.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The method will break with a
     * FTPAbortedException.
     *
     * @param name The name of the file to download.
     * @param output      The local file.
     * @param listener       The listener for the operation. Could be null.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void retr(String name, OutputStream output, FTPDataTransferListener listener)
            throws IOException, FTPException {
        retr(name, output, 0, listener);
    }

    /**
     * This method resumes a download operation from the remote server to a
     * local file.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The method will break with a
     * FTPAbortedException.
     *
     * @param name The name of the file to download.
     * @param output      The local file.
     * @param restartAt      The restart point (number of bytes already downloaded). Use
     *                       {@link FTPClient#isResumeSupported()} to check if the server
     *                       supports resuming of broken data transfers.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void retr(String name, OutputStream output, long restartAt)
            throws IOException, FTPException {
        retr(name, output, restartAt, null);
    }

    /**
     * This method resumes a download operation from the remote server.
     *
     * Calling this method blocks the current thread until the operation is
     * completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The method will break with a
     * FTPAbortedException.
     *
     * @param name     The name of the remote file.
     * @param outputStream The destination stream of data read during the download.
     * @param restartAt    The restart point (number of bytes already downloaded). Use
     *                     {@link FTPClient#isResumeSupported()} to check if the server
     *                     supports resuming of broken data transfers.
     * @param listener     The listener for the operation. Could be null.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException             If the operation fails.
     */
    public void retr(String name, OutputStream outputStream,
                         long restartAt, FTPDataTransferListener listener)
            throws IOException, FTPException {
        if (name == null) {
            return;
        }
        ensureConnected();
        synchronized (lock) {
            int tp = type;
            if (tp == TYPE_TEXTUAL) {
                communication.sendFTPCommand("TYPE A");
            } else if (tp == TYPE_BINARY) {
                communication.sendFTPCommand("TYPE I");
            }
            FTPReply r = communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            DataConnector connection = openDataTransferChannel();
            if (restSupported || restartAt > 0) {
                communication.sendFTPCommand("REST " + restartAt);
                r = communication.readFTPReply();
                touchAutoNoopTimer();
                if (r.getCode() != 350 && ((r.getCode() != 501 && r.getCode() != 502) || restartAt > 0)) {
                    throw new FTPException(r);
                }
            }
            boolean wasAborted = false;
            InputStream inputStream = null;
            communication.sendFTPCommand("RETR " + name);
            try {
                Socket socket = connection.openDataConnection();
                synchronized (abortLock) {
                    ongoingDataTransfer = true;
                    aborted = false;
                    consumeAborCommandReply = false;
                }
                try {
                    inputStream = new BufferedInputStream(socket.getInputStream());
                    if (modezEnabled) {
                        inputStream = new InflaterInputStream(inputStream);
                    }
                    if (listener != null) {
                        listener.started();
                    }
                    if (tp == TYPE_TEXTUAL) {
                        Reader reader = new InputStreamReader(inputStream, pickCharset());
                        Writer writer = new OutputStreamWriter(outputStream);
                        char[] buffer = new char[SEND_AND_RECEIVE_BUFFER_SIZE];
                        int l;
                        while ((l = reader.read(buffer, 0, buffer.length)) != -1) {
                            writer.write(buffer, 0, l);
                            writer.flush();
                            if (listener != null) {
                                listener.transferred(l);
                            }
                        }
                    } else if (tp == TYPE_BINARY) {
                        byte[] buffer = new byte[SEND_AND_RECEIVE_BUFFER_SIZE];
                        int l;
                        while ((l = inputStream.read(buffer, 0, buffer.length)) != -1) {
                            outputStream.write(buffer, 0, l);
                            if (listener != null) {
                                listener.transferred(l);
                            }
                        }
                    }
                } catch (IOException e) {
                    synchronized (abortLock) {
                        if (aborted) {
                            if (listener != null) {
                                listener.aborted();
                            }
                            throw new FTPException("abort");
                        } else {
                            if (listener != null) {
                                listener.failed();
                            }
                            throw e;
                        }
                    }
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable t) {
                            logger.error(t.getMessage(), t);
                        }
                    }
                    try {
                        if (!socket.isClosed()) {
                            socket.close();
                            logger.debug("socket closed {}", socket.getLocalAddress());
                        }
                    } catch (Throwable t) {
                        logger.error(t.getMessage(), t);
                    }
                    synchronized (abortLock) {
                        wasAborted = aborted;
                        ongoingDataTransfer = false;
                        aborted = false;
                    }
                }
            } finally {
                r = communication.readFTPReply();
                touchAutoNoopTimer();
                if (r.getCode() != 150 && r.getCode() != 125) {
                    throw new FTPException(r);
                }
                r = communication.readFTPReply();
                if (!wasAborted && r.getCode() != 226) {
                    throw new FTPException(r);
                }
                if (consumeAborCommandReply) {
                    communication.readFTPReply();
                    consumeAborCommandReply = false;
                }
            }
            if (listener != null) {
                listener.completed();
            }
        }
    }

    private DataConnector openDataTransferChannel()
            throws IOException, FTPException {
        if (modezSupported && compressionEnabled) {
            if (!modezEnabled) {
                communication.sendFTPCommand("MODE Z");
                FTPReply r = communication.readFTPReply();
                touchAutoNoopTimer();
                if (r.isSuccessCode()) {
                    modezEnabled = true;
                }
            }
        } else {
            if (modezEnabled) {
                communication.sendFTPCommand("MODE S");
                FTPReply r = communication.readFTPReply();
                touchAutoNoopTimer();
                if (r.isSuccessCode()) {
                    modezEnabled = false;
                }
            }
        }
        if (passive) {
            return openPassiveDataTransferChannel();
        } else {
            return openActiveDataTransferChannel();
        }
    }

    /**
     * This method opens a data transfer channel in active mode.
     */
    private DataConnector openActiveDataTransferChannel()
            throws IOException, FTPException {
        ActiveDataConnector dataConnection = new ActiveDataConnector(6000, 7000);
        try {
            dataConnection.openDataConnection();
        } catch (Throwable t) {
            dataConnection.close();
        }
        int port = dataConnection.getPort();
        int p1 = port >>> 8;
        int p2 = port & 0xff;
        int[] addr = pickLocalAddress();
        communication.sendFTPCommand("PORT " + addr[0] + "," + addr[1] + "," + addr[2] + "," +
                addr[3] + "," + p1 + "," + p2);
        FTPReply r = communication.readFTPReply();
        touchAutoNoopTimer();
        if (!r.isSuccessCode()) {
            try {
                dataConnection.close();
            } catch (Throwable t) {
                //
            }
            throw new FTPException(r);
        }
        return dataConnection;
    }

    /**
     * This method opens a data transfer channel in passive mode.
     */
    private DataConnector openPassiveDataTransferChannel()
            throws IOException, FTPException {
        communication.sendFTPCommand("PASV");
        FTPReply r = communication.readFTPReply();
        if (!r.isSuccessCode()) {
            throw new FTPException(r);
        }
        touchAutoNoopTimer();
        String addressAndPort = null;
        List<String> messages = r.getMessages();
        for (String message : messages) {
            Matcher m = PASV_PATTERN.matcher(message);
            if (m.find()) {
                int start = m.start();
                int end = m.end();
                addressAndPort = message.substring(start, end);
                break;
            }
        }
        if (addressAndPort == null) {
            throw new FTPException("no address/port");
        }
        StringTokenizer st = new StringTokenizer(addressAndPort, ",");
        int b1 = Integer.parseInt(st.nextToken());
        int b2 = Integer.parseInt(st.nextToken());
        int b3 = Integer.parseInt(st.nextToken());
        int b4 = Integer.parseInt(st.nextToken());
        int p1 = Integer.parseInt(st.nextToken());
        int p2 = Integer.parseInt(st.nextToken());
        final String pasvHost = b1 + "." + b2 + "." + b3 + "." + b4;
        final int pasvPort = (p1 << 8) | p2;
        return new PassiveDataConnector(connector, pasvHost, pasvPort);
    }

    /**
     * If there's any ongoing data transfer operation, this method aborts it.
     *
     * @param sendAborCommand If true the client will negotiate the abort procedure with the
     *                        server, through the standard FTP ABOR command. Otherwise the
     *                        open data transfer connection will be closed without any
     *                        advise has sent to the server.
     * @throws IOException              If the ABOR command cannot be sent due to any I/O error. This
     *                                  could happen only if force is false.
     */
    public void abortCurrentDataTransfer(boolean sendAborCommand)
            throws IOException, FTPException {
        synchronized (abortLock) {
            if (ongoingDataTransfer && !aborted) {
                if (sendAborCommand) {
                    communication.sendFTPCommand("ABOR");
                    touchAutoNoopTimer();
                    consumeAborCommandReply = true;
                }
                connector.close();
                aborted = true;
            }
        }
    }

    /**
     * Returns the name of the charset that should be used in textual
     * transmissions.
     *
     * @return The name of the charset that should be used in textual
     * transmissions.
     */
    private String pickCharset() {
        if (charset != null) {
            return charset;
        } else if (utf8Supported) {
            return "UTF-8";
        } else {
            return System.getProperty("file.encoding");
        }
    }

    /**
     * Picks the local address for an active data transfer operation.
     *
     * @return The local address as a 4 integer values array.
     * @throws IOException If an unexpected I/O error occurs while trying to resolve the
     *                     local address.
     */
    private int[] pickLocalAddress() throws IOException {
        return pickAutoDetectedLocalAddress();
    }

    /**
     * Auto-detects the local network address, and returns it in the form of a 4
     * elements integer array.
     *
     * @return The detected local address.
     * @throws IOException If an unexpected I/O error occurs while trying to resolve the
     *                     local address.
     */
    private int[] pickAutoDetectedLocalAddress() throws IOException {
        InetAddress addressObj = InetAddress.getLocalHost();
        byte[] addr = addressObj.getAddress();
        int b1 = addr[0] & 0xff;
        int b2 = addr[1] & 0xff;
        int b3 = addr[2] & 0xff;
        int b4 = addr[3] & 0xff;
        return new int[]{b1, b2, b3, b4};
    }

    public String toString() {
        synchronized (lock) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(getClass().getName());
            buffer.append(" [connected=");
            buffer.append(connected);
            if (connected) {
                buffer.append(", host=");
                buffer.append(host);
                buffer.append(", port=");
                buffer.append(port);
            }
            buffer.append(", connector=");
            buffer.append(connector);
            buffer.append(", security=");
            switch (security) {
                case SECURITY_FTP:
                    buffer.append("SECURITY_FTP");
                    break;
                case SECURITY_FTPS:
                    buffer.append("SECURITY_FTPS");
                    break;
                case SECURITY_FTPES:
                    buffer.append("SECURITY_FTPES");
                    break;
            }
            buffer.append(", authenticated=");
            buffer.append(authenticated);
            if (authenticated) {
                buffer.append(", username=");
                buffer.append(username);
                buffer.append(", password=");
                StringBuilder buffer2 = new StringBuilder();
                for (int i = 0; i < password.length(); i++) {
                    buffer2.append('*');
                }
                buffer.append(buffer2);
                buffer.append(", restSupported=");
                buffer.append(restSupported);
                buffer.append(", utf8supported=");
                buffer.append(utf8Supported);
                buffer.append(", mlsdSupported=");
                buffer.append(mlsdSupported);
                buffer.append(", mode=modezSupported");
                buffer.append(modezSupported);
                buffer.append(", mode=modezEnabled");
                buffer.append(modezEnabled);
            }
            buffer.append(", transfer mode=");
            buffer.append(passive ? "passive" : "active");
            buffer.append(", transfer type=");
            switch (type) {
                case TYPE_BINARY:
                    buffer.append("TYPE_BINARY");
                    break;
                case TYPE_TEXTUAL:
                    buffer.append("TYPE_TEXTUAL");
                    break;
            }
            FTPListParser[] listParsers = getListParsers();
            if (listParsers.length > 0) {
                buffer.append(", listParsers=");
                for (int i = 0; i < listParsers.length; i++) {
                    if (i > 0) {
                        buffer.append(", ");
                    }
                    buffer.append(listParsers[i]);
                }
            }
            FTPCommunicationListener[] communicationListeners = getCommunicationListeners();
            if (communicationListeners.length > 0) {
                buffer.append(", communicationListeners=");
                for (int i = 0; i < communicationListeners.length; i++) {
                    if (i > 0) {
                        buffer.append(", ");
                    }
                    buffer.append(communicationListeners[i]);
                }
            }
            buffer.append(", autoNoopTimeout=");
            buffer.append(autoNoopTimeout);
            buffer.append("]");
            return buffer.toString();
        }
    }

    /**
     * Starts the auto-noop timer thread.
     */
    private void startAutoNoopTimer() {
        if (autoNoopTimeout > 0) {
            autoNoopTimer = new AutoNoopTimer();
            autoNoopTimer.start();
        }
    }

    /**
     * Stops the auto-noop timer thread.
     */
    private void stopAutoNoopTimer() {
        if (autoNoopTimer != null) {
            autoNoopTimer.interrupt();
            autoNoopTimer = null;
        }
    }

    /**
     * Resets the auto noop timer.
     */
    private void touchAutoNoopTimer() {
        if (autoNoopTimer != null) {
            nextAutoNoopTime = System.currentTimeMillis() + autoNoopTimeout;
        }
    }

    class AutoNoopTimer extends Thread {

        public void run() {
            synchronized (lock) {
                if (nextAutoNoopTime <= 0 && autoNoopTimeout > 0) {
                    nextAutoNoopTime = System.currentTimeMillis() + autoNoopTimeout;
                }
                while (!Thread.interrupted() && autoNoopTimeout > 0) {
                    // Sleep till the next NOOP.
                    long delay = nextAutoNoopTime - System.currentTimeMillis();
                    if (delay > 0) {
                        try {
                            lock.wait(delay);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    // Is it really time to NOOP?
                    if (System.currentTimeMillis() >= nextAutoNoopTime) {
                        // Yes!
                        try {
                            noop();
                        } catch (Throwable t) {
                            ; // ignore...
                        }
                    }
                }
            }
        }
    }

}
