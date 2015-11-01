package org.xbib.io.archive.cpio;

import org.xbib.io.archive.ArchiveSession;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Cpio Session
 */
public class CpioSession extends ArchiveSession<CpioArchiveInputStream, CpioArchiveOutputStream> {

    private final static String SUFFIX = "cpio";

    private CpioArchiveInputStream in;

    private CpioArchiveOutputStream out;

    protected String getSuffix() {
        return SUFFIX;
    }

    protected void open(InputStream in) {
        this.in = new CpioArchiveInputStream(in);
    }

    protected void open(OutputStream out) {
        this.out = new CpioArchiveOutputStream(out);
    }

    public CpioArchiveInputStream getInputStream() {
        return in;
    }

    public CpioArchiveOutputStream getOutputStream() {
        return out;
    }
}
