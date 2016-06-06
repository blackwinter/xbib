package org.ghost4j;

/**
 * Ghostscript exception.
 */
public class GhostscriptException extends Exception {

    public GhostscriptException() {
        super();
    }

    public GhostscriptException(String message) {
        super(message);
    }

    public GhostscriptException(Throwable cause) {
        super(cause);
    }

    public GhostscriptException(String message, Throwable cause) {
        super(message, cause);
    }

}