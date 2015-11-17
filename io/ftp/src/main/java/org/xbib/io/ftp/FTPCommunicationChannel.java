
package org.xbib.io.ftp;

import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to represent a communication channel with a FTP server.
 */
public class FTPCommunicationChannel {

    /**
     * The FTPCommunicationListener objects registered on the channel.
     */
    private List<FTPCommunicationListener> communicationListeners = new ArrayList<>();

    private Logger logger;

    /**
     * The connection.
     */
    private Socket socket;

    /**
     * The name of the charset that has to be used to encode and decode the
     * communication.
     */
    private String encoding;

    /**
     * The stream-reader channel established with the remote server.
     */
    private NetworkVirtualTerminalASCIIReader reader;

    /**
     * The stream-writer channel established with the remote server.
     */
    private NetworkVirtualTerminalASCIIWriter writer;

    /**
     * It builds a FTP communication channel.
     *
     * @param socket  The underlying connection.
     * @param encoding The name of the charset that has to be used to encode and
     *                    decode the communication.
     * @throws IOException If a I/O error occurs.
     */
    public FTPCommunicationChannel(Logger logger, Socket socket, String encoding)
            throws IOException {
        this.logger = logger;
        this.socket = socket;
        this.encoding = encoding;
        this.reader = new NetworkVirtualTerminalASCIIReader(socket.getInputStream(), encoding);
        this.writer = new NetworkVirtualTerminalASCIIWriter( socket.getOutputStream(), encoding);
    }

    /**
     * This method adds a FTPCommunicationListener to the object.
     *
     * @param listener The listener.
     */
    public void addCommunicationListener(FTPCommunicationListener listener) {
        communicationListeners.add(listener);
    }

    /**
     * This method removes a FTPCommunicationListener previously added to the
     * object.
     *
     * @param listener The listener to be removed.
     */
    public void removeCommunicationListener(FTPCommunicationListener listener) {
        communicationListeners.remove(listener);
    }

    /**
     * Closes the channel.
     */
    public void close() throws IOException {
        socket.close();
    }

    /**
     * This method returns a list with all the FTPCommunicationListener used by
     * the client.
     *
     * @return A list with all the FTPCommunicationListener used by the client.
     */
    public List<FTPCommunicationListener> getCommunicationListeners() {
        return communicationListeners;
    }

    /**
     * This method reads a line from the remote server.
     *
     * @return The string read.
     * @throws IOException If an I/O error occurs during the operation.
     */
    private String read() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("connection closed");
        }
        for (FTPCommunicationListener l : communicationListeners) {
            l.received(line);
        }
        return line;
    }

    /**
     * This method sends a command line to the server.
     *
     * @param command The command to be sent.
     * @throws IOException If an I/O error occurs.
     */
    public void sendFTPCommand(String command) throws IOException {
        logger.info("{}", command);
        writer.writeLine(command);
        for (FTPCommunicationListener l : communicationListeners) {
            l.sent(command);
        }
    }

    /**
     * This method reads and parses a FTP reply triple from the server.
     *
     * @return The reply from the server.
     * @throws IOException              If an I/O error occurs.
     * @throws FTPException If the server doesn't reply in a FTP-compliant way.
     */
    public FTPReply readFTPReply() throws IOException, FTPException {
        int code = 0;
        List<String> messages = new ArrayList<>();
        do {
            String statement;
            do {
                statement = read();
                logger.info("{}", statement);
            } while (statement.trim().length() == 0);
            if (statement.startsWith("\n")) {
                statement = statement.substring(1);
            }
            int l = statement.length();
            if (code == 0 && l < 3) {
                throw new FTPException("illegal reply");
            }
            int aux;
            try {
                aux = Integer.parseInt(statement.substring(0, 3));
            } catch (Exception e) {
                if (code == 0) {
                    throw new FTPException("illegal reply");
                } else {
                    aux = 0;
                }
            }
            if (code != 0 && aux != 0 && aux != code) {
                throw new FTPException("illegal reply");
            }
            if (code == 0) {
                code = aux;
            }
            if (aux > 0) {
                if (l > 3) {
                    char s = statement.charAt(3);
                    String message = statement.substring(4, l);
                    messages.add(message);
                    if (s == ' ') {
                        break;
                    } else if (s == '-') {
                        //
                    } else {
                        throw new FTPException("illegal reply");
                    }
                } else if (l == 3) {
                    break;
                } else {
                    messages.add(statement);
                }
            } else {
                messages.add(statement);
            }
        } while (true);
        return new FTPReply(code, messages);
    }

    /**
     * Changes the current charset.
     *
     * @param charsetName The new charset.
     * @throws IOException If I/O error occurs.
     */
    public void changeCharset(String charsetName) throws IOException {
        this.encoding = charsetName;
        reader.changeCharset(charsetName);
        writer.changeCharset(charsetName);
    }

    /**
     * Applies SSL encryption to the communication channel.
     *
     * @param sslSocketFactory The SSLSocketFactory used to produce the SSL connection.
     * @throws IOException If a I/O error occurs.
     */
    public void ssl(SSLSocketFactory sslSocketFactory) throws IOException {
        String host = socket.getInetAddress().getHostName();
        int port = socket.getPort();
        socket = sslSocketFactory.createSocket(socket, host, port, true);
        InputStream inStream = socket.getInputStream();
        OutputStream outStream = socket.getOutputStream();
        reader = new NetworkVirtualTerminalASCIIReader(inStream, encoding);
        writer = new NetworkVirtualTerminalASCIIWriter(outStream, encoding);
    }

}
