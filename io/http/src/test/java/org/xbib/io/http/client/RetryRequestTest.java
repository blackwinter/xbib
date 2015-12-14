package org.xbib.io.http.client;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;
import org.xbib.io.http.client.exception.RemotelyClosedException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;

public class RetryRequestTest extends AbstractBasicTest {
    protected String getTargetUrl() {
        return String.format("http://localhost:%d/", port1);
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new SlowAndBigHandler();
    }

    @Test(groups = "standalone")
    public void testMaxRetry() throws Exception {
        try (AsyncHttpClient ahc = asyncHttpClient(config().setMaxRequestRetry(0))) {
            ahc.executeRequest(ahc.prepareGet(getTargetUrl()).build()).get();
            fail();
        } catch (Exception t) {
            assertEquals(t.getCause(), RemotelyClosedException.INSTANCE);
        }
    }

    public static class SlowAndBigHandler extends AbstractHandler {

        public void handle(String pathInContext, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {

            int load = 100;
            httpResponse.setStatus(200);
            httpResponse.setContentLength(load);
            httpResponse.setContentType("application/octet-stream");

            httpResponse.flushBuffer();

            OutputStream os = httpResponse.getOutputStream();
            for (int i = 0; i < load; i++) {
                os.write(i % 255);

                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    // nuku
                }

                if (i > load / 10) {
                    httpResponse.sendError(500);
                }
            }

            httpResponse.getOutputStream().flush();
            httpResponse.getOutputStream().close();
        }
    }
}
