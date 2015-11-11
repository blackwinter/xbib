package org.xbib.query.cql;

/**
 * CQL Syntax exception.
 */
public class SyntaxException extends RuntimeException {
    /**
     * Creates a new SyntaxException object.
     *
     * @param msg the message for this syntax exception
     */
    public SyntaxException(String msg) {
        super(msg);
    }

    /**
     * Creates a new SyntaxException object.
     *
     * @param msg the message for this syntax exception
     * @param t   the throwable for this syntax exception
     */
    public SyntaxException(String msg, Throwable t) {
        super(msg, t);
    }
}
