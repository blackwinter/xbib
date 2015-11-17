package org.xbib.io.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import java.io.File;
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
            InputStream fileInputStream = getClass().getResourceAsStream("/textfile.txt");
            client.stor("textfile.txt", fileInputStream);
            fileInputStream.close();
            Map<String, FTPEntry> entries = client.list();
            logger.info("{}", entries);
            File f = File.createTempFile("textfile.", ".txt");
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            logger.info(f.getAbsolutePath());
            client.retr(entries.get("textfile.txt"), fileOutputStream);
            fileOutputStream.close();
            client.disconnect(true);
        } catch (FTPException | IOException e) {
            logger.warn(e.getMessage());
        }
    }
}
