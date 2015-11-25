package org.xbib.io.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class FTPTest {

    private final static Logger logger = LogManager.getLogger(FTPTest.class);

    @Test
    public void test() throws IOException, FTPException {
        try {
            FTPClient client = new FTPClient();
            client.connect("localhost", 21);
            client.login();
            client.cwd("/");
            Map<String, FTPEntry> entries = client.list();
            logger.info("{}", entries);
            InputStream fileInputStream = getClass().getResourceAsStream("/textfile.txt");
            client.stor("test.txt", fileInputStream);
            fileInputStream.close();
            entries = client.list();
            logger.info("{}", entries);
            File f = File.createTempFile("ftpfile.", ".bin");
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            logger.info(f.getAbsolutePath());
            client.retr("test.txt", fileOutputStream);
            fileOutputStream.close();
            client.rein();
            client.disconnect(true);
        } catch (FTPException | IOException e) {
            logger.warn("error during test: " + e.getMessage(), e);
        }
    }
}
