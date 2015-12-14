package org.xbib.io.http.client.request.body;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.test.TestUtils.createTempFile;

public class PutLargeFileTest extends AbstractBasicTest {

    @Test(groups = "standalone")
    public void testPutLargeFile() throws Exception {

        File file = createTempFile(1024 * 1024);

        int timeout = (int) file.length() / 1000;

        try (AsyncHttpClient client = asyncHttpClient(config().setConnectTimeout(timeout))) {
            Response response = client.preparePut(getTargetUrl()).setBody(file).execute().get();
            assertEquals(response.getStatusCode(), 200);
        }
    }

    @Test(groups = "standalone")
    public void testPutSmallFile() throws Exception {

        File file = createTempFile(1024);

        try (AsyncHttpClient client = asyncHttpClient()) {
            Response response = client.preparePut(getTargetUrl()).setBody(file).execute().get();
            assertEquals(response.getStatusCode(), 200);
        }
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new AbstractHandler() {

            public void handle(String arg0, Request arg1, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

                resp.setStatus(200);
                resp.getOutputStream().flush();
                resp.getOutputStream().close();

                arg1.setHandled(true);
            }
        };
    }
}
