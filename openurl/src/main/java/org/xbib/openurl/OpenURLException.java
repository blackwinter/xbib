package org.xbib.openurl;

/**
 * A fatal error occurred within the OpenURL infrastructure.
 *
 * @see OpenURLResponse for non-fatal response conditions
 */
public class OpenURLException extends Exception {

    /**
     * Indicates a fatal condition
     *
     * @param e the original exception
     */
    public OpenURLException(Exception e) {
        super(e);
    }

    /**
     * Indicates a fatal condition
     *
     * @param message provides a clue to the conditions of the problem
     * @param e       the original exception
     */
    public OpenURLException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Indicates a fatal condition
     *
     * @param message provides a clue to the conditions of the problem
     */
    public OpenURLException(String message) {
        super(message);
    }
}
