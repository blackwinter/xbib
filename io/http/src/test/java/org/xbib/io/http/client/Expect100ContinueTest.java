package org.xbib.io.http.client;

import io.netty.handler.codec.http.HttpHeaders;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Future;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.test.TestUtils.SIMPLE_TEXT_FILE;
import static org.xbib.io.http.client.test.TestUtils.SIMPLE_TEXT_FILE_STRING;

/**
 * Test the Expect: 100-Continue.
 */
public class Expect100ContinueTest extends AbstractBasicTest {

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new ZeroCopyHandler();
    }

    @Test(groups = "standalone")
    public void Expect100Continue() throws Exception {
        try (AsyncHttpClient client = asyncHttpClient()) {
            Future<Response> f = client.preparePut("http://localhost:" + port1 + "/")//
                    .setHeader(HttpHeaders.Names.EXPECT, HttpHeaders.Values.CONTINUE)//
                    .setBody(SIMPLE_TEXT_FILE)//
                    .execute();
            Response resp = f.get();
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getResponseBody(), SIMPLE_TEXT_FILE_STRING);
        }
    }

    private static class ZeroCopyHandler extends AbstractHandler {
        public void handle(String s, Request r, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {

            int size = 10 * 1024;
            if (httpRequest.getContentLength() > 0) {
                size = httpRequest.getContentLength();
            }
            byte[] bytes = new byte[size];
            if (bytes.length > 0) {
                final int read = httpRequest.getInputStream().read(bytes);
                httpResponse.getOutputStream().write(bytes, 0, read);
            }

            httpResponse.setStatus(200);
            httpResponse.getOutputStream().flush();
        }
    }
}
