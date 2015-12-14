package org.xbib.io.http.client.request.body;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;

public class InputStreamTest extends AbstractBasicTest {

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new InputStreamHandler();
    }

    @Test(groups = "standalone")
    public void testInvalidInputStream() throws IOException, ExecutionException, TimeoutException, InterruptedException {

        try (AsyncHttpClient c = asyncHttpClient()) {
            HttpHeaders h = new DefaultHttpHeaders().add(HttpHeaders.Names.CONTENT_TYPE, HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED);

            InputStream is = new InputStream() {

                public int readAllowed;

                @Override
                public int available() {
                    return 1; // Fake
                }

                @Override
                public int read() throws IOException {
                    int fakeCount = readAllowed++;
                    if (fakeCount == 0) {
                        return (int) 'a';
                    } else if (fakeCount == 1) {
                        return (int) 'b';
                    } else if (fakeCount == 2) {
                        return (int) 'c';
                    } else {
                        return -1;
                    }
                }
            };

            Response resp = c.preparePost(getTargetUrl()).setHeaders(h).setBody(is).execute().get();
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getHeader("X-Param"), "abc");
        }
    }

    private static class InputStreamHandler extends AbstractHandler {
        public void handle(String s, Request r, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                byte[] bytes = new byte[3];
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int read = 0;
                while (read > -1) {
                    read = request.getInputStream().read(bytes);
                    if (read > 0) {
                        bos.write(bytes, 0, read);
                    }
                }

                response.setStatus(HttpServletResponse.SC_OK);
                response.addHeader("X-Param", new String(bos.toByteArray()));
            } else { // this handler is to handle POST request
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }
    }
}
