package org.xbib.io.ftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class SimpleFtpClient {

    private final int timeout;
    private final Socket socket;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private Socket dataSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public SimpleFtpClient(String host, int timeout) throws IOException {
        this.timeout = timeout;
        this.socket = new Socket(host, 21);
        this.socket.setSoTimeout(timeout);
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    void connect(String host, String userInfo, String user, String password) throws IOException {
        FtpReply reply = connect();
        if (!reply.isSuccess()) {
            throw new IOException("Could not connect to " + host);
        }
        if (userInfo != null) {
            String[] tmp = userInfo.split(":");
            user = tmp[0];
            if (tmp.length > 1) {
                password = tmp[1];
            }
        }
        reply = login(user, password);
        reply = binary();
        if (!reply.isSuccess()) {
            throw new IOException("Could not set binary mode on host: " + host);
        }
    }

    public FtpReply connect() throws IOException {
        FtpReply reply = new FtpReply(reader);
        if (!reply.isPositiveCompletion()) {
            disconnect();
        }
        return reply;
    }

    public FtpReply writeString(String command) throws IOException {
        writer.write(command);
        writer.flush();
        return new FtpReply(reader);
    }

    public FtpReply login(String username, String password) throws IOException {
        FtpReply response = writeString("user " + username);
        if (!response.isPositiveIntermediate()) {
            return response;
        }
        response = writeString("pass " + password);
        return response;
    }

    public FtpReply quit() throws IOException {
        return writeString("QUIT");
    }

    public FtpReply binary() throws IOException {
        return writeString("TYPE I");
    }

    public FtpReply pasv() throws IOException {
        FtpReply reply = writeString("PASV");
        if (reply.getCode() == 226 || reply.getCode() == 426) {
            reply = getReply();
        }
        String response = reply.getReplyString();
        int code = reply.getCode();
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);
        String passiveHost;
        int passivePort;
        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                passiveHost = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
                        + tokenizer.nextToken() + "." + tokenizer.nextToken();
                passivePort = Integer.parseInt(tokenizer.nextToken()) * 256
                        + Integer.parseInt(tokenizer.nextToken());
                if (reply.isPositiveCompletion()) {
                    this.dataSocket = new Socket(passiveHost, passivePort);
                    dataSocket.setSoTimeout(timeout);
                    inputStream = dataSocket.getInputStream();
                    outputStream = dataSocket.getOutputStream();
                }
            } catch (NumberFormatException | NoSuchElementException e) {
                throw new IOException("received bad data link information: " + response);
            }
        }
        return reply;
    }

    public FtpReply retr(String file, int position) throws IOException {
        if (position >= 0) {
            FtpReply restReply = writeString("REST " + position);
            if (!restReply.isSuccess()) {
                return restReply;
            }
        }
        return writeString("RETR " + file);
    }

    public FtpReply getReply() throws IOException {
        return new FtpReply(reader);
    }

    public FtpReply size(String file) throws IOException {
        return writeString("SIZE " + file);
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void disconnect() throws IOException {
        //quit();
        if (writer != null) {
            writer.close();
        }
        if (reader != null) {
            reader.close();
        }
        if (socket != null) {
            socket.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        if (dataSocket != null) {
            dataSocket.close();
        }
    }

    class FtpReply {

        private String reply;
        private int code;

        public FtpReply(BufferedReader reader) throws IOException {
            String response;
            do {
                response = reader.readLine();
            } while (response != null &&
                    !(Character.isDigit(response.charAt(0)) &&
                            Character.isDigit(response.charAt(1)) &&
                            Character.isDigit(response.charAt(2)) &&
                            response.charAt(3) == ' '));
            if (response == null || response.length() < 3) {
                code = -1;
            } else {
                code = Integer.parseInt(response.substring(0, 3));
                reply = response.substring(3).trim();
            }
        }

        /**
         * Gets server reply code from the control port after an ftp command has
         * been executed.  It knows the last line of the response because it begins
         * with a 3 digit number and a space, (a dash instead of a space would be a
         * continuation).
         */

        public int getCode() {
            return code;
        }

        /**
         * Gets server reply string from the control port after an ftp command has
         * been executed.  This consists only of the last line of the response,
         * and only the part after the response code.
         */
        public String getReplyString() {
            return reply;
        }

        public boolean isSuccess() {
            return isPositiveCompletion() || isPositiveIntermediate();
        }

        /**
         * Determine if a reply code is a positive completion response.  All
         * codes beginning with a 2 are positive completion responses.
         * The FTP server will send a positive completion response on the final
         * successful completion of a command.
         *
         * @return True if a reply code is a postive completion response, false
         * if not.
         * *
         */
        public boolean isPositiveCompletion() {
            return (code >= 200 && code < 300);
        }


        /**
         * Determine if a reply code is a positive intermediate response.  All
         * codes beginning with a 3 are positive intermediate responses.
         * The FTP server will send a positive intermediate response on the
         * successful completion of one part of a multi-part sequence of
         * commands.  For example, after a successful USER command, a positive
         * intermediate response will be sent to indicate that the server is
         * ready for the PASS command.
         *
         * @return True if a reply code is a postive intermediate response, false
         * if not.
         * *
         */
        public boolean isPositiveIntermediate() {
            return (code >= 300 && code < 400);
        }
    }
}