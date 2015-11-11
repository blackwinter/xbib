
package org.xbib.io.archive.dump;

/**
 * Unrecognized Format Exception. This is either not a recognized dump archive or there's
 * a bad tape segment header.
 */
public class UnrecognizedFormatException extends DumpArchiveException {
    private static final long serialVersionUID = 1L;

    public UnrecognizedFormatException() {
        super("this is not a recognized format.");
    }
}
