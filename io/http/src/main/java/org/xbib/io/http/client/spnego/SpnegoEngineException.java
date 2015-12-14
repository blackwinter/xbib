package org.xbib.io.http.client.spnego;

/**
 * Signals SPNEGO protocol failure.
 */
public class SpnegoEngineException extends Exception {

    public SpnegoEngineException(String message) {
        super(message);
    }

    public SpnegoEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}