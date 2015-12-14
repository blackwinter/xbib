package org.xbib.io.http.client;

import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.fail;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.head;

public class Head302Test extends AbstractBasicTest {

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new Head302handler();
    }

    @Test(groups = "standalone")
    public void testHEAD302() throws IOException, BrokenBarrierException, InterruptedException, ExecutionException, TimeoutException {
        try (AsyncHttpClient client = asyncHttpClient()) {
            final CountDownLatch l = new CountDownLatch(1);
            Request request = head("http://localhost:" + port1 + "/Test").build();

            client.executeRequest(request, new AsyncCompletionHandlerBase() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    l.countDown();
                    return super.onCompleted(response);
                }
            }).get(3, TimeUnit.SECONDS);

            if (!l.await(TIMEOUT, TimeUnit.SECONDS)) {
                fail("Timeout out");
            }
        }
    }

    /**
     * Handler that does Found (302) in response to HEAD method.
     */
    private static class Head302handler extends AbstractHandler {
        public void handle(String s, org.eclipse.jetty.server.Request r, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            if ("HEAD".equalsIgnoreCase(request.getMethod())) {
                if (request.getPathInfo().endsWith("_moved")) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_FOUND); // 302
                    response.setHeader("Location", request.getPathInfo() + "_moved");
                }
            } else { // this handler is to handle HEAD request
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        }
    }
}
