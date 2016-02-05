package org.xbib.io.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;

public class FTPFileSystemTest {

    private final static Logger logger = LogManager.getLogger(FTPFileSystem.class);

    // disabled for now
    public void testFileSystem() throws IOException {
        FileSystem fileSystem = FileSystems.newFileSystem(URI.create("ftp://herakles.hbz-nrw.de"),
                new HashMap<String,String>() {{
                    put("username", "hbz_k1");
                    put("password", "koehbz");
                }});
        assertNotNull(fileSystem);

        Finder finder = new Finder(fileSystem);

        logger.info("result={}", finder.find("/","*").getPathFiles() );

        fileSystem.close();

    }
}
