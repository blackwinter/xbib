package org.xbib.io.ftp;

import java.util.List;

/**
 * This class helps in represent FTP error codes and messages.
 */
public class FTPException extends Exception {

    private int code;

    private String message;

    private Throwable exception;

    public FTPException(int code) {
        this.code = code;
    }

    public FTPException(String message) {
        this.message = message;
    }

    public FTPException(String message, Throwable exception) {
        this.message = message;
        this.exception = exception;
    }

    public FTPException(int code, String message) {
        this.code = code;
        this.message = code + " " + message;
    }

    public FTPException(FTPReply reply) {
        StringBuilder message = new StringBuilder();
        List<String> lines = reply.getMessages();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                message.append(System.getProperty("line.separator"));
            }
            message.append(lines.get(i));
        }
        this.code = reply.getCode();
        this.message = code + " " + message.toString();
    }

    /**
     * Returns the code of the occurred FTP error.
     *
     * @return The code of the occurred FTP error.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the message of the occurred FTP error.
     *
     * @return The message of the occurred FTP error.
     */
    @Override
    public String getMessage() {
        return message;
    }

    public String toString() {
        return "[code=" + code + ", message= " + message + "]";
    }

}
