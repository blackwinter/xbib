package org.xbib.io.archives.zip;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.io.archive.zip.ZipArchiveEntry;
import org.xbib.io.archive.zip.ZipArchiveInputStream;

import java.io.InputStream;

public class ZipArchiveTest extends Assert {

    @Test
    public void testZip() throws Exception {
        InputStream in = getClass().getResourceAsStream("/test.zip");
        ZipArchiveInputStream z = new ZipArchiveInputStream(in);
        ZipArchiveEntry entry;
        byte[] buffer = new byte[1024];
        long total = 0L;
        while ((entry = z.getNextZipEntry()) != null) {
            int len = 0;
            while ((len = z.read(buffer)) > 0) {
                total += len;
            }
        }
        assertEquals(5L, total);
        z.close();
    }


}
