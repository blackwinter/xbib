package org.xbib.io.archive.ar;

import org.xbib.io.archive.ArchiveSession;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Ar Session
 */
public class ArSession extends ArchiveSession<ArArchiveInputStream, ArArchiveOutputStream> {

    private final static String SUFFIX = "ar";

    private ArArchiveInputStream in;

    private ArArchiveOutputStream out;

    protected String getSuffix() {
        return SUFFIX;
    }

    protected void open(InputStream in) {
        this.in = new ArArchiveInputStream(in);
    }

    protected void open(OutputStream out) {
        this.out = new ArArchiveOutputStream(out);
    }

    public ArArchiveInputStream getInputStream() {
        return in;
    }

    public ArArchiveOutputStream getOutputStream() {
        return out;
    }
}
