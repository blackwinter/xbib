package org.xbib.io.http.client.request.body;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.HttpResponseHeaders;
import org.xbib.io.http.client.HttpResponseStatus;
import org.xbib.io.http.client.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;

public class EmptyBodyTest extends AbstractBasicTest {
    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new NoBodyResponseHandler();
    }

    @Test(groups = "standalone")
    public void testEmptyBody() throws IOException {
        try (AsyncHttpClient ahc = asyncHttpClient()) {
            final AtomicBoolean err = new AtomicBoolean(false);
            final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
            final AtomicBoolean status = new AtomicBoolean(false);
            final AtomicInteger headers = new AtomicInteger(0);
            final CountDownLatch latch = new CountDownLatch(1);
            ahc.executeRequest(ahc.prepareGet(getTargetUrl()).build(), new AsyncHandler<Object>() {
                public void onThrowable(Throwable t) {
                    fail("Got throwable.", t);
                    err.set(true);
                }

                public State onBodyPartReceived(HttpResponseBodyPart e) throws Exception {
                    byte[] bytes = e.getBodyPartBytes();

                    if (bytes.length != 0) {
                        String s = new String(bytes);
                        logger.info("got part: {}", s);
                        logger.warn("Sampling stacktrace.", new Throwable("trace that, we should not get called for empty body."));
                        queue.put(s);
                    }
                    return State.CONTINUE;
                }

                public State onStatusReceived(HttpResponseStatus e) throws Exception {
                    status.set(true);
                    return AsyncHandler.State.CONTINUE;
                }

                public State onHeadersReceived(HttpResponseHeaders e) throws Exception {
                    if (headers.incrementAndGet() == 2) {
                        throw new Exception("Analyze this.");
                    }
                    return State.CONTINUE;
                }

                public Object onCompleted() throws Exception {
                    latch.countDown();
                    return null;
                }
            });
            try {
                assertTrue(latch.await(1, TimeUnit.SECONDS), "Latch failed.");
            } catch (InterruptedException e) {
                fail("Interrupted.", e);
            }
            assertFalse(err.get());
            assertEquals(queue.size(), 0);
            assertTrue(status.get());
            assertEquals(headers.get(), 1);
        }
    }

    @Test(groups = "standalone")
    public void testPutEmptyBody() throws Exception {
        try (AsyncHttpClient ahc = asyncHttpClient()) {
            Response response = ahc.preparePut(getTargetUrl()).setBody("String").execute().get();

            assertNotNull(response);
            assertEquals(response.getStatusCode(), 204);
            assertEquals(response.getResponseBody(), "");
            assertTrue(response.getResponseBodyAsStream() instanceof InputStream);
            assertEquals(response.getResponseBodyAsStream().read(), -1);
        }
    }

    private class NoBodyResponseHandler extends AbstractHandler {
        public void handle(String s, Request request, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

            if (!req.getMethod().equalsIgnoreCase("PUT")) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(204);
            }
            request.setHandled(true);
        }
    }
}
