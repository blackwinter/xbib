
package org.xbib.io.compress;

import org.xbib.io.StreamCodec;
import org.xbib.io.compress.zlib.ZInputStream;
import org.xbib.io.compress.zlib.ZOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ZStreamCodec implements StreamCodec<ZInputStream, ZOutputStream> {

    @Override
    public String getName() {
        return "Z";
    }

    @Override
    public ZInputStream decode(InputStream in) throws IOException {
        return new ZInputStream(in);
    }

    @Override
    public ZInputStream decode(InputStream in, int bufsize) throws IOException {
        return new ZInputStream(in, bufsize);
    }

    @Override
    public ZOutputStream encode(OutputStream out) throws IOException {
        return new ZOutputStream(out);
    }

    @Override
    public ZOutputStream encode(OutputStream out, int bufsize) throws IOException {
        return new ZOutputStream(out, bufsize);
    }
}
