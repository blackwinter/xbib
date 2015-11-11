package org.xbib.io.archive.jar;

import org.xbib.io.archive.zip.ZipArchiveEntry;

import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class JarArchiveEntry extends ZipArchiveEntry {

    private Attributes manifestAttributes = null;

    private Certificate[] certificates = null;

    public JarArchiveEntry() {
        super();
    }

    public JarArchiveEntry(ZipEntry entry) throws ZipException {
        super(entry);
    }

    public JarArchiveEntry(String name) {
        super(name);
    }

    public JarArchiveEntry(ZipArchiveEntry entry) throws ZipException {
        super(entry);
    }

    public JarArchiveEntry(JarEntry entry) throws ZipException {
        super(entry);

    }

    public Attributes getManifestAttributes() {
        return manifestAttributes;
    }

    public Certificate[] getCertificates() {
        if (certificates != null) {
            Certificate[] certs = new Certificate[certificates.length];
            System.arraycopy(certificates, 0, certs, 0, certs.length);
            return certs;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
