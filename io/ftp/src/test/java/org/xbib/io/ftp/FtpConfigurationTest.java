package org.xbib.io.ftp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class FtpConfigurationTest {
    private FTPConfiguration.Builder builder;

    @Before
    public void initBuilder() {
        builder = FTPConfiguration.newBuilder();
    }

    @Test
    public void cannotBuildWithoutHostname() {
        try {
            builder.build();
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), "no hostname has been provided");
        }
    }

    @Test
    public void cannotProvideNullHostname() {
        try {
            builder.setHostname(null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), "hostname cannot be null");
        }
    }

    @Test
    public void cannotProvideIllegalPort() {
        try {
            builder.setPort(-1);
            fail("No exception thrown!!");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "illegal port number -1");
        }

        try {
            builder.setPort(65536);
            fail("No exception thrown!!");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "illegal port number 65536");
        }
    }

    @Test
    public void cannotProvideNullUsername() {
        try {
            builder.setUsername(null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), "username cannot be null");
        }
    }

    @Test
    public void cannotProvideNullPassword() {
        try {
            builder.setPassword(null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), "password cannot be null");
        }
    }
}
