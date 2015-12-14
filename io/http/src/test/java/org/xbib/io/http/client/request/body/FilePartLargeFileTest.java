package org.xbib.io.http.client.request.body;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.Response;
import org.xbib.io.http.client.request.body.multipart.FilePart;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.test.TestUtils.LARGE_IMAGE_FILE;
import static org.xbib.io.http.client.test.TestUtils.createTempFile;

public class FilePartLargeFileTest extends AbstractBasicTest {

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new AbstractHandler() {

            public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

                ServletInputStream in = req.getInputStream();
                byte[] b = new byte[8192];

                int count = -1;
                int total = 0;
                while ((count = in.read(b)) != -1) {
                    b = new byte[8192];
                    total += count;
                }
                resp.setStatus(200);
                resp.addHeader("X-TRANFERED", String.valueOf(total));
                resp.getOutputStream().flush();
                resp.getOutputStream().close();

                baseRequest.setHandled(true);
            }
        };
    }

    @Test(groups = "standalone")
    public void testPutImageFile() throws Exception {
        try (AsyncHttpClient client = asyncHttpClient(config().setRequestTimeout(100 * 6000))) {
            Response response = client.preparePut(getTargetUrl()).addBodyPart(new FilePart("test", LARGE_IMAGE_FILE, "application/octet-stream", UTF_8)).execute().get();
            assertEquals(response.getStatusCode(), 200);
        }
    }

    @Test(groups = "standalone")
    public void testPutLargeTextFile() throws Exception {
        File file = createTempFile(1024 * 1024);

        try (AsyncHttpClient client = asyncHttpClient(config().setRequestTimeout(100 * 6000))) {
            Response response = client.preparePut(getTargetUrl()).addBodyPart(new FilePart("test", file, "application/octet-stream", UTF_8)).execute().get();
            assertEquals(response.getStatusCode(), 200);
        }
    }
}
