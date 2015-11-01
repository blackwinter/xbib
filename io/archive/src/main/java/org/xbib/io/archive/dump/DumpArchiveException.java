
package org.xbib.io.archive.dump;

import java.io.IOException;


/**
 * Dump Archive Exception
 */
public class DumpArchiveException extends IOException {

    public DumpArchiveException() {
    }

    public DumpArchiveException(String msg) {
        super(msg);
    }

    public DumpArchiveException(String msg, Throwable cause) {
        super(msg);
        initCause(cause);
    }
}
