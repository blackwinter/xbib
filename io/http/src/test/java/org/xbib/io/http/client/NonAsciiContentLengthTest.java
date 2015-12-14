package org.xbib.io.http.client;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpServer;

public class NonAsciiContentLengthTest extends AbstractBasicTest {

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {
        port1 = findFreePort();
        server = newJettyHttpServer(port1);
        server.setHandler(new AbstractHandler() {

            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                int MAX_BODY_SIZE = 1024; // Can only handle bodies of up to 1024 bytes.
                byte[] b = new byte[MAX_BODY_SIZE];
                int offset = 0;
                int numBytesRead;
                try (ServletInputStream is = request.getInputStream()) {
                    while ((numBytesRead = is.read(b, offset, MAX_BODY_SIZE - offset)) != -1) {
                        offset += numBytesRead;
                    }
                }
                assertEquals(request.getContentLength(), offset);
                response.setStatus(200);
                response.setCharacterEncoding(request.getCharacterEncoding());
                response.setContentLength(request.getContentLength());
                try (ServletOutputStream os = response.getOutputStream()) {
                    os.write(b, 0, offset);
                }
            }
        });
        server.start();
    }

    @Test(groups = "standalone")
    public void testNonAsciiContentLength() throws Exception {
        execute("test");
        execute("\u4E00"); // Unicode CJK ideograph for one
    }

    protected void execute(String body) throws IOException, InterruptedException, ExecutionException {
        try (AsyncHttpClient client = asyncHttpClient()) {
            BoundRequestBuilder r = client.preparePost(getTargetUrl()).setBody(body).setCharset(UTF_8);
            Future<Response> f = r.execute();
            Response resp = f.get();
            assertEquals(resp.getStatusCode(), 200);
            assertEquals(body, resp.getResponseBody(UTF_8));
        }
    }
}
