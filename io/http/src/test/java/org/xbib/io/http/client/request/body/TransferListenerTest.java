package org.xbib.io.http.client.request.body;

import io.netty.handler.codec.http.HttpHeaders;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.Response;
import org.xbib.io.http.client.handler.TransferCompletionHandler;
import org.xbib.io.http.client.handler.TransferListener;
import org.xbib.io.http.client.request.body.generator.FileBodyGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.test.TestUtils.createTempFile;

public class TransferListenerTest extends AbstractBasicTest {

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new BasicHandler();
    }

    @Test(groups = "standalone")
    public void basicGetTest() throws Exception {
        try (AsyncHttpClient c = asyncHttpClient()) {
            final AtomicReference<Throwable> throwable = new AtomicReference<>();
            final AtomicReference<HttpHeaders> hSent = new AtomicReference<>();
            final AtomicReference<HttpHeaders> hRead = new AtomicReference<>();
            final AtomicReference<byte[]> bb = new AtomicReference<>();
            final AtomicBoolean completed = new AtomicBoolean(false);

            TransferCompletionHandler tl = new TransferCompletionHandler();
            tl.addTransferListener(new TransferListener() {

                public void onRequestHeadersSent(HttpHeaders headers) {
                    hSent.set(headers);
                }

                public void onResponseHeadersReceived(HttpHeaders headers) {
                    hRead.set(headers);
                }

                public void onBytesReceived(byte[] b) {
                    if (b.length != 0) {
                        bb.set(b);
                    }
                }

                public void onBytesSent(long amount, long current, long total) {
                }

                public void onRequestResponseCompleted() {
                    completed.set(true);
                }

                public void onThrowable(Throwable t) {
                    throwable.set(t);
                }
            });

            Response response = c.prepareGet(getTargetUrl()).execute(tl).get();

            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
            assertNotNull(hRead.get());
            assertNotNull(hSent.get());
            assertNull(bb.get());
            assertNull(throwable.get());
        }
    }

    @Test(groups = "standalone")
    public void basicPutFileTest() throws Exception {
        final AtomicReference<Throwable> throwable = new AtomicReference<>();
        final AtomicReference<HttpHeaders> hSent = new AtomicReference<>();
        final AtomicReference<HttpHeaders> hRead = new AtomicReference<>();
        final AtomicInteger bbReceivedLenght = new AtomicInteger(0);
        final AtomicLong bbSentLenght = new AtomicLong(0L);

        final AtomicBoolean completed = new AtomicBoolean(false);

        File file = createTempFile(1024 * 100 * 10);

        int timeout = (int) (file.length() / 1000);

        try (AsyncHttpClient client = asyncHttpClient(config().setConnectTimeout(timeout))) {
            TransferCompletionHandler tl = new TransferCompletionHandler();
            tl.addTransferListener(new TransferListener() {

                public void onRequestHeadersSent(HttpHeaders headers) {
                    hSent.set(headers);
                }

                public void onResponseHeadersReceived(HttpHeaders headers) {
                    hRead.set(headers);
                }

                public void onBytesReceived(byte[] b) {
                    bbReceivedLenght.addAndGet(b.length);
                }

                public void onBytesSent(long amount, long current, long total) {
                    bbSentLenght.addAndGet(amount);
                }

                public void onRequestResponseCompleted() {
                    completed.set(true);
                }

                public void onThrowable(Throwable t) {
                    throwable.set(t);
                }
            });

            Response response = client.preparePut(getTargetUrl()).setBody(file).execute(tl).get();

            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
            assertNotNull(hRead.get());
            assertNotNull(hSent.get());
            assertEquals(bbReceivedLenght.get(), file.length(), "Number of received bytes incorrect");
            assertEquals(bbSentLenght.get(), file.length(), "Number of sent bytes incorrect");
        }
    }

    @Test(groups = "standalone")
    public void basicPutFileBodyGeneratorTest() throws Exception {
        try (AsyncHttpClient client = asyncHttpClient()) {
            final AtomicReference<Throwable> throwable = new AtomicReference<>();
            final AtomicReference<HttpHeaders> hSent = new AtomicReference<>();
            final AtomicReference<HttpHeaders> hRead = new AtomicReference<>();
            final AtomicInteger bbReceivedLenght = new AtomicInteger(0);
            final AtomicLong bbSentLenght = new AtomicLong(0L);

            final AtomicBoolean completed = new AtomicBoolean(false);

            File file = createTempFile(1024 * 100 * 10);

            TransferCompletionHandler tl = new TransferCompletionHandler();
            tl.addTransferListener(new TransferListener() {

                public void onRequestHeadersSent(HttpHeaders headers) {
                    hSent.set(headers);
                }

                public void onResponseHeadersReceived(HttpHeaders headers) {
                    hRead.set(headers);
                }

                public void onBytesReceived(byte[] b) {
                    bbReceivedLenght.addAndGet(b.length);
                }

                public void onBytesSent(long amount, long current, long total) {
                    bbSentLenght.addAndGet(amount);
                }

                public void onRequestResponseCompleted() {
                    completed.set(true);
                }

                public void onThrowable(Throwable t) {
                    throwable.set(t);
                }
            });

            Response response = client.preparePut(getTargetUrl()).setBody(new FileBodyGenerator(file)).execute(tl).get();

            assertNotNull(response);
            assertEquals(response.getStatusCode(), 200);
            assertNotNull(hRead.get());
            assertNotNull(hSent.get());
            assertEquals(bbReceivedLenght.get(), file.length(), "Number of received bytes incorrect");
            assertEquals(bbSentLenght.get(), file.length(), "Number of sent bytes incorrect");
        }
    }

    private class BasicHandler extends AbstractHandler {

        public void handle(String s, org.eclipse.jetty.server.Request r, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {

            Enumeration<?> e = httpRequest.getHeaderNames();
            String param;
            while (e.hasMoreElements()) {
                param = e.nextElement().toString();
                httpResponse.addHeader("X-" + param, httpRequest.getHeader(param));
            }

            int size = 10 * 1024;
            if (httpRequest.getContentLength() > 0) {
                size = httpRequest.getContentLength();
            }
            byte[] bytes = new byte[size];
            if (bytes.length > 0) {
                int read = 0;
                while (read != -1) {
                    read = httpRequest.getInputStream().read(bytes);
                    if (read > 0) {
                        httpResponse.getOutputStream().write(bytes, 0, read);
                    }
                }
            }

            httpResponse.setStatus(200);
            httpResponse.getOutputStream().flush();
            httpResponse.getOutputStream().close();
        }
    }
}
