package org.xbib.io.http;

import org.junit.Test;
import org.xbib.io.CustomURLStreamHandlerFactory;
import org.xbib.io.archive.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

public class HttpConnectionTest {

    static {
        URL.setURLStreamHandlerFactory(new CustomURLStreamHandlerFactory());
    }

    @Test
    public void testHttpConnection() throws IOException {
        URL url = new URL("http://google.de");
        InputStream in = url.openStream();
        StringWriter sw = new StringWriter();
        StreamUtil.copy(new InputStreamReader(in,"UTF-8"), sw);
        in.close();
    }
}
