package org.xbib.io.http.client.ntlm;

/**
 * Signals NTLM protocol failure.
 */
public class NtlmEngineException extends RuntimeException {

    /**
     * Creates a new NTLMEngineException with the specified message.
     *
     * @param message the exception detail message
     */
    public NtlmEngineException(String message) {
        super(message);
    }

    /**
     * Creates a new NTLMEngineException with the specified detail message and cause.
     *
     * @param message the exception detail message
     * @param cause   the <tt>Throwable</tt> that caused this exception, or <tt>null</tt>
     *                if the cause is unavailable, unknown, or not a <tt>Throwable</tt>
     */
    public NtlmEngineException(String message, Throwable cause) {
        super(message, cause);
    }

}
