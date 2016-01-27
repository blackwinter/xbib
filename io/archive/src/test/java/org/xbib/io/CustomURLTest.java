
package org.xbib.io;

import org.junit.Test;

import java.io.IOException;
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

}
