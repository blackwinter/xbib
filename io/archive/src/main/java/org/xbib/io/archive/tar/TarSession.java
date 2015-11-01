package org.xbib.io.archive.tar;

import org.xbib.io.archive.ArchiveSession;

import java.io.InputStream;
import java.io.OutputStream;

public class TarSession extends ArchiveSession<TarArchiveInputStream, TarArchiveOutputStream> {

    private final static String tar = "tarold";

    private TarArchiveInputStream in;

    private TarArchiveOutputStream out;

    protected String getSuffix() {
        return tar;
    }

    protected void open(InputStream in) {
        this.in = new TarArchiveInputStream(in);
    }

    protected void open(OutputStream out) {
        this.out = new TarArchiveOutputStream(out);
    }

    public TarArchiveInputStream getInputStream() {
        return in;
    }

    public TarArchiveOutputStream getOutputStream() {
        return out;
    }

}
