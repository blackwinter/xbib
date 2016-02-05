package org.xbib.io.ftp;

import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class FtpUriTest {
    @Test
    public void cannotSubmitNullURI() {
        try {
            FTPFileSystemProvider.normalizeAndCheck(null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), "uri cannot be null");
        }
    }

    @Test
    public void cannotSubmitNonAbsoluteURI() {
        try {
            FTPFileSystemProvider.normalizeAndCheck(URI.create("foo"));
            fail("No exception thrown!!");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "uri must be absolute");
        }
    }

    @Test
    public void schemeOfUriMustBeFtp() {
        try {
            FTPFileSystemProvider.normalizeAndCheck(URI.create("http://slashdot.org"));
            fail("No exception thrown!!");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "uri scheme must be \"ftp\"");
        }
    }

    @Test
    public void uriMustNotHaveUserInfo() {
        try {
            FTPFileSystemProvider.normalizeAndCheck(URI.create("ftp://foo:bar@host"));
            fail("No exception thrown!!");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "uri must not contain user info");
        }
    }

    @Test
    public void uriMustIncludeHostname() {
        try {
            FTPFileSystemProvider.normalizeAndCheck(URI.create("ftp:/foo"));
            fail("No exception thrown!!");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "uri must have a hostname");
        }
    }

    public Iterator<Object[]> getURIs() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"ftp://foo", "ftp://foo"});
        list.add(new Object[]{"Ftp://foo", "ftp://foo"});
        list.add(new Object[]{"ftp://fOO", "ftp://foo"});
        list.add(new Object[]{"FTP://Foo", "ftp://foo"});

        return list.iterator();
    }

    @Test
    public void urisAreCorrectlyNormalized() {
        getURIs().forEachRemaining(o -> {
            final String orig = (String) o[0];
            final String normalized = (String) o[1];
            final URI uri = URI.create(orig);
            assertEquals(FTPFileSystemProvider.normalizeAndCheck(uri).toString(), normalized);

        });
    }
}
