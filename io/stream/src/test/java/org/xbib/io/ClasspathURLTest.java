package org.xbib.io;

import org.junit.Test;

import java.net.URL;

public class ClasspathURLTest {

    @Test
    public void testClasspathURL() throws Exception {
        URL.setURLStreamHandlerFactory(new ClasspathURLStreamHandlerFactory());
        new URL("classpath:foobar");
        new URL("file:foobar");
        new URL("http://foobar");
    }
}
