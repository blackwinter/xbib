package org.xbib.io.http.client.channel;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncCompletionHandlerBase;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.test.TestUtils.findFreePort;
import static org.xbib.io.http.client.test.TestUtils.newJettyHttpServer;

public class MaxConnectionsInThreads extends AbstractBasicTest {

    @Test(groups = "standalone")
    public void testMaxConnectionsWithinThreads() throws Exception {

        String[] urls = new String[]{getTargetUrl(), getTargetUrl()};

        AsyncHttpClientConfig config = config()//
                .setConnectTimeout(1000)//
                .setRequestTimeout(5000)//
                .setKeepAlive(true)//
                .setMaxConnections(1)//
                .setMaxConnectionsPerHost(1)//
                .build();

        final CountDownLatch inThreadsLatch = new CountDownLatch(2);
        final AtomicInteger failedCount = new AtomicInteger();

        try (AsyncHttpClient client = asyncHttpClient(config)) {
            for (int i = 0; i < urls.length; i++) {
                final String url = urls[i];
                Thread t = new Thread() {
                    public void run() {
                        client.prepareGet(url).execute(new AsyncCompletionHandlerBase() {
                            @Override
                            public Response onCompleted(Response response) throws Exception {
                                Response r = super.onCompleted(response);
                                inThreadsLatch.countDown();
                                return r;
                            }

                            @Override
                            public void onThrowable(Throwable t) {
                                super.onThrowable(t);
                                failedCount.incrementAndGet();
                                inThreadsLatch.countDown();
                            }
                        });
                    }
                };
                t.start();
            }

            inThreadsLatch.await();

            assertEquals(failedCount.get(), 1, "Max Connections should have been reached when launching from concurrent threads");

            final CountDownLatch notInThreadsLatch = new CountDownLatch(2);
            failedCount.set(0);
            for (int i = 0; i < urls.length; i++) {
                final String url = urls[i];
                client.prepareGet(url).execute(new AsyncCompletionHandlerBase() {
                    @Override
                    public Response onCompleted(Response response) throws Exception {
                        Response r = super.onCompleted(response);
                        notInThreadsLatch.countDown();
                        return r;
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        super.onThrowable(t);
                        failedCount.incrementAndGet();
                        notInThreadsLatch.countDown();
                    }
                });
            }

            notInThreadsLatch.await();

            assertEquals(failedCount.get(), 1, "Max Connections should have been reached when launching from main thread");
        }
    }

    @Override
    @BeforeClass
    public void setUpGlobal() throws Exception {

        port1 = findFreePort();
        server = newJettyHttpServer(port1);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new MockTimeoutHttpServlet()), "/timeout/*");

        server.start();
    }

    public String getTargetUrl() {
        return "http://localhost:" + port1 + "/timeout/";
    }

    @SuppressWarnings("serial")
    public static class MockTimeoutHttpServlet extends HttpServlet {
        private static final String contentType = "text/plain";
        public static long DEFAULT_TIMEOUT = 2000;

        public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
            res.setStatus(200);
            res.addHeader("Content-Type", contentType);
            long sleepTime = DEFAULT_TIMEOUT;
            try {
                sleepTime = Integer.parseInt(req.getParameter("timeout"));

            } catch (NumberFormatException e) {
                sleepTime = DEFAULT_TIMEOUT;
            }

            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {

            }

            res.setHeader("XXX", "TripleX");

            byte[] retVal = "1".getBytes();
            OutputStream os = res.getOutputStream();

            res.setContentLength(retVal.length);
            os.write(retVal);
            os.close();
        }
    }
}
