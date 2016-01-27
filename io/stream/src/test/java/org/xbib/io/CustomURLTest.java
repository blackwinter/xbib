package org.xbib.io;

import org.junit.Test;

import java.net.URL;

public class CustomURLTest {

    @Test
    public void testClasspathURL() throws Exception {
        URL.setURLStreamHandlerFactory(new CustomURLStreamHandlerFactory());
        new URL("classpath:foobar");
        new URL("file:foobar");
        new URL("http://foobar");
    }
}
