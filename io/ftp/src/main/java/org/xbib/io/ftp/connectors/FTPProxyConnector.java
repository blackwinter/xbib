package org.xbib.io.ftp.connectors;

import org.apache.logging.log4j.Logger;
import org.xbib.io.ftp.FTPCommunicationChannel;
import org.xbib.io.ftp.FTPException;
import org.xbib.io.ftp.FTPReply;

import java.io.IOException;
import java.net.Socket;

/**
 * This one connects a remote host via a FTP proxy which supports the SITE or
 * the OPEN proxy method.
 *
 * The connector's default value for the
 * <em>useSuggestedAddressForDataConnections</em> flag is <em>true</em>.
 */
public class FTPProxyConnector extends FTPConnector {

    /**
     * Requires the connection to the remote host through a SITE command after
     * proxy authentication. Default one.
     */
    public static int STYLE_SITE_COMMAND = 0;

    /**
     * Requires the connection to the remote host through a OPEN command without
     * proxy authentication.
     */
    public static int STYLE_OPEN_COMMAND = 1;
    /**
     * The style used by the proxy.
     */
    public int style = STYLE_SITE_COMMAND;
    /**
     * The proxy host name.
     */
    private String proxyHost;
    /**
     * The proxy port.
     */
    private int proxyPort;
    /**
     * The proxyUser for proxy authentication.
     */
    private String proxyUser;
    /**
     * The proxyPass for proxy authentication.
     */
    private String proxyPass;

    /**
     * Builds the connector.
     *
     * Default value for the style is STYLE_SITE_COMMAND.
     *
     * @param proxyHost The proxy host name.
     * @param proxyPort The proxy port.
     * @param proxyUser The username for proxy authentication.
     * @param proxyPass The password for proxy authentication.
     */
    public FTPProxyConnector(Logger logger, String proxyHost, int proxyPort, String proxyUser, String proxyPass) {
        super(logger);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
    }

    /**
     * Sets the style used by the proxy.
     *
     * {@link FTPProxyConnector#STYLE_SITE_COMMAND} - Requires the connection to
     * the remote host through a SITE command after proxy authentication.
     *
     * {@link FTPProxyConnector#STYLE_OPEN_COMMAND} - Requires the connection to
     * the remote host through a OPEN command without proxy authentication.
     *
     * Default value for the style is STYLE_SITE_COMMAND.
     *
     * @param style The style.
     */
    public void setStyle(int style) {
        if (style != STYLE_OPEN_COMMAND && style != STYLE_SITE_COMMAND) {
            throw new IllegalArgumentException("Invalid style");
        }
        this.style = style;
    }

    public Socket connectForCommunicationChannel(String host, int port)
            throws IOException {
        Socket socket = createCommSocket(proxyHost, proxyPort);
        FTPCommunicationChannel communication = new FTPCommunicationChannel(logger, socket, "ASCII");
        // Welcome message.
        FTPReply r;
        try {
            r = communication.readFTPReply();
        } catch (FTPException e) {
            throw new IOException("Invalid proxy response");
        }
        // Does this reply mean "ok"?
        if (r.getCode() != 220) {
            // Mmmmm... it seems no!
            throw new IOException("Invalid proxy response");
        }
        if (style == STYLE_SITE_COMMAND) {
            // Usefull flags.
            boolean passwordRequired;
            // Send the user and read the reply.
            communication.sendFTPCommand("USER " + proxyUser);
            try {
                r = communication.readFTPReply();
            } catch (FTPException e) {
                throw new IOException("Invalid proxy response");
            }
            switch (r.getCode()) {
                case 230:
                    // Password isn't required.
                    passwordRequired = false;
                    break;
                case 331:
                    // Password is required.
                    passwordRequired = true;
                    break;
                default:
                    // User validation failed.
                    throw new IOException("Proxy authentication failed");
            }
            // Password.
            if (passwordRequired) {
                // Send the password.
                communication.sendFTPCommand("PASS " + proxyPass);
                try {
                    r = communication.readFTPReply();
                } catch (FTPException e) {
                    throw new IOException("Invalid proxy response");
                }
                if (r.getCode() != 230) {
                    // Authentication failed.
                    throw new IOException("Proxy authentication failed");
                }
            }
            communication.sendFTPCommand("SITE " + host + ":" + port);
        } else if (style == STYLE_OPEN_COMMAND) {
            communication.sendFTPCommand("OPEN " + host + ":" + port);
        }
        return socket;
    }

    public Socket connectForDataTransferChannel(String host, int port)
            throws IOException {
        return createDataSocket(host, port);
    }

}
