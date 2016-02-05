package org.xbib.io.ftp;

import org.junit.Before;
import org.junit.Test;
import org.xbib.io.ftp.path.SlashPath;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.ProviderMismatchException;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class FtpPathTest {
    private static final URI URI1 = URI.create("ftp://my.site/sub/path");
    private static final SlashPath PATH1 = SlashPath.fromString("/foo/bar");


    private FileSystem fs1;
    private FileSystem fs2;

    @Before
    public void init() {
        fs1 = mock(FileSystem.class);
        when(fs1.provider()).thenReturn(mock(FileSystemProvider.class));
        fs2 = mock(FileSystem.class);
        when(fs2.provider()).thenReturn(mock(FileSystemProvider.class));
    }

    @Test
    public void pathKnowsWhatItsFileSystemIs() {
        final FTPPath path = new FTPPath(fs1, URI1, PATH1);
        assertSame(path.getFileSystem(), fs1);
    }

    @Test
    public void pathsWithDifferentFileSystemsNeverStartWithOneAnother() {
        final SlashPath slashPath1 = SlashPath.fromString("/a/b");
        final SlashPath slashPath2 = SlashPath.fromString("/a");
        final FTPPath path1 = new FTPPath(fs1, URI1, slashPath1);
        final FTPPath path2 = new FTPPath(fs2, URI1, slashPath2);
        assertFalse(path1.startsWith(path2));
    }

    @Test
    public void pathsWithDifferentFileSystemsNeverEndWithOneAnother() {
        final SlashPath slashPath1 = SlashPath.fromString("/a/b");
        final SlashPath slashPath2 = SlashPath.fromString("b");
        final FTPPath path1 = new FTPPath(fs1, URI1, slashPath1);
        final FTPPath path2 = new FTPPath(fs2, URI1, slashPath2);
        assertFalse(path1.endsWith(path2));
    }

    @Test
    public void resolveFailsIfProvidersAreDifferent() {
        final SlashPath slashPath1 = SlashPath.fromString("/a/b");
        final SlashPath slashPath2 = SlashPath.fromString("b");

        final FTPPath path1 = new FTPPath(fs1, URI1, slashPath1);
        final FTPPath path2 = new FTPPath(fs2, URI1, slashPath2);

        try {
            path1.resolve(path2);
            fail("No exception thrown!");
        } catch (ProviderMismatchException ignored) {
            assertTrue(true);
        }
    }

    @Test
    public void resolveSiblingFailsIfProvidersAreDifferent() {
        final SlashPath slashPath1 = SlashPath.fromString("/a/b");
        final SlashPath slashPath2 = SlashPath.fromString("b");

        final FTPPath path1 = new FTPPath(fs1, URI1, slashPath1);
        final FTPPath path2 = new FTPPath(fs2, URI1, slashPath2);

        try {
            path1.resolveSibling(path2);
            fail("No exception thrown!");
        } catch (ProviderMismatchException ignored) {
            assertTrue(true);
        }
    }

    @Test
    public void relativizeFailsIfProvidersAreDifferent() {
        final SlashPath slashPath1 = SlashPath.fromString("/a/b");
        final SlashPath slashPath2 = SlashPath.fromString("b");

        final FTPPath path1 = new FTPPath(fs1, URI1, slashPath1);
        final FTPPath path2 = new FTPPath(fs2, URI1, slashPath2);

        try {
            path1.relativize(path2);
            fail("No exception thrown!");
        } catch (ProviderMismatchException ignored) {
            assertTrue(true);
        }
    }

    public Iterator<Object[]> getURIData() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"a", "ftp://my.site/sub/path/a"});
        list.add(new Object[]{"/a", "ftp://my.site/sub/path/a"});
        list.add(new Object[]{"/a/b", "ftp://my.site/sub/path/a/b"});
        list.add(new Object[]{"../../a", "ftp://my.site/sub/path/a"});
        list.add(new Object[]{"../a/../c/d", "ftp://my.site/sub/path/c/d"});

        return list.iterator();
    }

    @Test /*(dataProvider = "getURIData")*/
    public void getURIWorks() {
        getURIData().forEachRemaining(o -> {
            final String s = (String) o[0];
            final String t = (String) o[1];
            final SlashPath slashPath = SlashPath.fromString(s);
            final FTPPath path = new FTPPath(fs1, URI1, slashPath);
            final URI expected = URI.create(t);

            assertEquals(path.toUri(), expected);
        });
    }

    @Test
    public void getRootRespectsContract() {
        final SlashPath rootSlashPath = SlashPath.fromString("/");
        final SlashPath slashPath1 = SlashPath.fromString("/a/b");
        final SlashPath slashPath2 = SlashPath.fromString("b");

        final FTPPath rootPath = new FTPPath(fs1, URI1, rootSlashPath);
        final FTPPath path1 = new FTPPath(fs1, URI1, slashPath1);
        final FTPPath path2 = new FTPPath(fs1, URI1, slashPath2);

        assertEquals(path1.getRoot(), rootPath);
        assertNull(path2.getRoot());
    }

    @Test
    public void getParentRespectsContract() {
        final SlashPath slashPath1 = SlashPath.fromString("/a/b");
        final SlashPath slashPath2 = SlashPath.fromString("/a");
        final SlashPath rootSlashPath = SlashPath.fromString("/");

        final FTPPath path1 = new FTPPath(fs1, URI1, slashPath1);
        final FTPPath path2 = new FTPPath(fs1, URI1, slashPath2);
        final FTPPath rootPath = new FTPPath(fs1, URI1, rootSlashPath);

        assertEquals(path1.getParent(), path2);
        assertEquals(path2.getParent(), rootPath);
        //assertEquals("/", rootPath.getParent());
    }

    @Test
    public void getFileNameRespectsContract() {
        final SlashPath rootSlashPath = SlashPath.fromString("/");

        final FTPPath rootPath = new FTPPath(fs1, URI1, rootSlashPath);

        //assertEquals("/", rootPath.getFileName());
    }

    @Test
    public void resolveRespectsContract() {
        final SlashPath slashPath1 = SlashPath.fromString("/a/b");
        final SlashPath slashPath2 = SlashPath.fromString("b");
        final SlashPath emptySlashPath = SlashPath.fromString("");

        final FTPPath path1 = new FTPPath(fs1, URI1, slashPath1);
        final FTPPath path2 = new FTPPath(fs1, URI1, slashPath2);
        final FTPPath emptyPath = new FTPPath(fs1, URI1, emptySlashPath);

        assertSame("resolving an absolute path should return other",
                path2.resolve(path1), path1);
        assertSame("resolving empty path should return this",
                path2.resolve(emptyPath), path2);
    }

    @Test
    public void resolveSiblingRespectsContract() {
        final SlashPath slashPath1 = SlashPath.fromString("/a");
        final SlashPath slashPath2 = SlashPath.fromString("/");
        final SlashPath emptySlashPath = SlashPath.fromString("");

        final FTPPath path1 = new FTPPath(fs1, URI1, slashPath1);
        final FTPPath path2 = new FTPPath(fs1, URI1, slashPath2);
        final FTPPath emptyPath = new FTPPath(fs1, URI1, emptySlashPath);

        assertSame("path without a parent should return other",
                path2.resolveSibling(emptyPath), emptyPath);
        assertSame("resolving an absolute path should return other",
                path2.resolveSibling(path1), path1);
        assertEquals("resolving empty path as sibling should return parent",
                path1.resolveSibling(emptyPath), path2);
        assertEquals("path without parent resolving empty should return empty",
                path2.resolveSibling(emptyPath), emptyPath);
    }

    @Test
    public void relativizeRespectsContract() {
        final SlashPath slashPath1 = SlashPath.fromString("/a/b");
        final SlashPath slashPath2 = SlashPath.fromString("b");
        final SlashPath emptySlashPath = SlashPath.fromString("");

        final FTPPath path1 = new FTPPath(fs1, URI1, slashPath1);
        final FTPPath path2 = new FTPPath(fs1, URI1, slashPath2);
        final FTPPath emptyPath = new FTPPath(fs1, URI1, emptySlashPath);

        assertEquals(path1.relativize(path1), emptyPath);
        assertEquals(path2.relativize(path2), emptyPath);
        try {
            path1.relativize(path2);
            fail("No exception thrown!");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
}
