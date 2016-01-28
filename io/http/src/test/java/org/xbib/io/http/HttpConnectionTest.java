package org.xbib.io.http;

import org.junit.Test;
import org.xbib.io.CustomURLStreamHandlerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class HttpConnectionTest {

    static {
        URL.setURLStreamHandlerFactory(new CustomURLStreamHandlerFactory());
    }

    @Test(expected = java.net.UnknownServiceException.class)
    public void testHttpConnection() throws IOException {
        URL url = new URL("http://google.de");
        InputStream in = url.openStream();
        in.close();
    }
}
