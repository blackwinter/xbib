
package org.xbib.io.sftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FileSystemsTest extends Assert {

    private static MockSshSftpServer mockSftpServer;

    private static String user = "user";

    private static String pwd = "pwd";

    private static String host = "localhost";

    private static Integer port = 22999;

    private static String url = "sftp://" + user + ":" + pwd + "@" + host + ":" + port;

    private static FileSystemProvider sftpFileSystemProvider;

    private static SFTPFileSystem sftpFileSystem;

    @BeforeClass
    static public void createResources() throws URISyntaxException, IOException {
        for (FileSystemProvider fileSystemProvider : FileSystemProvider.installedProviders()) {
            if (SFTPFileSystemProvider.SFTP_SCHEME.equals(fileSystemProvider.getScheme())) {
                sftpFileSystemProvider = fileSystemProvider;
            }
        }
        if (sftpFileSystemProvider == null) {
            throw new ProviderNotFoundException("Unable to get a SFTP file system provider");
        }
        mockSftpServer = new MockSshSftpServer();
        mockSftpServer.start();
        URI uri = new URI(url);
        sftpFileSystem = (SFTPFileSystem) sftpFileSystemProvider.newFileSystem(uri, null);
    }

    @AfterClass
    static public void closeResources() throws IOException {
        if (sftpFileSystem != null) {
            sftpFileSystem.close();
        }
        if (mockSftpServer != null) {
            mockSftpServer.stop();
        }
    }

    @Test
    public void sftpFileSystemProviderIsNotNull() throws Exception {
        assertNotNull(sftpFileSystemProvider);
    }

    @Test
    public void sftpFileSystemIsNotNull() throws Exception {
        assertNotNull(sftpFileSystem);
    }

    @Test
    public void sftpFileSystemIsOpen() throws Exception {
        assertEquals("The file System must be opened", true, sftpFileSystem.isOpen());
    }

    @Test
    public void copy() throws IOException {
        Path src = Paths.get("./src/test/resources/testFileWrite.txt");
        Path dst = sftpFileSystem.getPath("src", "test", "resources", "sftp", "testFileWrite.txt");
        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        assertTrue("files exist", Files.exists(dst));
    }

    @Test
    public void posixFileAttribute() throws IOException {
        Path file = sftpFileSystem.getPath("src", "test", "resources", "sftp", "testFileRead.txt");
        SFTPPosixFileAttributes attrs = Files.readAttributes(file, SFTPPosixFileAttributes.class);
        assertNotNull("The file exist, we must get attributes", attrs);
        assertFalse("This is not a directory", attrs.isDirectory());
        assertTrue("This is a regular file", attrs.isRegularFile());
        assertFalse("This is not an symbolic link", attrs.isSymbolicLink());
        assertFalse("This is not an other file", attrs.isOther());
        assertEquals("The file size is", 38, attrs.size());
        // too flaky in IDE :)
        //assertEquals("The last modified time is: ", "2016-02-03T10:43:49Z", attrs.lastModifiedTime().toString());
        //assertEquals("The last modified time is the creation time (Creation time doesn't exist in SFTP", attrs.creationTime(), attrs.lastModifiedTime());
        //assertEquals("The last access time is ", "2016-02-03T10:46:32Z", attrs.lastAccessTime().toString());
        Set<PosixFilePermission> expectedPermission = new HashSet<PosixFilePermission>();
        expectedPermission.add(PosixFilePermission.OWNER_READ);
        expectedPermission.add(PosixFilePermission.OWNER_WRITE);
        expectedPermission.add(PosixFilePermission.GROUP_READ);
        expectedPermission.add(PosixFilePermission.OTHERS_READ);
        assertEquals("The permissions are equal", expectedPermission, attrs.permissions());
    }

}
