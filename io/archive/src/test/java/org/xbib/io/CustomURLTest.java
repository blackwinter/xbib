
package org.xbib.io;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class CustomURLTest {

    static {
        URL.setURLStreamHandlerFactory(new CustomURLStreamHandlerFactory());
    }

    @Test
    public void testFileTmp() throws Exception {
        new URL("file:dummy");
    }

    @Test(expected = java.net.MalformedURLException.class)
    public void testUnkownScheme() throws URISyntaxException, IOException {
        new URL("myscheme:dummy");
    }

    @Test
    public void testCanOpenAndCloseFileInputStream() throws Exception {
        URL url = new URL("file:src/test/resources/META-INF/services/org.xbib.io.CustomURLStreamHandler");
        InputStream in = url.openStream();
        in.close();
    }

}
