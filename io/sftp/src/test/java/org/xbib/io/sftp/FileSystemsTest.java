
package org.xbib.io.sftp;

import org.apache.sshd.client.subsystem.sftp.SftpFileSystem;
import org.apache.sshd.client.subsystem.sftp.SftpFileSystemProvider;
import org.apache.sshd.client.subsystem.sftp.SftpPosixFileAttributeView;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderNotFoundException;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.spi.FileSystemProvider;

public class FileSystemsTest extends Assert {

    private static MockSshSftpServer mockSftpServer;

    private static String user = "user";

    private static String pwd = "pwd";

    private static String host = "localhost";

    private static Integer port = 22999;

    private static String url = "sftp://" + user + ":" + pwd + "@" + host + ":" + port;

    private static SftpFileSystemProvider sftpFileSystemProvider;

    private static SftpFileSystem sftpFileSystem;

    @BeforeClass
    static public void createResources() throws URISyntaxException, IOException {
        for (FileSystemProvider fileSystemProvider : FileSystemProvider.installedProviders()) {
            if ("sftp".equals(fileSystemProvider.getScheme())) {
                sftpFileSystemProvider = (SftpFileSystemProvider) fileSystemProvider;
            }
        }
        if (sftpFileSystemProvider == null) {
            throw new ProviderNotFoundException("Unable to get a SFTP file system provider");
        }
        mockSftpServer = new MockSshSftpServer();
        mockSftpServer.start();
        URI uri = new URI(url);
        sftpFileSystem = sftpFileSystemProvider.newFileSystem(uri, null);
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
        if (!sftpFileSystemProvider.isSupportedFileAttributeView(file, SftpPosixFileAttributeView.class)) {
            return;
        }
        PosixFileAttributes attrs = Files.readAttributes(file, PosixFileAttributes.class);
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
        assertTrue(attrs.permissions().contains(PosixFilePermission.OWNER_READ));
        assertTrue(attrs.permissions().contains(PosixFilePermission.OWNER_WRITE));
        assertTrue(attrs.permissions().contains(PosixFilePermission.GROUP_READ));
        assertTrue(attrs.permissions().contains(PosixFilePermission.OTHERS_READ));
    }

}
