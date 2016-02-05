package org.xbib.graphics.vector.util;

import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

public class FlateEncodeStream extends DeflaterOutputStream {
    public FlateEncodeStream(OutputStream out) {
        super(out);
    }
}

