package org.xbib.graphics.vector.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LineWrapOutputStream extends FilterOutputStream {
    public static final String STANDARD_EOL = "\r\n";

    private final int lineWidth;
    private final byte[] eolBytes;
    private int written;

    public LineWrapOutputStream(OutputStream sink, int lineWidth, String eol) {
        super(sink);
        this.lineWidth = lineWidth;
        this.eolBytes = eol.getBytes();
        if (lineWidth <= 0) {
            throw new IllegalArgumentException("Width must be at least 0.");
        }
    }

    public LineWrapOutputStream(OutputStream sink, int lineWidth) {
        this(sink, lineWidth, STANDARD_EOL);
    }

    @Override
    public void write(int b) throws IOException {
        if (written == lineWidth) {
            out.write(eolBytes);
            written = 0;
        }
        out.write(b);
        written++;
    }
}

