
package org.xbib.io.archive.jar;

import org.xbib.io.archive.ArchiveEntry;
import org.xbib.io.archive.zip.ZipArchiveEntry;
import org.xbib.io.archive.zip.ZipArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implements an input stream that can read entries from jar files.
 */
public class JarArchiveInputStream extends ZipArchiveInputStream {

    public JarArchiveInputStream(final InputStream inputStream) {
        super(inputStream);
    }

    public JarArchiveEntry getNextJarEntry() throws IOException {
        ZipArchiveEntry entry = getNextZipEntry();
        return entry == null ? null : new JarArchiveEntry(entry);
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        return getNextJarEntry();
    }

}
