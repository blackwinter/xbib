package org.xbib.io.http.client;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.fail;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpServer;

public class IdleStateHandlerTest extends AbstractBasicTest {

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {
        port1 = findFreePort();
        server = newJettyHttpServer(port1);
        server.setHandler(new IdleStateHandler());
        server.start();
        logger.info("Local HTTP server started successfully");
    }

    @Test(groups = "standalone")
    public void idleStateTest() throws Exception {
        try (AsyncHttpClient c = asyncHttpClient(config().setPooledConnectionIdleTimeout(10 * 1000))) {
            c.prepareGet(getTargetUrl()).execute().get();
        } catch (ExecutionException e) {
            fail("Should allow to finish processing request.", e);
        }
    }

    private class IdleStateHandler extends AbstractHandler {

        public void handle(String s, Request r, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {

            try {
                Thread.sleep(20 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            httpResponse.setStatus(200);
            httpResponse.getOutputStream().flush();
            httpResponse.getOutputStream().close();
        }
    }
}
