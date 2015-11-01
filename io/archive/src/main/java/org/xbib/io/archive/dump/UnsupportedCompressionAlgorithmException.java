
package org.xbib.io.archive.dump;

/**
 * Unsupported compression algorithm. The dump archive uses an unsupported
 * compression algorithm (BZLIB2 or LZO).
 */
public class UnsupportedCompressionAlgorithmException
        extends DumpArchiveException {
    private static final long serialVersionUID = 1L;

    public UnsupportedCompressionAlgorithmException() {
        super("this file uses an unsupported compression algorithm.");
    }

    public UnsupportedCompressionAlgorithmException(String alg) {
        super("this file uses an unsupported compression algorithm: " + alg +
                ".");
    }
}
