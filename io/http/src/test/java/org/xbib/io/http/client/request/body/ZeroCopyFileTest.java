package org.xbib.io.http.client.request.body;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncCompletionHandler;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.BasicHttpsTest;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.HttpResponseHeaders;
import org.xbib.io.http.client.HttpResponseStatus;
import org.xbib.io.http.client.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.test.TestUtils.SIMPLE_TEXT_FILE;
import static org.xbib.io.http.client.test.TestUtils.SIMPLE_TEXT_FILE_STRING;

/**
 * Zero copy test which use FileChannel.transfer under the hood . The same SSL test is also covered in {@link
 * BasicHttpsTest}
 */
public class ZeroCopyFileTest extends AbstractBasicTest {

    @Test(groups = "standalone")
    public void zeroCopyPostTest() throws IOException, ExecutionException, TimeoutException, InterruptedException, URISyntaxException {
        try (AsyncHttpClient client = asyncHttpClient()) {
            final AtomicBoolean headerSent = new AtomicBoolean(false);
            final AtomicBoolean operationCompleted = new AtomicBoolean(false);

            Response resp = client.preparePost("http://localhost:" + port1 + "/").setBody(SIMPLE_TEXT_FILE).execute(new AsyncCompletionHandler<Response>() {

                public State onHeadersWritten() {
                    headerSent.set(true);
                    return State.CONTINUE;
                }

                public State onContentWritten() {
                    operationCompleted.set(true);
                    return State.CONTINUE;
                }

                @Override
                public Response onCompleted(Response response) throws Exception {
                    return response;
                }
            }).get();
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getResponseBody(), SIMPLE_TEXT_FILE_STRING);
            assertTrue(operationCompleted.get());
            assertTrue(headerSent.get());
        }
    }

    @Test(groups = "standalone")
    public void zeroCopyPutTest() throws IOException, ExecutionException, TimeoutException, InterruptedException, URISyntaxException {
        try (AsyncHttpClient client = asyncHttpClient()) {
            Future<Response> f = client.preparePut("http://localhost:" + port1 + "/").setBody(SIMPLE_TEXT_FILE).execute();
            Response resp = f.get();
            assertNotNull(resp);
            assertEquals(resp.getStatusCode(), HttpServletResponse.SC_OK);
            assertEquals(resp.getResponseBody(), SIMPLE_TEXT_FILE_STRING);
        }
    }

    @Override
    public AbstractHandler configureHandler() throws Exception {
        return new ZeroCopyHandler();
    }

    @Test(groups = "standalone")
    public void zeroCopyFileTest() throws IOException, ExecutionException, TimeoutException, InterruptedException, URISyntaxException {
        File tmp = new File(System.getProperty("java.io.tmpdir") + File.separator + "zeroCopy.txt");
        tmp.deleteOnExit();
        try (AsyncHttpClient client = asyncHttpClient()) {
            try (FileOutputStream stream = new FileOutputStream(tmp)) {
                Response resp = client.preparePost("http://localhost:" + port1 + "/").setBody(SIMPLE_TEXT_FILE).execute(new AsyncHandler<Response>() {
                    public void onThrowable(Throwable t) {
                    }

                    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                        stream.write(bodyPart.getBodyPartBytes());
                        return State.CONTINUE;
                    }

                    public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
                        return State.CONTINUE;
                    }

                    public State onHeadersReceived(HttpResponseHeaders headers) throws Exception {
                        return State.CONTINUE;
                    }

                    public Response onCompleted() throws Exception {
                        return null;
                    }
                }).get();
                assertNull(resp);
                assertEquals(SIMPLE_TEXT_FILE.length(), tmp.length());
            }
        }
    }

    @Test(groups = "standalone")
    public void zeroCopyFileWithBodyManipulationTest() throws IOException, ExecutionException, TimeoutException, InterruptedException, URISyntaxException {
        File tmp = new File(System.getProperty("java.io.tmpdir") + File.separator + "zeroCopy.txt");
        tmp.deleteOnExit();
        try (AsyncHttpClient client = asyncHttpClient()) {
            try (FileOutputStream stream = new FileOutputStream(tmp)) {
                Response resp = client.preparePost("http://localhost:" + port1 + "/").setBody(SIMPLE_TEXT_FILE).execute(new AsyncHandler<Response>() {
                    public void onThrowable(Throwable t) {
                    }

                    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                        stream.write(bodyPart.getBodyPartBytes());

                        if (bodyPart.getBodyPartBytes().length == 0) {
                            return State.ABORT;
                        }

                        return State.CONTINUE;
                    }

                    public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
                        return State.CONTINUE;
                    }

                    public State onHeadersReceived(HttpResponseHeaders headers) throws Exception {
                        return State.CONTINUE;
                    }

                    public Response onCompleted() throws Exception {
                        return null;
                    }
                }).get();
                assertNull(resp);
                assertEquals(SIMPLE_TEXT_FILE.length(), tmp.length());
            }
        }
    }

    private class ZeroCopyHandler extends AbstractHandler {
        public void handle(String s, Request r, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {

            int size = 10 * 1024;
            if (httpRequest.getContentLength() > 0) {
                size = httpRequest.getContentLength();
            }
            byte[] bytes = new byte[size];
            if (bytes.length > 0) {
                httpRequest.getInputStream().read(bytes);
                httpResponse.getOutputStream().write(bytes);
            }

            httpResponse.setStatus(200);
            httpResponse.getOutputStream().flush();
        }
    }
}
